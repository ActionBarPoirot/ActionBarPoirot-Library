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
import java.util.Arrays;
import java.util.LinkedHashSet;

import net.lp.actionbarpoirot.actions.UiActionManager.ActionModeUser;
import net.lp.actionbarpoirot.actions.UiActionManager.MultiChoiceModeUser;
import net.lp.actionbarpoirot.helpers.ActivityHelper.ActivityHelperUser;
import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ListView;

/**
 * An extension of {@link FragmentHelper} that provides Android 3.0-specific
 * functionality for Honeycomb tablets. It thus requires API level 11.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
public class FragmentHelperHoneycomb extends FragmentHelper implements ActionMode.Callback, OnItemLongClickListener, MultiChoiceModeListener {

	/*
	 * Key to save the positions of selected items in the instance state.
	 */
	public static final String STATE_SELECTED_POSITIONS_URIS = "selected_positions_uris";
	
	/*
	 * Key to save the last position of a selected item in the instance state.
	 */
	public static final String STATE_LAST_SELECTED_POSITION_URI = "last_selected_position_uri";
	
	/**
	 * Shortcut to the fragment as {@link ActionModeUser}.
	 */
	protected ActionModeUser mActionModeUser;

	/**
	 * Shortcut to the fragment as {@link MultiChoiceModeUser}.
	 */
	protected MultiChoiceModeUser mMultiChoiceModeUser;
	
	/**
	 * The action mode.
	 */
	protected ActionMode mActionMode;
	
	/**
	 * The adapter view group.
	 */
	protected AbsListView mAdapter;
	
	/**
	 * The last position-based uri of a selected item.
	 */
	protected Uri lastSelectedPositionUri;
	
    /**
     * The position-based uris of the selected items.
     */
	protected LinkedHashSet<Uri> mSelectedPositionsUris = new LinkedHashSet<Uri>();//TODO: switch to ArrayList to save conversions (more optimal than fast list)?
	
    /**
     * Adds the position-based uri of a newly selected item.
     * 
     * @param selectedPosition
     */
	protected void addSelectedPosition(int selectedPosition){
    	lastSelectedPositionUri = mActionModeUser.getContextItemUri(selectedPosition);
        mSelectedPositionsUris.add(lastSelectedPositionUri);
    }
    
    /**
     * Remove a certain position-based uri of a previously selected item.
     * 
     * @param selectedPosition
     */
	protected void removeSelectedPosition(int selectedPosition){
        mSelectedPositionsUris.remove(mActionModeUser.getContextItemUri(selectedPosition));
        lastSelectedPositionUri = mSelectedPositionsUris.isEmpty() ? mActionModeUser.getContextItemUri(-1) : (Uri)mSelectedPositionsUris.toArray()[0];
    }
    
    /**
     * Set the position-based uri of the last selected item.
     * 
     * @param lastSelectedPosition
     */
	protected void setLastSelectedPosition(int lastSelectedPosition){
    	lastSelectedPositionUri = mActionModeUser.getContextItemUri(lastSelectedPosition);
    }

	/**
	 * Constructor
	 * 
	 * @param fragment The {@link Fragment} we are helping. It must implement {@link ActionModeUser} and optionally {@link MultiChoiceModeUser}.
	 */
	public FragmentHelperHoneycomb(Fragment fragment){
		super(fragment);
		
        if (!(fragment instanceof ActionModeUser)) {
        	//return;
            throw new ClassCastException("Fragment must implement callbacks.");
        }

        mActionModeUser = (ActionModeUser) fragment;
	}

	/* (non-Javadoc)
	 * @see android.view.ActionMode.Callback#onActionItemClicked(android.view.ActionMode, android.view.MenuItem)
	 */
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		if(mSelectedPositionsUris.size()>1){
			//Not used currently
			return mMultiChoiceModeUser.onContextItemSelectedWithMultipleSelection(item, mSelectedPositionsUris);
		} else {
			boolean result = mFragment.onContextItemSelected(item);
			if (!result) mode.finish();// Alternative intent action picked, so close the CAB
			return result;
		}
		//Most of the times, returns false, and then the intent in the menuItem will be executed.
	}

	/* (non-Javadoc)
	 * @see android.view.ActionMode.Callback#onCreateActionMode(android.view.ActionMode, android.view.Menu)
	 */
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    	//mActionModeUser.getUiActionManager().enableAllActionItems(menu);//Not used currently
		return createMenuSingle(menu);
	}

	/* (non-Javadoc)
	 * @see android.view.ActionMode.Callback#onDestroyActionMode(android.view.ActionMode)
	 */
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		//mAdapter.clearChoices();
		mAdapter.requestLayout();//BUGSOLVED: Selection would not disappear after contextual action bar. Is this because the spotlight remains in the background and we're using the garbled transparent background? Or maybe a bug in the ShelvesView?
        mSelectedPositionsUris.clear();
		mActionMode = null;
	}

	/* (non-Javadoc)
	 * @see android.view.ActionMode.Callback#onPrepareActionMode(android.view.ActionMode, android.view.Menu)
	 */
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		changeTitle(mode);
    	if(mSelectedPositionsUris.size()>1){
    		return createMenuMultiple(menu);
    		//mActionModeUser.getUiActionManager().enableOnlyActionItemsForMultipleSelection(menu);//Not used currently
    	}else{
    		return createMenuSingle(menu);
        	//mActionModeUser.getUiActionManager().enableAllActionItems(menu);//Not used currently
    	}
	}
	
	/**
	 * Start the CAB.
	 * 
	 * @param fragment
	 * @param view
	 * @return
	 */
	public boolean startActionMode(Fragment fragment, View view){
		if (mActionMode != null) {
            return false;
        }

        startActionModeInner(fragment);
        view.setSelected(true);
        return true;
	}
	
	/**
	 * Start the CAB using the ActionMode.Callback defined above
	 * 
	 * @param fragment
	 */
	protected void startActionModeInner(Fragment fragment){
        mActionMode = fragment.getActivity().startActionMode(this);
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.helpers.FragmentHelper#configureGridContextMenu(android.widget.GridView, boolean)
	 */
	@Override
	public void configureGridContextMenu(GridView gridView, boolean multiChoiceMode) {
		configureViewGroupContextMenu(gridView, multiChoiceMode);
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.helpers.FragmentHelper#configureListContextMenu(android.widget.ListView, boolean)
	 */
	@Override
	public void configureListContextMenu(ListView listView, boolean multiChoiceMode) {
		configureViewGroupContextMenu(listView, multiChoiceMode);
	}

	/*
	 * For the CAB, make this helper the context listener for the view group.
	 * 
	 * @param viewGroup
	 * @param multiChoiceMode Whether to allow multi-choice mode.
	 */
	protected void configureViewGroupContextMenu(AbsListView viewGroup, boolean multiChoiceMode) {
		mAdapter = viewGroup;
		viewGroup.setOnItemLongClickListener(this);
		if (multiChoiceMode){
	        ensureMultiChoiceModeUser();
	        
	        viewGroup.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
	        viewGroup.setMultiChoiceModeListener(this);
		} else {
			viewGroup.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		}
	}

	/**
	 * Make sure the fragment implements {@link MultiChoiceModeUser}.
	 */
	protected void ensureMultiChoiceModeUser() {
		if (!(mFragment instanceof MultiChoiceModeUser)) {
		    throw new ClassCastException("Fragment must implement callbacks.");
		}

		mMultiChoiceModeUser = (MultiChoiceModeUser) mFragment;
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		setLastSelectedPosition(position);
		return startActionMode(mFragment, view);
	}

	/* (non-Javadoc)
	 * @see android.widget.AbsListView.MultiChoiceModeListener#onItemCheckedStateChanged(android.view.ActionMode, int, long, boolean)
	 */
	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
        if (checked) {
            addSelectedPosition(position);
        } else {
        	removeSelectedPosition(position);
        }

		changeTitle(mode);
        if (mSelectedPositionsUris.size()>1) {
			//Deactivate the menu items that are not for multiple selection.
        	createMenuMultiple(mode.getMenu());
        	mActionModeUser.getUiActionManager().enableOnlyActionItemsForMultipleSelection(mode.getMenu());//Not used currently
        } else {
            //Activate all the menu items.
        	createMenuSingle(mode.getMenu());
        	mActionModeUser.getUiActionManager().enableAllActionItems(mode.getMenu());//Not used currently
        }
		
	}

	/**
	 * Create the CAB menu, for the case of single selection.
	 * 
	 * @param menu
	 * @return
	 */
	protected boolean createMenuSingle(Menu menu) {
		return createMenu(menu, false);
	}

	/**
	 * Create the CAB menu, for the case of multiple selection.
	 * 
	 * @param menu
	 * @return
	 */
	protected boolean createMenuMultiple(Menu menu) {
		return createMenu(menu, true);
	}

	/*
	 * Create the CAB menu.
	 * 
	 * @param menu
	 * @param multiple If we have multiple selected.
	 * @return
	 */
	protected boolean createMenu(Menu menu, boolean multiple) {
		ArrayList<Uri> uriList = null;
		if (multiple){
			uriList = new ArrayList<Uri>(Arrays.asList(mSelectedPositionsUris.toArray(new Uri[0])));
		}
		
		return mActionModeUser.onCreateContextMenuInner(menu, lastSelectedPositionUri, uriList);
	}
	
	/**
	 * Change the CAB title.
	 * 
	 * @param mode
	 */
	protected void changeTitle(ActionMode mode){
        int numSelected = mSelectedPositionsUris.size();
        mode.setTitle(mActionModeUser.getContextualActionBarTitle(numSelected));
	}
    
	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.helpers.FragmentHelper#onLoadInstanceState(android.os.Bundle)
	 */
	@Override
	public void onLoadInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState==null) return;
		
		final String lastSelectedPositionUriString = savedInstanceState.getString(STATE_LAST_SELECTED_POSITION_URI);
		lastSelectedPositionUri = lastSelectedPositionUriString==null ? null : Uri.parse(lastSelectedPositionUriString);
		mSelectedPositionsUris = new LinkedHashSet<Uri>((ArrayList<? extends Uri>) savedInstanceState.getParcelableArrayList(STATE_SELECTED_POSITIONS_URIS));
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.helpers.FragmentHelper#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_LAST_SELECTED_POSITION_URI, lastSelectedPositionUri==null ? null : lastSelectedPositionUri.toString());
		outState.putParcelableArrayList(STATE_SELECTED_POSITIONS_URIS, new ArrayList<Uri>(Arrays.asList(mSelectedPositionsUris.toArray(new Uri[0]))));
	}
}
