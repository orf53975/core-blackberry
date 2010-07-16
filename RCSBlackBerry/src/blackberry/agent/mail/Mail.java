//#preprocess
package blackberry.agent.mail;

import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

public class Mail {
    //#ifdef DEBUG
    static Debug debug = new Debug("Mail", DebugLevel.VERBOSE);
    //#endif

    public boolean hasSupportedAttachment = false;
    public boolean hasUnsupportedAttachment = false;

    public String plainTextMessage;
    public String plainTextMessageContentType;
    public String htmlMessage;
    public String htmlMessageContentType;

    public final boolean isMultipart() {
        return hasText() && hasHtml();
    }

    public final boolean hasText() {
        int len = 0;

        if (plainTextMessage != null) {
            len = plainTextMessage.trim().length();
            //#ifdef DEBUG_TRACE
            debug.trace("hasText len: "
                    + len
                    + " plain: "
                    + plainTextMessage.substring(0, Math.min(200,
                            plainTextMessage.length())));
            //#endif
        }

        return len > 0;
    }

    public final boolean hasHtml() {
        return htmlMessage != null;
    }

    public boolean isEmpty() {
        return !hasText() && !hasHtml();
    }

}
