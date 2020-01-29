/*
 * Copyright (C) 2019-2020 The exTHmUI Open Source Project
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

package org.exthmui.thememanager.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.exthmui.thememanager.models.Theme;

import java.util.ArrayList;
import java.util.List;

public class ThemeDataService extends Service {

    private final static String TAG = "ThemeDataService";

    public final static String LIST_ACT_ADD = "add";
    public final static String LIST_ACT_REMOVE = "remove";

    private List<Theme> mThemeList;
    private boolean isApplying;
    private String mActedPackage;
    private List<ThemeDataUpdateListener> mThemeDataUpdateListenerList;

    public ThemeDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ThemeDataBinder();
    }

    public class ThemeDataBinder extends Binder {

        public void setDataUpdateListener(ThemeDataUpdateListener themeDataUpdateListener) {
            if (mThemeDataUpdateListenerList == null) mThemeDataUpdateListenerList = new ArrayList<>();
            mThemeDataUpdateListenerList.add(themeDataUpdateListener);
        }

        public void removeDataUpdateListener(ThemeDataUpdateListener themeDataUpdateListener) {
            mThemeDataUpdateListenerList.remove(themeDataUpdateListener);
        }

        public void addItemToList(int index, Theme val) {
            mThemeList.add(index, val);
            if (mThemeDataUpdateListenerList == null) return;
            for (ThemeDataUpdateListener listener : mThemeDataUpdateListenerList) {
                listener.onThemeListChangedListener(index, LIST_ACT_ADD, val);
            }
        }

        public void removeItemOnList(int index) {
            mThemeList.remove(index);
            if (mThemeDataUpdateListenerList == null) return;
            for (ThemeDataUpdateListener listener : mThemeDataUpdateListenerList) {
                listener.onThemeListChangedListener(index, LIST_ACT_REMOVE, null);
            }
        }

        public void setIsApplying(boolean val) {
            isApplying = val;
            if (mThemeDataUpdateListenerList == null) return;
            for (ThemeDataUpdateListener listener : mThemeDataUpdateListenerList) {
                listener.onApplyingListener(val);
            }
        }

        public boolean getIsApplying() {
            return isApplying;
        }

        public void setActedPackage(String val) {
            mActedPackage = val;
            if (mThemeDataUpdateListenerList == null) return;
            for (ThemeDataUpdateListener listener : mThemeDataUpdateListenerList) {
                listener.onActedPackageChangeListener(val);
            }
        }

        public String getActedPackage() {
            return mActedPackage;
        }

        public void setThemeList(List<Theme> themes) {
            mThemeList = themes;
        }

        public List<Theme> getThemeList() {
            return mThemeList;
        }

    }

    public interface ThemeDataUpdateListener {
        void onApplyingListener(boolean val);
        void onThemeListChangedListener(int index, String act, Theme val);
        void onActedPackageChangeListener(String val);
    }
}
