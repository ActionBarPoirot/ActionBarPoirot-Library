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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.helpers.ActivityHelper.ActivityHelperUser;
import net.lp.actionbarpoirot.util.UiUtilities;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Handles attachment of {@link UiAction}s to {@link Activity}s.
 * 
 * @author pjv
 * 
 */
public class UiActionManager<C extends FragmentActivity & PoirotWindow & ActivityHelperUser> {

	/**
	 * Represents an {@link Activity} that has action items and uses the {@link UiActionManager}. It also has call-throughs "lifecycle" methods for the Fragment to send through (for action items) to the Activity. 
	 */
	public static interface ActionManagerUser extends ActivityHelperUser {
	    public void onFragmentPrepareOptionsMenu(Menu menu);
	    public void onFragmentCreateOptionsMenu(Menu menu);
	    public boolean onFragmentContextItemSelectedWithMultipleSelection(MenuItem item,
				LinkedHashSet<Uri> mSelectedSessionPositionsUris);
	    public int addAndManageActionItem(UiAction action);
	    public int removeClientActionItems(String clientReference);
	    public void onFragmentActivityResult(int requestCode, int resultCode, Intent data);
	    public UiActionManager getUiActionManager();
	}

	/**
	 * Represents an {@link Activity} that has a CAB (or context menu) and uses the {@link UiActionManager}.
	 */
	public interface ActionModeUser{
		public boolean onCreateContextMenuInner(Menu menu, Uri uri, ArrayList<Uri> multipleUris);
		public Uri getContextItemUri(int position);
	    public UiActionManager getUiActionManager();
	    public String getContextualActionBarTitle(int numSelected);
	}

	/**
	 * Represents an {@link Activity} that has a CAB with multiple selection possibility.
	 */
	public interface MultiChoiceModeUser{
		public boolean onContextItemSelectedWithMultipleSelection(MenuItem item, LinkedHashSet<Uri> mSelectedSessionPositionsUris);
	}

	/**
	 * Represents an {@link Activity} that has a search bar.
	 */
	public interface SearchUser{
	    public String getLocalClassName();
	}

	/**
	 * Start value for counting the index (order for requestCode).
	 */
	public static final int I_START = Menu.FIRST + 3;

	/*
	 * Key to save the action's class type in the instance state.
	 */
	public static final String KEY_UI_ACTION_CLASS = "ui_action_class";

	/*
	 * Key to save the list of actions in the instance state.
	 */
	public static final String KEY_UI_ACTION_LIST = "ui_action_bundle_list";
	
	public static final String TAG = PoirotWindow.TAG + "UiActionManager";

	/**
	 * List of current managed actions.
	 */
	public ArrayList<UiAction<C>> actions = new ArrayList<UiAction<C>>();
	
	/**
	 * List of previously managed actions, that might still need to be cleaned up.
	 */
	public ArrayList<UiAction<C>> oldActions = new ArrayList<UiAction<C>>();

	/**
	 * Field to keep track at which index (order for requestCode) we are at.
	 */
	public int i = I_START;

	/**
	 * Constructor.
	 * 
	 * Without Up-navigation.
	 */
	public UiActionManager() {

	}

	/**
	 * Constructor. 
	 * 
	 * With Up-navigation.
	 * 
	 * @param clientClassName
	 */
	public UiActionManager(String clientClassName) {
		// Always add custom UpNav action using the HoneycombICSUpNavigator for
		// >=11.
		if (UiUtilities.hasAcceptableNativeActionBar()) {
			final NavigateUpUiAction<C> upAction = new NavigateUpUiAction<C>(
					clientClassName);
			upAction.setOrder(android.R.id.home);
			actions.add(upAction);
		}
	}

	/**
	 * Start managing a {@link UiAction}, if appropriate.
	 * 
	 * It will be added to a list of actions. 
	 * 
	 * @param action The action to be managed.
	 * @return The index (order for requestCode usage) to use for the next {@link UiAction}. Returns 0 if not appropriate.
	 */
	public int addAndManage(UiAction<C> action) {
		if (!action.isAppropriate(this)) {
			if (PoirotWindow.DEBUG)
				Log.d(TAG, "refused adding UiAction: " + action.toString());
			return 0;
		}
		int iOld = i;
		int nextOrder = action.setOrder(i);
		i = nextOrder;
		if (PoirotWindow.DEBUG)
			Log.d(TAG, "added UiAction: " + action.toString() + " order=" + i);
		return (actions.add(action) ? iOld : 0);
	}

