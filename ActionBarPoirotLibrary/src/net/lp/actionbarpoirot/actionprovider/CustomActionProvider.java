/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*   
 * 	 Copyright (C) 2014 pjv (and others)
 * 
 * 	 This file has been incorporated from its previous location into, 
 *   and is part of, ActionBarPoirot, and the license has been adapted.
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

package net.lp.actionbarpoirot.actionprovider;

import net.lp.actionbarpoirot.R;
import net.lp.actionbarpoirot.actionprovider.ActivityChooserModel.OnChooseActivityListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;

/**
 * This is a provider for the various actions to perform custom actions.
 *
 * @see ActionProvider
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public abstract class CustomActionProvider extends ActionProvider {

    /**
     * Listener for the event of selecting a target.
     */
    public interface OnTargetSelectedListener {

        /**
         * Called when a target has been selected. The client can
         * decide whether to perform some action before the action is
         * actually performed.
         * <p>
         * <strong>Note:</strong> Modifying the intent is not permitted and
         *     any changes to the latter will be ignored.
         * </p>
         * <p>
         * <strong>Note:</strong> You should <strong>not</strong> handle the
         *     intent here. This callback aims to notify the client that an
         *     action is being performed, so the client can update the UI
         *     if necessary.
         * </p>
         *
         * @param source The source of the notification.
         * @param intent The intent for launching the chosen target.
         * @return The return result is ignored. Always return false for consistency.
         */
        public boolean onTargetSelected(CustomActionProvider source, Intent intent);
    }

    /**
     * The default for the maximal number of activities shown in the sub-menu.
     */
    private static final int DEFAULT_INITIAL_ACTIVITY_COUNT = 6;

    /**
     * The the maximum number activities shown in the sub-menu.
     */
    private int mMaxShownActivityCount = DEFAULT_INITIAL_ACTIVITY_COUNT;

    /**
     * Listener for handling menu item clicks.
     */
    private final MenuItemOnMenuItemClickListener mOnMenuItemClickListener =
        new MenuItemOnMenuItemClickListener();

    /**
     * Context for accessing resources (themed).
     */
    protected final Context mContext;

    /**
     * Activity for doing startActivity() with.
     */
    protected final Activity mActivity;

    /**
     * The name of the file with history data.
     */
    private String mHistoryFileName;

    /**
     * Listener for target being selected.
     */
    private OnTargetSelectedListener mOnTargetSelectedListener;

    /**
     * Listener for choosing an activity.
     */
    private OnChooseActivityListener mOnChooseActivityListener;

	private String mLabel;

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public CustomActionProvider(Activity context, Context themedContext, String defaultHistoryFileName) {
        super(context);
        mActivity = context;
        mContext = themedContext;
        setHistoryFileName(defaultHistoryFileName);
    }

    /**
     * Sets a listener to be notified when a target has been selected.
     * The listener can optionally decide to handle the selection and
     * not rely on the default behavior which is to launch the activity.
     * <p>
     * <strong>Note:</strong> If you choose the backing history file
     *     you will still be notified in this callback.
     * </p>
     * @param listener The listener.
     */
    public void setOnTargetSelectedListener(OnTargetSelectedListener listener) {
        mOnTargetSelectedListener = listener;
        setActivityChooserPolicyIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateActionView() {
        // Create the view and set its data model.
        ActivityChooserModel dataModel = ActivityChooserModel.get(mContext, mHistoryFileName);
        ActivityChooserView activityChooserView = new ActivityChooserView(mActivity);
        activityChooserView.setActivityChooserModel(dataModel);

        // Lookup and set the expand action icon.
        TypedValue outTypedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.actionModeShareDrawable, outTypedValue, true);//TODO: should change the name of that attribute
        Drawable drawable = mContext.getResources().getDrawable(outTypedValue.resourceId);
        activityChooserView.setExpandActivityOverflowButtonDrawable(drawable);
        
        // Lookup and set the expand action main label.
        String label = getLabel();
        activityChooserView.setMainLabel(label);
        activityChooserView.setProvider(this);

        // Set content description.
        activityChooserView.setDefaultActionButtonContentDescription(//TODO: should change the names of those strings
                R.string.shareactionprovider_share_with_application);
        activityChooserView.setExpandActivityOverflowButtonContentDescription(
                R.string.shareactionprovider_share_with);

        return activityChooserView;
    }

	/**
	 * Lookup the action's main label.
	 */
	protected abstract String getLabel();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSubMenu() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        // Clear since the order of items may change.
        subMenu.clear();

        ActivityChooserModel dataModel = ActivityChooserModel.get(mContext, mHistoryFileName);
        PackageManager packageManager = mContext.getPackageManager();

        final int expandedActivityCount = dataModel.getActivityCount();
        final int collapsedActivityCount = Math.min(expandedActivityCount, mMaxShownActivityCount);

        // Populate the sub-menu with a sub set of the activities.
        for (int i = 0; i < collapsedActivityCount; i++) {
            ResolveInfo activity = dataModel.getActivity(i);
            subMenu.add(0, i, i, activity.loadLabel(packageManager))
                .setIcon(activity.loadIcon(packageManager))
                .setOnMenuItemClickListener(mOnMenuItemClickListener);
        }

        if (collapsedActivityCount < expandedActivityCount) {
            // Add a sub-menu for showing all activities as a list item.
            SubMenu expandedSubMenu = subMenu.addSubMenu(Menu.NONE, collapsedActivityCount,
                    collapsedActivityCount,
                    mContext.getString(R.string.activity_chooser_view_see_all));
            for (int i = 0; i < expandedActivityCount; i++) {
                ResolveInfo activity = dataModel.getActivity(i);
                expandedSubMenu.add(0, i, i, activity.loadLabel(packageManager))
                    .setIcon(activity.loadIcon(packageManager))
                    .setOnMenuItemClickListener(mOnMenuItemClickListener);
            }
        }
    }

    /**
     * Sets the file name of a file for persisting the history which
     * history will be used for ordering targets. This file will be used
     * for all view created by {@link #onCreateActionView()}. Defaults to
     * {@link #DEFAULT_HISTORY_FILE_NAME}. Set to <code>null</code>
     * if history should not be persisted between sessions.
     * <p>
     * <strong>Note:</strong> The history file name can be set any time, however
     * only the action views created by {@link #onCreateActionView()} after setting
     * the file name will be backed by the provided file. Therefore, if you want to
     * use different history files for specific types of content, every time
     * you change the history file {@link #setHistoryFileName(String)} you must
     * call {@link android.app.Activity#invalidateOptionsMenu()} to recreate the
     * action view. You should <strong>not</strong> call
     * {@link android.app.Activity#invalidateOptionsMenu()} from
     * {@link android.app.Activity#onCreateOptionsMenu(Menu)}."
     * <p>
     * <code>
     * private void doShare(Intent intent) {
     *     if (IMAGE.equals(intent.getMimeType())) {
     *         mShareActionProvider.setHistoryFileName(SHARE_IMAGE_HISTORY_FILE_NAME);
     *     } else if (TEXT.equals(intent.getMimeType())) {
     *         mShareActionProvider.setHistoryFileName(SHARE_TEXT_HISTORY_FILE_NAME);
     *     }
     *     mShareActionProvider.setIntent(intent);
     *     invalidateOptionsMenu();
     * }
     * <code>
     *
     * @param historyFile The history file name.
     */
    public void setHistoryFileName(String historyFile) {
        mHistoryFileName = historyFile;
        setActivityChooserPolicyIfNeeded();
    }

    /**
     * Sets an intent with information about the action. Here is a
     * sample for constructing an intent:
     * <p>
     * <pre>
     * <code>
     *  Intent shareIntent = new Intent(Intent.ACTION_SEND);
     *  shareIntent.setType("image/*");
     *  Uri uri = Uri.fromFile(new File(getFilesDir(), "foo.jpg"));
     *  shareIntent.putExtra(Intent.EXTRA_STREAM, uri.toString());
     * </pre>
     * </code>
     * </p>
     *
     * @param intent The intent.
     *
     * @see Intent#ACTION_SEND
     * @see Intent#ACTION_SEND_MULTIPLE
     */
    public void setIntent(Intent intent) {
        ActivityChooserModel dataModel = ActivityChooserModel.get(mContext,
            mHistoryFileName);
        dataModel.setIntent(intent);
    }
    
	/**
	 * Set the action's main label.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		mLabel = label;
	}

    /**
     * Reusable listener for handling item clicks.
     */
    private class MenuItemOnMenuItemClickListener implements OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
	        // Request progress bar
        	//mContext.setProgressBarIndeterminateVisibility(true);//Not the right context
        	
            ActivityChooserModel dataModel = ActivityChooserModel.get(mContext,
                    mHistoryFileName);
            final int itemId = item.getItemId();
            Intent launchIntent = dataModel.chooseActivity(itemId);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                mContext.startActivity(launchIntent);
            }
            return true;
        }
    }

    /**
     * Set the activity chooser policy of the model backed by the current
     * history file if needed which is if there is a registered callback.
     */
    private void setActivityChooserPolicyIfNeeded() {
        if (mOnTargetSelectedListener == null) {
            return;
        }
        if (mOnChooseActivityListener == null) {
            mOnChooseActivityListener = new ActivityChooserModelPolicy();
        }
        ActivityChooserModel dataModel = ActivityChooserModel.get(mContext, mHistoryFileName);
        dataModel.setOnChooseActivityListener(mOnChooseActivityListener);
    }

    /**
     * Policy that delegates to the {@link OnTargetSelectedListener}, if such.
     */
    private class ActivityChooserModelPolicy implements OnChooseActivityListener {
        @Override
        public boolean onChooseActivity(ActivityChooserModel host, Intent intent) {
            if (mOnTargetSelectedListener != null) {
                mOnTargetSelectedListener.onTargetSelected(
                        CustomActionProvider.this, intent);
            }
            return false;
        }
    }
    
    /**
     * Listener for visibility changes.
     * 
     * @author pjv
     *
     * 
     */
    public interface SubUiVisibilityListener {
        public void onSubUiVisibilityChanged(boolean isVisible);
    }
    
    public void setSubUiVisibilityListener(SubUiVisibilityListener listener) {
        mSubUiVisibilityListener = listener;
    }
    
    /**
     * Notify the system that the visibility of an action view's sub-UI such as
     * an anchored popup has changed. This will affect how other system
     * visibility notifications occur.
     */
    public void subUiVisibilityChanged(boolean isVisible) {
        if (mSubUiVisibilityListener != null) {
            mSubUiVisibilityListener.onSubUiVisibilityChanged(isVisible);
        }
    }
    
    private SubUiVisibilityListener mSubUiVisibilityListener;
}
