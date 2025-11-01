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

import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.Preferences;

import java.util.Map;

/**
 * Interface that defines the mechanism for converting a {@link ICalendarAccount}
 * to an {@link ScheduleOwner}.
 * Implementations can decide what logic is used to determine
 * which {@link ICalendarAccount}s are eligible for {@link ScheduleOwner} status.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OwnerDao.java 2124 2010-05-19 16:36:43Z npblair $
 */
public interface OwnerDao {

	/**
	 * Register a {@link ICalendarAccount} with the available application.
	 * Successful invocation results in a record of the user stored in the 
	 * available data source.
	 * 
	 * Returns the completed {@link ScheduleOwner} that corresponds.
	 * 
	 * @param calendarUser
	 * @return a {@link ScheduleOwner}
	 * @throws IneligibleException if the {@link ICalendarAccount} is not eligible
	 */
	ScheduleOwner register(ICalendarAccount calendarUser);
	
	/**
	 * Unregister the specified {@link ScheduleOwner}.
	 * 
	 * Successful invocation only removes {@link ScheduleOwner} account data and 
	 * and preferences; no appointments created will be touched.
	 * 
	 * @param owner
	 */
	void removeAccount(ScheduleOwner owner);
	
	/**
	 * Attempt to locate an existing {@link ScheduleOwner} for the specified
	 * {@link ICalendarAccount}, returning null if not registered.
	 * 
	 * @param calendarUser
	 * @return
	 */
	ScheduleOwner locateOwner(ICalendarAccount calendarUser);
	
	/**
	 * Attempt to locate an existing {@link ScheduleOwner} by  
	 * internal id, returning null if not found.
	 * 
	 * @see ScheduleOwner#getId()
	 * @param internalId
	 * @return
	 */
	ScheduleOwner locateOwnerByAvailableId(long internalId);
	
	/**
	 * Set the value for the specified {@link Preferences} for the {@link ScheduleOwner}.
	 * 
	 * The returned {@link ScheduleOwner} will have the updated preferences.
	 * 
	 * @param owner
	 * @param preference
	 * @param value
	 * @return
	 */
	ScheduleOwner updatePreference(ScheduleOwner owner, Preferences preference, String value);
	
	/**
	 * Retrieve a single preference value for the given {@link ScheduleOwner}.
	 * 
	 * @param owner
	 * @param preference
	 * @return
	 */
	String retreivePreference(ScheduleOwner owner, Preferences preference);
	
	/**
	 * Remove the {@link Preferences} value from this {@link ScheduleOwner}'s account
	 * completely.
	 * The net effect of this call should make future calls to {@link #retreivePreference(ScheduleOwner, Preferences)}
	 * for the same {@link Preferences} return the system's default value.
	 * 
	 * @param owner
	 * @param preference
	 */
	ScheduleOwner removePreference(ScheduleOwner owner, Preferences preference);
	
	/**
	 * Retrieve all stored preferences->values for the given {@link ScheduleOwner}.
	 * 
	 * @param owner
	 * @return
	 */
	Map<Preferences, String> retrievePreferences(ScheduleOwner owner);
	
	/**
	 * 
	 * @param internalId
	 * @return
	 */
	String lookupUsername(long internalId);
	
	/**
	 * 
	 * @param internalId
	 * @return
	 */
	String lookupUniqueId(long internalId);
	
}
