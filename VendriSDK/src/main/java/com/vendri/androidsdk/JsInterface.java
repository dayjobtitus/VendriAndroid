package com.vendri.androidsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JsInterface {
    WebView mView;
    Context mContext;
    Activity pActivity;
    Dialog mDialog;
    AlertDialog aDialog;

    /**
     * Instantiate the interface and set the context
     */
    JsInterface(final WebView v, final Activity a, final AlertDialog dialog) {
        mView = v;
        mContext = mView.getContext();
        pActivity = a;
        aDialog = dialog;
    }

    @JavascriptInterface
    public void adStarted(final String url) {
        Log.v("adStarted", "in VendriAndroidApp.adStarted()");
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (aDialog != null) {
                    aDialog.show();
                }
                Log.i("JSInterface", "Displayed the ad dialog at adStarted Event");
            }
        });
    }

    @JavascriptInterface
    public void adFinished(final String status) {
        String msg = ("in VendriAndroidApp.adFinished() with " + status);
        Log.v("adFinished", msg);

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (aDialog != null) {
                    aDialog.hide();
                }
                Log.i("JSInterface", "Closed the ad dialog at adFinished Event");
            }
        });
    }

    @JavascriptInterface
    public void triggerEvent(final String eventName, final String status, final String dataJSON) {
        /*
        In our JS engine, we have this:
        if (window && window.VendriAndroidApp) {
            window.VendriAndroidApp.triggerEvent(eName, instance.events[ eName ], JSON.stringify(data));
        }
        Which will call this method when we want to fire some event into the application which publishers can use.
         */
        String msg = ("in VendriAndroidApp.triggerEvent() with " + status);
        Log.v("triggerEvent", msg);

        try {

            Class c = Class.forName(pActivity.getClass().getCanonicalName());
            Method method = pActivity.getClass().getDeclaredMethod(status, null);
            method.invoke(pActivity, null);

        } catch (NoSuchMethodException exception) {
            Log.v("Method not found", exception.getMessage());
        } catch (IllegalAccessException exception) {
            Log.v("IllegalAccessException", exception.getMessage());
        } catch (InvocationTargetException exception) {
            Log.v("InvocationTarget", exception.getMessage());
        } catch (ClassNotFoundException exc) {
            Log.v("ClassNotFoundException", exc.getMessage());
        }

    }

    @JavascriptInterface
    public String getScopedVariables(String variable) {
        String msg = ("in VendriAndroidApp.getScopedVariables() with " + variable);
        Object value = null;
        String val = "";
        try {
            Field field = pActivity.getClass().getDeclaredField(variable);

            field.setAccessible(true);

            value = field.get(pActivity);

            JSONObject a = new JSONObject();
            Gson gson = new Gson();
            val = gson.toJson(value);
            Toast.makeText(mContext, val, Toast.LENGTH_LONG).show();
            return val;
        } catch (NoSuchFieldException exception) {
            Log.v("Field not found", exception.getMessage());
        } catch (IllegalAccessException exception) {
            Log.v("IllegalAccessException", exception.getMessage());
        }

        return val;
    }
}
