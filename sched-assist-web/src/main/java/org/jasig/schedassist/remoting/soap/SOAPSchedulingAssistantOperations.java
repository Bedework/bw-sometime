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

package org.jasig.schedassist.remoting.soap;

import org.jasig.schedassist.CalendarAccountNotFoundException;
import org.jasig.schedassist.SchedulingAssistantService;
import org.jasig.schedassist.SchedulingException;
import org.jasig.schedassist.impl.owner.NotRegisteredException;
import org.jasig.schedassist.impl.visitor.NotAVisitorException;
import org.bedework.messaging.CancelAppointmentRequest;
import org.bedework.messaging.CancelAppointmentResponse;
import org.bedework.messaging.CreateAppointmentRequest;
import org.bedework.messaging.CreateAppointmentResponse;
import org.bedework.messaging.GetRelationshipsRequest;
import org.bedework.messaging.GetRelationshipsResponse;
import org.bedework.messaging.GetScheduleOwnerByIdRequest;
import org.bedework.messaging.GetScheduleOwnerByIdResponse;
import org.bedework.messaging.GetTargetAvailableBlockRequest;
import org.bedework.messaging.GetTargetAvailableBlockResponse;
import org.bedework.messaging.IsEligibleRequest;
import org.bedework.messaging.IsEligibleResponse;
import org.bedework.messaging.VisibleScheduleRequest;
import org.bedework.messaging.VisibleScheduleResponse;
import org.bedework.messaging.VisitorConflictsRequest;
import org.bedework.messaging.VisitorConflictsResponse;
import org.jasig.schedassist.model.InputFormatException;

/**
 * Interface defining remote version of {@link SchedulingAssistantService}.
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: RemoteAvailableService.java 2976 2011-01-25 14:04:08Z npblair $
 */
public interface SOAPSchedulingAssistantOperations {

	/**
	 * Simple method to determine eligibility for a visitor.
	 * 
	 * @param request
	 * @return
	 */
	IsEligibleResponse isEligible(IsEligibleRequest request);
	/**
	 * 
	 * @param request
	 * @return
	 * @throws NotRegisteredException 
	 * @throws SchedulingException 
	 */
	GetTargetAvailableBlockResponse getTargetAvailableBlock(GetTargetAvailableBlockRequest request) throws NotRegisteredException, SchedulingException;
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws NotAVisitorException
	 * @throws CalendarAccountNotFoundException 
	 * @throws NotRegisteredException 
	 */
	VisibleScheduleResponse getVisibleSchedule(final VisibleScheduleRequest query)
			throws NotAVisitorException, 
			CalendarAccountNotFoundException, NotRegisteredException;

	/**
	 * 
	 * @param request
	 * @return
	 * @throws NotAVisitorException
	 * @throws InputFormatException
	 * @throws SchedulingException
	 * @throws CalendarAccountNotFoundException 
	 * @throws NotRegisteredException 
	 */
	CreateAppointmentResponse scheduleAppointment(
			final CreateAppointmentRequest request)
			throws NotAVisitorException,
			InputFormatException, SchedulingException,
			CalendarAccountNotFoundException, NotRegisteredException;

	/**
	 * 
	 * @param request
	 * @return
	 * @throws NotAVisitorException
	 * @throws InputFormatException
	 * @throws SchedulingException
	 * @throws CalendarAccountNotFoundException 
	 * @throws NotRegisteredException 
	 */
	CancelAppointmentResponse cancelAppointment(CancelAppointmentRequest request)
			throws NotAVisitorException, 
			InputFormatException, SchedulingException,
			CalendarAccountNotFoundException, NotRegisteredException;
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws NotAVisitorException 
	 * @throws CalendarAccountNotFoundException 
	 */
	GetRelationshipsResponse getRelationships(GetRelationshipsRequest request) 
			throws NotAVisitorException, CalendarAccountNotFoundException;
	
	/**
	 * 
	 * @param request
	 * @return
	 * @throws NotAnOwnerException
	 * @throws CalendarAccountNotFoundException
	 * @throws NotRegisteredException 
	 */
	GetScheduleOwnerByIdResponse getScheduleOwnerById(GetScheduleOwnerByIdRequest request) throws CalendarAccountNotFoundException, NotRegisteredException;

	/**
	 * 
	 * @param request
	 * @return
	 * @throws NotAVisitorException 
	 * @throws NotRegisteredException 
	 */
	VisitorConflictsResponse getVisitorConflicts(VisitorConflictsRequest request) throws NotAVisitorException, NotRegisteredException;
}