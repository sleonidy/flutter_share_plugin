package com.sleonidy.share.fluttershareplugin

import android.content.Intent
import android.os.Bundle
import io.flutter.app.FlutterActivity

/**
 * Share intent receiver, passes it along to the flutter activity
 *
 * @author Duarte Silveira
 * @version 1
 * @since 25/05/18
 */
class ShareReceiverActivityWorker : FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get intent, action and MIME type
        val action = intent.action
        val type = intent.type
        if ((Intent.ACTION_SEND == action || Intent.ACTION_SEND_MULTIPLE == action) && type != null) {
            passShareToMainActivity(intent)
        } else {
            finish()
        }

    }

    private fun passShareToMainActivity(intent: Intent) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.action = intent.action
        launchIntent?.type = intent.type
        launchIntent?.putExtras(intent)

        startActivity(launchIntent)
        finish()
    }
}
