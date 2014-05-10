/*
 * Copyright (C) 2008 Romain Guy
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
/*   
 * 	 Copyright (C) 2014 pjv (and others)
 * 
 * 	 This file has been incorporated from its previous location into, 
 *   and is part of, ActionBarPoirot, and the license has been adapted.
 *
 *   ActionBarPoirot is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ActionBarPoirot is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ActionBarPoirot.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.lp.actionbarpoirot.util;

import java.util.List;
import java.util.Locale;

import net.lp.actionbarpoirot.PoirotWindow;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * A class full of shortcuts related to the UI. Also enables/disables activities.
 * 
 * @author pjv
 *
 */
public class UiUtilities {
	public static final String SYSTEM_FEATURE_GOOGLE_TV = "com.google.android.tv";
	public static final String LOADER_BUNDLE_KEY_PROJECTION = "projection";
	public static final String LOADER_BUNDLE_KEY_SELECTION = "selection";
	public static final String LOADER_BUNDLE_KEY_SELECTION_ARGS = "selectionArgs";
	public static final String LOADER_BUNDLE_KEY_URI = "uri";
	private static final int SW480DP = 480;
	private static final int SW600DP = 600;
	private static final String TARGET_DEVICE = "target_device";
	private static final String TARGET_DEVICE_PHONE = "phone";
	private static final String TARGET_DEVICE_PHONE_AND_TABLET = "phone_tablet";
	private static final String TARGET_DEVICE_PHONE_AND_TELEVISION = "phone_television";
	private static final String TARGET_DEVICE_TABLET = "tablet";
	private static final String TARGET_DEVICE_TABLET_AND_TELEVISION = "tablet_television";
	private static final String TARGET_DEVICE_TELEVISION = "television";
	private static final String TARGET_DEVICE_UNIVERSAL = "universal";
	private static final String TARGET_PLATFORM = "target_platform";
	private static final String TARGET_PLATFORM_HONEYCOMB_OR_UP = "honeycomborup";
	private static final String TARGET_PLATFORM_ICS_OR_UP = "icsorup";
	private static final String TARGET_PLATFORM_PREHONEYCOMB = "prehoneycomb";

	/**
	 * Convert a pixels metric to density-independent-pixels.
	 * 
	 * @param context
	 * @param pixelValue
	 * @return
	 */
	protected static int convertPixelsToDp(Context context, float pixelValue) {
		return (int) ((pixelValue) / context.getResources().getDisplayMetrics().density);
	}

