package pl.rufik.watchhelperfixer.module;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import pl.rufik.watchhelperfixer.BuildConfig;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by rufik on 30.01.2017.
 */
public class Fixer implements IXposedHookLoadPackage {

    private static final String LOG_TAG = "WHFixer: ";
    private static final String PKG_NAME = "com.android.BluetoothSocketTest";
    private static XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPkgParam) throws Throwable {
        if (!loadPkgParam.packageName.equals(PKG_NAME)) {
            return;
        }

        XposedBridge.log(LOG_TAG + "Hooked successfully on package " + PKG_NAME);
        loadPrefs();

        //disabling all sounds
        findAndHookMethod("android.media.SoundPool", loadPkgParam.classLoader, "play",
                int.class, float.class, float.class, int.class, int.class, float.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        final boolean isSoundDisabled = getPreference("switch_sound_off", false);
                        if (isSoundDisabled) {
                            logDebug("Alert sound is disabled (play method)");
                            param.setResult(1);
                        }
                    }
                }
        );
        findAndHookMethod("android.media.SoundPool", loadPkgParam.classLoader, "stop", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        final boolean isSoundDisabled = getPreference("switch_sound_off", false);
                        if (isSoundDisabled) {
                            logDebug("Alert sound is disabled (stop method)");
                            param.setResult(null);
                        }
                    }
                }
        );


        //disable alert dialog
        try {
            findAndHookMethod("android.app.Dialog", loadPkgParam.classLoader, "show",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            final boolean isAlertDisabled = getPreference("switch_alert_off", false);
                            if (!isAlertDisabled) {
                                return;
                            }
                            if (param.thisObject != null) {
                                logDebug("Dialog is instance of: " + param.thisObject.getClass().getName());
                                if ((param.thisObject instanceof AlertDialog) && !(param.thisObject instanceof ProgressDialog)) {
                                    //AlertDialog dialog = (AlertDialog) param.thisObject;
                                    logDebug("Dialog.show() is gonna be replaced and not called");
                                    try {
                                        param.setResult(null);
                                    } catch (Throwable t) {
                                        param.setThrowable(t);
                                    }
                                } else {
                                    XposedBridge.log(LOG_TAG + "Not an instance of android.app.AlertDialog, so not replacing");
                                }
                            } else {
                                XposedBridge.log(LOG_TAG + "Dialog.show() is not bound to any 'this' object :( Cannot continue...");
                            }

                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log(LOG_TAG + "Exception hooking on Dialog.show()!");
            XposedBridge.log(e);
        }

    }

    private static boolean getPreference(String name, boolean defaultValue) {
        if (prefs == null) {
            XposedBridge.log(LOG_TAG + "[ERROR] Preferences are not loaded!");
            return defaultValue;
        }
        if (name == null) {
            return defaultValue;
        }
        prefs.reload();
        return prefs.getBoolean(name, defaultValue);
    }

    private static void loadPrefs() {
        prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, "prefs_whfixer");
        prefs.makeWorldReadable();
        prefs.reload();
        XposedBridge.log(LOG_TAG + "Preferences loaded");
    }

    private static void logDebug(String msg) {
        if (getPreference("debug", false)) {
            XposedBridge.log(LOG_TAG + msg);
        }
    }

}
