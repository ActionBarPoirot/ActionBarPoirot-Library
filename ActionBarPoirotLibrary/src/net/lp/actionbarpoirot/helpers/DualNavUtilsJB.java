/*
 * Copyright (C) 2012 The Android Open Source Project
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
package net.lp.actionbarpoirot.helpers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;

/**
 * NavUtils that supports the phone/tablet Activity duality and doesn't read
 * just android.support.PARENT_ACTIVITY, but rather both
 * METADATA_PARENT_ACTIVITY_NAME_ON_TABLET and
 * METADATA_PARENT_ACTIVITY_NAME_ON_PHONE.
 * 
 * @note When the support library updates, I need to verify that I rewrite all
 *       mentions of PARENT_ACTIVITY.
 * @author pjv
 * @link 
 *       https://github.com/mastro/android-support-library-archive/blob/master/v4
 *       /src/jellybean/android/support/v4/app/NavUtilsJB.java
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class DualNavUtilsJB {
	public static Intent getParentActivityIntent(Activity activity) {
		return activity.getParentActivityIntent();
	}

	public static String getParentActivityName(Context context,
			ActivityInfo info) {
		return DualNavUtils.getParentActivityNameInner(context, info);
	}

	public static boolean navigateUpTo(Activity activity, Intent upIntent) {
		return activity.navigateUpTo(upIntent);
	}

	public static boolean shouldUpRecreateTask(Activity activity,
			Intent targetIntent) {
		return activity.shouldUpRecreateTask(targetIntent);
	}
}