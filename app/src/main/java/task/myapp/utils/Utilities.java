package task.myapp.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import task.myapp.R;


/**
 * Created by shivam.srivastava on 1/11/2018.
 */

public class Utilities {

    public static void setStatusBarColor(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ((Activity)context).getWindow();
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            );
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            );
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.transparent));
        }
    }

    public static void setNavigationBarColor(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ((Activity)context).getWindow();
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            );
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
//            window.clearFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
//            );
//            window.setNavigationBarColor(ContextCompat.getColor(context, R.color.transparent));
        }
    }

    public static void showToast(@StringRes int resId, View view, View.OnClickListener listener) {
        final Snackbar snackbar = Snackbar.make(view, resId, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.setAction("Ok", listener==null? tv -> snackbar.dismiss():listener);
        snackbar.show();
    }

    public static void showToast(String msg, View view, View.OnClickListener listener) {
        final Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.setAction("Ok", listener==null? tv -> snackbar.dismiss():listener);
        snackbar.show();
    }

    public static <T> boolean isNotEmptyOrNull(T[] array) {
        return array != null && array.length > 0;
    }


}
