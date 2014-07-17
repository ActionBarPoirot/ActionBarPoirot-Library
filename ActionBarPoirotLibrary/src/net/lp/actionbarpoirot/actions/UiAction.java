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
package net.lp.actionbarpoirot.actions;

import java.util.LinkedHashSet;

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.helpers.ActivityHelper;
import net.lp.actionbarpoirot.helpers.ActivityHelperHoneycomb;
import net.lp.actionbarpoirot.helpers.ActivityHelper.ActivityHelperUser;
import net.lp.actionbarpoirot.util.UiUtilities;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.google.tv.leftnavbar.LeftNavBar;
import com.markupartist.android.widget.ActionBar.Action;

/**
 * Performs a user faced action when triggered. A {@link UiAction} represents
 * both an action item in an old-style "compatibility" action bar or menu, as
 * well as in the modern Honeycomb-and-up native action bars from the support
 * library, and in the Google TV left nav bar. It can also represent contextual
 * action bar (CAB) action items, even in multiple selection. The action items
 * can be two-stage actions, where the {@link Activity} starts another
 * {@link Activity} with an intent and waits for the result to decide its own
 * action.
 * <p>
 * Implement this abstract class with representing, logical and executing code.
 * In an {@link Activity}, typically a {@link FragmentActivity}, or in a
 * {@link Fragment}, add an object to the {@link UiActionManager} for the
 * {@link UiAction} to be considered in the user interface.
 * <p>
 * <p>
 * Setup is allowed from both an {@link Activity} and a {@link Fragment} because
 * actions often belong with one or the other. Reuse of a {@link Fragment}
 * elsewhere will then still show its relevant {@link UiAction}s, if the
 * {@link Activity} supports them.
 * <p>
 * A {@link UiAction} can be respresented as, besides in an old action bar, a
 * menu item in the context menu or options menu. It can also be triggered with
 * alphabetical or numerical key shortcuts, just like menu items. It allows for
 * per-Activity configuration of various options, such as whether this
 * particular action should be hidden on the compatibility action bar, or on TV.
 * <p>
 * Choose the representation (zone) in the left nav bar with... The left nav bar
 * example from Google is highly experimental and is very much a moving target.
 * <p>
 * You can control the order of action items with ORDER_FIRST_OR_CONTROLS and similar.
 * <p>
 * TODO: CAB support coming, context menu too
 * 
 * 
 * @author pjv
 * 
 * @param <C>
 *            The {@link Activity} or group of Activities this {@link UiAction}
 *            will be used in.
 */
