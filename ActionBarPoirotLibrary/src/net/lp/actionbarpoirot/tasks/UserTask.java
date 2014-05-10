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
package net.lp.actionbarpoirot.tasks;

import net.lp.actionbarpoirot.PoirotWindow;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * A custom {@link AsyncTask} used for user actions.
 * 
 * @author pjv
 * 
 * @param <String>
 * @param <Progress>
 * @param <Result>
 */
public abstract class UserTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	public static final String TAG = PoirotWindow.TAG + " UserTask";

	/**
	 * Returns the param for execute.
	 * 
	 * @return
	 */
	public abstract Params getParam();

	/**
	 * Compares with another active {@link UserTask}.
	 * 
	 * @param other
	 *            Other {@link UserTask}
	 * @param params
	 *            Any params.
	 * @return True if matches, otherwise false.
	 */
	public boolean matches(UserTask<?, ?, ?> other, String... params) {
		return (other.getClass().equals(this.getClass()) || other.equals(this))
				&& /* mWorker.mParams.equals(params) && */getStatus() != Status.FINISHED;
		// TODO: this for now also says only one VisibleTask of any sort can be
		// active at the same time, because we can only put one in outState.
	}

	/**
	 * Save task for lifecycle interrupt.
	 * 
	 * @param outState
	 */
	public abstract boolean saveTask(Bundle outState);
}
