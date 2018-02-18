/*
 * Copyright (c) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package task.myapp.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;

import task.myapp.archframework.Model;
import task.myapp.archframework.QueryEnum;
import task.myapp.archframework.UserActionEnum;
import task.myapp.R;
import task.myapp.ui.explore.MapsActivity;
import task.myapp.ui.homepage.MainActivity;
import task.myapp.ui.profile.ProfileActivity;


/**
 * Determines which items to show in the {@link AppNavigationView}.
 */
public class NavigationModel implements Model<NavigationModel.NavigationQueryEnum, NavigationModel.NavigationUserActionEnum> {

    private NavigationItemEnum[] mItems;

    public NavigationItemEnum[] getItems() {
        return mItems;
    }

    @Override
    public NavigationQueryEnum[] getQueries() {
        return NavigationQueryEnum.values();
    }

    @Override
    public NavigationUserActionEnum[] getUserActions() {
        return NavigationUserActionEnum.values();
    }

    @Override
    public void deliverUserAction(final NavigationUserActionEnum action,
                                  @Nullable final Bundle args,
                                  final UserActionCallback<NavigationUserActionEnum> callback) {
        switch (action) {
            case RELOAD_ITEMS:
                mItems = null;
                populateNavigationItems();
                callback.onModelUpdated(this, action);
                break;
        }
    }

    @Override
    public void requestData(final NavigationQueryEnum query,
                            final DataQueryCallback<NavigationQueryEnum> callback) {
        switch (query) {
            case LOAD_ITEMS:
                if (mItems != null) {
                    callback.onModelUpdated(this, query);
                } else {
                    populateNavigationItems();
                    callback.onModelUpdated(this, query);
                }
                break;
        }
    }

    private void populateNavigationItems() {
        NavigationItemEnum[] items = NavigationConfig.ITEMS;
        mItems = NavigationConfig.filterOutItemsDisabledInBuildConfig(items);
    }

    @Override
    public void cleanUp() {
        // no-op
    }

    /**
     * List of all possible navigation items.
     */
    public enum NavigationItemEnum {
        PROFILE(R.id.home, R.string.bottom_home, R.drawable.ic_account_circle_black_24dp, ProfileActivity.class, true),
        HOME(R.id.request_img, R.string.bottom_main_page, R.drawable.ic_tv_black_24dp, MainActivity.class, true),
        EXPLORE(R.id.utilities, R.string.bottom_explore, R.drawable.ic_explore_black_24dp, MapsActivity.class, true),
        INVALID(-1, -1, -1, null);
        private int id;

        private int titleResource;

        private int iconResource;

        private Class classToLaunch;

        private boolean finishCurrentActivity;

        NavigationItemEnum(int id, int titleResource, int iconResource, Class classToLaunch) {
            this(id, titleResource, iconResource, classToLaunch, false);
        }

        NavigationItemEnum(int id, int titleResource, int iconResource, Class classToLaunch,
                           boolean finishCurrentActivity) {
            this.id = id;
            this.titleResource = titleResource;
            this.iconResource = iconResource;
            this.classToLaunch = classToLaunch;
            this.finishCurrentActivity = finishCurrentActivity;
        }

        public static NavigationItemEnum getById(int id) {
            for (NavigationItemEnum value : NavigationItemEnum.values()) {
                if (value.getId() == id) {
                    return value;
                }
            }
            return INVALID;
        }

        public int getId() {
            return id;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getIconResource() {
            return iconResource;
        }

        public Class getClassToLaunch() {
            return classToLaunch;
        }

        public boolean finishCurrentActivity() {
            return finishCurrentActivity;
        }

        }

    public enum NavigationQueryEnum implements QueryEnum {
        LOAD_ITEMS(0);

        private int id;

        NavigationQueryEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return new String[0];
        }
    }

    public enum NavigationUserActionEnum implements UserActionEnum {
        RELOAD_ITEMS(0);

        private int id;

        NavigationUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
