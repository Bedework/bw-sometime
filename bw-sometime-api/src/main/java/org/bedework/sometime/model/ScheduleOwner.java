/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 * <br/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <br/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.bedework.sometime.model;

import org.bedework.base.ToString;

import java.util.Map;

/**
 * All {@link ICalendarAccount} methods are implemented by delegating to the
 * internal {@link ICalendarAccount} required by the sole constructor.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AbstractScheduleOwner.java 2983 2011-01-26 21:52:38Z npblair $
 */
public class ScheduleOwner
				extends Dbentity<ScheduleOwner> {
	private String username;
	private String calendarUniqueId;
	private Map<String, String> preferences;

	// Non-db
	private final ICalendarAccount calendarAccount;

	/**
	 * 
	 * @param calendarAccount for the owner
	 */
	public ScheduleOwner(final ICalendarAccount calendarAccount) {
		this.calendarAccount = calendarAccount;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param val the username to set
	 */
	public void setUsername(final String val) {
		username = val;
	}

	/**
	 * @return the calendarUniqueId
	 */
	public String getCalendarUniqueId() {
		return calendarUniqueId;
	}

	/**
	 * @param val the calendarUniqueId to set
	 */
	public void setCalendarUniqueId(final String val) {
		calendarUniqueId = val;
	}

	public void setPreferences(final Map<String, String> val) {
		preferences = val;
	}

	/**
	 *
	 * @return a map of the owner's {@link String} values for their chosen {@link Preferences}
	 */
	public Map<String, String> getPreferences() {
		return preferences;
	}

	/**
	 *
	 * @return the {@link ICalendarAccount} that registered this account
	 */
	public ICalendarAccount getCalendarAccount() {
		return calendarAccount;
	}

	/**
	 *
	 * @param preference the key
	 * @param preferenceValue the value
	 */
	public void setPreference(final Preferences preference,
														final String preferenceValue) {
		final var key = preference.getKey();
		if (preferenceValue == null) {
			preferences.remove(key);
		} else {
			preferences.put(preference.getKey(), preferenceValue);
		}
	}

	/**
	 *
	 * @param preference to fetch
	 * @return the owner's {@link String} value for the specified {@link Preferences}
	 */
	public String getPreference(final Preferences preference) {
		return preferences.get(preference.getKey());
	}

	/**
	 * Short cut method to return the owner's value for {@link Preferences#LOCATION}.
	 * @return the owner's preferred location preference
	 */
	public final String getPreferredLocation() {
		return getPreference(Preferences.LOCATION);
	}

	/**
	 * Short cut method to return the owner's value for {@link Preferences#REMINDERS}.
	 * @return the owner's preferred {@link Reminders}
	 */
	public final Reminders getRemindersPreference() {
		final var emailPreferencesValue =
						getPreference(Preferences.REMINDERS);
		return Reminders.fromKey(emailPreferencesValue);
	}

	/**
	 * Short cut method to return the owner's value for {@link Preferences#DURATIONS}.
	 * @return the owner's preferred {@link MeetingDurations}
	 */
	public final MeetingDurations getPreferredMeetingDurations() {
		final var meetingDurationsValue =
						getPreference(Preferences.DURATIONS);
		return MeetingDurations.fromKey(meetingDurationsValue);
	}

	/**
	 * Short cut method to return the owner's value for {@link Preferences#VISIBLE_WINDOW}.
	 * @return the owner's preferred {@link VisibleWindow}
	 */
	public final VisibleWindow getPreferredVisibleWindow() {
		final var visibleWindowValue =
						getPreference(Preferences.VISIBLE_WINDOW);
		return VisibleWindow.fromKey(visibleWindowValue);
	}

	/**
	 * Short cut method to return the owner's min value for {@link Preferences#DURATIONS} as an integer.
	 * @return the owner's min value for {@link Preferences#DURATIONS} as an integer.
	 */
	public final int getPreferredMinimumDuration() {
		final var preferredDurations =
						getPreferredMeetingDurations();
		return preferredDurations.getMinLength();
	}

	/**
	 *
	 * @param visitor
	 * @return true if this instance and the {@link IScheduleVisitor} represent the same {@link ICalendarAccount}
	 */
	public final boolean isSamePerson(final IScheduleVisitor visitor) {
		if (null == visitor) {
			return false;
		}

		return calendarAccount.equals(
						visitor.getCalendarAccount());
	}

	/**
	 * Short cut method to determine whether this owner restricts the number of appointments
	 * that a visitor can create within a visible window.
	 *
	 * @see Preferences#MEETING_LIMIT
	 * @return true if the owner's value for {@link Preferences#MEETING_LIMIT} is not equal to -1
	 */
	public final boolean hasMeetingLimit() {
		final var prefValue =
						getPreference(Preferences.MEETING_LIMIT);
		return Integer.parseInt(prefValue) > 0;
	}

	/**
	 * Short cut method to check if an attendee count for an event exceeds the owner's preferences.
	 *
	 * @param visibleScheduleAttendingCount the number of available attendees for an event
	 * @return true if the arguments equals or exceeds this owner's value for {@link Preferences#MEETING_LIMIT}
	 */
	public final boolean isExceedingMeetingLimit(
					final int visibleScheduleAttendingCount) {
		final var prefValue =
						getPreference(Preferences.MEETING_LIMIT);
		final var limit = Integer.parseInt(prefValue);
		if (limit == -1) {
			return false;
		} else {
			return visibleScheduleAttendingCount >= limit;
		}
	}

	/**
	 *
	 * @return this owner's preference for {@link Preferences#DEFAULT_VISITOR_LIMIT}.
	 */
	public final int getPreferredDefaultVisitorLimit() {
		final var prefValue =
						getPreference(Preferences.DEFAULT_VISITOR_LIMIT);
		return Integer.parseInt(prefValue);
	}

	/**
	 * Short cut method backed by {@link Preferences#REFLECT_SCHEDULE}.
	 *
	 * @return true if the owner has enabled the schedule reflection preference.
	 */
	public final boolean isReflectSchedule() {
		final var prefValue =
						getPreference(Preferences.REFLECT_SCHEDULE);
		return Boolean.parseBoolean(prefValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		+ ((calendarAccount == null) ? 0 : calendarAccount.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if ((obj == null)||
						(getClass() != obj.getClass())) {
			return false;
		}

		final var other = (ScheduleOwner)obj;
		if (calendarAccount == null) {
			if (other.calendarAccount != null) {
				return false;
			}
		} else if (!calendarAccount.equals(other.calendarAccount)) {
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
								.append("username", getUsername())
								.append("calendarUniqueId", getCalendarUniqueId())
								.append("calendarAccount", calendarAccount);
	}

	@Override
	public String toString() {
		return toStringSegment(new ToString(this))
						.toString();
	}
}
