/*
 * Copyright (C) 2012 The Android Open Source Project
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

package net.lp.actionbarpoirot.helpers;

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.helpers.ActivityHelper.ActivityHelperUser;
import net.lp.actionbarpoirot.util.UiUtilities;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v4.content.IntentCompat;
import android.util.Log;

/**
 * DualNavUtils that supports the phone/tablet Activity duality and doesn't read
 * just android.support.PARENT_ACTIVITY, but rather both
 * METADATA_PARENT_ACTIVITY_NAME_ON_TABLET and
 * METADATA_PARENT_ACTIVITY_NAME_ON_PHONE.
 * 
 * @note When the support library updates, I need to verify that I rewrite all
 *       mentions of PARENT_ACTIVITY.
 * @author pjv
 * @link 
 *       https://github.com/mastro/android-support-library-archive/blob/master/v4
 *       /src/java/android/support/v4/app/DualNavUtils.java
 * 
 */
public class DualNavUtils {
	interface DualNavUtilsImpl {
		Intent getParentActivityIntent(Activity activity);

		String getParentActivityName(Context context, ActivityInfo info);

		boolean navigateUpTo(Activity activity, Intent upIntent);

		boolean shouldUpRecreateTask(Activity activity, Intent targetIntent);
	}

	static class DualNavUtilsImplBase implements DualNavUtilsImpl {

		@Override
		public Intent getParentActivityIntent(Activity activity) {
			String parentName = DualNavUtils.getParentActivityName(activity);
			if (parentName == null)
				return null;

			// If the parent itself has no parent, generate a main activity
			// intent.
			ComponentName target = new ComponentName(activity, parentName);

			String grandparent = null;
			try {
				grandparent = DualNavUtils.getParentActivityName(activity,
						target);
			} catch (NameNotFoundException e) {
				// ***ActionBarPoirot
				// added*************************************************************************************************************************************
				if (PoirotWindow.DEBUG)
					e.printStackTrace();
				// If this runs in an extension app, then the grandparent
				// search is going to throw an error. It can be equated with
				// null, so the MAIN intent is created below.
				final String mainPackageName = ((ActivityHelperUser)activity).getHomePackageName();
				if (!mainPackageName.equalsIgnoreCase(activity
						.getPackageName())
						&& activity.getPackageName().startsWith(
								mainPackageName)) {
					target = new ComponentName(
							mainPackageName, parentName);
				} else {
					Log.e(TAG,
							"getParentActivityIntent: bad parentActivityName '"
									+ parentName + "' in manifest");
					return null;
				}
			}
			final Intent parentIntent = grandparent == null ? IntentCompat
					.makeMainActivity(target) : new Intent()
					.setComponent(target);
			return parentIntent;
		}

		@Override
		public String getParentActivityName(Context context, ActivityInfo info) {
			if (info.metaData == null)
				return null;
			String parentActivity = info.metaData
					.getString(NavUtils.PARENT_ACTIVITY);
			if (parentActivity == null)
				return null;
			if (parentActivity.charAt(0) == '.') {
				parentActivity = context.getPackageName() + parentActivity;
			}
			return parentActivity;
		}

		@Override
		public boolean navigateUpTo(Activity activity, Intent upIntent) {
			upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(upIntent);
			activity.finish();
			return true;
		}

		@Override
		public boolean shouldUpRecreateTask(Activity activity,
				Intent targetIntent) {
			String action = activity.getIntent().getAction();
			return action != null && !action.equals(Intent.ACTION_MAIN);
		}
	}

	static class DualNavUtilsImplJB extends DualNavUtilsImplBase {

		@Override
		public Intent getParentActivityIntent(Activity activity) {
			// Prefer the "real" JB definition if available,
			// else fall back to the meta-data element.
			Intent result = DualNavUtilsJB.getParentActivityIntent(activity);
			if (result == null) {
				result = superGetParentActivityIntent(activity);
			}
			return result;
		}

		@Override
		public String getParentActivityName(Context context, ActivityInfo info) {
			String result = DualNavUtilsJB.getParentActivityName(context, info);
			if (result == null) {
				result = super.getParentActivityName(context, info);
			}
			return result;
		}

