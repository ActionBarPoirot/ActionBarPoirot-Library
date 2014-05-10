/*   
 * 	 Copyright (C) 2014 pjv (and others)
 * 
 * 	 This file is part of ActionBarPoirot.
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

import java.util.ArrayList;
import java.util.Iterator;

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.helpers.ActivityHelper.ActivityHelperUser;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.util.Log;

/**
 * DualTaskStackBuilder that supports the phone/tablet Activity duality and uses
 * DualDualNavUtils instead of DualNavUtils.
 * 
 * @note When the support library updates, I need to verify that I rewrite all
 *       calls to DualNavUtils.
 * @author pjv
 * @link 
 *       https://github.com/mastro/android-support-library-archive/blob/master/v4
 *       /src/java/android/support/v4/app/DualTaskStackBuilder.java
 * 
 */
public class DualTaskStackBuilder implements Iterable<Intent> {

	interface DualTaskStackBuilderImpl {
		PendingIntent getPendingIntent(Context context, Intent[] intents,
				int requestCode, int flags, Bundle options);
	}

	static class DualTaskStackBuilderImplBase implements
			DualTaskStackBuilderImpl {
		@Override
		public PendingIntent getPendingIntent(Context context,
				Intent[] intents, int requestCode, int flags, Bundle options) {
			Intent topIntent = new Intent(intents[intents.length - 1]);
			topIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			return PendingIntent.getActivity(context, requestCode, topIntent,
					flags);
		}
	}

