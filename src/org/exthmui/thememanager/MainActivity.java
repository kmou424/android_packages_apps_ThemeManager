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

package org.exthmui.thememanager;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.exthmui.thememanager.adapters.ThemeAdapter;
import org.exthmui.thememanager.models.Theme;
import org.exthmui.thememanager.services.ThemeDataService;
import org.exthmui.thememanager.services.ThemeManageService;
import org.exthmui.thememanager.utils.NotificationUtil;
import org.exthmui.thememanager.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private GridView mGridView;
    private SharedPreferences mSharedPreferences;

    private Intent mThemeDataService;
    private ThemeDataService.ThemeDataBinder mThemeDataBinder;
    private ThemeDataConn mThemeDataConn;
    private Intent mThemeManageService;
    private ThemeManageService.ThemeManageBinder mThemeManageBinder;
    private ThemeManageConn mThemeManageConn;

    private List<Theme> mThemesList;
    private ThemeAdapter mThemeAdapter;

    private int cardHeight;
    private int cardWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = findViewById(R.id.themesGrid);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        cardHeight = getResources().getDimensionPixelOffset(R.dimen.card_image_height);
        cardWidth = cardHeight / 16 * 10;

        PermissionUtil.verifyStoragePermission(this);
        PermissionUtil.verifyWriteSettingsPermission(this);
        NotificationUtil.createNotificationChannel(this, NotificationUtil.CHANNEL_APPLY_STATUS, getString(R.string.channel_apply_status), NotificationUtil.IMPORTANCE_DEFAULT);

        mThemesList = new ArrayList<>();
        mThemeAdapter = new ThemeAdapter(mThemesList, R.layout.item_theme);
        mThemeAdapter.setActedPackage(mSharedPreferences.getString("actedPackage", "null"));
        mGridView.setAdapter(mThemeAdapter);
        mGridView.setNumColumns(getResources().getDisplayMetrics().widthPixels / cardWidth);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ThemePreview.class);
                intent.putExtra("themeIndex", position);
                startActivity(intent);
            }
        });

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                askUninstallTheme(mThemesList.get(position));
                return true;
            }
        });

        mThemeManageService = new Intent(this, ThemeManageService.class);
        mThemeManageConn = new ThemeManageConn();
        mThemeDataService = new Intent(this, ThemeDataService.class);
        mThemeDataConn = new ThemeDataConn();

        startService(mThemeManageService);
        bindService(mThemeManageService, mThemeManageConn, Context.BIND_AUTO_CREATE);

    }

    private void askUninstallTheme(Theme theme) {
        if (theme.isSystemPackage()) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_remove_package_title)
                .setMessage(getString(R.string.dialog_remove_package_text, theme.getName()))
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    // do uninstall
                    Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + theme.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.cancel,  (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mThemeManageConn);
        unbindService(mThemeDataConn);
    }

    private class ThemeManageConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mThemeManageBinder = (ThemeManageService.ThemeManageBinder) iBinder;
            bindService(mThemeDataService, mThemeDataConn, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class ThemeDataConn implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mThemeDataBinder = (ThemeDataService.ThemeDataBinder) iBinder;

            mThemeDataBinder.setDataUpdateListener(new ThemeDataService.ThemeDataUpdateListener() {
                @Override
                public void onActedPackageChangeListener(String val) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSharedPreferences.edit().putString("actedPackage", val).apply();
                            if (mThemeAdapter != null) {
                                mThemeAdapter.setActedPackage(val);
                                mThemeAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                }

                @Override
                public void onApplyingListener(boolean val) {
                }

                @Override
                public void onThemeListChangedListener(List<Theme> val) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mThemesList.clear();
                            mThemesList.addAll(val);
                            if (mThemeAdapter != null) mThemeAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });

            mThemeDataBinder.setThemeList(mThemeManageBinder.getThemesList());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

}
