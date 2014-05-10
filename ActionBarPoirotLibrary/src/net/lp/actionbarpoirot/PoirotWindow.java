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

import net.lp.actionbarpoirot.tasks.TaskManager;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

/**
 * @author pjv
 * 
 * Implement this interface if you want to use the features of this library.
 *
 * @param <W> Your activity class, which should implement the support library FragmentActivity class and this interface.
 */
public interface PoirotWindow<W extends FragmentActivity & PoirotWindow> {

	/**
	 * Enable debug logging
	 */
	final static boolean DEBUG = false;
	
	final static String TAG = "ActionBarPoirot";

	/**
	 * Returns the processed intent of the activity. Rather than the raw (possibly new) intent from getIntent(), this intent has been filtered or slightly adapted to contain the right info and is kept at the activity level.
	 * 
	 * @return processed intent
	 */
	public Intent getViewIntent();

	/**
	 * Gets the task manager. Singleton pattern with the quirk that if the
	 * activity doesn't use tasks we want to keep taskManager null and not
	 * trigger the singleton creation.
	 * 
	 * @return task manager
	 */
	public TaskManager<W> getTaskManager();
}