		@Override
		public boolean navigateUpTo(Activity activity, Intent upIntent) {
			return DualNavUtilsJB.navigateUpTo(activity, upIntent);
		}

		@Override
		public boolean shouldUpRecreateTask(Activity activity,
				Intent targetIntent) {
			return DualNavUtilsJB.shouldUpRecreateTask(activity, targetIntent);
		}

		Intent superGetParentActivityIntent(Activity activity) {
			return super.getParentActivityIntent(activity);
		}
	}

	private static final DualNavUtilsImpl IMPL;

	static final String METADATA_PARENT_ACTIVITY_NAME_ON_PHONE = "net.lp.actionbarpoirot.PARENT_ACTIVITY_ON_PHONE";

	static final String METADATA_PARENT_ACTIVITY_NAME_ON_TABLET = "net.lp.actionbarpoirot.PARENT_ACTIVITY_ON_TABLET";

	private static final String TAG = "DualNavUtils";

	static {
		final int version = android.os.Build.VERSION.SDK_INT;
		if (version >= 16) {
			IMPL = new DualNavUtilsImplJB();
		} else {
			IMPL = new DualNavUtilsImplBase();
		}
	}

	/**
	 * Obtain an {@link Intent} that will launch an explicit target activity
	 * specified by sourceActivity's {@link #PARENT_ACTIVITY} &lt;meta-data&gt;
	 * element in the application's manifest. If the device is running Jellybean
	 * or newer, the android:parentActivityName attribute will be preferred if
	 * it is present.
	 * 
	 * @param sourceActivity
	 *            Activity to fetch a parent intent for
	 * @return a new Intent targeting the defined parent activity of
	 *         sourceActivity
	 */
	public static Intent getParentActivityIntent(Activity sourceActivity) {
		return IMPL.getParentActivityIntent(sourceActivity);
	}

	/**
	 * Obtain an {@link Intent} that will launch an explicit target activity
	 * specified by sourceActivityClass's {@link #PARENT_ACTIVITY}
	 * &lt;meta-data&gt; element in the application's manifest.
	 * 
	 * @param context
	 *            Context for looking up the activity component for
	 *            sourceActivityClass
	 * @param sourceActivityClass
	 *            {@link java.lang.Class} object for an Activity class
	 * @return a new Intent targeting the defined parent activity of
	 *         sourceActivity
	 * @throws NameNotFoundException
	 *             if the ComponentName for sourceActivityClass is invalid
	 */
	public static Intent getParentActivityIntent(Context context,
			Class<?> sourceActivityClass) throws NameNotFoundException {
		String parentActivity = getParentActivityName(context,
				new ComponentName(context, sourceActivityClass));
		if (parentActivity == null)
			return null;

		// If the parent itself has no parent, generate a main activity intent.
		final ComponentName target = new ComponentName(context, parentActivity);
		final String grandparent = getParentActivityName(context, target);
		final Intent parentIntent = grandparent == null ? IntentCompat
				.makeMainActivity(target) : new Intent().setComponent(target);
		return parentIntent;
	}

	/**
	 * Obtain an {@link Intent} that will launch an explicit target activity
	 * specified by sourceActivityClass's {@link #PARENT_ACTIVITY}
	 * &lt;meta-data&gt; element in the application's manifest.
	 * 
	 * @param context
	 *            Context for looking up the activity component for the source
	 *            activity
	 * @param componentName
	 *            ComponentName for the source Activity
	 * @return a new Intent targeting the defined parent activity of
	 *         sourceActivity
	 * @throws NameNotFoundException
	 *             if the ComponentName for sourceActivityClass is invalid
	 */
	public static Intent getParentActivityIntent(Context context,
			ComponentName componentName) throws NameNotFoundException {
		String parentActivity = getParentActivityName(context, componentName);
		if (parentActivity == null)
			return null;

		// If the parent itself has no parent, generate a main activity intent.
		ComponentName target = new ComponentName(
				componentName.getPackageName(), parentActivity);
		String grandparent = null;
		try {
			grandparent = getParentActivityName(context, target);
		} catch (NameNotFoundException e) {
			// ***ActionBarPoirot
			// added*************************************************************************************************************************************
			if (PoirotWindow.DEBUG)
				e.printStackTrace();
			// If this runs in an extension app, then the grandparent search is
			// going to throw an error. It can be equated with null, so the MAIN
			// intent is created below.
			final String mainPackageName = ((ActivityHelperUser)context).getHomePackageName();
			if (!mainPackageName
					.equalsIgnoreCase(componentName.getPackageName())
					&& componentName.getPackageName().startsWith(
							mainPackageName)) {
				target = new ComponentName(mainPackageName,
						parentActivity);
			} else {
				throw e;
			}
		}
		final Intent parentIntent = grandparent == null ? IntentCompat
				.makeMainActivity(target) : new Intent().setComponent(target);
		return parentIntent;
	}

