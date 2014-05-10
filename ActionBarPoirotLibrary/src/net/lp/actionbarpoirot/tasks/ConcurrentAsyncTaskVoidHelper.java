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

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * AsyncTask Helper to make parallel {@link AsyncTask}s possible again on >=11.
 * 
 * 
 * Aggregated from StackOverflow, possibly http://stackoverflow.com/questions/4068984/running-multiple-asynctasks-at-the-same-time-not-possible.
 */
public class ConcurrentAsyncTaskVoidHelper {//version with Void params, otherwise inlined	
	public static AsyncTask<Void, Void, Void> execute(AsyncTask<Void, Void, Void> as) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			as.execute();
		} else {
			executeHoneycomb(as);
		}
		return as;
	}
	
	/*
	 * Explicitly execute in parallel.
	 * 
	 * @param as async task
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) private static void executeHoneycomb(AsyncTask<Void, Void, Void> as){
		as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
}

