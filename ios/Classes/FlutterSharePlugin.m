#import "FlutterSharePlugin.h"
#import <flutter_share_plugin/flutter_share_plugin-Swift.h>

@implementation FlutterSharePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterSharePlugin registerWithRegistrar:registrar];
}
@end
