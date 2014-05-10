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

import net.lp.actionbarpoirot.PoirotVisibleTaskWindow;
import net.lp.actionbarpoirot.PoirotWindow;
import net.lp.actionbarpoirot.R;
import net.lp.actionbarpoirot.exceptions.SpecificTaskDomainException;
import net.lp.actionbarpoirot.exceptions.TaskExistsException;
import net.lp.actionbarpoirot.exceptions.TaskInvalidStateException;
import net.lp.actionbarpoirot.exceptions.TaskUnavailableException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;

/**
 * A custom {@link AsyncTask} that shows up in the user interface.
 * 
 * @author pjv
 * 
 * @param <Result>
 */
public abstract class VisibleTask<Params, Progress, Result, E extends Throwable & SpecificTaskDomainException, C extends FragmentActivity & PoirotVisibleTaskWindow>
		extends UserTask<Params, Progress, Result> {

	/**
	 * Indicator of task progress: OK
	 */
	protected static final Integer PROGRESS_OK = 0;

	/**
	 * Indicator of task progress: Failed
	 */
	protected static final Integer PROGRESS_FAILED = 1;
	
	public static final String TAG = PoirotWindow.TAG + " VisibleTask";
	
	/**
	 * The context.
	 */
	protected C context;
	
	/**
	 * A lock.
	 */
	protected final Object mLock = new Object();
	
	/**
	 * The UI panel.
	 */
	protected View panel;
	
	/**
	 * The progress bar in the UI.
	 */
	protected ProgressBar progressBar;

	/**
	 * Constructor
	 * 
	 * Needed for reflection (object instantiation with default constructor)
	 * from onSearch() etc. (as well as all child constructors without params).
	 * Don't throw away too easily.
	 * 
	 */
	public VisibleTask() {
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context, but narrowed down to a
	 *            {@link CollectionistaComplicatedWindow}.
	 */
	public VisibleTask(C context) {
		this.context = context;
	}

	/**
	 * Updates the panel and returns it.
	 * 
	 * @return
	 */
	protected abstract View getPanel();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onCancelled()
	 */
	@Override
	public void onCancelled() {
		// Cancelled then hide
		context.hidePanel(getPanel(), !panelSlidesDown());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	public void onPostExecute(Result result) {
		// Done then hide
		context.hidePanel(getPanel(), !panelSlidesDown());
	}
	
	@Deprecated
	protected boolean hasDeterminateProgressBar(){
		// TODO: Later when used for the first time, this should be abstract and
		// implemented in every action.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	public void onPreExecute() {
		// Only if we haven't got the progressbar yet.
		if (progressBar == null) {
			// Find the progressbar and reset progress visually
			progressBar = (ProgressBar) getPanel().findViewById(R.id.progress);
			if (hasDeterminateProgressBar()){
				progressBar.setProgress(0);
			}

			// Set label
			setupView();

			// Find the cancel button and attach event
			final View cancelButton = getPanel().findViewById(R.id.no);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Cancel task
					context.getTaskManager().onCancelTask(VisibleTask.this);
				}
			});
		}

		if (PoirotWindow.DEBUG) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// pass
			}
		}

		// Show this panel
		context.showPanel(getPanel(), !panelSlidesDown());
	}

	/**
	 * Whether the UI panel slides down.
	 * 
	 * @return
	 */
	protected boolean panelSlidesDown() {
		return true;
	}

	/**
	 * Post-constructor for reconstruction after lifecycle interrupt.
	 * 
	 * @param context
	 *            The context, but narrowed down to a
	 *            {@link CollectionistaComplicatedWindow}.
	 * @param savedInstanceState
	 * @throws TaskUnavailableException
	 * @throws QueryInvalidException
	 * @throws TaskInvalidStateException
	 * @throws TaskExistsException
	 * @throws ProductExistsException
	 */
	public void restart(C context, Bundle savedInstanceState) throws E,
			TaskExistsException, TaskInvalidStateException,
			TaskUnavailableException {
		this.context = context;
	}

	/**
	 * Setup the View
	 */
	protected abstract void setupView();

	/**
	 * Post-constructor with param.
	 * 
	 * @param context
	 * @param productId
	 * @throws ProductExistsException
	 * @throws TaskExistsException
	 * @throws TaskUnavailableException
	 */
	public void start(C context, Params... params) throws E,
			TaskExistsException, TaskUnavailableException {
		this.context = context;
	}
}
