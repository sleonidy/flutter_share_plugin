package com.sleonidy.share.fluttershareplugin

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*

class FlutterSharePlugin private constructor(private val registrar: Registrar) : MethodCallHandler {
    enum class ShareType(internal var mimeType: String) {
        TYPE_PLAIN_TEXT("text/plain"),
        TYPE_IMAGE("image/*"),
        TYPE_FILE("*/*");

        override fun toString(): String {
            return mimeType
        }

        companion object {

            internal fun fromMimeType(mimeType: String?): ShareType? {
                for (shareType in values()) {
                    if (shareType.mimeType == mimeType) {
                        return shareType
                    }
                }
                return null /* dialog title optional */
            }
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "share") {
            if (call.arguments == null)
                throw IllegalArgumentException("Arguments cannot be null")
            if (call.arguments !is Map<*, *>) {
                throw IllegalArgumentException("Map argument expected")
            }
            val params = FlutterSharePluginParams(
                call.argument(FlutterSharePluginParams.IS_MULTIPLE) ?: false,
                ShareType.fromMimeType(call.argument(FlutterSharePluginParams.TYPE))
                    ?: ShareType.TYPE_FILE,
                call.argument(FlutterSharePluginParams.TITLE) ?: "",
                call.argument(FlutterSharePluginParams.TEXT) ?: "",
                call.argument(FlutterSharePluginParams.PATH) ?: ""
            )
            // Android does not support showing the share sheet at a particular point on screen.
            when {
                params.isMultiple -> shareMultiple(
                    getUriListFromArguments(call),
                    params.type,
                    params.title
                )
                else -> share(
                    params.path,
                    params.text,
                    params.type,
                    params.title
                )
            }
            result.success(null)
        } else {
            result.notImplemented()
        }
    }

    private fun getUriListFromArguments(call: MethodCall): ArrayList<Uri> {
        val dataList = ArrayList<Uri>()
        var i = 0
        while (call.hasArgument(i.toString())) {
            dataList.add(Uri.parse(call.argument<String>(i.toString())))
            i++
        }
        return dataList
    }

    private fun share(path: String?, text: String?, shareType: ShareType?, title: String) {
        if (ShareType.TYPE_PLAIN_TEXT != shareType && (path == null || path.isEmpty())) {
            throw IllegalArgumentException("Non-empty path expected")
        } else if (ShareType.TYPE_PLAIN_TEXT == shareType && (text == null || text.isEmpty())) {
            throw IllegalArgumentException("Non-empty text expected")
        }
        if (shareType == null) {
            throw IllegalArgumentException("Non-empty mimeType expected")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            if (!TextUtils.isEmpty(title)) {
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            if (ShareType.TYPE_PLAIN_TEXT != shareType) {
                putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                if (!TextUtils.isEmpty(text)) {
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            } else {
                putExtra(Intent.EXTRA_TEXT, text)
            }
            type = shareType.mimeType
        }


        val chooserIntent = Intent.createChooser(shareIntent, null/* dialog title optional */)
        if (registrar.activity() != null) {
            registrar.activity().startActivity(chooserIntent)
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            registrar.context().startActivity(chooserIntent)
        }
    }

    private fun shareMultiple(dataList: ArrayList<Uri>, shareType: ShareType, title: String) {
        if (dataList.isEmpty()) {
            throw IllegalArgumentException("Non-empty data expected")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            if (!TextUtils.isEmpty(title)) {
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, dataList)
            type = shareType.mimeType
        }

        val chooserIntent = Intent.createChooser(shareIntent, null)
        if (registrar.activity() != null) {
            registrar.activity().startActivity(chooserIntent)
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            registrar.context().startActivity(chooserIntent)
        }
    }

    companion object {

        private val CHANNEL = "com.github.sleonidy/share"
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), CHANNEL)
            val instance = FlutterSharePlugin(registrar)
            channel.setMethodCallHandler(instance)
        }
    }

}