	static class DualTaskStackBuilderImplHoneycomb implements
			DualTaskStackBuilderImpl {
		@Override
		public PendingIntent getPendingIntent(Context context,
				Intent[] intents, int requestCode, int flags, Bundle options) {
			intents[0] = new Intent(intents[0])
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
							| IntentCompat.FLAG_ACTIVITY_TASK_ON_HOME);
			return DualTaskStackBuilderHoneycomb.getActivitiesPendingIntent(
					context, requestCode, intents, flags);
		}
	}

	static class DualTaskStackBuilderImplJellybean implements
			DualTaskStackBuilderImpl {
		@Override
		public PendingIntent getPendingIntent(Context context,
				Intent[] intents, int requestCode, int flags, Bundle options) {
			intents[0] = new Intent(intents[0])
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
							| IntentCompat.FLAG_ACTIVITY_TASK_ON_HOME);
			return DualTaskStackBuilderJellybean.getActivitiesPendingIntent(
					context, requestCode, intents, flags, options);
		}
	}

	private static final DualTaskStackBuilderImpl IMPL;

	private static final String TAG = "DualTaskStackBuilder";

	static {
		if (Build.VERSION.SDK_INT >= 11) {
			IMPL = new DualTaskStackBuilderImplHoneycomb();
		} else {
			IMPL = new DualTaskStackBuilderImplBase();
		}
	}

	/**
	 * Return a new DualTaskStackBuilder for launching a fresh task stack
	 * consisting of a series of activities.
	 * 
	 * @param context
	 *            The context that will launch the new task stack or generate a
	 *            PendingIntent
	 * @return A new DualTaskStackBuilder
	 */
	public static DualTaskStackBuilder create(Context context) {
		return new DualTaskStackBuilder(context);
	}

	/**
	 * Return a new DualTaskStackBuilder for launching a fresh task stack
	 * consisting of a series of activities.
	 * 
	 * @param context
	 *            The context that will launch the new task stack or generate a
	 *            PendingIntent
	 * @return A new DualTaskStackBuilder
	 * 
	 * @deprecated use {@link #create(Context)} instead
	 */
	@Deprecated
	public static DualTaskStackBuilder from(Context context) {
		return create(context);
	}

	private final ArrayList<Intent> mIntents = new ArrayList<Intent>();

	private final Context mSourceContext;

	private DualTaskStackBuilder(Context a) {
		mSourceContext = a;
	}

	/**
	 * Add a new Intent to the task stack. The most recently added Intent will
	 * invoke the Activity at the top of the final task stack.
	 * 
	 * @param nextIntent
	 *            Intent for the next Activity in the synthesized task stack
	 * @return This DualTaskStackBuilder for method chaining
	 */
	public DualTaskStackBuilder addNextIntent(Intent nextIntent) {
		mIntents.add(nextIntent);
		return this;
	}

	/**
	 * Add a new Intent with the resolved chain of parents for the target
	 * activity to the task stack.
	 * 
	 * <p>
	 * This is equivalent to calling {@link #addParentStack(ComponentName)
	 * addParentStack} with the resolved ComponentName of nextIntent (if it can
	 * be resolved), followed by {@link #addNextIntent(Intent) addNextIntent}
	 * with nextIntent.
	 * </p>
	 * 
	 * @param nextIntent
	 *            Intent for the topmost Activity in the synthesized task stack.
	 *            Its chain of parents as specified in the manifest will be
	 *            added.
	 * @return This DualTaskStackBuilder for method chaining.
	 */
	public DualTaskStackBuilder addNextIntentWithParentStack(Intent nextIntent) {
		ComponentName target = nextIntent.getComponent();
		if (target == null) {
			target = nextIntent.resolveActivity(mSourceContext
					.getPackageManager());
		}
		if (target != null) {
			addParentStack(target);
		}
		addNextIntent(nextIntent);
		return this;
	}

	/**
	 * Add the activity parent chain as specified by manifest &lt;meta-data&gt;
	 * elements to the task stack builder.
	 * 
	 * @param sourceActivity
	 *            All parents of this activity will be added
	 * @return This DualTaskStackBuilder for method chaining
	 */
	public DualTaskStackBuilder addParentStack(Activity sourceActivity) {
		final Intent parent = DualNavUtils
				.getParentActivityIntent(sourceActivity);
		if (parent != null) {
			// We have the actual parent intent, build the rest from static
			// metadata
			// then add the direct parent intent to the end.
			ComponentName target = parent.getComponent();
			if (target == null) {
				target = parent.resolveActivity(mSourceContext
						.getPackageManager());
			}
			addParentStack(target);
			addNextIntent(parent);
		}
		return this;
	}

	/**
	 * Add the activity parent chain as specified by manifest &lt;meta-data&gt;
	 * elements to the task stack builder.
	 * 
	 * @param sourceActivityClass
	 *            All parents of this activity will be added
	 * @return This DualTaskStackBuilder for method chaining
	 */
	public DualTaskStackBuilder addParentStack(Class<?> sourceActivityClass) {
		return addParentStack(new ComponentName(mSourceContext,
				sourceActivityClass));
	}

	/**
	 * Add the activity parent chain as specified by manifest &lt;meta-data&gt;
	 * elements to the task stack builder.
	 * 
	 * @param sourceActivityName
	 *            Must specify an Activity component. All parents of this
	 *            activity will be added
	 * @return This DualTaskStackBuilder for method chaining
	 */
	public DualTaskStackBuilder addParentStack(ComponentName sourceActivityName) {
		final int insertAt = mIntents.size();
		try {
			Intent parent = DualNavUtils.getParentActivityIntent(
					mSourceContext, sourceActivityName);
			while (parent != null) {
				mIntents.add(insertAt, parent);
				parent = DualNavUtils.getParentActivityIntent(mSourceContext,
						parent.getComponent());
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG,
					"Bad ComponentName while traversing activity parent metadata");
			// ***ActionBarPoirot
			// added*******************************************************************************************************************
			if (PoirotWindow.DEBUG)
				e.printStackTrace();
			// If this runs in the extension app, then some parent search is
			// going to throw an error eventually. By then the right intent has
			// been added.
			final String mainPackageName = ((ActivityHelperUser)mSourceContext).getHomePackageName();
			if (!mainPackageName
					.equalsIgnoreCase(sourceActivityName.getPackageName())
					&& sourceActivityName.getPackageName().startsWith(
							mainPackageName)) {
				// do nothing
			} else {
				throw new IllegalArgumentException(e);
			}
		}
		return this;
	}

	/**
	 * Return the intent at the specified index for modification. Useful if you
	 * need to modify the flags or extras of an intent that was previously
	 * added, for example with {@link #addParentStack(Activity)}.
	 * 
	 * @param index
	 *            Index from 0-getIntentCount()
	 * @return the intent at position index
	 */
	public Intent editIntentAt(int index) {
		return mIntents.get(index);
	}

	/**
	 * Get the intent at the specified index. Useful if you need to modify the
	 * flags or extras of an intent that was previously added, for example with
	 * {@link #addParentStack(Activity)}.
	 * 
	 * @param index
	 *            Index from 0-getIntentCount()
	 * @return the intent at position index
	 * 
	 * @deprecated Renamed to editIntentAt to better reflect intended usage
	 */
	@Deprecated
	public Intent getIntent(int index) {
		return editIntentAt(index);
	}

	/**
	 * @return the number of intents added so far.
	 */
	public int getIntentCount() {
		return mIntents.size();
	}

	/**
	 * Return an array containing the intents added to this builder. The intent
	 * at the root of the task stack will appear as the first item in the array
	 * and the intent at the top of the stack will appear as the last item.
	 * 
	 * @return An array containing the intents added to this builder.
	 */
	public Intent[] getIntents() {
		Intent[] intents = new Intent[mIntents.size()];
		if (intents.length == 0)
			return intents;

		intents[0] = new Intent(mIntents.get(0))
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
						| IntentCompat.FLAG_ACTIVITY_TASK_ON_HOME);
		for (int i = 1; i < intents.length; i++) {
			intents[i] = new Intent(mIntents.get(i));
		}
		return intents;
	}

	/**
	 * Obtain a {@link PendingIntent} for launching the task constructed by this
	 * builder so far.
	 * 
	 * @param requestCode
	 *            Private request code for the sender
	 * @param flags
	 *            May be {@link PendingIntent#FLAG_ONE_SHOT},
	 *            {@link PendingIntent#FLAG_NO_CREATE},
	 *            {@link PendingIntent#FLAG_CANCEL_CURRENT},
	 *            {@link PendingIntent#FLAG_UPDATE_CURRENT}, or any of the flags
	 *            supported by {@link Intent#fillIn(Intent, int)} to control
	 *            which unspecified parts of the intent that can be supplied
	 *            when the actual send happens.
	 * @return The obtained PendingIntent
	 */
	public PendingIntent getPendingIntent(int requestCode, int flags) {
		return getPendingIntent(requestCode, flags, null);
	}

