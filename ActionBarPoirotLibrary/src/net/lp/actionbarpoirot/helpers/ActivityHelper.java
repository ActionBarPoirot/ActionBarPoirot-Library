package net.lp.actionbarpoirot.helpers;

/*
 * Copyright 2011 Google Inc.
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

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.R;
import net.lp.actionbarpoirot.actions.UiAction;
import net.lp.actionbarpoirot.util.UiUtilities;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.example.google.tv.leftnavbar.LeftNavBar;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

/**
 * A class that handles some common activity-related functionality in the app,
 * such as setting up the action bar. This class provides functionality useful
 * for both phones and tablets, and does not require any Android 3.0-specific
 * features.
 * 
 * The compat action bar is managed mostly outside of this class, in contrast to
 * ActivityHelperHoneycomb where the support library action bar is managed
 * internally, as to action items.
 * 
 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
 * 
 * 
 */
public class ActivityHelper {
	public interface ActivityHelperUser {
		/**
		 * Returns the {@link ActivityHelper} object associated with this
		 * activity.
		 */
		public ActivityHelper getActivityHelper();

		/**
		 * Get the complicated up intent for up navigation that leads to the
		 * correct parent activity contents.
		 * 
		 * @return
		 */
		public Intent getComplicatedUpIntent();

		/**
		 * Can up navigation not be derived statically.
		 * 
		 * @return
		 */
		public boolean hasComplicatedUpIntent();

		/**
		 * Get the package name for the app that contains the home activity, for
		 * up navigation that leads to the correct parent activity contents.
		 * 
		 * @return
		 */
		public String getHomePackageName();
		
		/**
		 * Get the intent that leads to the home activity.
		 * 
		 * @return
		 */
		public Intent getHomeIntent();
	}

	public static final String TAG = PoirotWindow.TAG + "ActivityHelper";

	/**
	 * Factory method for creating {@link ActivityHelper} objects for a given
	 * activity. Depending on which device the app is running, either a basic
	 * helper or Honeycomb-specific helper will be returned.
	 */
	public static ActivityHelper createInstance(Activity activity) {
		return UiUtilities.hasHoneycombOrUp() ? new ActivityHelperHoneycomb(
				activity) : new ActivityHelper(activity);
	}

	/**
	 * The action bar for pre-Honeycomb.
	 * 
	 * Remove if you are not using Johan Nilsson's action bar (com.markupartist.android.widget.ActionBar).
	 */
	protected ActionBar actionBarCompat;
	
	/**
	 * The {@link Activity} we are helping.
	 */
	protected Activity mActivity;

	/**
	 * Constructor
	 * 
	 * @param activity The {@link Activity} we are helping. It must implement {@link ActivityHelperUser}.
	 */
	protected ActivityHelper(Activity activity) {
		mActivity = activity;

		if (!(activity instanceof ActivityHelperUser)) {
			throw new ClassCastException("Activity must implement callbacks.");
		}
	}
	
    /*
     * Singleton for a {@link IntentAction} that takes you to the home screen of the app.
     */
    private Action homeIntentAction;

	/**
	 * {@link IntentAction} for a button on an action bar that takes you home. Getter for the Singleton.
	 *
	 * @return intent action
	 */
	public Action getHomeIntentAction(Context context) {//TODO: leaks context?
		if(homeIntentAction == null){
			//Not using LAUNCH because there might be multiple in the future.
	        homeIntentAction = new IntentAction(context, ((ActivityHelperUser)mActivity).getHomeIntent(), R.drawable.ic_title_home_default);
		}

        return homeIntentAction;
	}