	/**
	 * Destructor. Destroys and clears the managed {@link UiAction}s.
	 */
	public void destruct() {
		for (Iterator<UiAction<C>> iterator = actions.iterator(); iterator
				.hasNext();) {
			UiAction<C> action = iterator.next();
			iterator.remove();
			action.destruct();
		}
		actions.clear();

		for (Iterator<UiAction<C>> iterator = oldActions.iterator(); iterator
				.hasNext();) {
			UiAction<C> action = iterator.next();
			iterator.remove();
			action.destruct();
		}
		oldActions.clear();
	}

	/**
	 * Set all managed {@link UiAction}s as visible.
	 * 
	 * @param menu Menu (CAB) to set them visible for.
	 */
	public void enableAllActionItems(Menu menu) {
		for (UiAction<C> action : actions) {
			action.setActionItemVisible(menu, true);
		}
	}

	/**
	 * Set only the managed {@link UiAction}s that fit in with multiple selection CAB as visible. The rest invisible.
	 * 
	 * @param menu Menu (CAB) to set them visible for.
	 */
	public void enableOnlyActionItemsForMultipleSelection(Menu menu) {
		for (UiAction<C> action : actions) {
			if (action.shouldShowWhenMultipleSelected()) {
				action.setActionItemVisible(menu, true);
			} else {
				action.setActionItemVisible(menu, false);
			}
		}
	}

	/**
	 * Whether this other {@link UiAction} is already managed.
	 * 
	 * @param otherAction
	 * @return
	 */
	public boolean has(UiAction<C> otherAction) {
		// actions then
		for (Iterator<UiAction<C>> iterator = actions.iterator(); iterator
				.hasNext();) {
			UiAction<C> action = iterator.next();
			if (action.equals(otherAction)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Pass on what should happen in onActivityResult to the matching {@link UiAction}.
	 * 
	 * "Lifecycle" method onActivityResult from Activity should call-through to here.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @param context
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data,
			C context) {
		if ((actions.size() > 1 && UiUtilities.hasAcceptableNativeActionBar())
				|| (actions.size() > 0 && !UiUtilities
						.hasAcceptableNativeActionBar())) {// NavigateUpUiAction
															// is almost always
															// there
			for (UiAction<C> action : actions) {
				action.onActivityResult(requestCode, resultCode, data, context);
			}
		} else {
			for (UiAction<C> action : oldActions) {
				action.onActivityResult(requestCode, resultCode, data, context);
			}
		}
	}

	/**
	 * Pass on what should happen in onContextItemSelected to the matching {@link UiAction}, in case of multiple selection.
	 * 
	 * "Lifecycle" method onContextItemSelected from Activity should call-through to here.
	 * 
	 * @param item
	 * @param context
	 * @param mSelectedSessionPositionsUris
	 * @return
	 */
	public boolean onContextItemSelectedWithMultipleSelection(MenuItem item,
			C context, LinkedHashSet<Uri> mSelectedSessionPositionsUris) {
		for (UiAction<C> action : actions) {
			if (action.onContextItemSelectedWithMultipleSelection(item,
					context, mSelectedSessionPositionsUris))
				return true;
		}
		return false;
	}

	/**
	 * Pass on what should happen in onCreateOptionsMenu to the matching {@link UiAction}.
	 * 
	 * "Lifecycle" method onCreateOptionsMenu from Activity should call-through to here.
	 * 
	 * @param menu
	 * @param context
	 */
	public void onCreateOptionsMenu(Menu menu, C context) {
		for (UiAction<C> action : actions) {
			action.onCreateOptionsMenu(menu, context);
		}
	}

	/**
	 * For onCreateOptionsMenu, generate the category-alternative intent-based items.
	 * 
	 * @param menu
	 * @param context
	 * @param uri
	 * @param component
	 * @param extraCategories
	 */
	public void onCreateAlternativeOptionsMenu(Menu menu, C context, Uri uri, Class<? extends C> component, List<String> extraCategories){
		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, uri);
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		if(extraCategories!=null){
			for(String extraCategory : extraCategories){
				intent.addCategory(extraCategory);
			}
		}
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, UiAction.ORDER_SIXTH_OR_ALTERNATIVE,
				new ComponentName(context, component), null,
				intent, 0, null);
	}
	
