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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bedework.sometime.ICalendarDataDao;
import org.bedework.sometime.impl.owner.AvailableScheduleDao;
import org.bedework.sometime.impl.owner.OwnerDao;
import org.bedework.sometime.model.AvailableSchedule;
import org.bedework.sometime.model.ScheduleOwner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

/**
 * Default implementation of {@link AvailableScheduleReflectionService}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: DefaultAvailableScheduleReflectionServiceImpl.java $
 */
public class DefaultAvailableScheduleReflectionServiceImpl implements AvailableScheduleReflectionService {

	protected static final Log LOG = LogFactory.getLog(DefaultAvailableScheduleReflectionServiceImpl.class);
	private JdbcTemplate simpleJdbcTemplate;
	private ICalendarDataDao calendarDataDao;
	private AvailableScheduleDao availableScheduleDao;
	private OwnerDao ownerDao;
	private TransactionTemplate transactionTemplate;
	private boolean supportsForUpdate = false;
	/**
	 * @param dataSource the dataSource to set
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
	}
	/**
	 * @param platformTransactionManager the platformTransactionManager to set
	 */
	@Autowired
	public void setPlatformTransactionManager(
			PlatformTransactionManager platformTransactionManager) {
		this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
		this.transactionTemplate.setIsolationLevel(Isolation.READ_COMMITTED.value());
	}	
	/**
	 * @param calendarDataDao the calendarDataDao to set
	 */
	@Autowired
	public void setCalendarDataDao(ICalendarDataDao calendarDataDao) {
		this.calendarDataDao = calendarDataDao;
	}
	/**
	 * @param availableScheduleDao the availableScheduleDao to set
	 */
	@Autowired
	public void setAvailableScheduleDao(AvailableScheduleDao availableScheduleDao) {
		this.availableScheduleDao = availableScheduleDao;
	}
	/**
	 * @param ownerDao the ownerDao to set
	 */
	@Autowired
	public void setOwnerDao(OwnerDao ownerDao) {
		this.ownerDao = ownerDao;
	}
	/**
	 * @param supportsForUpdate the supportsForUpdate to set
	 */
	public void setSupportsForUpdate(boolean supportsForUpdate) {
		this.supportsForUpdate = supportsForUpdate;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.AvailableScheduleReflectionService#reflectAvailableSchedule(org.jasig.schedassist.model.ScheduleOwner)
	 */
	@Override
	public void reflectAvailableSchedule(ScheduleOwner owner) {
		boolean success = processScheduleOwner(owner);
		if (!success) {
			LOG.warn("failed to process owner " + owner);
		}
	}

	/**
	 * First attempts to obtain the semaphore for the specified {@link ScheduleOwner}.
	 * If successful, then retrieve's the owner's current {@link AvailableSchedule} and
	 * passes it to {@link CalendarDao#reflectAvailableSchedule(ScheduleOwner, AvailableSchedule)}.
	 * 
	 * @param owner
	 * @return true if able to execute the operation, false if failed to obtain the lock
	 */
	protected boolean processScheduleOwner(final ScheduleOwner owner) {
		// add owner to lock table
		addOwnerToLockTableIfNotPresent(owner);
		boolean result = this.transactionTemplate.execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				// obtain "lock" for owner				
				if (lock(owner)) {
					// reflect schedule
					AvailableSchedule schedule = availableScheduleDao.retrieve(owner);
					calendarDataDao.reflectAvailableSchedule(owner, schedule);
					return true;
				} else {
					return false;
				}
			}
		});
		return result;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.AvailableScheduleReflectionService#reflectAvailableSchedule(long)
	 */
	@Override
	public void reflectAvailableSchedule(long ownerId) {
		ScheduleOwner owner = this.ownerDao.locateOwnerByAvailableId(ownerId);
		reflectAvailableSchedule(owner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.AvailableScheduleReflectionService#purgeReflections(org.jasig.schedassist.model.ScheduleOwner, java.util.Date, java.util.Date)
	 */
	@Override
	public void purgeReflections(ScheduleOwner owner, Date start, Date end) {
		this.calendarDataDao.purgeAvailableScheduleReflections(owner, start, end);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.AvailableScheduleReflectionService#purgeReflections(long, java.util.Date, java.util.Date)
	 */
	@Override
	public void purgeReflections(long ownerId, Date start, Date end) {
		ScheduleOwner owner = this.ownerDao.locateOwnerByAvailableId(ownerId);
		purgeReflections(owner, start, end);
	}

	/**
	 * Store a row in the reflect_locks table for the specified {@link ScheduleOwner}, if
	 * there isn't a row already.
	 * This row will be used as a semaphore in {@link #processScheduleOwner(ScheduleOwner)}.
	 * 
	 * @param owner
	 */
	void addOwnerToLockTableIfNotPresent(ScheduleOwner owner) {
		List<Long> locks = this.simpleJdbcTemplate.query("select owner_id from reflect_locks where owner_id = ?",
				new SingleColumnRowMapper<Long>(Long.class),
				owner.getId());
		Long lock = DataAccessUtils.singleResult(locks);
		if (lock == null) {
			int rows = this.simpleJdbcTemplate.update("insert into reflect_locks (owner_id) values (?)", owner.getId());
			if (LOG.isDebugEnabled()) {
				LOG.debug("inserted " + rows + " row into reflect_locks for owner id " + owner.getId());
			}
		}
	}
	/**
	 * Attempt to acquire the semaphore for the specified {@link ScheduleOwner}.
	 * 
	 * Only really functional within a transaction with appropriate Isolation.
	 * 
	 * @param owner
	 * @return true if the lock is set, false if cannot acquire lock
	 */
	boolean lock(ScheduleOwner owner) {
		StringBuilder sql = new StringBuilder();
		sql.append("select owner_id from reflect_locks where owner_id = ?");
		if (supportsForUpdate) {
			sql.append(" for update nowait");
		}
		try {
			this.simpleJdbcTemplate.query(sql.toString(), 
				new SingleColumnRowMapper<Long>(),
				owner.getId());
			LOG.debug("lock acquired for owner " + owner);
			return true;
		} catch (CannotAcquireLockException e) {
			LOG.warn("lock attempt failed for owner " + owner);
			return false;
		}
	}
}