	/**
	 * Create a Cursor Loader bundle.
	 * 
	 * @param uri
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public static Bundle createLoaderBundle(final Uri uri,
			final String selection, final String[] selectionArgs) {
		final Bundle loaderBundle = new Bundle();
		loaderBundle.putParcelable(LOADER_BUNDLE_KEY_URI, uri);
		loaderBundle.putString(LOADER_BUNDLE_KEY_SELECTION, selection);
		loaderBundle.putStringArray(LOADER_BUNDLE_KEY_SELECTION_ARGS,
				selectionArgs);
		return loaderBundle;
	}

	/**
	 * Create a Cursor Loader bundle, including projection.
	 * 
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public static Bundle createLoaderBundle(final Uri uri,
			final String[] projection, final String selection,
			final String[] selectionArgs) {
		final Bundle loaderBundle = createLoaderBundle(uri, selection,
				selectionArgs);
		loaderBundle.putStringArray(LOADER_BUNDLE_KEY_PROJECTION, projection);
		return loaderBundle;
	}

	/**
	 * Enables and disables {@linkplain android.app.Activity activities} based
	 * on their "target_device" meta-data and the current device. Add <meta-data
	 * name="target_device" value="tablet|phone|universal" /> to an activity to
	 * specify its target device.
	 * 
	 * @param context
	 *            the current context of the device
	 * @see #isHoneycombTablet(android.content.Context)
	 * @see http
	 *      ://stackoverflow.com/questions/13202805/how-to-specify-activities
	 *      -that-are-only-for-phones-or-tablets-on-android
	 */
	public static void enableDisableActivities(Context context) {
		final PackageManager pm = context.getPackageManager();
		final boolean isHoneycombTablet = isHoneycombTablet(context);
		final boolean isTelevision = isGoogleTV(context);

		final boolean hasICSOrUp = hasIcsOrUp();
		final boolean hasHoneycombOrUp = hasHoneycombOrUp();
		final boolean hasPreHoneycomb = !hasHoneycombOrUp && !hasICSOrUp;

		if (PoirotWindow.DEBUG) {
			Log.w(PoirotWindow.TAG,
					"EnableDisableActivities for package name started: "
							+ context.getPackageName());
		}

		try {
			final ActivityInfo[] activityInfo = pm.getPackageInfo(
					context.getPackageName(), PackageManager.GET_ACTIVITIES
							| PackageManager.GET_META_DATA).activities;
			for (ActivityInfo info : activityInfo) {
				if (info.metaData == null)
					break;
				String targetDevice = info.metaData.getString(TARGET_DEVICE);
				if (targetDevice == null)
					break;
				targetDevice = targetDevice.toLowerCase(Locale.US);
				final boolean isForTablet = targetDevice
						.equals(TARGET_DEVICE_TABLET)
						|| targetDevice
								.equals(TARGET_DEVICE_TABLET_AND_TELEVISION)
						|| targetDevice.equals(TARGET_DEVICE_PHONE_AND_TABLET)
						|| targetDevice.equals(TARGET_DEVICE_UNIVERSAL);
				final boolean isForPhone = targetDevice
						.equals(TARGET_DEVICE_PHONE)
						|| targetDevice
								.equals(TARGET_DEVICE_PHONE_AND_TELEVISION)
						|| targetDevice.equals(TARGET_DEVICE_PHONE_AND_TABLET)
						|| targetDevice.equals(TARGET_DEVICE_UNIVERSAL);
				final boolean isForTelevision = targetDevice
						.equals(TARGET_DEVICE_TELEVISION)
						|| targetDevice
								.equals(TARGET_DEVICE_PHONE_AND_TELEVISION)
						|| targetDevice
								.equals(TARGET_DEVICE_TABLET_AND_TELEVISION)
						|| targetDevice.equals(TARGET_DEVICE_UNIVERSAL);

				final String className = /* info.packageName + */info.name;

				String targetPlatform = info.metaData
						.getString(TARGET_PLATFORM);
				if (targetPlatform == null)
					break;
				targetPlatform = targetPlatform.toLowerCase(Locale.US);
				final boolean isForHoneycombOrUp = targetPlatform
						.equals(TARGET_PLATFORM_HONEYCOMB_OR_UP)
						|| targetPlatform.equals(TARGET_DEVICE_UNIVERSAL);
				final boolean isForICSOrUp = targetPlatform
						.equals(TARGET_PLATFORM_ICS_OR_UP)
						|| targetPlatform
								.equals(TARGET_PLATFORM_HONEYCOMB_OR_UP)
						|| targetPlatform.equals(TARGET_DEVICE_UNIVERSAL);
				final boolean isForPreHoneycomb = targetPlatform
						.equals(TARGET_PLATFORM_PREHONEYCOMB)
						|| targetPlatform.equals(TARGET_DEVICE_UNIVERSAL);

				boolean enabled = (isHoneycombTablet && !isTelevision && isForTablet)
						|| (!isHoneycombTablet && !isTelevision && isForPhone)
						|| (isTelevision && isForTelevision);

				enabled = enabled
						&& ((hasHoneycombOrUp && isForHoneycombOrUp)
								|| (hasICSOrUp && isForICSOrUp) || (hasPreHoneycomb && isForPreHoneycomb));

				pm.setComponentEnabledSetting(
						new ComponentName(context, Class.forName(className)),
						enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
								: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);

				if (className.contains("BookCollectionViewWindow")) {
					//For debugging, we single out one possible class.
					Log.w(PoirotWindow.TAG,
							"BookCollectionViewWindow: "
									+ pm.getComponentEnabledSetting(new ComponentName(
											context, Class.forName(className))));
				}
			}
		} catch (PackageManager.NameNotFoundException error) {
			Log.w(PoirotWindow.TAG, "Could not EnableDisableActivities: ",
					error.getCause());
		} catch (ClassNotFoundException error) {
			Log.w(PoirotWindow.TAG, "Could not EnableDisableActivities: ",
					error.getCause());
		}
	}

