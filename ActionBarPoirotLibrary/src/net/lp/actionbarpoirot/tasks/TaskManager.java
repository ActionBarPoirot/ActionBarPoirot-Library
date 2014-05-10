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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.RejectedExecutionException;

import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.exceptions.TaskExistsException;
import net.lp.actionbarpoirot.exceptions.TaskInvalidStateException;
import net.lp.actionbarpoirot.exceptions.TaskRefusedException;
import net.lp.actionbarpoirot.exceptions.TaskUnavailableException;
import net.lp.actionbarpoirot.util.UiUtilities;
import net.lp.actionbarpoirot.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Handles lifecycle for tasks and allows to attach {@link VisibleTask}s to {@link Activity}s. This is
 * the Strategy pattern versus {@link Activity}s Contexts.
 * 
 * @author pjv
 * 
 */
public class TaskManager<C extends FragmentActivity & PoirotWindow> {

	/*
	 * Key to save the task's class type in the instance state.
	 */
	public static final String KEY_TASK_CLASS = "task_class";

	/*
	 * Key to save the list of tasks in the instance state.
	 */
	public static final String KEY_TASK_LIST = "task_bundle_list";
	
	
	public static final String TAG = PoirotWindow.TAG + "TaskManager";

	/**
	 * List of managed {@link VisibleTask}s.
	 */
	public ArrayList<VisibleTask<?, ?, ?, ?, ?>> tasks = new ArrayList<VisibleTask<?, ?, ?, ?, ?>>();

	/**
	 * Add a task to this manager.
	 * 
	 * @param task
	 */
	public void addAndManage(VisibleTask<?, ?, ?, ?, ?> task) {
		tasks.add(task);
	}

	/**
	 * Destructor.
	 */
	public void destroy() {
		for (Iterator<VisibleTask<?, ?, ?, ?, ?>> iterator = tasks.iterator(); iterator
				.hasNext();) {
			VisibleTask<?, ?, ?, ?, ?> task = iterator.next();
			iterator.remove();
			cancelTask(task);
		}
		tasks.clear();
	}

