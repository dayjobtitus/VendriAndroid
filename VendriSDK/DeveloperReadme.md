VendriSDKandroid
================

Android client SDK for Vendri

* Vendri needs to be initialized when the Android activity is loaded. Recommended to load it at onCreate in an Activity to make sure Vendri loads only one time.
* In activity publisher has  implement **VendriListener** to subscribe to the vendri events to the publisher application. This listener needs to be passed while initializing the Vendri

```
public class VendriPlayer extends Activity implements VendriListener {
        VendriListerner vendriListener;
        @Override
        public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Your view loading code here
        ......
        
        
        // VendriListener initialized the listener as a varaiable for further use.
        if (vendriListener == null) {
            vendriListener = this;
        }
    }
```

* Initialize the VendriEngine by passing VendriListener and pid. By passing the pid, Vendri creates its instance with your configuration that has been created using the Vendri Management site. To initialize, just call **initVendriEngine**. This function is avaiable in VendriEngineHanlder where takes the pid, creates an AlertDialog and loads the webview into the url.
As mentioned AlertDialog is just created and not shown.
```
public class Vendri{
   public static void init(final Context mContext, final VendriListener callback,
                                        final String pid) {
        PA = (Activity) mContext;
        // final Context mContext = context;
        PA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
                    View dialogView = null;
                    dialogView = inflater.inflate(
                            R.layout.vendri_player_layout, null);
                    // playVideoInHTMLPlayer(mContext, dialogView, videoURL);

                    mDialog = new AlertDialog.Builder(PA)
                            .setView(
                                    dialogView)
                            .create();
                    playVideoInHTMLPlayer(mContext, dialogView,
                            adUrl + "?pid=" + pid);
                } catch (Exception e) {
                    Log.e("Error at Launch Add", e.getMessage());
                }

            }
        });

    }
}
```

* **playVideoInHTMLPlayer** takes care on adding the webview into the dialog and loads the url. *adUrl* is the string which is hardcoded globally. Make sure it is pointing to right url while shipping the build.
* Webview loaded needs a Javascript interface to make communication between Android and javascript. Params are JSInterface and the string which has to be used in javascript to call the native functions.
```
adwebview.addJavascriptInterface(new JsInterface(adwebview, PA, mDialog),
                "VendriAndroidApp");
```
* To call native function from JS, simply call the function using VendriAndroidApp
```
For example, to call adStarted
window.VendrioAndroidApp.adStarted();
```
* In JSInterface, all the functions are handled which were invoked by javascript. The constructor for JSInterface has all major instances like webview, current activity and the dialog. So we can easily handle the requirements at any point of time using those.

**Triggers**
* For triggers, Vendri class supports a function called *trigger(eventName,jsonObject)* 
* Publisher has to call this function whenever he wants to say to vendri about his trigger.

**Start**
* *Vendri.start()* or *Vendri.start(configJSON)* is used by publisher to call the Vendri setup manually.
* This function will call Vendri.setup() in the engine.

**Play**
* *Vendri.play()* or *Vendri.play(configJSON)* is used by publisher whenever he wants to play the media manually.
* This function will call Vendri().play() in the engine.

Note :
- Vendri has to be loaded on every activity if the media wants to play at every place. 
- If your app is Fragment based, then creating Vendri on your Activity where Fragment replaces will run Vendri throughout the application.
- VendriListener is under development and callbacks are not functional yet.