	/**
	 * Return the fully qualified class name of sourceActivity's parent activity
	 * as specified by a {@link #PARENT_ACTIVITY} &lt;meta-data&gt; element
	 * within the activity element in the application's manifest.
	 * 
	 * @param sourceActivity
	 *            Activity to fetch a parent class name for
	 * @return The fully qualified class name of sourceActivity's parent
	 *         activity or null if it was not specified
	 */
	public static String getParentActivityName(Activity sourceActivity) {
		try {
			return getParentActivityName(sourceActivity,
					sourceActivity.getComponentName());
		} catch (NameNotFoundException e) {
			// Component name of supplied activity does not exist...?
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Return the fully qualified class name of a source activity's parent
	 * activity as specified by a {@link #PARENT_ACTIVITY} &lt;meta-data&gt;
	 * element within the activity element in the application's manifest. The
	 * source activity is provided by componentName.
	 * 
	 * @param context
	 *            Context for looking up the activity component for the source
	 *            activity
	 * @param componentName
	 *            ComponentName for the source Activity
	 * @return The fully qualified class name of sourceActivity's parent
	 *         activity or null if it was not specified
	 */
	public static String getParentActivityName(Context context,
			ComponentName componentName) throws NameNotFoundException {
		final PackageManager pm = context.getPackageManager();
		final ActivityInfo info = pm.getActivityInfo(componentName,
				PackageManager.GET_META_DATA);

		return getParentActivityNameInner(context, info);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static String getParentActivityNameFlag(ActivityInfo info) {
		if (UiUtilities.hasJellyBeanOrUp()) {
			return info.parentActivityName;
		}
		return null;
	}

	static String getParentActivityNameInner(Context context,
			final ActivityInfo info) {// This would not have been necessary if
										// we could just write the info.metaData
										// in
										// UiUtilities.enableDisableActivities().
		// If somehow the regular Jelly Bean parentActivityName was specified
		// anyway, use that.
		String parentActivity = getParentActivityNameFlag(info);
		if (parentActivity != null) {
			if (parentActivity.charAt(0) == '.') {
				parentActivity = context.getPackageName() + parentActivity;
			}
			if (PoirotWindow.DEBUG)
				Log.w(PoirotWindow.TAG, "parentActivityNameNative="
						+ parentActivity + " , info.name=" + info.name);
			return parentActivity;
		}

		if (info.metaData == null)
			return null;

		// If somehow the regular android.support.PARENT_ACTIVITY was specified
		// anyway, use that.
		parentActivity = info.metaData.getString(NavUtils.PARENT_ACTIVITY);
		if (parentActivity != null) {
			if (parentActivity.charAt(0) == '.') {
				parentActivity = context.getPackageName() + parentActivity;
			}
			if (PoirotWindow.DEBUG)
				Log.w(PoirotWindow.TAG, "parentActivityNameSupport="
						+ parentActivity + " , info.name=" + info.name);
			return parentActivity;
		}

		// Get the advised parentActivities for both phone and tablet, from our
		// own metadata, and set it in stone in the real flag and compat
		// metadata.
		if (UiUtilities.isHoneycombTablet(context)) {
			parentActivity = info.metaData
					.getString(DualNavUtils.METADATA_PARENT_ACTIVITY_NAME_ON_TABLET);
			if (parentActivity != null) {
				if (parentActivity.charAt(0) == '.') {
					parentActivity = context.getPackageName() + parentActivity;
				}
				if (PoirotWindow.DEBUG)
					Log.w(PoirotWindow.TAG, "parentActivityNameOnTablet="
							+ parentActivity + " , info.name=" + info.name);
				return parentActivity;
			} else {
				if (PoirotWindow.DEBUG)
					Log.w(PoirotWindow.TAG,
							"parentActivityNameOnTablet=null, info.name="
									+ info.name);
			}
		} else {
			parentActivity = info.metaData
					.getString(DualNavUtils.METADATA_PARENT_ACTIVITY_NAME_ON_PHONE);
			if (parentActivity != null) {
				if (parentActivity.charAt(0) == '.') {
					parentActivity = context.getPackageName() + parentActivity;
				}
				if (PoirotWindow.DEBUG)
					Log.w(PoirotWindow.TAG, "parentActivityNameOnPhone="
							+ parentActivity + " , info.name=" + info.name);
				return parentActivity;
			} else {
				if (PoirotWindow.DEBUG)
					Log.w(PoirotWindow.TAG,
							"parentActivityNameOnPhone=null, info.name="
									+ info.name);
			}
		}
		return null;
	}

	/**
	 * Convenience method that is equivalent to calling
	 * <code>{@link #navigateUpTo(Activity, Intent) navigateUpTo}(sourceActivity,
	 * {@link #getParentActivityIntent(Activity) getParentActivityIntent} (sourceActivity))</code>
	 * . sourceActivity will be finished by this call.
	 * 
	 * <p>
	 * <em>Note:</em> This method should only be used when sourceActivity and
	 * the corresponding parent are within the same task. If up navigation
	 * should cross tasks in some cases, see
	 * {@link #shouldUpRecreateTask(Activity, Intent)}.
	 * </p>
	 * 
	 * @param sourceActivity
	 *            The current activity from which the user is attempting to
	 *            navigate up
	 */
	public static void navigateUpFromSameTask(Activity sourceActivity) {
		Intent upIntent = getParentActivityIntent(sourceActivity);

		if (upIntent == null) {
			throw new IllegalArgumentException(
					"Activity "
							+ sourceActivity.getClass().getSimpleName()
							+ " does not have a parent activity name specified."
							+ " (Did you forget to add the android.support.PARENT_ACTIVITY <meta-data> "
							+ " element in your manifest?)");
		}

		navigateUpTo(sourceActivity, upIntent);
	}

	/**
	 * Navigate from sourceActivity to the activity specified by upIntent,
	 * finishing sourceActivity in the process. upIntent will have the flag
	 * {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} set by this method, along with any
	 * others required for proper up navigation as outlined in the Android
	 * Design Guide.
	 * 
	 * <p>
	 * This method should be used when performing up navigation from within the
	 * same task as the destination. If up navigation should cross tasks in some
	 * cases, see {@link #shouldUpRecreateTask(Activity, Intent)}.
	 * </p>
	 * 
	 * @param sourceActivity
	 *            The current activity from which the user is attempting to
	 *            navigate up
	 * @param upIntent
	 *            An intent representing the target destination for up
	 *            navigation
	 */
	public static boolean navigateUpTo(Activity sourceActivity, Intent upIntent) {
		return IMPL.navigateUpTo(sourceActivity, upIntent);
	}

	/**
	 * Returns true if sourceActivity should recreate the task when navigating
	 * 'up' by using targetIntent.
	 * 
	 * <p>
	 * If this method returns false the app can trivially call
	 * {@link #navigateUpTo(Activity, Intent)} using the same parameters to
	 * correctly perform up navigation. If this method returns false, the app
	 * should synthesize a new task stack by using {@link DualTaskStackBuilder}
	 * or another similar mechanism to perform up navigation.
	 * </p>
	 * 
	 * @param sourceActivity
	 *            The current activity from which the user is attempting to
	 *            navigate up
	 * @param targetIntent
	 *            An intent representing the target destination for up
	 *            navigation
	 * @return true if navigating up should recreate a new task stack, false if
	 *         the same task should be used for the destination
	 */
	public static boolean shouldUpRecreateTask(Activity sourceActivity,
			Intent targetIntent) {
		return IMPL.shouldUpRecreateTask(sourceActivity, targetIntent);
	}

	/** No instances! */
	private DualNavUtils() {
	}
}