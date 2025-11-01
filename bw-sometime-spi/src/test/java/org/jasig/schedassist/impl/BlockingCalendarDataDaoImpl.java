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

package org.jasig.schedassist.impl;

import org.bedework.sometime.ConflictExistsException;
import org.bedework.sometime.ICalendarDataDao;
import org.bedework.sometime.SchedulingException;
import org.bedework.sometime.model.AvailableBlock;
import org.bedework.sometime.model.AvailableSchedule;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.IScheduleVisitor;
import org.bedework.sometime.model.ScheduleOwner;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * {@link ICalendarDataDao} implementation useful for {@link DefaultAvailableScheduleReflectionServiceImplTest}.
 * Depends on a {@link CountDownLatch}; {@link #reflectAvailableSchedule(ScheduleOwner, AvailableSchedule)} 
 * calls await on the latch.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: BlockingCalendarDao.java $
 */
public class BlockingCalendarDataDaoImpl implements ICalendarDataDao {
	private final CountDownLatch latch;
	
	
	public BlockingCalendarDataDaoImpl(final CountDownLatch val) {
		latch = val;
	}

	/**
	 * @return the latch
	 */
	public CountDownLatch getLatch() {
		return latch;
	}

	@Override
	public Calendar getCalendar(final ICalendarAccount calendarAccount,
                              final Date startDate, final Date endDate) {
		return null;
	}

	@Override
	public VEvent getExistingAppointment(final ScheduleOwner owner,
                                       final AvailableBlock block) {
		return null;
	}

	@Override
	public VEvent createAppointment(final IScheduleVisitor visitor,
                                  final ScheduleOwner owner,
                                  final AvailableBlock block,
                                  final String eventDescription) {
		return null;
	}

	@Override
	public void cancelAppointment(IScheduleVisitor visitor, ScheduleOwner owner, VEvent event) {

	}

	@Override
	public VEvent joinAppointment(IScheduleVisitor visitor,
			ScheduleOwner owner, VEvent appointment)
			throws SchedulingException {

		return null;
	}

	@Override
	public VEvent leaveAppointment(IScheduleVisitor visitor,
			ScheduleOwner owner, VEvent appointment)
			throws SchedulingException {
		return null;
	}

	@Override
	public void checkForConflicts(ScheduleOwner owner, AvailableBlock block)
			throws ConflictExistsException {

	}

	/**
	 * Calls {@link CountDownLatch#await()}.
	 * (non-Javadoc)
	 * @see ICalendarDataDao#reflectAvailableSchedule(ScheduleOwner, AvailableSchedule)
	 */
	@Override
	public void reflectAvailableSchedule(ScheduleOwner owner,
			AvailableSchedule schedule) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void purgeAvailableScheduleReflections(ScheduleOwner owner,
			Date startDate, Date endDate) {

	}

}
