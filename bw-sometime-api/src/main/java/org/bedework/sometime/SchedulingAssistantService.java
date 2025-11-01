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

package org.bedework.sometime;

import org.bedework.sometime.model.AvailableBlock;
import org.bedework.sometime.model.IScheduleVisitor;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.VisibleSchedule;
import org.bedework.sometime.model.VisibleWindow;

import net.fortuna.ical4j.model.component.VEvent;

import java.util.Date;
import java.util.List;

/**
 * Main service interface for Scheduling Assistant; provides methods
 * for exposing a {@link ScheduleOwner}'s {@link VisibleSchedule} and
 * appointment creation/cancellation by {@link IScheduleVisitor}s.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AvailableService.java 2348 2010-08-09 22:06:51Z npblair $
 */
public interface SchedulingAssistantService {
	
	/**
	 * Return the {@link VisibleSchedule} for the specified {@link ScheduleOwner}
	 * scoped to the {@link IScheduleVisitor}.
	 * Implementations should use the owner's {@link VisibleWindow} to determine
	 * the boundaries for the return value.
	 * 
	 * @param visitor
	 * @param owner
	 * @return an appropriate {@link VisibleSchedule} to display to the {@link IScheduleVisitor}
	 */
	VisibleSchedule getVisibleSchedule(IScheduleVisitor visitor, ScheduleOwner owner);
	
	/**
	 * Return the {@link VisibleSchedule} for the specified {@link ScheduleOwner}
	 * scoped to the {@link IScheduleVisitor}.
	 * If the start and end {@link Date} arguments go outside of the boundaries of the
	 * {@link ScheduleOwner}'s {@link VisibleWindow}, either value will be truncated to the limits
	 * prescribed by the {@link VisibleWindow}.
	 * 
	 * Ideally, this method can provide a subset of a {@link ScheduleOwner}'s {@link VisibleWindow}
	 * for start and end dates that fall completely inside the {@link VisibleWindow}.
	 * 
	 * @param visitor
	 * @param owner
	 * @param start
	 * @param end
	 * @return an appropriate {@link VisibleSchedule} to display to the {@link IScheduleVisitor}
	 */
	VisibleSchedule getVisibleSchedule(IScheduleVisitor visitor, ScheduleOwner owner, Date start, Date end);
	
	/**
	 * Return the {@link VEvent} for an existing Scheduling Assistant Appointment at the times
	 * specified by the targetBlock in the {@link ScheduleOwner}'s schedule.
	 * 
	 * @param targetBlock
	 * @param owner
	 * @return the existing scheduling assistant appointment, or null if no appointment exists
	 */
	VEvent getExistingAppointment(AvailableBlock targetBlock, ScheduleOwner owner);
	
	/**
	 * Return the {@link VEvent} for an existing Scheduling Assistant Appointment at the times
	 * specified by the targetBlock in the {@link ScheduleOwner}'s schedule with the specified
	 * {@link IScheduleVisitor} in attendance.
	 * 
	 * @param targetBlock
	 * @param owner
	 * @param visitor
	 * @return the existing scheduling assistant appointment, or null if no appointment exists, or the visitor is not in attendance
	 */
	VEvent getExistingAppointment(AvailableBlock targetBlock, ScheduleOwner owner, IScheduleVisitor visitor);
	
	/**
	 * Schedule an available appointment for {@link IScheduleVisitor}/{@link ScheduleOwner} within
	 * the times specified in the {@link AvailableBlock}. 
	 * 
	 * @param visitor
	 * @param owner
	 * @param block
	 * @param eventDescription
	 * @return the created/updated {@link VEvent} (never null on success)
	 * @throws SchedulingException
	 */
	VEvent scheduleAppointment(IScheduleVisitor visitor, ScheduleOwner owner, AvailableBlock block, String eventDescription) throws SchedulingException;
	
	/**
	 * Cancel an available appointment for {@link IScheduleVisitor}/{@link ScheduleOwner}.
	 * 
	 * @param visitor
	 * @param owner
	 * @param event
	 * @param block
	 */
	void cancelAppointment(IScheduleVisitor visitor, ScheduleOwner owner, VEvent event, AvailableBlock block, String cancelReason) throws SchedulingException;
	
	/**
	 * Retrieve the {@link IScheduleVisitor}'s calendar data and compare it to the {@link ScheduleOwner}'s availability.
	 * Return a (never null, but possibly empty) {@link List} of {@link AvailableBlock}s in the {@link ScheduleOwner}'s
	 * availability that the {@link IScheduleVisitor} has conflicts for in their calendar data.
	 * 
	 * @param visitor
	 * @param owner
	 * @param start
	 * @param end
	 * @return a never null, but possibly empty {@link List} of {@link AvailableBlock}s that conflict for the {@link IScheduleVisitor}.
	 */
	List<AvailableBlock> calculateVisitorConflicts(IScheduleVisitor visitor, ScheduleOwner owner, Date start, Date end);
}
