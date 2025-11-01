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

package org.bedework.sometime.impl.events;

import org.bedework.sometime.model.AvailableSchedule;
import org.bedework.sometime.model.ScheduleOwner;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when a schedule owner changes their available schedule.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AvailableScheduleChangedEvent.java 1911 2010-04-14 21:15:46Z npblair $
 */
public class AvailableScheduleChangedEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 53706L;
	private ScheduleOwner owner;
	
	/**
	 * 
	 * @param schedule
	 * @param owner
	 */
	public AvailableScheduleChangedEvent(AvailableSchedule schedule, ScheduleOwner owner) {
		super(schedule);
		this.owner = owner;
	}

	/**
	 * @return the owner
	 */
	public ScheduleOwner getOwner() {
		return owner;
	}

	/**
	 * 
	 * @return
	 */
	public final AvailableSchedule getAvailableSchedule() {
		return (AvailableSchedule) getSource();
	}
}
