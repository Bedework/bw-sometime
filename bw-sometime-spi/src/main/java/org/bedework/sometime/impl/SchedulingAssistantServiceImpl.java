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

package org.bedework.sometime.impl;

import org.bedework.sometime.ICalendarDataDao;
import org.bedework.sometime.NoAppointmentExistsException;
import org.bedework.sometime.SchedulingAssistantService;
import org.bedework.sometime.SchedulingException;
import org.bedework.sometime.impl.events.AppointmentCancelledEvent;
import org.bedework.sometime.impl.events.AppointmentCreatedEvent;
import org.bedework.sometime.impl.events.AppointmentJoinedEvent;
import org.bedework.sometime.impl.events.AppointmentLeftEvent;
import org.bedework.sometime.impl.owner.AvailableScheduleDao;
import org.bedework.sometime.model.AvailableBlock;
import org.bedework.sometime.model.AvailableSchedule;
import org.bedework.sometime.model.AvailableVersion;
import org.bedework.sometime.model.IEventUtils;
import org.bedework.sometime.model.IScheduleVisitor;
import org.bedework.sometime.model.IVisibleScheduleBuilder;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.VisibleSchedule;
import org.bedework.sometime.model.VisibleWindow;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link SchedulingAssistantService}.
 * 
 * Note that the scheduleAppointment method is synchronized, as there is
 * no guarantees that the {@link CalendarDao} will reject event creation in case of conflict.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AvailableServiceImpl.java 2891 2010-11-11 16:19:39Z npblair $
 */
