package task.myapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by shivam.srivastava on 1/29/2018.
 */

public class ActivityUtils {

    public static @NonNull
    <T> T checkNotNull(final T reference) {
        if (reference == null)
            throw new NullPointerException();
        return reference;
    }

    public static @NonNull
    <T> T checkNotNull(final T reference, final Object message) {
        if (reference == null)
            throw new NullPointerException(String.valueOf(message));
        return reference;
    }

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int containerId, String tag) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, tag);
        transaction.commit();
    }

    public static void replaceFragmentFromActivity(@NonNull FragmentManager fragmentManager,
                                                   @NonNull Fragment fragment, int containerId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerId, fragment);
        transaction.commit();
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See {@code
     * AndroidManifest.xml} to find out the parent activity names for each activity.
     *
     * @param intent
     */
    public static void createBackStack(Activity activity, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(activity);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
