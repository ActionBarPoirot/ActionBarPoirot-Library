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
package net.lp.actionbarpoirot.helpers;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Implementation of TaskStackBuilder that can call Honeycomb APIs.
 * TaskStackBuilder that supports the phone/tablet Activity duality and uses
 * DualNavUtils instead of NavUtils.
 * 
 * @note When the support library updates, I need to verify that I rewrite all
 *       calls to NavUtils.
 * @author pjv
 * @link 
 *       https://github.com/mastro/android-support-library-archive/blob/master/v4
 *       /src/honeycomb/android/support/v4/app/TaskStackBuilderHoneycomb.java
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DualTaskStackBuilderHoneycomb {
	public static PendingIntent getActivitiesPendingIntent(Context context,
			int requestCode, Intent[] intents, int flags) {
		return PendingIntent
				.getActivities(context, requestCode, intents, flags);
	}
}