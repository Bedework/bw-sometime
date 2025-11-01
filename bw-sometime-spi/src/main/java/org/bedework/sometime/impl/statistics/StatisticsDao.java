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

package org.bedework.sometime.impl.statistics;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bedework.sometime.impl.EventType;
import org.bedework.sometime.model.ScheduleOwner;

/**
 * Interface defines operations for retrieving {@link AppointmentEvent}s.
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: StatisticsDao.java 2321 2010-07-30 17:32:27Z npblair $
 */
public interface StatisticsDao {

	/**
	 * Get a count of all {@link AppointmentEvent}s that occurred each day between
	 * the start and end time arguments.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return a never null, but possibly empty {@link Map} of event counts per day
	 */
	List<DailyEventSummary> getEventCounts(Date startTime, Date endTime);
	/**
	 * Get a count of {@link AppointmentEvent}s that match the {@link EventType} argument
	 * that occurred each day between the start and end time arguments.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param eventType
	 * @return a never null, but possibly empty {@link Map} of event counts per day
	 */
	List<DailyEventSummary> getEventCounts(Date startTime, Date endTime, EventType eventType);
	/**
	 * Get all {@link AppointmentEvent}s between the start and end times.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(Date startTime, Date endTime);
	/**
	 * Get all {@link AppointmentEvent}s for the specified {@link ScheduleOwner}
	 * between the start and end times.
	 * 
	 * @param owner
	 * @param startTime
	 * @param endTime
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(ScheduleOwner owner, Date startTime, Date endTime);
	
	/**
	 * Get the {@link AppointmentEvent}s for the specified {@link ScheduleOwner} and {@link EventType}
	 * between the start and end times.
	 * 
	 * @param owner
	 * @param startTime
	 * @param endTime
	 * @param eventType
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(ScheduleOwner owner, Date startTime, Date endTime, EventType eventType);

	/**
	 * Get the {@link AppointmentEvent}s for the specified {@link ScheduleOwner} and {@link EventType}s
	 * between the start and end times.
	 * 
	 * @param owner
	 * @param startTime
	 * @param endTime
	 * @param eventTypes
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(ScheduleOwner owner, Date startTime, Date endTime, List<EventType> eventTypes);
	/**
	 * 
	 * @param owner
	 * @param visitorUsername
	 * @param startTime
	 * @param endTime
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(ScheduleOwner owner, String visitorUsername, Date startTime, Date endTime);
	
	/**
	 * 
	 * @param owner
	 * @param visitorUsername
	 * @param startTime
	 * @param endTime
	 * @param eventType
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(ScheduleOwner owner, String visitorUsername, Date startTime, Date endTime, EventType eventType);
	
	/**
	 * 
	 * @param owner
	 * @param visitorUsername
	 * @param startTime
	 * @param endTime
	 * @param eventTypes
	 * @return a never null, but possibly empty, {@link List} of {@link AppointmentEvent}s.
	 */
	List<AppointmentEvent> getEvents(ScheduleOwner owner, String visitorUsername, Date startTime, Date endTime, List<EventType> eventTypes);

}