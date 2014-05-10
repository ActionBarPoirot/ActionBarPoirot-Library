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

import net.lp.actionbarpoirot.util.UiUtilities;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;

/**
 * A class that handles some common fragment-related functionality in the app, such as handling
 * the action mode contextual menu. This class provides functionality useful for both phones and tablets, and does
 * not require any Android 3.0-specific features.
 */
public class FragmentHelper {

	/**
	 * The {@link Fragment} to help.
	 */
	protected Fragment mFragment;

    /**
     * Factory method for creating {@link FragmentHelper} objects for a given fragment. Depending
     * on which device the app is running, either a basic helper or Honeycomb-specific helper will
     * be returned.
     */
    public static FragmentHelper createInstance(Fragment fragment) {
        return UiUtilities.hasHoneycombOrUp() ?
                new FragmentHelperHoneycomb(fragment) :
                new FragmentHelper(fragment);
    }

	/**
	 * Constructor
	 * 
	 * @param fragment The {@link Fragment} we are helping.
	 */
	public FragmentHelper(Fragment fragment){
		mFragment = fragment;
	}

	/**
	 * No-op
	 * 
	 * Start the CAB.
	 * 
	 * @param activity
	 * @param view
	 * @return
	 */
	public boolean startActionMode(FragmentActivity activity, View view){
        return false;
	}

	/**
	 * For a view group that is a {@link GridView}, setup a context menu or CAB.
	 * 
	 * @param gridView the view
	 * @param multiChoiceMode For CAB, whether to allow multi-choice mode.
	 */
	public void configureGridContextMenu(GridView gridView, boolean multiChoiceMode) {
		// Inform the grid we provide context menus for items
		gridView.setOnCreateContextMenuListener(mFragment);
	}

	/**
	 * For a view group that is a {@link ListView}, setup a context menu or CAB.
	 * 
	 * @param listView the view
	 * @param multiChoiceMode For CAB, whether to allow multi-choice mode.
	 */
	public void configureListContextMenu(ListView listView, boolean multiChoiceMode) {
		// Inform the list we provide context menus for items
		listView.setOnCreateContextMenuListener(mFragment);
	}

	/**
	 * No-op.
	 * 
	 * Help to load/restore per-instance state for fragment. Used in early lifecycle calls.
	 * 
	 * @param savedInstanceState
	 */
	public void onLoadInstanceState(Bundle savedInstanceState) {
		//nothing
	}

	/**
	 * No-op.
	 * 
	 * Help to save per-instance state for fragment.
	 * 
	 * @param outState
	 */
	public void onSaveInstanceState(Bundle outState) {
		//nothing
	}
}