	/**
	 * Whether the task is managed here.
	 * 
	 * @param other
	 * @param params
	 * @return
	 */
	public boolean existsTask(VisibleTask<?, ?, ?, ?, ?> other,
			String... params) {
		if (tasks == null){
			return false;
		}
		for (VisibleTask<?, ?, ?, ?, ?> task : tasks) {
			if (task.matches(other, params)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Perform actions when canceling a task, and cancel the task.
	 * 
	 * @param task
	 */
	public void onCancelTask(VisibleTask<?, ?, ?, ?, ?> task) {
		cancelTask(task);
	}

	/**
	 * Cancel a task.
	 * 
	 * @param task
	 */
	protected void cancelTask(VisibleTask<?, ?, ?, ?, ?> task) {
		if (task != null && task.getStatus() != VisibleTask.Status.FINISHED) {
			task.cancel(true);
			tasks.remove(task);
		}
	}

	/**
	 * Restore the per-instance state of the activity, and perform the work related to tasks.
	 * 
	 * @param context
	 * @param savedInstanceState
	 * @param lastNonConfigurationInstance
	 */
	public void restoreInstanceState(C context, Bundle savedInstanceState,
			ArrayList<VisibleTask<?, ?, ?, ?, ?>> lastNonConfigurationInstance) { 
		// TODO: should be pulled up (and then only expanded with all possible catches).
		
		// Restore still known tasks.
		restoreQuickRetainedTasks(lastNonConfigurationInstance);

		// Need not do the rest if we still had known tasks.
		if (tasks.size() > 0)
			return;

		restoreSavedTasks(context, savedInstanceState);
	}

	/**
	 * Retrieve the non-configuration instance data of the activity, and perform the work related to tasks.
	 * 
	 * @param lastNonConfigurationInstance
	 */
	protected void restoreQuickRetainedTasks(
			ArrayList<VisibleTask<?, ?, ?, ?, ?>> lastNonConfigurationInstance) {
		tasks = lastNonConfigurationInstance;
		if (tasks == null) {
			tasks = new ArrayList<VisibleTask<?, ?, ?, ?, ?>>();
		}

		for (VisibleTask<?, ?, ?, ?, ?> task : tasks) {
			try {
				// Not parallel but sequential, post-Honeycomb
				((VisibleTask) task)
						.execute(((VisibleTask<?, ?, ?, ?, ?>) task).getParam());
			} catch (RejectedExecutionException e) {// BUGSOLVED lp: #796532
				// No room for another AsyncTask, will just drop it.
				if (PoirotWindow.DEBUG)
					Log.w(VisibleTask.TAG,
							"Restoring task state, but no more room for a AsyncTask. Dropped. productid="
									+ ((VisibleTask<?, ?, ?, ?, ?>) task)
											.getParam());
			}
		}
	}

	/**
	 * Restore saved tasks from the per-instance state.
	 * 
	 * @param context
	 * @param savedInstanceState
	 */
	protected void restoreSavedTasks(C context, Bundle savedInstanceState) {
		// Restore saved tasks.
		ArrayList<Parcelable> savedTaskList = savedInstanceState
				.getParcelableArrayList(KEY_TASK_LIST);
		for (Parcelable taskBundle : savedTaskList) {
			try {
				// TODO BUG: Since setting the productId will only happen a
				// short while after the task is created/restored, in the mean
				// time the screen may be rotated and the task will tried to be
				// saved but there will be no productId so it will be omitted.
				// This makes the task go away if you rotate often and early on
				// in the progress. In Shelves this is slightly different
				// because checks for restoring are done first before
				// instantiating a new task object. Allthough I believe the same
				// bug is present in Shelves too. Not a deal breaker currently.
				VisibleTask task = null;
				try {
					task = (VisibleTask<?, ?, ?, ?, ?>) Class.forName(
							((Bundle) taskBundle).getString(KEY_TASK_CLASS))
							.newInstance();
				} catch (InstantiationException e) {
					// Because the class we are trying to instantiate might be a
					// non-static nested class, we need to pass in a first
					// parameter, even for the empty constructor, which is the
					// object of its outer class. The non-static class is in the
					// object of its outer class. Only a static nested class
					// would be directly accessible as above.
					task = (VisibleTask<?, ?, ?, ?, ?>) Class
							.forName(
									((Bundle) taskBundle)
											.getString(KEY_TASK_CLASS))
							.getConstructor(new Class[] { context.getClass() })
							.newInstance(context);
				}
				task.restart(context, ((Bundle) taskBundle));
				if (PoirotWindow.DEBUG)
					Log.d(VisibleTask.TAG, "Restoring task state");
				tasks.add(task);
			} catch (TaskExistsException e) {
				if (PoirotWindow.DEBUG) Log.d(VisibleTask.TAG, "Tried restoring task state but task exists");
				UiUtilities.showToast(context.getApplicationContext(), R.string.progress_failed_task_exists);
			} catch (TaskUnavailableException e) {
				
				 if (PoirotWindow.DEBUG) Log.d(VisibleTask.TAG, "Tried restoring task state but no room for another AsyncTask"); 
				 UiUtilities.showToast(context.getApplicationContext(), R.string.task_no_room);
				 
			} catch (TaskInvalidStateException e) {
				 if (PoirotWindow.DEBUG) Log.d(VisibleTask.TAG, "Tried restoring task state (but invalid state)"); //ignore
				 //e.printStackTrace();
				 
			} catch (TaskRefusedException e) {
				 if (PoirotWindow.DEBUG) Log.d(VisibleTask.TAG, "Tried restoring task state but product exists");
				 UiUtilities.showToast(context.getApplicationContext(), R.string.progress_failed_product_exists);
				 
			} catch (Throwable e) {
				// Other SpecificTaskDomainExceptions-Throwables
				// (SpecificAddTaskDomainExceptions,
				// SpecificExportTaskDomainExceptions or
				// SpecificSearchTaskDomainExceptions) than
				// ProductExistsException should not occur.
				// TaskExistsExceptions for Export should not occur.

				if (PoirotWindow.DEBUG)
					Log.d(TaskManager.TAG, "Other throwable");
			}
		}
	}

	/**
	 * Retain the non-configuration instance data of the activity, and perform the work related to tasks.
	 * 
	 * Optimization. //TODO: finish
	 * 
	 * @return tasks
	 */
	public ArrayList<VisibleTask<?, ?, ?, ?, ?>> retainNonConfigurationInstance() {
		return tasks;
	}

	/**
	 * Save the per-instance state of the activity, and perform the work related to tasks.
	 * 
	 * @param outState
	 */
	public void saveInstanceState(Bundle outState) {
		ArrayList<Parcelable> outTaskList = new ArrayList<Parcelable>();
		for (VisibleTask<?, ?, ?, ?, ?> task : tasks) {
			Bundle bundle = new Bundle();
			if (task.saveTask(bundle)) {
				bundle.putString(KEY_TASK_CLASS, task.getClass().getName());
				outTaskList.add(bundle);
			}
		}
		outState.putParcelableArrayList(KEY_TASK_LIST, outTaskList);
		tasks = new ArrayList<VisibleTask<?, ?, ?, ?, ?>>();
	}
}
