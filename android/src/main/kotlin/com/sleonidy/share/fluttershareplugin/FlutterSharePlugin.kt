package com.sleonidy.share.fluttershareplugin

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterSharePlugin: MethodCallHandler {
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "com.github.sleonidy/share")
      channel.setMethodCallHandler(FlutterSharePlugin())
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {

  }
}
