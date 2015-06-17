package com.vendri.androidsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.rapidbizapps.vendrisdk.R;

import org.json.JSONObject;

public class Vendri {
    public static WebView vendriwebview;
    protected final static String ADURL = "adUrl";
    private static Activity PA;
    protected static VendriListener sCallback;
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
        sCallback = callback;
        PA = (Activity) mContext;
        PA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    LayoutInflater inflater = (LayoutInflater) mContext
                                .getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
                        View dialogView = null;
                        dialogView = inflater.inflate(
                                R.layout.vendri_player_layout, null);
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
        ((RelativeLayout) dialogView).addView(vendriwebview);
        vendriwebview.setWebChromeClient(new
                WebChromeClient());
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
