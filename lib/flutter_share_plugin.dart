import 'dart:async';

import 'package:flutter/services.dart';

enum ShareType { Image, File, Text }

class FlutterSharePlugin {
  static const MethodChannel _channel =
      MethodChannel('com.github.sleonidy/share');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future shareImages({required String title, String? message}) async {}
}
