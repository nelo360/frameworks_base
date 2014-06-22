package com.android.systemui.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class NetworkSpeedTile extends QuickSettingsTile {

    private boolean mEnabled;

    public NetworkSpeedTile(Context context, QuickSettingsController qsc, Handler handler) {
        super(context, qsc);

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.System.putIntForUser(mContext.getContentResolver(), Settings.System.STATUS_BAR_TRAFFIC_ENABLE,
                        mEnabled ? 0 : 1, UserHandle.USER_CURRENT);
		if (isFlipTilesEnabled()) {
                    flipTile(0);
                }
            }
        };
        qsc.registerObservedContent(Settings.System.getUriFor(Settings.System.STATUS_BAR_TRAFFIC_ENABLE), this);
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        mEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_TRAFFIC_ENABLE, 0, UserHandle.USER_CURRENT) == 1;
        if (mEnabled) {
            mDrawable = R.drawable.ic_qs_network_speed_on;
            mLabel = mContext.getString(R.string.quick_settings_network_speed_on);
        } else {
            mDrawable = R.drawable.ic_qs_network_speed_off;
            mLabel = mContext.getString(R.string.quick_settings_network_speed_off);
        }
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateResources();
    }

}
