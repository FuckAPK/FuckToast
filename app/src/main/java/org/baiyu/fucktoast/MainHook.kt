package org.baiyu.fucktoast

import android.content.Context
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        try {
            XposedBridge.hookAllMethods(
                Toast::class.java,
                "makeText",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        handleMakeText(param)
                    }
                })

            XposedBridge.hookAllMethods(
                Toast::class.java,
                "setText",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        handleSetText(param)
                    }
                })

            XposedBridge.hookAllMethods(
                Toast::class.java,
                "show",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        handleShowMethod(param)
                    }
                })
        } catch (e: Exception) {
            // Handle exceptions appropriately
            e.printStackTrace()
        }

        try {
            XposedHelpers.findClassIfExists(
                "androidx.fragment.app.DialogFragment",
                lpparam.classLoader
            )?.let {
                XposedBridge.hookAllMethods(
                    it,
                    "show",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (param.thisObject.javaClass.name == FullScreenNotificationDialog) {
                                param.result = null
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleMakeText(param: XC_MethodHook.MethodHookParam) {
        val context = param.args[0] as Context
        val text = when (val arg1 = param.args[1]) {
            is Int -> context.getString(arg1)
            is CharSequence -> arg1.toString()
            else -> ""
        }

        // Record the text value using setAdditionalInstanceField
        XposedHelpers.setAdditionalInstanceField(param.result, CUSTOM_FIELD, text)
    }

    private fun handleSetText(param: XC_MethodHook.MethodHookParam) {
        val text = when (val arg0 = param.args[0]) {
            is Int -> {
                val context =
                    XposedHelpers.getObjectField(param.thisObject, "mContext") as? Context ?: return
                context.getString(arg0)
            }

            is CharSequence -> arg0.toString()
            else -> ""
        }
        // Record the text value using setAdditionalInstanceField
        XposedHelpers.setAdditionalInstanceField(param.thisObject, CUSTOM_FIELD, text)
    }

    private fun handleShowMethod(param: XC_MethodHook.MethodHookParam) {
        // Retrieve the recorded text value using getAdditionalInstanceField
        val toast = param.thisObject as Toast
        val text =
            XposedHelpers.getAdditionalInstanceField(toast, CUSTOM_FIELD) as? String ?: return
        // Add your logic here based on the recorded text value
        if (BLOCKED_STRING.any { text.lowercase().contains(it) }) {
            // Cancel the Toast by returning early
            param.result = null
        }
    }

    companion object {
        private val BLOCKED_STRING = setOf("full screen", "fullscreen", "全屏")
        private const val CUSTOM_FIELD = "xpToastText"
        private const val FullScreenNotificationDialog =
            "mozilla.components.feature.prompts.dialog.FullScreenNotificationDialog"
    }
}