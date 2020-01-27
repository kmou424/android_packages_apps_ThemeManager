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

package org.exthmui.thememanager.adapters;

import android.view.View;

import org.exthmui.thememanager.R;
import org.exthmui.thememanager.models.Theme;

import java.util.List;

public class ThemeAdapter extends CommonAdapter<Theme> {

    String mActedPackage = "null";

    public void setActedPackage(String packageName) {
        mActedPackage = packageName;
    }

    public ThemeAdapter(List<Theme> data, int layoutRes) {
        super(data, layoutRes);
    }

    @Override
    public void bindView(ViewHolder holder, Theme obj) {
        holder.setImageResource(R.id.theme_card_image, obj.getThemeImage());
        holder.setText(R.id.theme_card_title, obj.getName());

        if (obj.getPackageName().equals(mActedPackage)) {
            holder.setVisibility(R.id.theme_card_acted_overlay, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.theme_card_acted_overlay, View.GONE);
        }
    }
}
