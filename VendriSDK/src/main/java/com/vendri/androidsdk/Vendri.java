package com.vendri.androidsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import org.json.JSONObject;

public class Vendri {
    public static WebView vendriwebview;
    protected final static String ADURL = "adUrl";
    private static Activity PA;
    protected static VendriListener vendriCallback;
    public static AlertDialog mDialog;
    public static final String adUrl = "http://vendri.com/test/android.html";

    /**
     * adds a view to the app, in which the ad in the url will be shown and on
     * finished event the view will be automatically closed / removed.
     *
     * @param mContext
     * @param callback
     * @param pid
     */
    @SuppressLint("NewApi")
    public static void init(final Context mContext, final VendriListener callback,
                            final String pid) {
        vendriCallback = callback;
        PA = (Activity) mContext;
        PA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    RelativeLayout dialogView = new RelativeLayout(PA);

                    RelativeLayout.LayoutParams dialogParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);


                    dialogView.setLayoutParams(dialogParams);

                    mDialog = new AlertDialog.Builder(PA)
                                .setView(
                                        dialogView)
                                .create();
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        public void onCancel(DialogInterface dialog) {

                            if(vendriwebview != null){
                                // adwebview.loadUrl("javascript:ADD_YOUR_CALL_HERE");
                            }
                        }
                    });

                    playVideoInHTMLPlayer(mContext, dialogView,
                                adUrl + "?pid=" + pid);
                } catch (Exception e) {
                    Log.e("Error at Launch Add", e.getMessage());
                }
            }
        });
    }

    @SuppressLint("NewApi")
    public static void playVideoInHTMLPlayer(final Context context, final View dialogView,
                                             final String videoURL) {
        vendriwebview = new WebView(context);
//        final Activity activity = (Activity) context;
//        String vendriHtml = "function getParameterByName(name) {name = name.replace(/[\\[]/, \"\\\\[\").replace(/[\\]]/, \"\\\\]\");var regex = new RegExp(\"[\\\\?&]\" + name + \"=([^&#]*)\"),results = regex.exec(location.search);return results === null ? \"\" : decodeURIComponent(results[1].replace(/\\+/g, \" \"));}\n" +
//                "var pid = getParameterByName(\"pid\"), scripts = document.createElement('script');scripts.src = \"http://vendri.com/test/js/vendri_test.min.js?pid=\"+pid;document.head.appendChild(scripts);";

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        vendriwebview.setLayoutParams(params);

        vendriwebview.setWebChromeClient(new
                WebChromeClient());
        vendriwebview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    PA.startActivity(intent);
                    //Tell the WebView you took care of it.
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });


        WebSettings webSettings = vendriwebview.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        vendriwebview.addJavascriptInterface(new JsInterface(vendriwebview, PA, mDialog),
                "VendriAndroidApp");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        ((RelativeLayout) dialogView).addView(vendriwebview);

        vendriwebview.loadUrl(videoURL);
//        vendriwebview.loadDataWithBaseURL(null, vendriHtml, "text/html", "utf-8", null);
    }

    @SuppressLint("NewApi")
    public static void viewSaveState(Bundle outState) {
        vendriwebview.saveState(outState);
    }

    @SuppressLint("NewApi")
    public static void viewRestoreState(Bundle savedInstanceState) {
        vendriwebview.restoreState(savedInstanceState);
    }

    @SuppressLint("NewApi")
    public static void trigger(final String eventName, final JSONObject eventData) {
        Gson gson = new Gson();
        String customPub = "";
        if(eventData != null){
            customPub = gson.toJson(eventData);
        }
        fireCallEventIntoJs(eventName, customPub);
    }

    @SuppressLint("NewApi")
    public static void fireCallEventIntoJs(final String evtName, final String evtData) {
        if(vendriwebview != null && PA != null){
            PA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vendriwebview.loadUrl("javascript:fireEvent('"+evtName+"','"+evtData+"');");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    //Function that will call Vendri.setup() with the given json object as string.
    public static void start(final JSONObject config) {
        Gson gson = new Gson();

        final String customConfig = gson.toJson(config);

        if(vendriwebview != null && PA != null){
            PA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called start with JSON");
                    vendriwebview.loadUrl("javascript:Vendri.setup("+customConfig+");");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    //Function that will call Vendri.setup()
    public static void start() {


        if(vendriwebview != null && PA != null){
            PA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called start without JSON");
                    vendriwebview.loadUrl("javascript:Vendri.setup();");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    //Function that will call Vendri().play() with the given json object as string.
    public static void play(final JSONObject config) {
        Gson gson = new Gson();

        final String customConfig = gson.toJson(config);

        if(vendriwebview != null && PA != null){
            PA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called play with JSON");
                    vendriwebview.loadUrl("javascript:Vendri().play("+customConfig+");");
                }
            });
        }
    }

    @SuppressLint("NewApi")
    //Function that will call Vendri().play()
    public static void play() {


        if(vendriwebview != null && PA != null){
            PA.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Vendri", "Called play without JSON");
                    vendriwebview.loadUrl("javascript:Vendri().play();");
                }
            });
        }
    }
}
