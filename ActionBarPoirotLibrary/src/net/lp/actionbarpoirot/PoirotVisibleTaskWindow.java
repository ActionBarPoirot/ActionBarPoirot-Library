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
package net.lp.actionbarpoirot;

import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * @author pjv
 * 
 * Implement this interface in the activities where you'll be using {@VisibleTask}s.
 *
 * @param <W> Your activity class, which should implement the support library FragmentActivity class and this interface.
 */
public interface PoirotVisibleTaskWindow<W extends FragmentActivity & PoirotVisibleTaskWindow> extends PoirotWindow<W> {

	/**
	 * Hides the panel or other view that holds the progress bar for a task.
	 * 
	 * @param panel The view to hide.
	 * @param slideDown When hiding, slide downwards or upwards?
	 */
	public void hidePanel(View panel, boolean slideDown);

	/**
	 * Show the panel or other view that holds the progress bar for a task.
	 * 
	 * @param panel The view to show.
	 * @param slideUp When showing, slide upwards or downwards?
	 */
	public void showPanel(View panel, boolean slideUp);
}
