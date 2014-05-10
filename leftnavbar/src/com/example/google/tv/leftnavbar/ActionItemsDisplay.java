/*
 * Copyright (C) 2011 Google Inc.
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

package com.example.google.tv.leftnavbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Manages the visual cues related to an action item.
 */
class ActionItemsDisplay {

    private final Context mContext;
    private ViewGroup mView;
    private boolean mExpanded;

    ActionItemsDisplay(Context context, ViewGroup parent, TypedArray attributes) {
        mContext = context;
        createView(parent, attributes);
    }

    View getView() {
        return mView;
    }

    ActionItemsDisplay setVisible(boolean visible) {
        mView.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    boolean isVisible() {
        return mView.getVisibility() == View.VISIBLE;
    }

    ActionItemsDisplay setExpanded(boolean expanded) {
        mExpanded = expanded;
        refreshExpandedState();
        return this;
    }

    private void refreshExpandedState() {
        // "Show always" options.
        ViewGroup actionItemsContainer = getActionItemsContainer();
        for (int i = 0; i < actionItemsContainer.getChildCount(); ++i) {
            setActionItemExpanded(actionItemsContainer.getChildAt(i), mExpanded);
        }
    }
    
    public void addActionItem(MenuItem actionItem, View.OnClickListener onClickListener){
    	final View actionItemView = (ViewGroup) LayoutInflater.from(mContext).inflate(
                R.layout.leftnav_bar_action_item, null, false);
        final ImageView iconView = getActionItemIcon(actionItemView);
        iconView.setEnabled(true);
        iconView.setImageDrawable(actionItem.getIcon());
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        getActionItemTitle(actionItemView).setText(actionItem.getTitle());
        actionItemView.setClickable(true);
        actionItemView.setFocusable(true);
        actionItemView.setOnClickListener(onClickListener);
        setDuplicateParentState(getActionItemIcon(actionItemView));
        setDuplicateParentState(getActionItemTitle(actionItemView));

    	getActionItemsContainer().addView(actionItemView, 0);//Insert newest at the top (for bottom use: /*getActionItemsContainer().getChildCount()*/ ).
    }
    
    public void removeActionItems(){
    	getActionItemsContainer().removeAllViews();
    }

    private void createView(ViewGroup parent, TypedArray attributes) {
        mView = (ViewGroup) LayoutInflater.from(mContext).inflate(
                R.layout.leftnav_bar_action_items, parent, false);
    }

    /**
     * Forces a view to duplicate its parent state, working around a bug whereby the attribute only
     * works if set before the view is added to its parent.
     */
    private void setDuplicateParentState(View view) {
        view.setDuplicateParentStateEnabled(true);
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            return;
        }
        int index = parent.indexOfChild(view);
        parent.removeViewAt(index);
        parent.addView(view, index);
    }

    private static void setActionItemExpanded(View item, boolean expanded) {
        getActionItemTitle(item).setVisibility(expanded ? View.VISIBLE : View.GONE);
    }

    private static ImageView getActionItemIcon(View item) {
        return (ImageView) item.findViewById(R.id.icon);
    }

    private static TextView getActionItemTitle(View item) {
        return (TextView) item.findViewById(R.id.title);
    }

    private ViewGroup getActionItemsContainer() {
        return (ViewGroup) mView.findViewById(R.id.action_items_list);
    }
}
