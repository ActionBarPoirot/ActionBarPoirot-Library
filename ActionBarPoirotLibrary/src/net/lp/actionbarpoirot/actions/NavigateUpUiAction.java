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

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.helpers.ActivityHelper.ActivityHelperUser;
//import proguard.annotation.KeepName;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

//@KeepName
/**
 * A {@link UiAction} for the common affordance to navigate up from the current activity. Typically known as the left caret. 
 * 
 * @author pjv
 *
 * @param <C>
 */
public class NavigateUpUiAction<C extends FragmentActivity & PoirotWindow & ActivityHelperUser>
		extends UiAction<C> {

	public static final String TAG = PoirotWindow.TAG + "NavigateUpUiAction";

	public NavigateUpUiAction(String clientClassName) {
		super(clientClassName);
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#execute(android.support.v4.app.FragmentActivity)
	 */
	@Override
	public void execute(C context) {
		context.getActivityHelper().goHome();
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#getActionBarIcon()
	 */
	@Override
	protected int getActionBarIcon() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#isAppropriate()
	 */
	@Override
	public boolean isAppropriate(UiActionManager<C> uiActionManager) {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#onActivityResult(int, int, android.content.Intent, android.support.v4.app.FragmentActivity)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data,
			C context) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#onCreateOptionsMenu(android.view.Menu, android.support.v4.app.FragmentActivity)
	 */
	@Override
	public MenuItem onCreateOptionsMenu(Menu menu, C context) {
		// nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#onKeyUp(int, android.view.KeyEvent, android.support.v4.app.FragmentActivity)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event, C context) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			execute(context);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#onOptionsItemSelected(android.view.MenuItem, android.support.v4.app.FragmentActivity)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item, C context) {
		if (item.getItemId() == android.R.id.home) {
			execute(context);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see net.lp.actionbarpoirot.actions.UiAction#onPrepareOptionsMenu(android.view.Menu, android.support.v4.app.FragmentActivity)
	 */
	@Override
	public void onPrepareOptionsMenu(Menu menu, C context) {
		// nothing
	}

}