	public static long getCurrentTime(final Context context) {
		// SharedPreferences prefs = context.getSharedPreferences("mock_data",
		// 0);
		// prefs.edit().commit();
		// return prefs.getLong("mock_current_time",
		// System.currentTimeMillis());
		return System.currentTimeMillis();
	}

	public static Drawable getIconForIntent(final Context context, Intent i) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (infos.size() > 0) {
			return infos.get(0).loadIcon(pm);
		}
		return null;
	}

	/**
	 * Whether we should rely on the native action bar. In this case it is modern, but does not have all features.
	 * 
	 * @return
	 */
	public static boolean hasAcceptableNativeActionBar() {
		return hasHoneycombOrUp();
	}

	/**
	 * Whether we should rely on the native action bar. In this case it is fully featured and modern.
	 * 
	 * @return
	 */
	public static boolean hasProperNativeActionBar() {
		return hasIcsOrUp();
	}

	public static boolean hasFroyoOrUp() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbreadOrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycombMR1OrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	public static boolean hasHoneycombMR2OrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
	}

	public static boolean hasHoneycombOrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasIcsOrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static boolean hasIcsMR1OrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
	}

	public static boolean hasJellyBeanOrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	public static boolean hasJellyBeanMR1OrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}

	public static boolean hasJellyBeanMR2OrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}

	public static boolean hasKitKatOrUp() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	public static boolean isGoogleTV(Context context) {
		return context.getPackageManager().hasSystemFeature(
				SYSTEM_FEATURE_GOOGLE_TV);
	}

	public static boolean isHoneycombTablet(Context context) {
		return hasHoneycombOrUp() && isTablet(context);
	}

	/**
	 * Whether we should favor a multi-pane layout.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMultiPane(Context context) {
		return isHoneycombTablet((Context) context)
				|| isGoogleTV((Context) context);
	}

	public static boolean isTablet(Context context) {
		if (hasHoneycombMR2OrUp()) {
			return isTabletByLatestMethod(context);
		} else {
			return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	protected static boolean isTabletByLatestMethod(Context context) {
		return context.getResources().getConfiguration().smallestScreenWidthDp >= SW600DP;
	}

	/**
	 * 
	 * @param context
	 * @return
	 * 
	 * TODO: too client-specific?
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static boolean isTooNarrowForComplexUI(Context context) {
		// return
		// context.getResources().getConfiguration().smallestScreenWidthDp <
		// SW480DP; //Does not work depending on screen rotation.
		final boolean tooNarrowPhone = !isMultiPane(context)
				&& convertPixelsToDp(context, context.getResources()
						.getDisplayMetrics().widthPixels) < SW480DP;
		final boolean tooNarrowMultiPane = isMultiPane(context)
				&& convertPixelsToDp(context, context.getResources()
						.getDisplayMetrics().widthPixels) < 776; // dp; narrow
																	// part is
																	// on the
																	// golden
																	// ration
																	// part of
																	// the
																	// screen
																	// (so 61%
																	// needs to
																	// be bigger
																	// than
																	// 480dp).
		return tooNarrowPhone || tooNarrowMultiPane;
	}

	public static void showFormattedToast(Context context, int id,
			Object... args) {
		Toast.makeText(
				context.getApplicationContext(),
				String.format(context.getApplicationContext().getText(id)
						.toString(), args), Toast.LENGTH_LONG).show();
	}

	public static void showToast(Context context, CharSequence message,
			boolean longToast) {
		Toast.makeText(context.getApplicationContext(), message,
				longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	public static void showToast(Context context, int id) {
		showToast(context, id, false);
	}

	public static void showToast(Context context, int id, boolean longToast) {
		Toast.makeText(context.getApplicationContext(), id,
				longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	public static void startHomeActivity(Context context,
			Intent defaultHomeIntent) {
		final Intent homeIntent = defaultHomeIntent;
		// final Intent homeIntent = new Intent(Intent.ACTION_VIEW,
		// ....CONTENT_URI);
		// homeIntent.setPackage(...);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(homeIntent);
	}

	/**
	 * Static use only.
	 */
	private UiUtilities() {
	}
}
