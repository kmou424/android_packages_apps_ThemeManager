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

package org.exthmui.thememanager.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.exthmui.thememanager.R;
import org.exthmui.thememanager.services.ThemeManageService;
import org.exthmui.thememanager.utils.NotificationUtil;

public class ThemeStatusReceiver extends BroadcastReceiver {

    private final static String TAG = "ThemeStatusReceiver";
    private int NotificationId;

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = "";
        int max = 0, progress = 0;
        boolean indeterminate = false;

        NotificationId = intent.getStringExtra("themePackage").hashCode();

        String themeName = intent.getStringExtra("themeName");

        switch (intent.getAction()) {
            case ThemeManageService.BROADCAST_ACTION_APPLY_SUCCEED:
                msg = context.getString(R.string.skin_apply_status_succeed, themeName);
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLY_FAILED:
                msg = context.getString(R.string.skin_apply_status_failed, themeName);
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING:
                msg = context.getString(R.string.skin_apply_status_running);
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING_ALARM:
                msg = context.getString(R.string.skin_apply_status_alarm);
                indeterminate = true;
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING_RINGTONE:
                msg = context.getString(R.string.skin_apply_status_ringtone);
                indeterminate = true;
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING_NOTIFICATION:
                msg = context.getString(R.string.skin_apply_status_notification);
                indeterminate = true;
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING_WALLPAPER:
                msg = context.getString(R.string.skin_apply_status_wallpaper);
                indeterminate = true;
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING_LOCKSCREEN:
                msg = context.getString(R.string.skin_apply_status_lockscreen);
                indeterminate = true;
                break;
            case ThemeManageService.BROADCAST_ACTION_APPLYING_OVERLAY:
                max = intent.getIntExtra("max", 0);
                progress = intent.getIntExtra("progress", 0);
                indeterminate = intent.getBooleanExtra("indeterminate", false);
                msg = context.getString(R.string.skin_apply_status_overlay, intent.getStringExtra("nowPackageLabel"));
        }

        NotificationUtil.showNotification(
                context, null,
                NotificationUtil.CHANNEL_APPLY_STATUS, NotificationId,
                msg, null, R.drawable.ic_stat_notification,
                max, progress, indeterminate);
    }
}
