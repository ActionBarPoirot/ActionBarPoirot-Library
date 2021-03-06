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

/**
 * There is no room for another AsyncTask.
 * 
 * @author pjv
 * 
 */
public class TaskUnavailableException extends TaskRefusedException {

	private static final long serialVersionUID = -4919040242092020057L;

	public TaskUnavailableException() {
		super();
	}

	public TaskUnavailableException(Exception e) {
		super();
		initCause(e);
	}

	public TaskUnavailableException(String message) {
		super(message);
	}

}
