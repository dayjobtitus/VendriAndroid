package com.vendri.androidsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import org.json.JSONObject;

public class Vendri {
    private static WebView webView;
    private static Activity tetheredActivity;
    protected static AlertDialog alertDialog;
    private static volatile boolean dialogIsReady = false;

    protected static VendriListener eventListener;

    public static final String VENDRI_URL = "https://vendri.com/test/android.html";

    // call when parent activity dies
    public static void untether() {
        Log.i("Vendri", "Removing WebView from Dialog and cleaning up");
        // remove webview from dialog before killing it
        // or our webview will also get killed
        if (webView.getParent() != null) {
            ViewGroup parent = (ViewGroup)webView.getParent();
            parent.removeView(webView);
        }
        dialogIsReady = false;
        // dismiss the dialog so it doesn't get leaked
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        tetheredActivity = null;
    }

    /**
     * adds a view to the app, in which the ad in the url will be shown and on
     * finished event the view will be automatically closed / removed.
     *
     * @param applicationContext
     * @param pid
     */
    @SuppressLint("NewApi")
    public static void init (final Context applicationContext, final String pid) {
        if (webView != null) {
            Log.w("Vendri", "Called init() when already initialized");
            return;
        }
        Log.i("Vendri", "Creating Vendri WebView");
        webView = new WebView(applicationContext);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        webView.setLayoutParams(params);

        webView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        WebView.setWebContentsDebuggingEnabled(true);

        webView.loadUrl(VENDRI_URL + "?PID=" + pid);
    }

    public static void tetherToActivity(final Activity activity) {
        UiHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    tetheredActivity = activity;

                    RelativeLayout viewLayout = new RelativeLayout(activity);
                    RelativeLayout.LayoutParams dialogParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    viewLayout.setLayoutParams(dialogParams);

                    alertDialog = new AlertDialog.Builder(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                            .setView(viewLayout)
                            .create();
                    alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            if (webView != null) {
                                // adwebview.loadUrl("javascript:ADD_YOUR_CALL_HERE");
                            }
                        }
                    });

                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler errorHandler, SslError error) {
                            errorHandler.proceed();
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                activity.startActivity(intent);
                                // Tell the WebView you took care of it.
                                return true;
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                    try {
                        webView.removeJavascriptInterface("VendriAndroidApp");
                    }
                    catch (Exception e) {
                        Log.e("Vendri", "Can't remove JS Interface");
                        Log.e("Vendri", e.toString());
                    }
                    webView.addJavascriptInterface(new JsInterface(), "VendriAndroidApp");

                    if (webView.getParent() != null) {
                        ViewGroup parent = (ViewGroup)webView.getParent();
                        parent.removeView(webView);
                    }
                    viewLayout.addView(webView);
                    dialogIsReady = true;
                } catch (Exception e) {
                    Log.e("Error at Launch Ad", e.getMessage());
                }
            }
        });
    }

    public static void setListener(VendriListener listener) {
        eventListener = listener;
    }

    @SuppressLint("NewApi")
    public static void viewSaveState(Bundle outState) {
        webView.saveState(outState);
    }

    @SuppressLint("NewApi")
    public static void viewRestoreState(Bundle savedInstanceState) {
        webView.restoreState(savedInstanceState);
    }

    @SuppressLint("NewApi")
    public static void trigger(final String eventName, final JSONObject eventData) {
        String customPub = "";
        if (eventData != null){
            customPub = new Gson().toJson(eventData);
        }
        fireCallEventIntoJs(eventName, customPub);
    }

    @SuppressLint("NewApi")
    public static void fireCallEventIntoJs(final String evtName, final String evtData) {
        if (webView != null && tetheredActivity != null) {
            UiHelper.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:fireEvent('"+evtName+"','"+evtData+"');");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    // Function that will call Vendri.setup() with the given json object as string.
    public static void start(final JSONObject config) {
        final String customConfig = new Gson().toJson(config);

        if (webView != null && tetheredActivity != null) {
            UiHelper.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called start with JSON");
                    webView.loadUrl("javascript:Vendri.setup(" + customConfig + ");");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    // Function that will call Vendri.setup()
    public static void start() {
        if (webView != null && tetheredActivity != null) {
            UiHelper.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called start without JSON");
                    webView.loadUrl("javascript:Vendri.setup();");
                }
            });
        }
    }

    private static void playWhenReady(final JSONObject config, final int PLID) {
        final String customConfig = new Gson().toJson(config);

        if (webView != null && tetheredActivity != null) {
            UiHelper.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called play with JSON");
                    webView.loadUrl("javascript:Vendri.play(" + customConfig + "," + PLID + ");");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    // Function that will call Vendri().play() with the given json object as string.
    public static void play(final JSONObject config, final int PLID) {
        // TODO: Add this safeguard to all play methods
        if (dialogIsReady) {
            playWhenReady(config, PLID);
        }
        else {
            // re-call ourself until we're ready (TODO: add timeout or something)
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    play(config, PLID);
                }
            }, 500);
        }
    }

    @SuppressLint("NewApi")
    // Function that will call Vendri().play()
    public static void play() {
        if (webView != null && tetheredActivity != null) {
            UiHelper.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called play without JSON");
                    webView.loadUrl("javascript:Vendri().play();");
                }
            });
        }
    }
}
