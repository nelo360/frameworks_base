package com.android.internal.util.slim;

import android.R;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.PhoneConstants;

public class QSUtils {

        public static boolean deviceSupportsUsbTether(Context ctx) {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getTetherableUsbRegexs().length != 0);
        }

        public static boolean deviceSupportsWifiDisplay(Context ctx) {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            return (dm.getWifiDisplayStatus().getFeatureState() != WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE);
        }

        public static boolean deviceSupportsMobileData(Context ctx) {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
        }

        public static boolean deviceSupportsBluetooth() {
            return (BluetoothAdapter.getDefaultAdapter() != null);
        }

        public static boolean systemProfilesEnabled(ContentResolver resolver) {
            return (Settings.System.getInt(resolver, Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1);
        }

        public static boolean expandedDesktopEnabled(ContentResolver resolver) {
            /*return (Settings.System.getIntForUser(resolver, Settings.System.EXPANDED_DESKTOP_STYLE, 0,
                    UserHandle.USER_CURRENT_OR_SELF) != 0);*/ return false;
        }

        public static boolean deviceSupportsNfc(Context ctx) {
            return NfcAdapter.getDefaultAdapter(ctx) != null;
        }

        public static boolean deviceSupportsLte(Context ctx) {
            final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            return (tm.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) || tm.getLteOnGsmMode() != 0;
        }

        public static boolean deviceSupportsDockBattery(Context ctx) {
            Resources res = ctx.getResources();
            //return res.getBoolean(com.android.internal.R.bool.config_hasDockBattery);
        return false;
        }

        public static boolean deviceSupportsCamera() {
            return Camera.getNumberOfCameras() > 0;
        }

        public static boolean deviceSupportsGps(Context context) {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        }


        public static boolean adbEnabled(ContentResolver resolver) {
            return (Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0)) == 1;
        }

        public static boolean isTorchAvailable(Context context) {
            PackageManager pm = context.getPackageManager();
            try {
                return pm.getPackageInfo(TorchConstants.APP_PACKAGE_NAME, 0) != null;
            } catch (PackageManager.NameNotFoundException e) {
                // ignored, just catched so we can return false below
            }
            return false;
	}

        public static boolean deviceSupportsCompass(Context context) {
            SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            return (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                    && sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
        }
}
