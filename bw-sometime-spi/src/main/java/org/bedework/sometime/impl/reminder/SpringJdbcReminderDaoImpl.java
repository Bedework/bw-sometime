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

package org.bedework.sometime.impl.reminder;

import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bedework.sometime.model.AvailableBlock;
import org.jasig.schedassist.model.Database;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.ScheduleOwner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

/**
 * Spring JDBC implementation of internal {@link ReminderDao} interface.
 * 
 * @author Nicholas Blair
 * @version $Id: SpringJdbcReminderDaoImpl.java $
 */
@Repository
public class SpringJdbcReminderDaoImpl implements ReminderDao {
	private JdbcTemplate simpleJdbcTemplate;
	private DataFieldMaxValueIncrementer reminderIdSequence;
	private final Log LOG = LogFactory.getLog(this.getClass());
	private String identifyingAttributeName = "uid";
	private Database database;

	/**
	 * 
	 * @param ds the dataSource to set
	 */
	@Autowired
	public void setDataSource(final DataSource ds) {
		simpleJdbcTemplate = new JdbcTemplate(ds);
	}

	/**
	 * @param val the database to set
	 */
	@Autowired
	public void setDatabase(final Database val) {
		this.database = val;
		reminderIdSequence = database.reminderIdSequence();
	}

	/**
	 * 
	 * @param identifyingAttributeName
	 */
	@Value("${users.visibleIdentifierAttributeName:uid}")
	public void setIdentifyingAttributeName(String identifyingAttributeName) {
		this.identifyingAttributeName = identifyingAttributeName;
	}
	/**
	 * 
	 * @return the attribute used to commonly uniquely identify an account
	 */
	public String getIdentifyingAttributeName() {
		return identifyingAttributeName;
	}

	@Override
	@Transactional
	public IReminder createEventReminder(ScheduleOwner owner,
																			 ICalendarAccount recipient,
																			 AvailableBlock appointmentBlock,
																			 VEvent event,
																			 Date sendTime) {
		final String recipientIdentifier = getIdentifyingAttribute(recipient);
		long newReminderId = this.reminderIdSequence.nextLongValue();
		int rows = this.simpleJdbcTemplate.update("insert into reminders (reminder_id,owner_id,recipient,event_start,event_end,send_time) values (?,?,?,?,?,?)",
				newReminderId,
				owner.getId(),
				recipientIdentifier,
				appointmentBlock.getStartTime(),
				appointmentBlock.getEndTime(),
				sendTime);
		
		if (rows == 1) {
			ReminderImpl reminder = new ReminderImpl(newReminderId, owner, recipient, sendTime, event);
			return reminder;
		} else {
			LOG.error("failed to store reminder for " + owner + ", " + recipient);
			return null;
		}
	}
	
	@Override
	@Transactional
	public void deleteEventReminder(IReminder reminder) {
		int rows = this.simpleJdbcTemplate.update("delete from reminders where reminder_id=?", reminder.getReminderId());
		if (LOG.isDebugEnabled()) {
			LOG.debug("delete " + reminder + ", rows affected=" + rows);
		}
	}
	
	@Override
	public List<PersistedReminderImpl> getPendingReminders() {
		Date now = new Date();
		List<PersistedReminderImpl> persisted = this.simpleJdbcTemplate.query(
				"select * from reminders where send_time <= ?", 
				new PersistedReminderImplRowMapper(), 
				now);
		
		return persisted;
	}

	@Override
	public List<PersistedReminderImpl> getReminders(ScheduleOwner owner,
			AvailableBlock appointmentBlock) {
		List<PersistedReminderImpl> persisted = this.simpleJdbcTemplate.query(
				"select * from reminders where owner_id=? and event_start=? and event_end=?", 
				new PersistedReminderImplRowMapper(), 
				owner.getId(),
				appointmentBlock.getStartTime(),
				appointmentBlock.getEndTime());
		
		return persisted;
	}

	@Override
	public PersistedReminderImpl getReminder(ScheduleOwner owner,
			ICalendarAccount recipient, AvailableBlock appointmentBlock) {
		final String recipientIdentifier = getIdentifyingAttribute(recipient);
		List<PersistedReminderImpl> persisted = this.simpleJdbcTemplate.query(
				"select * from reminders where owner_id=? and recipient=? and event_start=? and event_end=?", 
				new PersistedReminderImplRowMapper(), 
				owner.getId(),
				recipientIdentifier,
				appointmentBlock.getStartTime(),
				appointmentBlock.getEndTime());
		
		PersistedReminderImpl p = DataAccessUtils.singleResult(persisted);
		return p;
	}
	
	/**
	 * 
	 * @param account
	 * @return the value of {@link #getIdentifyingAttributeName()} for the account
	 * @throws IllegalStateException if the account does not have a value for that attribute.
	 */
	protected String getIdentifyingAttribute(ICalendarAccount account) {
		final String ownerIdentifier = account.getAttributeValue(identifyingAttributeName);
		if (StringUtils.isBlank(ownerIdentifier)) {
			LOG.error(identifyingAttributeName + " attribute not present for calendarAccount " + account + "; this scenario suggests either a problem with the account, or a deployment configuration problem. Please set the 'users.visibleIdentifierAttributeName' appropriately.");
			throw new IllegalStateException(identifyingAttributeName + " attribute not present for calendarAccount " + account);
		}
		return ownerIdentifier;
	}
}
