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

import org.bedework.base.ToString;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.Preferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link ScheduleOwner}.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: DefaultScheduleOwnerImpl.java 2996 2011-01-27 17:38:54Z npblair $
 */
public class DefaultScheduleOwnerImpl
				extends ScheduleOwner {
	private Map<Preferences, String> preferences;
	
	/**
	 * 
	 * @param calendarAccount
	 * @param id
	 */
	public DefaultScheduleOwnerImpl(ICalendarAccount calendarAccount, long id) {
		super(calendarAccount);
		this.id = id;
		preferences = Preferences.getDefaultPreferences();
	}
	/**
	 * @param calendarAccount
	 * @param id
	 * @param preferences
	 */
	public DefaultScheduleOwnerImpl(ICalendarAccount calendarAccount, long id,
			Map<Preferences, String> preferences) {
		super(calendarAccount);
		this.id = id;
		this.preferences = preferences;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();

		result = prime * result +
						(int) (getId() ^ (getId() >>> 32));
		result = prime * result
				+ ((preferences == null) ? 0 : preferences.hashCode());

		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!super.equals(obj)) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final var other = (DefaultScheduleOwnerImpl) obj;
		if (getId() != other.getId()) {
			return false;
		}

		if (preferences == null) {
			if (other.preferences != null) {
				return false;
			}
		} else if (!preferences.equals(other.preferences)) {
			return false;
		}

		return true;
	}

	/** Add our stuff to the ToString object
	 *
	 * @param ts    ToString for result
	 */
	protected ToString toStringSegment(final ToString ts) {
		return super.toStringSegment(ts)
								.append("preferences", getPreferences())
								.append("calendarAccount", getCalendarAccount());
	}

	@Override
	public String toString() {
		return toStringSegment(new ToString(this))
						.toString();
	}

}
