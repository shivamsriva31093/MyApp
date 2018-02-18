package task.myapp.navigation

/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import task.myapp.R
import task.myapp.navigation.NavigationModel.NavigationItemEnum
import task.myapp.utils.FontCache
import task.myapp.utils.LogUtils
import task.myapp.utils.Utilities
import task.myapp.widgets.BadgedBottomNavigationView

/**
 * A base activity that handles common functionality in the app. This includes the navigation
 * drawer, login and authentication, Action Bar tweaks, amongst others.
 */
abstract class BaseFragmentActivity : FragmentActivity() {
    // Navigation drawer
    private var mAppNavigationView: AppNavigationView? = null
    // Toolbar
    private var mToolbar: Toolbar? = null
    private var mToolbarTitle: TextView? = null


    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses of
     * BaseActivity override this to indicate what nav drawer item corresponds to them Return
     * NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */

    var selfNavDrawerItem: NavigationModel.NavigationItemEnum = NavigationModel.NavigationItemEnum.INVALID

    var navigationTitleId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utilities.setStatusBarColor(this)
    }


    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val bottomNav = findViewById<View>(R.id.bottom_navigation) as BadgedBottomNavigationView
        if (bottomNav != null) {
/*            bottomNav.enableAnimation(false)
            bottomNav.enableItemShiftingMode(false)
            bottomNav.enableShiftingMode(false)
            bottomNav.setTypeface(FontCache.getTypeface("Roboto" + "-Regular.ttf", this))
            bottomNav.setTextSize(12f)
            bottomNav.setIconSize(20f, 20f)*/

            mAppNavigationView = AppNavigationViewAsBottomNavImpl(bottomNav)
            mAppNavigationView?.activityReady(this, selfNavDrawerItem)
            // Since onPostCreate happens after onStart, we can't badge during onStart when the
            // Activity is launched because the AppNavigationView isn't instantiated until this
            // point.
            updateFeedBadge()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    fun updateFeedBadge() {
//        if (FeedState.getInstance().isNewFeedItem(this)) {
//            showFeedBadge()
//        } else {
//            clearFeedBadge()
//        }
    }


    /**
     * @param clickListener The [android.view.View.OnClickListener] for the navigation icon of
     * the toolbar.
     */
    protected fun setToolbarAsUp(clickListener: View.OnClickListener) {
        // Initialise the toolbar
//        toolbar
//        if (mToolbar != null) {
//            mToolbar?.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//            mToolbar?.setNavigationOnClickListener(clickListener)
//        }
    }

    override fun onStart() {
        // Update feed badge during onStart because if the app receives the Feed FCM (and updates
        // the FeedState) while the Activity is no longer visible, this will update the badge
        // when the Activity is visible again.
        updateFeedBadge()
        super.onStart()
    }


    protected fun setFullscreenLayout() {
        val decor = window.decorView
        var flags = decor.systemUiVisibility
        flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        decor.systemUiVisibility = flags
    }

    protected fun showFeedBadge() {
//        if (mAppNavigationView != null) {
//            mAppNavigationView?.showItemBadge(NavigationItemEnum.PROFILE)
//        }
    }

    protected fun clearFeedBadge() {
//        if (mAppNavigationView != null) {
//            mAppNavigationView?.clearItemBadge(NavigationItemEnum.PROFILE)
//        }
    }


    companion object {

        private val TAG = LogUtils.makeLogTag(BaseFragmentActivity::class.java)

        /**
         * This utility method handles Up navigation intents by searching for a parent activity and
         * navigating there if defined. When using this for an activity make sure to define both the
         * native parentActivity as well as the AppCompat one when supporting API levels less than 16.
         * when the activity has a single parent activity. If the activity doesn't have a single parent
         * activity then don't define one and this method will use back button functionality. If "Up"
         * functionality is still desired for activities without parents then use `syntheticParentActivity` to define one dynamically.
         *
         *
         * Note: Up navigation intents are represented by a back arrow in the top left of the Toolbar in
         * Material Design guidelines.
         *
         * @param currentActivity         Activity in use when navigate Up action occurred.
         * @param syntheticParentActivity Parent activity to use when one is not already configured.
         */
        fun navigateUpOrBack(currentActivity: Activity,
                             syntheticParentActivity: Class<out Activity>?) {
            // Retrieve parent activity from AndroidManifest.
            var intent = NavUtils.getParentActivityIntent(currentActivity)

            // Synthesize the parent activity when a natural one doesn't exist.
            if (intent == null && syntheticParentActivity != null) {
                try {
                    intent = NavUtils.getParentActivityIntent(currentActivity, syntheticParentActivity)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

            }

            if (intent == null) {
                // No parent defined in manifest. This indicates the activity may be used by
                // in multiple flows throughout the app and doesn't have a strict parent. In
                // this case the navigation up button should act in the same manner as the
                // back button. This will result in users being forwarded back to other
                // applications if currentActivity was invoked from another application.
                currentActivity.onBackPressed()
            } else {
                if (NavUtils.shouldUpRecreateTask(currentActivity, intent)) {
                    // Need to synthesize a backstack since currentActivity was probably invoked by a
                    // different app. The preserves the "Up" functionality within the app according to
                    // the activity hierarchy defined in AndroidManifest.xml via parentActivity
                    // attributes.
                    val builder = TaskStackBuilder.create(currentActivity)
                    builder.addNextIntentWithParentStack(intent)
                    builder.startActivities()
                } else {
                    // Navigate normally to the manifest defined "Up" activity.
                    NavUtils.navigateUpTo(currentActivity, intent)
                }
            }
        }

        /**
         * Converts an intent into a [Bundle] suitable for use as fragment arguments.
         */
        fun intentToFragmentArguments(intent: Intent?): Bundle {
            val arguments = Bundle()
            if (intent == null) {
                return arguments
            }

            val data = intent.data
            if (data != null) {
                arguments.putParcelable("_uri", data)
            }

            val extras = intent.extras
            if (extras != null) {
                arguments.putAll(intent.extras)
            }

            return arguments
        }
    }

}
