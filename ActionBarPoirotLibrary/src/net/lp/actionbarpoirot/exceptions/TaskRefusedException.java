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
package net.lp.actionbarpoirot.exceptions;

import net.lp.actionbarpoirot.tasks.UserTask;

/**
 * The {@link UserTask} refused to start. Invalid request.
 * 
 * @author pjv
 * 
 */
public abstract class TaskRefusedException extends Exception {

	private static final long serialVersionUID = 3117329150433407099L;

	public TaskRefusedException() {
		super();
	}

	public TaskRefusedException(Exception e) {
		super();
		initCause(e);
	}

	public TaskRefusedException(String message) {
		super(message);
	}

}
