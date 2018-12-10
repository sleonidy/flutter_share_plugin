package com.sleonidy.share.fluttershareplugin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.sleonidy.share.fluttershareplugin.FlutterSharePluginParams.Companion.IS_MULTIPLE
import com.sleonidy.share.fluttershareplugin.FlutterSharePluginParams.Companion.PATH
import com.sleonidy.share.fluttershareplugin.FlutterSharePluginParams.Companion.TEXT
import com.sleonidy.share.fluttershareplugin.FlutterSharePluginParams.Companion.TITLE
import com.sleonidy.share.fluttershareplugin.FlutterSharePluginParams.Companion.TYPE

import java.util.ArrayList
import java.util.HashMap

import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel


/**
 * main activity super, handles eventChannel sink creation
 * , share intent parsing and redirecting to eventChannel sink stream
 *
 * @author Duarte Silveira
 * @version 1
 * @since 25/05/18
 */
class FlutterShareReceiverActivity : FlutterActivity() {

    private var eventSink: EventChannel.EventSink? = null
    private var inited = false
    private val backlog = ArrayList<Intent>()
    private var ignoring = false

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        if (!inited) {
            init(flutterView, this)
        }
    }

    private fun init(flutterView: BinaryMessenger, context: Context) {
        Log.i(this::class.java.simpleName, "initializing eventChannel")

        context.startActivity(Intent(context, ShareReceiverActivityWorker::class.java))

        // Handle other intents, such as being started from the home screen
        EventChannel(flutterView, STREAM).setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(args: Any, events: EventChannel.EventSink) {
                Log.i(this::class.java.simpleName, "adding listener")
                eventSink = events
                ignoring = false
                for (i in backlog.indices) {
                    handleIntent(backlog.removeAt(i))
                }
            }

            override fun onCancel(args: Any) {
                Log.i(this::class.java.simpleName, "cancelling listener")
                ignoring = true
                eventSink = null
            }
        })

        inited = true

        handleIntent(intent)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    fun handleIntent(intent: Intent) {
        // Get intent, action and MIME type
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                Log.i(this::class.java.simpleName, "receiving shared title: $sharedTitle")
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                Log.i(this::class.java.simpleName, "receiving shared text: $sharedText")
                if (eventSink != null) {
                    val params = HashMap<String, String>()
                    params[TYPE] = type
                    params[TEXT] = sharedText
                    if (!TextUtils.isEmpty(sharedTitle)) {
                        params[TITLE] = sharedTitle
                    }
                    eventSink!!.success(params)
                } else if (!ignoring && !backlog.contains(intent)) {
                    backlog.add(intent)
                }
            } else {
                val sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                Log.i(this::class.java.simpleName, "receiving shared title: $sharedTitle")
                val sharedUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                Log.i(this::class.java.simpleName, "receiving shared file: $sharedUri")
                if (eventSink != null) {
                    val params = HashMap<String, String>()
                    params[TYPE] = type
                    params[PATH] = sharedUri.toString()
                    if (!TextUtils.isEmpty(sharedTitle)) {
                        params[TITLE] = sharedTitle
                    }
                    if (!intent.hasExtra(Intent.EXTRA_TEXT)) {
                        params[TEXT] = intent.getStringExtra(Intent.EXTRA_TEXT)
                    }
                    eventSink!!.success(params)
                } else if (!ignoring && !backlog.contains(intent)) {
                    backlog.add(intent)
                }
            }

        } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            Log.i(this::class.java.simpleName, "receiving shared files!")
            val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            if (eventSink != null) {
                val params = HashMap<String, String>()
                params[TYPE] = type
                params[IS_MULTIPLE] = "true"
                for (i in uris.indices) {
                    params[Integer.toString(i)] = uris[i].toString()
                }
                eventSink!!.success(params)
            } else if (!ignoring && !backlog.contains(intent)) {
                backlog.add(intent)
            }

        }
    }

    companion object {

        val STREAM = "com.github.sleonidy/receiveshare"
    }
}
