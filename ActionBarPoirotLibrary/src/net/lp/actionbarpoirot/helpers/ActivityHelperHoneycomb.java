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
import net.lp.actionbarpoirot.actions.UiAction;
import net.lp.actionbarpoirot.util.UiUtilities;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.google.tv.leftnavbar.LeftNavBar;
import com.example.google.tv.leftnavbar.LeftNavBarService;

/**
 * An extension of {@link ActivityHelper} that provides Android 3.0-specific
 * functionality for Honeycomb tablets. It thus requires API level 11.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ActivityHelperHoneycomb extends ActivityHelper {
	
	/**
	 * Resource id for the title for the action bar.
	 */
	public static int actionBarTitleResId;
	
	/**
	 * Resource id for the left nav bar.
	 */
	public static int leftNavActionBarResId;
	
	/**
	 * The action bar.
	 */
	protected ActionBar actionBar;

	/*
	 * The left nav bar.
	 */
	private LeftNavBar leftNavBar;
	
	/*
	 * The options menu.
	 */
	private Menu mOptionsMenu;

	/**
	 * Constructor
	 * 
	 * @param activity The {@link Activity} we are helping. It must implement {@link ActivityHelperUser}.
	 */
	protected ActivityHelperHoneycomb(Activity activity) {//TODO: This has a dependency on some layout elements.
		super(activity);
		actionBarTitleResId = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		leftNavActionBarResId = Resources.getSystem().getIdentifier("main",
				"id", "android");
	}

	/** {@inheritDoc} */
	@Override
	public void addActionItemToCompatActionBar(UiAction action) {
		// Only has effect on pre-Honeycomb. On Honeycomb and up this is
		// performed ("SHOW_ACTION") directly on the MenuItem.

	}

	/** {@inheritDoc} */
	@Override
	public void enableActionBarTitleMarquee() {
		// Make title scrolling, a bit of a hack
		final TextView titleView = (TextView) mActivity
				.findViewById(actionBarTitleResId);
		if (titleView != null) {
			titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			titleView.setMarqueeRepeatLimit(-1);
			titleView.setFocusable(true);
			titleView.setFocusableInTouchMode(true);
			titleView.requestFocus();// TODO: action bar title doesn't keep
										// scrolling after you've touched
										// something else. Should fix that.
		}

		// Also focus LeftNavBar, a bit of a hack too
		final View view = (mActivity.findViewById(leftNavActionBarResId));
		if (view != null) {
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.requestFocus();
		}
	}

	/**
	 * Returns the {@link ActionBar} for the action bar on newer devices (>=11).
	 */
	public ActionBar getActionBar() {
		return actionBar;
	}

	/** {@inheritDoc} */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mOptionsMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	/** {@inheritDoc} */
	@Override
	public boolean onOptionsItemSelected(MenuItem item,
			boolean goBackInsteadOfUp) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Respond to the action bar's Up/Home button
			if (goBackInsteadOfUp) {
				try {
					((FragmentActivity) mActivity).getSupportFragmentManager()
							.popBackStack();//TODO: depends on FragmentActivity
				} catch (ClassCastException e) {
					if (PoirotWindow.DEBUG)
						Log.i(TAG,
								"Not FragmentActivity. Likely Preferences or related.",
								e);
					try {
						if (((PreferenceActivity) mActivity)
								.getFragmentManager().getBackStackEntryCount() == 0) {
							goHome();
							return true;
						}
						((PreferenceActivity) mActivity).getFragmentManager()
								.popBackStack();
					} catch (ClassCastException e2) {
						if (PoirotWindow.DEBUG)
							Log.i(TAG,
									"Not FragmentActivity, not Preferences either.",
									e2);
						goHome();
					}
				}
			} else {
				goHome();
			}
			return true;
		}
		return super.onOptionsItemSelected(item, goBackInsteadOfUp);
	}

	/** {@inheritDoc} */
	@Override
	public void setActionBarTitle(CharSequence title) {
		actionBar.setTitle(title);
	}

	/** {@inheritDoc} */
	@Override
	public void setDisplayHomeAsUpEnabled(boolean b) {
		getActionBar().setDisplayHomeAsUpEnabled(b);
	}

	/** {@inheritDoc} */
	@Override
	public void setRefreshActionButtonCompatState(boolean refreshing) {
		
		/* For a different possible implementation using ActionBarCompat, see
		 * https://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/ActivityHelper.java?r=27a82ff10b436da5914a3961df245ff8f66b6252
		 */
	}

	/** {@inheritDoc} */
	@Override
	public void setupActionBar(CharSequence title, int color) {
		if (UiUtilities.isGoogleTV(mActivity)) {
			// Left Nav Bar
			actionBar = leftNavBar = (LeftNavBarService.instance())
					.getLeftNavBar(mActivity);
			leftNavBar
					.setDisplayOptions(LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED);
			leftNavBar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);

			actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
			// TODO: set proper background for left nav bar.

			// Hide the title bar (like empty action bar at top for GoogleTV).
			actionBar.setDisplayShowTitleEnabled(false);

			// Hide the native >Honeycomb action bar in favor of the left nav
			// bar.
			final ActionBar nativeActionBar = mActivity.getActionBar();
			nativeActionBar.hide();
		} else {
			actionBar = mActivity.getActionBar();
		}
		actionBar.setTitle(title);
		actionBar.setDisplayHomeAsUpEnabled(true);

		enableActionBarTitleMarquee();
	}
}
