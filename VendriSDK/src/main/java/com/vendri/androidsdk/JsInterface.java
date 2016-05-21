package com.vendri.androidsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JsInterface {

    /**
     * Instantiate the interface and set the context
     */
    JsInterface() {
    }

    @JavascriptInterface
    public void adStarted(final String url) {
        Log.v("adStarted", "in VendriAndroidApp.adStarted()");

        // TODO: try/catch this
        if (Vendri.eventListener != null){
            Vendri.eventListener.adStarted();
        }
        UiHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Vendri.alertDialog != null) {
                    // TODO: What if alertDialog has been dismissed?

                    // make sure it's full screen since Dialogs are wonky
                    Vendri.alertDialog.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
                    Vendri.alertDialog.getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;

                    Vendri.alertDialog.show();
                    Log.i("JSInterface", "Displayed the ad dialog at adStarted Event");
                }
            }
        });
    }

    @JavascriptInterface
    public void adFinished(final String status) {
        Log.v("adFinished", "in VendriAndroidApp.adFinished() with " + status);

        // TODO: try/catch this
        if (Vendri.eventListener != null){
            Vendri.eventListener.adFinished();
        }
        UiHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Vendri.alertDialog != null && Vendri.alertDialog.isShowing()) {
                    Vendri.alertDialog.hide();
                    Log.i("JSInterface", "Closed the ad dialog at adFinished Event");
                }
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
        if (Vendri.eventListener == null) {
            return;
        }
        String msg = ("in VendriAndroidApp.triggerEvent() with " + status);
        Log.v("triggerEvent", msg);

        try {
            Method method = Vendri.eventListener.getClass().getDeclaredMethod(status, null);
            method.invoke(Vendri.eventListener, null);
        } catch (NoSuchMethodException exception) {
            Log.v("Method not found", exception.getMessage());
        } catch (IllegalAccessException exception) {
            Log.v("IllegalAccessException", exception.getMessage());
        } catch (InvocationTargetException exception) {
            Log.v("InvocationTarget", exception.getMessage());
        }
    }

    @JavascriptInterface
    public String getScopedVariables(String variable) {
        /*
        String msg = ("in VendriAndroidApp.getScopedVariables() with " + variable);
        Object value = null;
        String val = "";
        try {
            Field field = parentActivity.getClass().getDeclaredField(variable);

            field.setAccessible(true);

            value = field.get(parentActivity);

            JSONObject a = new JSONObject();
            Gson gson = new Gson();
            val = gson.toJson(value);
            Toast.makeText(parentActivity, val, Toast.LENGTH_LONG).show();
            return val;
        } catch (NoSuchFieldException exception) {
            Log.v("Field not found", exception.getMessage());
        } catch (IllegalAccessException exception) {
            Log.v("IllegalAccessException", exception.getMessage());
        }

        return val;*/
        return "";
    }
}