	/*
	 * No-op
	 * 
	 * Adds an action bar button to the compatibility action bar (on phones).
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	@Deprecated
	private View addActionButtonCompat(int iconResId, int textResId,
			View.OnClickListener clickListener, boolean separatorAfter) {
		return null;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/*
	 * No-op
	 * 
	 * Adds an action button to the compatibility action bar, using menu
	 * information from a {@link MenuItem}. If the menu item ID is
	 * <code>menu_refresh</code>, the menu item's state can be changed to show a
	 * loading spinner using
	 * {@link ActivityHelper#setRefreshActionButtonCompatState(boolean)}.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	@Deprecated
	private View addActionButtonCompatFromMenuItem(final MenuItem item) {
		return null;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Add a {@link UiAction} to the compat action bar.
	 * 
	 * Only has effect on pre-Honeycomb. On Honeycomb and up this is performed
	 * ("SHOW_ACTION") directly on the MenuItem.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 * 
	 * @param action
	 */
	public void addActionItemToCompatActionBar(UiAction action) {
		if (action.shouldShowOnCompatActionBar()) {
			getActionBarCompat().addAction(action);
		}
	}

	/**
	 * Remove all {@link UiAction}s from the compat action bar.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public void clearCompatActionBarActionItems() {
		if (getActionBarCompat() == null)
			return;
		getActionBarCompat().removeActions();
	}

	/**
	 * Remove all {@link UiAction}s from the left nav bar.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public void clearLeftNavBarActionItems() {
		if (UiUtilities.isGoogleTV(mActivity.getApplicationContext())) {
			try {
				final LeftNavBar leftNavBar = ((LeftNavBar) ((ActivityHelperHoneycomb) this)
						.getActionBar());
				leftNavBar.removeActionItems();
			} catch (ClassCastException ce) {
				if (PoirotWindow.DEBUG)
					Log.w(TAG, "Clear left nav bar action items failed.");
			}
		}
	}

	/**
	 * Enable the marquee (scrolling) effect for the title text in the action bar.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public void enableActionBarTitleMarquee() {
		// Already by default. Needs no reenabling.
	}

	/**
	 * Finish affinity, as in {@link Activity.finishAffinity()}.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void finishAffinity() {
		try {
			mActivity.finishAffinity();
		} catch (IllegalStateException e) {
			// We can't finishAffinity if we have a result. In that case
			// IllegalStateException will also be thrown.
			// Fall back and simply finish the current activity instead.

			// Tell the developer what's going on to avoid hair-pulling.
			if (PoirotWindow.DEBUG)
				Log.i(TAG,
						"onNavigateUp only finishing topmost activity to return a result");
			mActivity.finish();
		}
	}

	/**
	 * On older devices, fix the background.
	 * 
	 * @param window
	 */
	public void fixBackground(Window window) {
		if (!UiUtilities.hasHoneycombOrUp()) {// BUGFIXED lp:791976: For
												// Honeycomb devices, if not
												// covered by UI elements, the
												// background would actually
												// become garbled and
												// transparent.
			window.setBackgroundDrawable(null);
		}
	}

	/**
	 * Returns the {@link ActionBar} for the action bar on phones (compatibility
	 * action bar). Can return null, and will return null on Honeycomb.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public ActionBar getActionBarCompat() {
		return actionBarCompat;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Invoke "home" action
	 */
	public void goHome() {
		// Automatically handle hierarchical Up navigation if the proper
		// metadata is available.
		Intent upIntent = DualNavUtils.getParentActivityIntent(mActivity);
		if (upIntent != null) {
			final PackageManager pm = mActivity.getPackageManager();
			ActivityInfo ai = null;
			try {
				ai = pm.getActivityInfo(mActivity.getComponentName(), 0);
			} catch (NameNotFoundException e) {
				// nothing
			}
			if (ai != null && ai.taskAffinity == null) {
				// Activities with a null affinity are special; they really
				// shouldn't
				// specify a parent activity intent in the first place. Just
				// finish
				// the current activity and call it a day.
				mActivity.finish();
			} else if (DualNavUtils.shouldUpRecreateTask(mActivity, upIntent)) {
				// This activity is NOT part of this app's task (was launched
				// from foreign app), so create a new task
				// when navigating up, with a synthesized back stack.
				DualTaskStackBuilder b = DualTaskStackBuilder.create(mActivity);
				// Add all of this activity's parents to the back stack
				b.addNextIntentWithParentStack(upIntent);
				// Navigate up to the closest parent
				b.startActivities();

				if (UiUtilities.hasJellyBeanOrUp()) {
					finishAffinity();
				}
			} else {
				// This activity is part of this app's task, so simply
				// navigate up to the logical parent activity.

				// If up intent is complicated, ask the activity, otherwise just use the static up intent.
				if (((ActivityHelperUser) mActivity).hasComplicatedUpIntent()) {
					upIntent = ((ActivityHelperUser) mActivity)
							.getComplicatedUpIntent();
		}

				final boolean foundActivityInHistory = DualNavUtils
						.navigateUpTo(mActivity, upIntent);
				if (!foundActivityInHistory) {
					mActivity.startActivity(upIntent);
				}
			}
		} else {
			// Probably has no PARENT_ACTIVITY set for this form factor. So
			// activity might be used from multiple places. So just go back.
			if (UiUtilities.hasHoneycombOrUp()) {
				popBackStack();
			} else {
				mActivity.finish();// never really called
			}
		}
	}

