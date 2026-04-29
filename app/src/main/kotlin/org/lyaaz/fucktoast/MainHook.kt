package org.lyaaz.fucktoast

import android.util.Log
import android.widget.Toast
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class MainHook : XposedModule() {

    override fun onPackageReady(param: PackageReadyParam) {
        val classLoader = param.classLoader
        // mozilla.components.feature.prompts.dialog.FullScreenNotificationDialog
        runCatching {
            val dialogFragment = Class.forName(
                "androidx.fragment.app.DialogFragment",
                true,
                classLoader
            )
            val fragmentManager = Class.forName(
                "androidx.fragment.app.FragmentManager",
                true,
                classLoader
            )
            val showMethod = dialogFragment.getMethod("show", fragmentManager, String::class.java)
            hook(showMethod).intercept { chain ->
                if (chain.args[1] == FULLSCREEN_NOTIFICATION_TAG) {
                    null
                } else {
                    chain.proceed()
                }
            }
        }.onFailure {
            log(Log.ERROR, TAG, "Failed to hook DialogFragment.show", it)
        }
        // Firefox 132+
        runCatching {
            val showMethod = Toast::class.java.getMethod("show")
            hook(showMethod).intercept { chain ->
                if (Thread.currentThread().stackTrace.map { it.methodName }
                        .any { it.startsWith("fullScreenChanged") }) {
                    null
                } else {
                    chain.proceed()
                }
            }
        }.onFailure {
            log(Log.ERROR, TAG, "Failed to hook Toast.show", it)
        }
    }

    companion object {
        private const val TAG = "FuckToast"
        private const val FULLSCREEN_NOTIFICATION_TAG =
            "mozac_feature_prompts_full_screen_notification_dialog"
    }
}