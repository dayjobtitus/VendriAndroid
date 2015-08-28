VendriSDKandroid
================

Android client SDK for Vendri V2

* Vendri needs to be initialized when the Android activity is loaded. Recommended to load it at onCreate in an Activity to make sure Vendri loads only one time.
* In your activity, implement **VendriListener** to subscribe to the vendri events to your application
```
public class VendriPlayer extends Activity implements VendriListener {
        
  }
```
* Intitalize the VendriListener to current context.
```
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

* Initialize the VendriEngine by passing VendriListener and pid. By passing the pid, Vendri creates its instance with your configuration that has been created using the Vendri Management site. To initialize, just call **init**

**Vendri.init**

```
   Vendri.init(this, VendriListener,pid);
```

* You are done with Vendri setup. Now Vendri takes care of playing media in your Activity based on the configuration. Here is the complete code example.
```
public class VendriPlayer extends Activity implements VendriListener {
        VendriListerner vendriListener;
        String pid = "YOUR_VENDRI_PID";
        @Override
        public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Your view loading code here
        ......
        
        
        // VendriListener initialized the listener as a varaiable for further use.
        if (vendriListener == null) {
            vendriListener = this;
        }
        Vendri.init(this, vendriListener,pid);
    }
 }
```

**Triggers**

*Vendri.trigger* is to inform the Vendri engine with the custom setting action you want to perform.

```
/*
*@param eventName
*@param eventData
*/
Vendri.trigger(String eventName,JSONObject eventData);
```


**Vendri.start**

*Vendri.start* is to call the Vendri engine to initialize your Vendri media with custom configuration.

```
/*
*@param configJSON 
* Param is optional. Passing configJSON will init the Vendri with your own config irrespective of pid respective config
*/
Vendri.start(JSONObject configJSON);
```
* With this call, you can have an option to load your own configurataion from custom action.

**Vendri.play**

*Vendri.play* is to call the Vendri engine to initialize your Vendri media with custom configuration.

```
/*
*@params configJSON 
* Param is optional. Passing configJSON will play the Vendri with your own config irrespective of pid respective config
*/
Vendri.play(JSONObject configJSON);
```
* With this call, you can have an option to play media with your own configurataion from custom action.





Note :
- Vendri has to be loaded on every activity if the media wants to play at every place. 
- If your app is Fragment based, then creating Vendri on your Activity where Fragment replaces will run Vendri throughout the application.
- VendriListener is under development and callbacks are not supported for beta.
