import Flutter
import UIKit

public class SwiftFlutterSharePlugin: NSObject, FlutterPlugin, UIActivityItemSource {
    private var flutterShareParams: FlutterSharePluginParams? = nil

    public func activityViewControllerPlaceholderItem(_ activityViewController: UIActivityViewController) -> Any {
        return ""
    }

    public func activityViewController(_ activityViewController: UIActivityViewController, itemForActivityType activityType: UIActivity.ActivityType?) -> Any? {
        if (activityType == .mail) {
            return flutterShareParams?.text
        }
        return nil
    }

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "com.github.sleonidy/share", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterSharePlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        result("iOS " + UIDevice.current.systemVersion)
        NSLog("Inside handle")
        switch call.method {
        case "share":
            guard let params = parseArgs(call, result: result) else {
                result(FlutterError(code: "ParamsMissing", message: "Cannot parse params", details: nil))
                break
            }
            flutterShareParams = params
            share(params, result: result)
        default:
            result(FlutterMethodNotImplemented)
        }
    }

    private func parseArgs(_ call: FlutterMethodCall, result: @escaping FlutterResult) -> FlutterSharePluginParams? {
        guard let args = call.arguments as? [String: Any?] else {
            return nil
        }

        return FlutterSharePluginParams(
                type: args[FlutterSharePluginParams.TYPE] as? String,
                path: args[FlutterSharePluginParams.PATH] as? String,
                title: args[FlutterSharePluginParams.TITLE] as? String,
                text: args[FlutterSharePluginParams.TEXT] as? String,
                isMultiple: args[FlutterSharePluginParams.IS_MULTIPLE] as! Bool,
                originX: args[FlutterSharePluginParams.IS_MULTIPLE] as? NSNumber,
                originY: args[FlutterSharePluginParams.IS_MULTIPLE] as? NSNumber,
                originHeight: args[FlutterSharePluginParams.IS_MULTIPLE] as? NSNumber,
                originWidth: args[FlutterSharePluginParams.IS_MULTIPLE] as? NSNumber,
                arguments: args
        )
    }

    private func share(_ params: FlutterSharePluginParams, result: @escaping FlutterResult) {
        
        NSLog("Share")
        var avc:UIActivityViewController
        switch params.type {
        case "image/*":
            avc = shareImages(params, result: result)
        case "text/plain":
            avc = shareTexts(params, result: result)
        default:
            avc = shareFiles(params, result: result)

        }
        avc.setValue(params.title, forKey: "subject")
        UIApplication.shared.keyWindow?.rootViewController!.present(avc, animated: true, completion: nil)
    }

    private func shareImages(_ params: FlutterSharePluginParams,  result: @escaping FlutterResult) ->UIActivityViewController {
        var array: Array<Any?> = Array<Any?>()
        if params.isMultiple {
            var i = 0
            while params.arguments[String(i)] != nil {
                array.append(UIImage(contentsOfFile: params.arguments[String(i)] as! String))
                i = i + 1
            }
        } else {
            array.append(UIImage(contentsOfFile: params.path!))
        }
        array.append(self)
        let avc = UIActivityViewController(activityItems: array as [Any], applicationActivities: nil)
        return avc
    }

    private func shareTexts(_ params: FlutterSharePluginParams,  result: @escaping FlutterResult) ->UIActivityViewController {
        var array: Array<Any> = Array<Any>()
        if params.isMultiple {
            var i = 0
            while params.arguments[String(i)] != nil {
                array.append(params.arguments[String(i)] as? String ?? "")
                i = i + 1
            }
        } else {
            array.append(params.text ?? "")
        }
        array.append(self)
        let avc = UIActivityViewController(activityItems: array as [Any], applicationActivities: nil)
        return avc
    }

    private func shareFiles(_ params: FlutterSharePluginParams,  result: @escaping FlutterResult)->UIActivityViewController  {
        var array: Array<Any?> = Array<Any?>()
        if params.isMultiple {
            var i = 0
            while params.arguments[String(i)] != nil {
                array.append(URL(fileURLWithPath: params.arguments[String(i)] as! String))
                i = i + 1
            }
        } else {
            array.append(URL(fileURLWithPath: params.path!))
        }
        array.append(self)
        let avc = UIActivityViewController(activityItems: array as [Any], applicationActivities: nil)
        return avc
    }
}

struct FlutterSharePluginParams {
    static let TYPE = "type"
    static let PATH = "path"
    static let TITLE = "title"
    static let TEXT = "text"
    static let IS_MULTIPLE = "is_multiple"
    static let ORIGIN_X = "originX"
    static let ORIGIN_Y = "originY"
    static let ORIGIN_HEIGHT = "originHeight"
    static let ORIGIN_WIDTH = "originWidth"
    let type: String?
    let path: String?
    let title: String?
    let text: String?
    let isMultiple: Bool
    let originX: NSNumber?
    let originY: NSNumber?
    let originHeight: NSNumber?
    let originWidth: NSNumber?
    let arguments: [String: Any?]
}
