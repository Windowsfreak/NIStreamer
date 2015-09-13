package org.openni.android.tools.niviewer;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by lazer_000 on 08.05.2015.
 */
public class MyToaster {
    private static Context context;
    public static void setContext(Context context) {
        MyToaster.context = context;
    }
    public static void toast(String message) {
        if (context == null) return;
        Toast toast = Toast.makeText(context,
                message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