	/**
	 * Pass on what should happen in onKeyUp to the matching {@link UiAction}.
	 * 
	 * "Lifecycle" method onKeyUp from Activity should call-through to here.
	 * 
	 * @param keyCode
	 * @param event
	 * @param context
	 * @return
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event, C context) {
		for (UiAction<C> action : actions) {
			if (action.onKeyUp(keyCode, event, context))
				return true;
		}
		return false;
	}

	/**
	 * Pass on what should happen in onOptionsItemSelected to the matching {@link UiAction}.
	 * 
	 * "Lifecycle" method onOptionsItemSelected from Activity should call-through to here.
	 * 
	 * @param item
	 * @param context
	 * @return
	 */
	public boolean onOptionsItemSelected(MenuItem item, C context) {
		for (UiAction<C> action : actions) {
			if (action.onOptionsItemSelected(item, context))
				return true;
		}
		return false;
	}

	/**
	 * Pass on what should happen in onPrepareOptionsMenu to the matching {@link UiAction}.
	 * 
	 * "Lifecycle" method onPrepareOptionsMenu from Activity should call-through to here.
	 * 
	 * @param menu
	 * @param context
	 */
	public void onPrepareOptionsMenu(Menu menu, C context) {
		for (UiAction<C> action : actions) {
			action.onPrepareOptionsMenu(menu, context);
		}
	}

	/**
	 * Again add all action items to the action bar. It will clear the action bar first.
	 * 
	 * Compat and LeftNavBar only. This is for such state-based action bars only, not for an action bar that queries which items it should show, like the native action bar.
	 */
	public void readdAllActionItems(C context) {
		context.getActivityHelper().clearCompatActionBarActionItems();
		context.getActivityHelper().clearLeftNavBarActionItems();

		for (UiAction<C> action : actions) {
			// LeftNavBar
			if (UiUtilities.hasHoneycombOrUp()
					&& UiUtilities.isGoogleTV(context)
					&& action.getNavBarZoneType() != UiAction.ZONE_NONE
					&& action.isAppropriate(((UiActionManager.ActionManagerUser) context)
							.getUiActionManager())) {
				action.addActionItemToLeftNavBar(context);
			}

			// CompatActionBar
			if (!UiUtilities.hasHoneycombOrUp()) {
				context.getActivityHelper().addActionItemToCompatActionBar(
						action);
			}
		}
	}

	/**
	 * Remove the {@link UiAction}s with this clientReference. In effect, this allows to remove actions that were added from one Activity or Fragment. Useful when the Fragment is swapped out.
	 * 
	 * @param clientReference
	 * @return
	 */
	public int remove(String clientReference) {
		// old actions first
		for (Iterator<UiAction<C>> iterator = oldActions.iterator(); iterator
				.hasNext();) {
			UiAction<C> action = iterator.next();
			if (action.getClientReference().equals(clientReference)) {
				iterator.remove();
				action.destruct();
			}
		}

		// actions then
		int count = 0;
		for (Iterator<UiAction<C>> iterator = actions.iterator(); iterator
				.hasNext();) {
			UiAction<C> action = iterator.next();
			if (action.getClientReference().equals(clientReference)) {
				oldActions.add(action);
				iterator.remove();
				if (PoirotWindow.DEBUG)
					Log.d(TAG, "removed UiAction: " + action.toString()
							+ " clientReference=" + clientReference);
				count++;
			}
		}

		return count;
	}

	/**
	 * Start counting the index (order for requestCode) from the start value again.
	 * 
	 * @return
	 */
	public int resetCount() {
		i = I_START;
		return i;
	}

	/**
	 * Get actions list for non-configuration instance state.
	 * 
	 * @return actions
	 */
	public ArrayList<UiAction<C>> retainNonConfigurationInstance() {
		return actions;
	}
	
	//TODO: incorporate onCreateContextMenu() both for single and multiple.
}