	/**
	 * Invoke "search" action, triggering a default search.
	 */
	public void goSearch() {
		mActivity.startSearch(null, false, Bundle.EMPTY, false);
	}

	/**
	 * Returns true.
	 * 
	 * Perform rest of onCreateOptionsMenu().
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}


	/**
	 * Returns false.
	 * 
	 * Perform rest of onKeyDown().
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Perform rest of onKeyLongPress().
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goHome();
			return true;
		}
		return false;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Returns false.
	 * 
	 * Perform rest of onOptionsItemSelected().
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public boolean onOptionsItemSelected(MenuItem item,
			boolean goBackInsteadOfUp) {
		return false;
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/*
	 * Pop from the back stack.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void popBackStack() {
		((FragmentActivity) mActivity).getSupportFragmentManager()
				.popBackStack();
	}

	/**
	 * Show the action bar circular (indeterminate) progress bar.
	 */
	public void requestProgressBar() {
		// Request progress bar
		mActivity.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		if (!UiUtilities.hasAcceptableNativeActionBar())
			mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mActivity.setProgressBarIndeterminateVisibility(true);
	}

	/**
	 * Sets the action bar title to the given string.
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public void setActionBarTitle(CharSequence title) {
		getActionBarCompat().setTitle(title);
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Set the title for the context menu.
	 * 
	 * @param menu Menu that is shown.
	 * @param title Title text to set.
	 */
	public void setContextTitle(ContextMenu menu, final String title) {
		// Setup the menu header
		menu.setHeaderTitle(title);
	}

	/**
	 * Set whether the home icon should work as up navigation.
	 * 
	 * @param enabled
	 */
	public void setDisplayHomeAsUpEnabled(boolean enabled) {
		// nothing
	}

	/**
	 * No-op
	 * 
	 * Sets the indeterminate loading state of a refresh button added with
	 * {@link ActivityHelper#addActionButtonCompatFromMenuItem(android.view.MenuItem)}
	 * (where the item ID was menu_refresh).
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public void setRefreshActionButtonCompatState(boolean refreshing) {
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Sets up the action bar with the given title. Only a default color is used for now.
	 * 
	 * @param title title to set.
	 * @param color (currently unused.)
	 * 
	 * Reimplement if you are not using Johan Nilsson's action bar
	 * (com.markupartist.android.widget.ActionBar).
	 */
	public void setupActionBar(CharSequence title, int color) {
		actionBarCompat = (ActionBar) mActivity.findViewById(R.id.actionbar);
		actionBarCompat.setTitle(title);
		actionBarCompat.setHomeAction(getHomeIntentAction(mActivity));
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/**
	 * Method, to be called in <code>onPostCreate</code>, that sets up this
	 * activity as the home activity for the app.
	 */
	public void setupHomeActivity() {
	}

	/**
	 * Method, to be called in <code>onPostCreate</code>, that sets up this
	 * activity as a sub-activity in the app.
	 */
	public void setupSubActivity() {
	}
}