/**
	     * Obtain a {@link PendingIntent} for launching the task constructed by this builder so far.
	     *
	     * @param requestCode Private request code for the sender
	     * @param flags May be {@link PendingIntent#FLAG_ONE_SHOT},
	     *              {@link PendingIntent#FLAG_NO_CREATE}, {@link PendingIntent#FLAG_CANCEL_CURRENT},
	     *              {@link PendingIntent#FLAG_UPDATE_CURRENT}, or any of the flags supported by
	     *              {@link Intent#fillIn(Intent, int)} to control which unspecified parts of the
	     *              intent that can be supplied when the actual send happens.
	     * @param options Additional options for how the Activity should be started.
	     * See {@link android.content.Context#startActivity(Intent, Bundle)
	     * @return The obtained PendingIntent
	     */
	public PendingIntent getPendingIntent(int requestCode, int flags,
			Bundle options) {
		if (mIntents.isEmpty()) {
			throw new IllegalStateException(
					"No intents added to DualTaskStackBuilder; cannot getPendingIntent");
		}

		Intent[] intents = mIntents.toArray(new Intent[mIntents.size()]);
		intents[0] = new Intent(intents[0])
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
						| IntentCompat.FLAG_ACTIVITY_TASK_ON_HOME);
		// Appropriate flags will be added by the call below.
		return IMPL.getPendingIntent(mSourceContext, intents, requestCode,
				flags, options);
	}

	/**
	 * @deprecated Use editIntentAt instead
	 */
	@Deprecated
	@Override
	public Iterator<Intent> iterator() {
		return mIntents.iterator();
	}

	/**
	 * Start the task stack constructed by this builder. The Context used to
	 * obtain this builder must be an Activity.
	 * 
	 * <p>
	 * On devices that do not support API level 11 or higher the topmost
	 * activity will be started as a new task. On devices that do support API
	 * level 11 or higher the new task stack will be created in its entirety.
	 * </p>
	 */
	public void startActivities() {
		startActivities(null);
	}

	/**
	 * Start the task stack constructed by this builder. The Context used to
	 * obtain this builder must be an Activity.
	 * 
	 * <p>
	 * On devices that do not support API level 11 or higher the topmost
	 * activity will be started as a new task. On devices that do support API
	 * level 11 or higher the new task stack will be created in its entirety.
	 * </p>
	 * 
	 * @param options
	 *            Additional options for how the Activity should be started. See
	 *            {@link android.content.Context#startActivity(Intent, Bundle)

	 */
	public void startActivities(Bundle options) {
		if (mIntents.isEmpty()) {
			throw new IllegalStateException(
					"No intents added to DualTaskStackBuilder; cannot startActivities");
		}

		Intent[] intents = mIntents.toArray(new Intent[mIntents.size()]);
		intents[0] = new Intent(intents[0])
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
						| IntentCompat.FLAG_ACTIVITY_TASK_ON_HOME);
		if (!ContextCompat.startActivities(mSourceContext, intents, options)) {
			Intent topIntent = new Intent(intents[intents.length - 1]);
			topIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mSourceContext.startActivity(topIntent);
		}
	}
}
