package zhuoyuan.li.fluttershareme;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterShareMePlugin
 */
public class FlutterShareMePlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

    final private static String _methodWhatsApp = "whatsapp_share";
    final private static String _methodWhatsAppPersonal = "whatsapp_personal";
    final private static String _methodWhatsAppBusiness = "whatsapp_business_share";
    final private static String _methodFaceBook = "facebook_share";
    final private static String _methodTwitter = "twitter_share";
    final private static String _methodSystemShare = "system_share";
    final private static String _methodInstagramShare = "instagram_share";
    final private static String _methodTelegramShare = "telegram_share";
    final private static String _methodWechatShare = "wechat_share";
    final private static String _methodMessengerShare = "messenger_share";
    final private static String _methodViberShare = "viber_share";
    final private static String _methodCheckIsAppInstalled = "check_is_app_installed";


    private Activity activity;
    private static CallbackManager callbackManager;
    private MethodChannel methodChannel;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final FlutterShareMePlugin instance = new FlutterShareMePlugin();
        instance.onAttachedToEngine(registrar.messenger());
        instance.activity = registrar.activity();
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        activity = null;
    }

    private void onAttachedToEngine(BinaryMessenger messenger) {
        methodChannel = new MethodChannel(messenger, "flutter_share_me");
        methodChannel.setMethodCallHandler(this);
        callbackManager = CallbackManager.Factory.create();
    }

    /**
     * method
     *
     * @param call   methodCall
     * @param result Result
     */
    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {
        String url, msg;
        switch (call.method) {
            case _methodFaceBook:
                url = call.argument("url");
                msg = call.argument("msg");
                shareToFacebook(url, msg, result);
                break;
            case _methodTwitter:
                url = call.argument("url");
                msg = call.argument("msg");
                shareToTwitter(url, msg, result);
                break;
            case _methodWhatsApp:
                msg = call.argument("msg");
                url = call.argument("url");
                shareWhatsApp(url, msg, result, false);
                break;
            case _methodWhatsAppBusiness:
                msg = call.argument("msg");
                url = call.argument("url");
                shareWhatsApp(url, msg, result, true);
                break;
            case _methodWhatsAppPersonal:
                msg = call.argument("msg");
                String phoneNumber = call.argument("phoneNumber");
                shareWhatsAppPersonal(msg, phoneNumber, result);
                break;
            case _methodSystemShare:
                msg = call.argument("msg");
                shareSystem(result, msg);
                break;
            case _methodInstagramShare:
                msg = call.argument("url");
                shareInstagramStory(msg, result);
                break;
            case _methodTelegramShare:
                msg = call.argument("msg");
                shareToTelegram(msg, result);
                break;
            case _methodWechatShare:
                msg = call.argument("msg");
                shareToWechat(msg, result);
                break;
            case _methodMessengerShare:
                url = call.argument("url");
                msg = call.argument("msg");
                shareToMessenger(msg, result);
                break;
            case _methodViberShare:
                msg = call.argument("msg");
                shareToViber(msg, result);
                break;
            case _methodCheckIsAppInstalled:
                ArrayList<String> appNames = (ArrayList<String>) call.argument("apps");
                checkIsAppInstalled(appNames, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /**
     * system share
     *
     * @param msg    String
     * @param result Result
     */
    private void shareSystem(Result result, String msg) {
        try {
            Intent textIntent = new Intent("android.intent.action.SEND");
            textIntent.setType("text/plain");
            textIntent.putExtra("android.intent.extra.TEXT", msg);
            activity.startActivity(Intent.createChooser(textIntent, "Share to"));
            result.success("success");
        } catch (Exception var7) {
            result.error("error", var7.toString(), "");
        }
    }

    /**
     * share to twitter
     *
     * @param url    String
     * @param msg    String
     * @param result Result
     */

    private void shareToTwitter(String url, String msg, Result result) {
        String urlScheme = "http://www.twitter.com/intent/tweet?text="+msg+" "+url;
        Intent twitterIntent = new Intent(Intent.ACTION_VIEW);
        twitterIntent.setType("text/plain");
        twitterIntent.setData(Uri.parse(urlScheme));

        try {
            activity.startActivity(twitterIntent);
            result.success("success");
        } catch (Exception e) {
            result.error("error", e.toString(), "");
            e.printStackTrace();
        }
    }

    /**
     * share to Facebook
     *
     * @param url    String
     * @param msg    String
     * @param result Result
     */
    private void shareToFacebook(String url, String msg, Result result) {

        ShareDialog shareDialog = new ShareDialog(activity);
        // this part is optional
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                System.out.println("--------------------success");
            }

            @Override
            public void onCancel() {
                System.out.println("-----------------onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println("---------------onError");
            }
        });

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .setQuote(msg)
                .build();
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            shareDialog.show(content);
            result.success("success");
        }

    }

    /**
     * share to whatsapp
     *
     * @param msg                String
     * @param result             Result
     * @param shareToWhatsAppBiz boolean
     */
    private void shareWhatsApp(String imagePath, String msg, Result result, boolean shareToWhatsAppBiz) {
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            
            whatsappIntent.setPackage(shareToWhatsAppBiz ? "com.whatsapp.w4b" : "com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, msg);
            // if the url is the not empty then get url of the file and share
            if (!TextUtils.isEmpty(imagePath)) {
                whatsappIntent.setType("*/*");
                System.out.print(imagePath+"url is not empty");
                File file = new File(imagePath);
                Uri fileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            else {
                whatsappIntent.setType("text/plain");
            }
            activity.startActivity(whatsappIntent);
            result.success("success");
        } catch (Exception var9) {
            result.error("error", var9.toString(), "");
        }
    }
      /**
     * share to telegram
     *
     * @param msg                String
     * @param result             Result
     */
    
    private void shareToTelegram(String msg, Result result) {
        try {
            Intent telegramIntent = new Intent(Intent.ACTION_SEND);
            telegramIntent.setType("text/plain");
            telegramIntent.setPackage("org.telegram.messenger");
            telegramIntent.putExtra(Intent.EXTRA_TEXT, msg);
            try {
                activity.startActivity(telegramIntent);
                result.success("true");
            } catch (Exception ex) {
                result.success("false:Telegram app is not installed on your device");
            }
        } catch (Exception var9) {
            result.error("error", var9.toString(), "");
        }
    }
    /**
     * share whatsapp message to personal number
     *
     * @param msg         String
     * @param phoneNumber String with country code
     * @param result
     */
    private void shareWhatsAppPersonal(String msg, String phoneNumber, Result result) {
        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage("com.whatsapp");
        i.setData(Uri.parse(url));
        activity.startActivity(i);
        result.success("success");
    }

    /**
     * share to instagram
     *
     * @param url    local image path
     * @param result flutterResult
     */
    private void shareInstagramStory(String url, Result result) {
        if (instagramInstalled()) {
            File file = new File(url);
            Uri fileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);

            Intent instagramIntent = new Intent(Intent.ACTION_SEND);
            instagramIntent.setType("image/*");
            instagramIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            instagramIntent.setPackage("com.instagram.android");
            try {
                activity.startActivity(instagramIntent);
                result.success("Success");
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                result.success("Failure");
            }
        } else {
            result.error("Instagram not found", "Instagram is not installed on device.", "");
        }
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {

    }

    ///Utils methods
    private boolean instagramInstalled() {
        try {
            if (activity != null) {
                activity.getPackageManager()
                        .getApplicationInfo("com.instagram.android", 0);
                return true;
            } else {
                Log.d("App", "Instagram app is not installed on your device");
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
//        return false;
    }

    /**
     * share to wechat
     *
     * @param msg                String
     * @param result             Result
     */
    
    private void shareToWechat(String msg, Result result) {
        try {
            Intent wechatIntent = new Intent(Intent.ACTION_SEND);
            wechatIntent.setType("text/plain");
            wechatIntent.setPackage("com.tencent.mm");
            wechatIntent.putExtra(Intent.EXTRA_TEXT, msg);
            try {
                activity.startActivity(wechatIntent);
                result.success("true");
            } catch (Exception ex) {
                result.success("false:Wechat app is not installed on your device");
            }
        } catch (Exception var9) {
            result.error("error", var9.toString(), "");
        }
    }

    /**
     * share to messenger
     *
     * @param msg                String
     * @param result             Result
     */
    
    private void shareToMessenger(String msg, Result result) {
        try {
            Intent messengerIntent = new Intent();
            messengerIntent.setAction(Intent.ACTION_SEND);
            messengerIntent.setType("text/plain");
            messengerIntent.setPackage("com.facebook.orca");
            messengerIntent.putExtra(Intent.EXTRA_TEXT, msg);
            try {
                activity.startActivity(messengerIntent);
                result.success("true");
            } catch (Exception ex) {
                result.success("false:Messenger app is not installed on your device");
            }
        } catch (Exception var9) {
            result.error("error", var9.toString(), "");
        }
    }

    /**
     * share to viber
     *
     * @param msg                String
     * @param result             Result
     */
    
    private void shareToViber(String msg, Result result) {
        String urlScheme = "viber://forward?text="+msg;
        Intent viberIntent = new Intent(Intent.ACTION_VIEW);
        viberIntent.setType("text/plain");
        viberIntent.setData(Uri.parse(urlScheme));

        try {
            activity.startActivity(viberIntent);
            result.success("success");
        } catch (Exception e) {
            result.error("error", e.toString(), "");
            e.printStackTrace();
        }
    }

    private void checkIsAppInstalled(ArrayList<String> appNames, Result result) {
        String packageName = "";
        ArrayList<String> installedApps = new ArrayList<String>();
    
        for (int i = 0; i < appNames.size(); i++) {
            String name = appNames.get(i);

            switch (name) {
                case "facebook":
                    packageName = "com.facebook.katana";
                    break;
                case "messenger":
                    packageName = "com.facebook.orca";
                    break;
                case "instagram":
                    packageName = "com.instagram.android";
                    break;
                case "twitter":
                    packageName = "com.twitter.android";
                    break;
                case "whatsapp":
                    packageName = "com.whatsapp";
                    break;
                case "telegram":
                    packageName = "org.telegram.messenger";
                    break;
                case "wechat":
                    packageName = "com.tencent.mm";
                    break;
                default:
                    result.notImplemented();
                    break;
            }

            try {
                if (packageName != "" && activity != null) {
                    activity.getPackageManager()
                            .getApplicationInfo(packageName, 0);
                    installedApps.add(name);
                } else {
                    Log.i("App", packageName + "App is not installed on your device");
                }
            } catch (PackageManager.NameNotFoundException e) {
                // do nothing
            }
        }

        // client to split the string
        String returnedString = String.join(",", installedApps);

        result.success(returnedString);
    }
}
