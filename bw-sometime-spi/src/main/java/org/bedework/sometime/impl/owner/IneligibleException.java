/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.bedework.sometime.impl.owner;

import org.bedework.base.exc.BedeworkException;

import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.ScheduleOwner;

/**
 * Can be thrown by the {@link OwnerDao} interface if the
 * {@link ICalendarAccount} is not eligible to be an {@link ScheduleOwner}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: IneligibleException.java 1907 2010-04-14 21:12:15Z npblair $
 */
public class IneligibleException extends BedeworkException {

	private static final long serialVersionUID = 53706L;
	
	/**
	 * 
	 */
	public IneligibleException() {
	}

	/**
	 * @param message
	 */
	public IneligibleException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IneligibleException(final Throwable cause) {
		super(cause);
	}
}