public final class SchedulingAssistantServiceImpl
				implements Logged, SchedulingAssistantService {

	private ICalendarDataDao calendarDao;
	private AvailableScheduleDao availableScheduleDao;
	private ApplicationEventPublisher applicationEventPublisher;
	private IVisibleScheduleBuilder visibleScheduleBuilder;
	private IEventUtils eventUtils;

	@Autowired
	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	/**
	 * @param availableScheduleDao the availableScheduleDao to set
	 */
	@Autowired
	public void setAvailableScheduleDao(final AvailableScheduleDao availableScheduleDao) {
		this.availableScheduleDao = availableScheduleDao;
	}
	/**
	 * @param calendarDataDao the calendarDataDao to set
	 */
	@Autowired
	public void setCalendarDataDao(final ICalendarDataDao calendarDataDao) {
		this.calendarDao = calendarDataDao;
	}
	/**
	 * @param visibleScheduleBuilder the visibleScheduleBuilder to set
	 */
	@Autowired
	public void setVisibleScheduleBuilder(
			IVisibleScheduleBuilder visibleScheduleBuilder) {
		this.visibleScheduleBuilder = visibleScheduleBuilder;
	}
	/**
	 * @param eventUtils the eventUtils to set
	 */
	@Autowired
	public void setEventUtils(IEventUtils eventUtils) {
		this.eventUtils = eventUtils;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.SchedulingAssistantService#getExistingAppointment(org.jasig.schedassist.model.AvailableBlock, org.jasig.schedassist.model.ScheduleOwner)
	 */
	@Override
	public VEvent getExistingAppointment(final AvailableBlock targetBlock,
                                       final ScheduleOwner owner) {
		return calendarDao.getExistingAppointment(owner, targetBlock);
	}
	
	@Override
	public VEvent getExistingAppointment(
          final AvailableBlock targetBlock,
          final ScheduleOwner owner,
					final IScheduleVisitor visitor) {
		final var event = getExistingAppointment(targetBlock, owner);
		if (event != null && this.eventUtils.isAttendingAsVisitor(event, visitor.getCalendarAccount())) {
			return event;
		}
		
		return null;
	}

	@Override
	public VisibleSchedule getVisibleSchedule(
          final IScheduleVisitor visitor,
          final ScheduleOwner owner) {
		final var windowBoundaries = calculateOwnerWindowBounds(owner);
		final var result = getVisibleSchedule(visitor,
																					owner,
																					windowBoundaries[0],
																					windowBoundaries[1]);
		return result;
	}

	@Override
	public VisibleSchedule getVisibleSchedule(final IScheduleVisitor visitor,
																						final ScheduleOwner owner,
																						final Date start,
																						final Date end) {
		Validate.notNull(start, "start parameter cannot be null");
		Validate.notNull(end, "start parameter cannot be null");

		final var windowBoundaries = calculateOwnerWindowBounds(owner);
		
		Date localStart = start;
		if (start.before(windowBoundaries[0]) ||
						start.after(windowBoundaries[1])) {
			if (debug()) {
				debug("ignoring submitted start for getVisibleSchedule: " + start + " (using windowBoundary of " + windowBoundaries[0] + ")");
			}
			localStart = windowBoundaries[0];
		}

		Date localEnd = end;
		if (end.after(windowBoundaries[1])) {
			if (debug()) {
				debug("ignoring submitted end for getVisibleSchedule: " + end + " (using windowBoundary of " + windowBoundaries[1] + ")");
			}
			localEnd = windowBoundaries[1];
		}

		Calendar calendar = calendarDao.getCalendar(owner.getCalendarAccount(), localStart, localEnd);
		AvailableSchedule schedule = availableScheduleDao.retrieve(owner);

		VisibleSchedule result = this.visibleScheduleBuilder.calculateVisibleSchedule(
				localStart,
				localEnd,
				calendar, 
				schedule, 
				owner,
				visitor);
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.SchedulingAssistantService#calculateVisitorConflicts(org.jasig.schedassist.model.IScheduleVisitor, org.jasig.schedassist.model.ScheduleOwner, java.util.Date, java.util.Date)
	 */
	@Override
	public List<AvailableBlock> calculateVisitorConflicts(
          final IScheduleVisitor visitor,
          final ScheduleOwner owner,
          final Date start, final Date end) {
		
		final var windowBoundaries = calculateOwnerWindowBounds(owner);
		
		Date localStart = start;
		if (start.before(windowBoundaries[0])) {
			localStart = windowBoundaries[0];
		}
		Date localEnd = end;
		if (end.after(windowBoundaries[1])) {
			localEnd = windowBoundaries[1];
		}

		final var availableSchedule =
						availableScheduleDao.retrieve(owner, localStart, localEnd);
		
		// get the VISITOR's Calendar data
		Calendar calendar = calendarDao.getCalendar(visitor.getCalendarAccount(), localStart, localEnd);
		
		// calculate a VisibleSchedule using the owner's availability but the Visitor's calendar data
		VisibleSchedule result = this.visibleScheduleBuilder.calculateVisitorConflicts(
				availableSchedule.getScheduleStartTime(),
				availableSchedule.getScheduleEndTime(),
				calendar, 
				availableSchedule, 
				owner.getPreferredMeetingDurations(), visitor);
		// return only the conflicts (the busy list)
		List<AvailableBlock> visitorConflicts = result.getBusyList();
		return visitorConflicts;
	}

	
	/**
	 * 
	 * @param owner
	 * @return an array containing 2 {@link Date}s that represent the start and end date/times per the owner's preference
	 */
	protected Date[] calculateOwnerWindowBounds(ScheduleOwner owner) {
		VisibleWindow window = owner.getPreferredVisibleWindow();

		Date now = new Date();
		Date startTime = DateUtils.addHours(now, window.getWindowHoursStart());
		Date boundary = DateUtils.addWeeks(now, window.getWindowWeeksEnd());
		
		return new Date[] { startTime, boundary };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.SchedulingAssistantService#cancelAppointment(org.jasig.schedassist.model.IScheduleVisitor, org.jasig.schedassist.model.ScheduleOwner, net.fortuna.ical4j.model.component.VEvent, org.jasig.schedassist.model.AvailableBlock, java.lang.String)
	 */
	@Override
	public void cancelAppointment(final IScheduleVisitor visitor,
                                final ScheduleOwner owner,
                                final VEvent event,
                                final AvailableBlock block,
																final String cancelReason) {
		if (owner.isSamePerson(visitor)) {
			warn("ignoring request to cancelAppointment for owner/visitor same person: " + owner);
			return;
		}

		final var availableAppointment =
						calendarDao.getExistingAppointment(owner, block);
		if (null == availableAppointment ||
						eventUtils.isAttendingMatch(availableAppointment, visitor, owner)) {
			// if this is a 1.0 appointment (no available version set) or visitor limit is 1
			if (null == availableAppointment.getProperty(AvailableVersion.AVAILABLE_VERSION) || block.getVisitorLimit() == 1) {
				calendarDao.cancelAppointment(visitor, owner, availableAppointment);
				if (null !=  applicationEventPublisher) {
					applicationEventPublisher.publishEvent(new AppointmentCancelledEvent(availableAppointment, owner, visitor, block, cancelReason));
				}
				return;
			} else {
				int currentVisitorCount = this.eventUtils.getScheduleVisitorCount(availableAppointment);
				if (currentVisitorCount == 1) {
					// this attendee is the last one, cancel
					calendarDao.cancelAppointment(visitor, owner, availableAppointment);
				} else {
					// there are other attendees, just leave
					calendarDao.leaveAppointment(visitor, owner, availableAppointment);
				}
				if (null !=  applicationEventPublisher) {
					applicationEventPublisher.publishEvent(new AppointmentLeftEvent(availableAppointment, owner, visitor, block));
				}
				return;
			}
		} else {
			error("no appointment found within block " + block);
			throw new NoAppointmentExistsException("no matching appointment can be found");
		}
	}

	@Override
	public VEvent scheduleAppointment(
					final IScheduleVisitor visitor,
					final ScheduleOwner owner,
					final AvailableBlock block,
					final String eventDescription) throws SchedulingException {
		if (owner.isSamePerson(visitor)) {
			warn("ignoring request to scheduleAppointment for owner/visitor same person: " + owner);
			return null;
		}
		
		// assert the requested block is within the owner's current schedule
		final AvailableBlock ownerPersistedBlock =
						availableScheduleDao.retrieveTargetBlock(
										owner, block.getStartTime());

		if (null == ownerPersistedBlock) {
			throw new SchedulingException(
							"requested time is not available in schedule: " + block);
		}

		if (ownerPersistedBlock.getVisitorLimit() == 1) {
			// check to see if there is a conflict
			calendarDao.checkForConflicts(owner, block);
			// no conflicts, create the appointment
			final VEvent event = calendarDao.createAppointment(
							visitor, owner, block, eventDescription);

			if (null !=  applicationEventPublisher) {
				applicationEventPublisher.publishEvent(
								new AppointmentCreatedEvent(
												event, owner, visitor, block, eventDescription));
			}
			return event;
		}

		// owner supports multiple visitors
		// look for an existing appointment
		final VEvent existingAppointment =
						calendarDao.getExistingAppointment(owner, block);

		if (null == existingAppointment) {
			// check to see if there is a conflict
			calendarDao.checkForConflicts(owner, block);
			// lets create it
			final VEvent event = calendarDao.createAppointment(
							visitor, owner, block, eventDescription);

			if (null !=  applicationEventPublisher) {
				applicationEventPublisher.publishEvent(
								new AppointmentJoinedEvent(event, owner, visitor, block));
			}
			return event;
		}

		// try to join if attendee count hasn't been exceeded
		final int visitorCount =
						eventUtils.getScheduleVisitorCount(existingAppointment);

		if (visitorCount >= ownerPersistedBlock.getVisitorLimit()) {
			// visitor limit exceeded
			throw new SchedulingException(
							"visitor limit for this appointment has been met");
		}

		// join!
		final VEvent event = calendarDao.joinAppointment(
						visitor, owner, existingAppointment);

		if (null !=  applicationEventPublisher) {
			applicationEventPublisher.publishEvent(
							new AppointmentJoinedEvent(event, owner, visitor, block));
		}
		return event;
	}

	/* =================================================
	 *                   Logged methods
	 * ================================================= */

	private final BwLogger logger = new BwLogger();

	@Override
	public BwLogger getLogger() {
		if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
			logger.setLoggedClass(getClass());
		}

		return logger;
	}
}