public abstract class UiAction<C extends FragmentActivity & PoirotWindow & ActivityHelperUser>
		extends Object implements Action, View.OnClickListener {
	// Remove "implements Action" if you are not using Johan Nilsson's action
	// bar (com.markupartist.android.widget.ActionBar).

	/**
	 * Order first among menu items. This is for really important actions, that might as well have been buttons in the main UI layout. There are seven groups in total.
	 */
	public static final int ORDER_FIRST_OR_CONTROLS = Menu.FIRST;

	/**
	 * Order second among menu items. This is for important actions related to this fragment. The current setup assumes up to three fragments next to each other for a multi-pane layout, each with its own actions. This is for the deepest, most detailed, right one. There are seven groups in total.
	 */
	public static final int ORDER_SECOND_OR_RIGHT_ITEM_LEVEL = ORDER_FIRST_OR_CONTROLS + 1;

	/**
	 * Order third among menu items. This is for important actions related to this fragment. The current setup assumes up to three fragments next to each other for a multi-pane layout, each with its own actions. This is for the next-to-deepest, next-most detailed, middle one. There are seven groups in total.
	 */
	public static final int ORDER_THIRD_OR_MIDDLE_COLLECTION_LEVEL = ORDER_SECOND_OR_RIGHT_ITEM_LEVEL + 1;

	/**
	 * Order fourth among menu items. This is for important actions related to this fragment. The current setup assumes up to three fragments next to each other for a multi-pane layout, each with its own actions. This is for the less deepest, less detailed, left one. There are seven groups in total.
	 */
	public static final int ORDER_FOURTH_OR_LEFT_COLLECTIONS_LIST_LEVEL = ORDER_THIRD_OR_MIDDLE_COLLECTION_LEVEL + 1;

	/**
	 * Order fifth among menu items. This is for extra actions, that don't belong strictly to a fragment, but are needed anyway. There are seven groups in total.
	 */
	public static final int ORDER_FIFTH_OR_EXTRA = ORDER_FOURTH_OR_LEFT_COLLECTIONS_LIST_LEVEL + 1;

	/**
	 * Order sixth among menu items. This is for category-alternative actions, that are automatically added based on intents. There are seven groups in total.
	 */
	public static final int ORDER_SIXTH_OR_ALTERNATIVE = ORDER_FIFTH_OR_EXTRA + 1;

	/**
	 * Order seventh (last) among menu items. This is for very extra actions, that are barely important so that they end up at the bottom of the list. There are seven groups in total.
	 */
	public static final int ORDER_SEVENTH_OR_VERY_EXTRA = ORDER_SIXTH_OR_ALTERNATIVE + 1;

	private static final String TAG = PoirotWindow.TAG + "UiAction";

	/**
	 * Show in the contextual zone. Not built yet.
	 */
	public static final int ZONE_CONTEXTUAL = 2;

	/**
	 * Show in the detail zone. Not built yet.
	 */
	public static final int ZONE_DETAIL = 3;

	/**
	 * Show in the global zone of the left nav bar for Google TV. This is
	 * currently the only one.
	 */
	public static final int ZONE_GLOBAL = 1;

	/**
	 * No zone set.
	 */
	public static final int ZONE_NONE = 0;

	/*
	 * The class name ({@link Object.getLocalClassName()} of the {@link
	 * Activity}/{@link Fragment} (group) this {@link UiAction} is related to.
	 */
	protected String clientReference;

	/*
	 * A unique number among all {@link UiAction}s for one {@link
	 * Activity}/{@link Fragment} (group). This allows the clicks and activity
	 * results to be sent to the correct {@link UiAction}.
	 */
	protected int menuItemAndRequestCodeId;

	/*
	 * How to show the action item in the action bar, as in the Android action
	 * bar API.
	 */
	protected int showActionEnum = MenuItem.SHOW_AS_ACTION_NEVER;

	/*
	 * Whether the action item should show on the compatibility action bar on
	 * old platform versions.
	 */
	protected boolean showActionOnCompatActionBar = false;

	/*
	 * Whether the action item should show in the contextual action bar in the
	 * situation of multiple selection.
	 */
	protected boolean showActionWhenMultipleSelected = false;

	/*
	 * Which zone to show the action item in. Mostly for Google TV.
	 */
	protected int zoneActionEnum = ZONE_NONE;

	/**
	 * Default constructor.
	 * 
	 * The constructor is simple, but there are some modifier methods that can
	 * be used immediately after construction (and in one line).
	 * 
	 * @param clientReference
	 *            The class name ({@link Object.getLocalClassName()} of the
	 *            {@link Activity}/{@link Fragment} (group) this
	 *            {@link UiAction} is related to.
	 */
	public UiAction(String clientReference) {
		this.clientReference = clientReference;
	}

	/*
	 * Add this {@link UiAction} as action item to the Google TV left nav bar.
	 * 
	 * @param context The {@link Activity} context.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void addActionItemToLeftNavBar(C context) {
		if (UiUtilities.isGoogleTV(context.getApplicationContext())) {
			try {
				final LeftNavBar leftNavBar = ((LeftNavBar) ((ActivityHelperHoneycomb) context
						.getActivityHelper()).getActionBar());
				final PopupMenu tempMenu = new PopupMenu(context,
						leftNavBar.getCustomView());// TODO: Should not use
													// MenuItem and
													// onCreateOptionsMenu, but
													// build an own stack of
													// methods for the
													// LeftNavBar.
				leftNavBar.addActionItem(
						onCreateOptionsMenu(tempMenu.getMenu(), context), this);
			} catch (ClassCastException ce) {
				if (PoirotWindow.DEBUG)
					Log.d(TAG, "ClassCastException with LeftNavBar", ce);
			}
		}
	}

	/*
	 * Add this {@link UiAction} as action item to the Google TV left nav bar.
	 * 
	 * @param context The {@link Activity} context.
	 * 
	 * @param activityHelper The {@link ActivityHelper} you created in the
	 * {@link Activity}/{@link Fragment}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void addActionItemToLeftNavBar(C context,
			ActivityHelper activityHelper) {
		if (UiUtilities.isGoogleTV(context.getApplicationContext())) {
			try {
				final LeftNavBar leftNavBar = ((LeftNavBar) ((ActivityHelperHoneycomb) activityHelper)
						.getActionBar());
				final PopupMenu tempMenu = new PopupMenu(context,
						leftNavBar.getCustomView());// TODO: Should not use
													// MenuItem and
													// onCreateOptionsMenu, but
													// build an own stack of
													// methods for the
													// LeftNavBar.
				leftNavBar.addActionItem(
						onCreateOptionsMenu(tempMenu.getMenu(), context), this);
			} catch (ClassCastException ce) {
				if (PoirotWindow.DEBUG)
					Log.d(TAG, "ClassCastException with LeftNavBar", ce);
			}
		}
	}

	/**
	 * Destructor
	 */
	public void destruct() {
		this.showActionOnCompatActionBar = false;
		this.showActionWhenMultipleSelected = false;
		this.clientReference = null;
		this.menuItemAndRequestCodeId = -1;
		this.showActionEnum = MenuItem.SHOW_AS_ACTION_NEVER;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		// ... rest not necessary right now.
		return true;
	}

	/**
	 * Execute the action. Implement this method.
	 * 
	 * Similar to the core of what you would do in {@link
	 * Activity.onOptionsItemSelected()} or {@link
	 * Activity.onContextItemSelected()} for example.
	 * 
	 * @param context
	 *            The {@link Activity} context.
	 */
	public abstract void execute(C context);

	/*
	 * Returns the resource id of the icon for use in an action bar.
	 * 
	 * @return The resource id of the icon for use in an action bar.
	 */
	protected abstract int getActionBarIcon();

	/**
	 * Returns something to recognize the client by. This reference was provided when this action was created and added to the client (activity or fragment). It is used to remove duplicates of actions based on which client has disappeared.
	 * 
	 * @return string reference to a client (activity or fragment).
	 */
	public String getClientReference() {
		return clientReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.markupartist.android.widget.ActionBar.Action#getDrawable()
	 */
	@Override
	public int getDrawable() {
		return getActionBarIcon();
	}

	/*
	 * Returns the (maximum) range of orders for this one action. {@link
	 * UiAction}s need to be ordered in order for {@link onActivityResult()} to
	 * work. Some need a multiplicity of their order number. Basically, this is
	 * the number of request codes an action needs.
	 * 
	 * @return The (maximum) range of orders. By default 1.
	 */
	protected int getMaxOrderRange() {
		return 1;
	}

	/**
	 * Returns in which zone of the left nav bar this action should be displayed.
	 * 
	 * @return Zone of the left nav bar.
	 */
	public int getNavBarZoneType() {
		return zoneActionEnum;
	}

	/**
	 * Is this {@link UiAction} appropriate for the current situation and
	 * device.
	 * 
	 * @return True if appropriate; false otherwise.
	 */
	public abstract boolean isAppropriate(UiActionManager<C> uiActionManager);

	/**
	 * Called when an activity you launched exits, giving you the requestCode
	 * you started it with, the resultCode it returned, and any additional data
	 * from it. Implement this method.
	 * 
	 * Similar to the core of what you would do in {@link
	 * Activity.onActivityResult()}. This is a call-through that is made to each
	 * {@link UiAction}, so still check if request code matches.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @param context
	 *            The {@link Activity} context.
	 */
	public abstract void onActivityResult(int requestCode, int resultCode,
			Intent data, C context);

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		performAction(v);
	}

	boolean onContextItemSelectedWithMultipleSelection(MenuItem item,
			C context, LinkedHashSet<Uri> mSelectedSessionPositionsUris) {
		// TODO: Later when used for the first time, this should be abstract and
		// implemented in every action.
		return false;
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Implement this method.
	 * 
	 * Similar to the core of what you would do in {@link
	 * Activity.onCreateOptionsMenu()}. This is a call-through that is made to
	 * each {@link UiAction}.
	 * 
	 * @param menu
	 * @param context
	 *            The {@link Activity} context.
	 * @return
	 */
	public abstract MenuItem onCreateOptionsMenu(Menu menu, C context);

	/**
	 * Add the {@link UiAction} to the native action bar.
	 * 
	 * Call this method selectively from {@link onCreateOptionsMenu()}.
	 * 
	 * @param menuItem
	 * @param context
	 *            The {@link Activity} context.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void onCreateOptionsMenuMoreNative(MenuItem menuItem, C context) {
		menuItem.setShowAsAction(showActionEnum);
		menuItem.setIcon(getActionBarIcon());
	}

	/**
	 * Called when a key was released and not handled by any of the views inside
	 * of the activity. Implement this method.
	 * 
	 * Similar to the core of what you would do in {@link Activity.onKeyUp()}.
	 * This is a call-through that is made to each {@link UiAction}.
	 * 
	 * @param keyCode
	 * @param event
	 * @param context
	 *            The {@link Activity} context.
	 * @return
	 */
	public abstract boolean onKeyUp(int keyCode, KeyEvent event, C context);

	/**
	 * This hook is called whenever an item in your options menu is selected.
	 * Derived classes should call through to the base class for it to perform
	 * the default menu handling. Implement this method.
	 * 
	 * Similar to the core of what you would do in {@link
	 * Activity.onOptionsItemSelected()}. This is a call-through that is made to
	 * each {@link UiAction}.
	 * 
	 * @param item
	 * @param context
	 *            The {@link Activity} context.
	 * @return
	 */
	public abstract boolean onOptionsItemSelected(MenuItem item, C context);

	/**
	 * Prepare the Screen's standard options menu to be displayed. This is
	 * called right before the menu is shown, every time it is shown. You can
	 * use this method to efficiently enable/disable items or otherwise
	 * dynamically modify the contents. Derived classes should call through to
	 * the base class for it to perform the default menu handling. Implement
	 * this method.
	 * 
	 * Similar to the core of what you would do in {@link
	 * Activity.onPrepareOptionsMenu()}. This is a call-through that is made to
	 * each {@link UiAction}.
	 * 
	 * @param menu
	 * @param context
	 *            The {@link Activity} context.
	 * @return
	 */
	public abstract void onPrepareOptionsMenu(Menu menu, C context);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.markupartist.android.widget.ActionBar.Action#performAction(android
	 * .view.View)
	 */
	@Override
	public void performAction(View view) {
		execute((C) view.getContext());
	}

	/**
	 * Set how to show the action item in the (native) action bar.
	 * 
	 * @param showActionEnum
	 *            How to show the action item.
	 * @return The {@link UiAction} itself for easy concatenating.
	 */
	public UiAction<C> setActionBarShowType(int showActionEnum) {
		this.showActionEnum = showActionEnum;
		return this;
	}

	public void setActionItemVisible(Menu menu, boolean visible) {
		// TODO: Later, this currently unused code should be enabled. If we ever
		// are going to use UiActions with the CAB. Now it saves resources.
		/*
		 * final MenuItem item = menu.findItem(menuItemAndRequestCodeId); if
		 * (item != null) item.setVisible(visible);
		 */
	}

	/**
	 * Add this {@link UiAction} as action item to the Google TV left nav bar in
	 * a specific zone of the bar. Returns this {@link UiAction}.
	 * 
	 * Call at the moment you want to show it there, typically in a
	 * setUiActions() method in {@link Activity.onStart()} or {@link
	 * Fragment.onStart()}. Make sure to call again after left nav bar is
	 * cleared.
	 * 
	 * @param zoneActionEnum
	 *            Which zone to show the action item in. For instance,
	 *            {@link UiAction.ZONE_GLOBAL}.
	 * @param context
	 *            The {@link Activity} context.
	 * @param activityHelper
	 *            The {@link ActivityHelper} you created in the {@link Activity}
	 *            /{@link Fragment}.
	 * @return The {@link UiAction} itself for easy concatenating.
	 */
	public UiAction<C> setNavBarZoneType(int zoneActionEnum, C context,
			ActivityHelper activityHelper) {
		this.zoneActionEnum = zoneActionEnum;
		/*
		 * if(UiUtilities.hasHoneycombOrUp() && getNavBarZoneType()!=ZONE_NONE
		 * && isAppropriate(((ActionManagerUser)context).getUiActionManager())){
		 * addActionItemToLeftNavBar(context); }
		 */
		return this;
	}

	/**
	 * Set the order of this action (done at creation time). This determines what the first (and subsequent) requestCodeId is that will be used by this action.
	 * 
	 * @param i Order of this action.
	 * @return The next suggested order for the next {@link UiAction}, which is offset by the maximum offset, to make room for more requestCodeId's.
	 */
	public int setOrder(int i) {
		menuItemAndRequestCodeId = i;
		return i + getMaxOrderRange();
	}

	/**
	 * Set that the action item should show on the compatibility action bar on
	 * old platform versions.
	 * 
	 * @return The {@link UiAction} itself for easy concatenating.
	 */
	public UiAction<C> setShowOnCompatActionBar() {
		this.showActionOnCompatActionBar = true;
		return this;
	}

	/**
	 * Set that the action item should show in the contextual action bar in the
	 * situation of multiple selection.
	 * 
	 * @return The {@link UiAction} itself for easy concatenating.
	 */
	public UiAction<C> setShowWhenMultipleSelected() {
		this.showActionWhenMultipleSelected = true;
		return this;
	}

	/**
	 * Whether the action item should show on the compatibility action bar on
	 * old platform versions.
	 * 
	 * @return True if the action item should show on the compatibility action
	 *         bar on old platform versions; false otherwise.
	 */
	public boolean shouldShowOnCompatActionBar() {
		return showActionOnCompatActionBar;
	}

	/**
	 * Whether the action item should show in the contextual action bar in the
	 * situation of multiple selection.
	 * 
	 * @return True if the action item should show in the contextual action bar
	 *         in the situation of multiple selection; false otherwise.
	 */
	public boolean shouldShowWhenMultipleSelected() {
		return showActionWhenMultipleSelected;
	}

	/**
	 * Returns whether this action should be shown on the old action bar on old devices.
	 * 
	 * @return whether action should be shown on old action bar.
	 */
	public UiAction<C> showOnCompatActionBar() {
		this.showActionOnCompatActionBar = true;
		return this;
	}

}
