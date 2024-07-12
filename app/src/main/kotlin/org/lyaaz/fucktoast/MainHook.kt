package org.lyaaz.fucktoast

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        try {
            // mozilla.components.feature.prompts.dialog.FullScreenNotificationDialog
            val dialogFragment = XposedHelpers.findClass(
                "androidx.fragment.app.DialogFragment",
                lpparam.classLoader
            )
            val fragmentManager = XposedHelpers.findClass(
                "androidx.fragment.app.FragmentManager",
                lpparam.classLoader
            )
            XposedHelpers.findAndHookMethod(
                dialogFragment,
                "show",
                fragmentManager,
                String::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (param.args[1] == FULLSCREEN_NOTIFICATION_TAG) {
                            param.result = null
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
    }

    companion object {
        private const val FULLSCREEN_NOTIFICATION_TAG =
            "mozac_feature_prompts_full_screen_notification_dialog"
    }
}