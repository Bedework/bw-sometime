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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bedework.sometime.ICalendarAccountDao;
import org.jasig.schedassist.model.Database;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.Preferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Spring JDBC backed implementation of {@link OwnerDao}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: SpringJDBCOwnerDaoImpl.java 3100 2011-02-28 18:41:40Z npblair $
 */
@Service("ownerDao")
public class SpringJDBCOwnerDaoImpl implements
		OwnerDao {

	private final Log LOG = LogFactory.getLog(this.getClass());
	private JdbcTemplate simpleJdbcTemplate;
	private DataFieldMaxValueIncrementer ownerIdSequence;
	private OwnerAuthorization ownerAuthorization;
	private ICalendarAccountDao calendarAccountDao;
	private String identifyingAttributeName = "uid";
	private Database database;
	
	/**
	 * @param dataSource the dataSource to set
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		simpleJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * @param database the database to set
	 */
	@Autowired
	public void setDatabase(final Database database) {
		this.database = database;
		ownerIdSequence = database.ownerIdSequence();
	}

	/**
	 * @param ownerAuthorization the ownerAuthorization to set
	 */
	@Autowired
	public void setOwnerAuthorization(OwnerAuthorization ownerAuthorization) {
		this.ownerAuthorization = ownerAuthorization;
	}
	/**
	 * @param calendarAccountDao the calendarAccountDao to set
	 */
	@Autowired
	public void setCalendarAccountDao(@Qualifier("composite") ICalendarAccountDao calendarAccountDao) {
		this.calendarAccountDao = calendarAccountDao;
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

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#register(org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Transactional
	@Override
	public ScheduleOwner register(final ICalendarAccount calendarAccount)
			throws IneligibleException {
		Validate.notNull(calendarAccount, "ICalendarAccount argument cannot be null");
		if (!ownerAuthorization.isEligible(calendarAccount)) {
			throw new IneligibleException("user is not eligible for owner role: " + calendarAccount);
		}
		ScheduleOwner internal = internalLookup(calendarAccount);
		if (null == internal) {
			ScheduleOwner newOwner = internalStoreAsOwner(calendarAccount);
			return newOwner;
		} else {
			return internal;
		}
	}	

	@Transactional
	@Override
	public void removeAccount(ScheduleOwner owner) {
		Validate.notNull(owner, "ScheduleOwner argument cannot be null");
		// 1. remove all entries from adhoc authz table
		this.simpleJdbcTemplate.update("delete from owner_adhoc_authz where owner_username = ?", owner.getCalendarAccount().getUsername());
		// 2. delete from owners table (will cascade to preferences and schedules)
		this.simpleJdbcTemplate.update("delete from owners where internal_id = ?", owner.getId());
		LOG.warn("removed owner: " + owner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#locateOwner(org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Transactional
	@Override
	public ScheduleOwner locateOwner(final ICalendarAccount calendarAccount) {
		Validate.notNull(calendarAccount, "ICalendarAccount argument cannot be null");
		ScheduleOwner owner = internalLookup(calendarAccount);
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#updatePreference(org.jasig.schedassist.model.ScheduleOwner, org.jasig.schedassist.model.Preferences, java.lang.String)
	 */
	@Transactional
	@Override
	public ScheduleOwner updatePreference(final ScheduleOwner owner, final Preferences preference, 
			final String value) {
		Validate.notNull(owner, "ScheduleOwner argument cannot be null");
		replacePreference(owner, preference, value);
		ScheduleOwner stored = internalLookup(owner.getCalendarAccount());
		return stored;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#retreivePreference(org.jasig.schedassist.model.ScheduleOwner, org.jasig.schedassist.model.Preferences)
	 */
	@Override
	public String retreivePreference(final ScheduleOwner owner, final Preferences preference) {
		Validate.notNull(owner, "ScheduleOwner argument cannot be null");
		Map<Preferences, String> prefs = retrievePreferences(owner);
		return prefs.get(preference);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#retrievePreferences(org.jasig.schedassist.model.ScheduleOwner)
	 */
	@Override
	public Map<Preferences, String> retrievePreferences(final ScheduleOwner owner) {
		Validate.notNull(owner, "ScheduleOwner argument cannot be null");
		List<PersistencePreference> stored = this.simpleJdbcTemplate.query(
				"select * from preferences where owner_id = ?", 
				new PersistencePreferenceRowMapper(),
				owner.getId());
		
		Map<Preferences, String> results = new HashMap<Preferences, String>();
		for(PersistencePreference single : stored) {
			Preferences pref = Preferences.fromKey(single.getPreferenceKey());
			if (null == pref) {
				// ignore
				LOG.debug("no matching preference for " + single);
			} else {
				results.put(pref, single.getPreferenceValue());
			}
		}
		
		// verify results contains a value for all preferences
		for(Preferences preference : Preferences.values()) {
			if (null == results.get(preference)) {
				results.put(preference, preference.getDefaultValue());
			}
		}
		
		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#removePreference(org.jasig.schedassist.model.ScheduleOwner, org.jasig.schedassist.model.Preferences)
	 */
	@Transactional
	@Override
	public ScheduleOwner removePreference(ScheduleOwner owner, Preferences preference) {
		Validate.notNull(owner, "ScheduleOwner argument cannot be null");
		int rowsUpdated = this.simpleJdbcTemplate.update(
				"delete from preferences where owner_id = ? and preference_key = ?",
				owner.getId(),
				preference.getKey());
		
		LOG.info("deleted preference " + preference.getKey() + " for owner " + owner.getId() + ", rowsUpdated: " + rowsUpdated);
		ScheduleOwner stored = internalLookup(owner.getCalendarAccount());
		return stored;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#lookupUniqueId(long)
	 */
	@Override
	public String lookupUniqueId(long id) {
		List<String> uniqueIdResults = this.simpleJdbcTemplate.query(
				"select external_unique_id from owners where internal_id = ?",
				new SingleColumnRowMapper<String>(),
				id);
		
		return (String) DataAccessUtils.singleResult(uniqueIdResults);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#lookupUsername(long)
	 */
	@Override
	public String lookupUsername(long id) {
		List<String> usernameResults = this.simpleJdbcTemplate.query(
				"select username from owners where internal_id = ?",
				new SingleColumnRowMapper<String>(),
				id);
		
		return (String) DataAccessUtils.singleResult(usernameResults);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.owner.OwnerDao#locateOwnerByAvailableId(long)
	 */
	@Transactional
	@Override
	public ScheduleOwner locateOwnerByAvailableId(long internalId) {
		ScheduleOwner result = internalLookup(internalId);
		return result;
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
	
	/**
	 * 
	 * @param owner
	 * @return
	 */
	protected List<OwnerDefinedRelationship> internalRetrieveRelationships(final ScheduleOwner owner) {
		List<OwnerDefinedRelationship> relationships = this.simpleJdbcTemplate.query(
				"select * from owner_adhoc_authz where owner_username = ?", 
				new OwnerDefinedRelationshipRowMapper(), 
				owner.getCalendarAccount().getUsername());
		return relationships;
	}
	
	
	/**
	 * Executes "insert into owners (owner_id, unique_id, username) values (?, ?, ?)".
	 * 
	 * @param calendarUser
	 * @return the new {@link ScheduleOwner}
	 */
	protected ScheduleOwner internalStoreAsOwner(final ICalendarAccount calendarUser) {
		long newOwnerId = ownerIdSequence.nextLongValue();
		final String visibleIdentifier = getIdentifyingAttribute(calendarUser);
		int rows = this.simpleJdbcTemplate.update(
				"insert into owners (internal_id, external_unique_id, username) values (?, ?, ?)", 
				newOwnerId, 
				calendarUser.getCalendarUniqueId(), 
				visibleIdentifier);
		DefaultScheduleOwnerImpl newOwner = new DefaultScheduleOwnerImpl(calendarUser, newOwnerId);
		LOG.info("stored new owner: " + newOwner + "; rows updated: " + rows);
		return newOwner;
	}

	/**
	 * 
	 * @param calendarAccount
	 * @return
	 * @throws IncorrectResultSizeDataAccessException if more than 1 corresponding {@link ScheduleOwner} is stored
	 */
	protected ScheduleOwner internalLookup(final ICalendarAccount calendarAccount) {
		final String uniqueId = calendarAccount.getCalendarUniqueId();
		final String visibleIdentifier = getIdentifyingAttribute(calendarAccount);
		List<PersistenceScheduleOwner> matching =  this.simpleJdbcTemplate.query(
				"select * from owners where external_unique_id = ? or username = ?",
				new PersistenceScheduleOwnerRowMapper(), 
				uniqueId,
				visibleIdentifier);
		PersistenceScheduleOwner internal = (PersistenceScheduleOwner) DataAccessUtils.singleResult(matching);
		if (null != internal){
			// verify the internal record matches calendarAccount
			internal = updateScheduleOwnerIfNecessary(calendarAccount, internal);
			// trust the passed in CalendarUser is legit, only make a ScheduleOwner out of it
			DefaultScheduleOwnerImpl owner = new DefaultScheduleOwnerImpl(calendarAccount, internal.getId());
			Map<Preferences, String> prefs = retrievePreferences(owner);
			owner.setPreferences(prefs);
		
			LOG.debug("found owner " + owner);
			
			return owner;
		} else {
			return null;
		}
	}
	
	/**
	 * MUST only be called on related accounts.
	 * If the external_unique_id or username fields in the {@link PersistenceScheduleOwner} do not match the values in the {@link ICalendarAccount}
	 * argument, update the database to match the {@link ICalendarAccount}.
	 * 
	 * @param calendarAccount
	 * @param persisted
	 * @return the {@link PersistenceScheduleOwner}, with any updates applied
	 */
	protected PersistenceScheduleOwner updateScheduleOwnerIfNecessary(ICalendarAccount calendarAccount, PersistenceScheduleOwner persisted) {
		final String accountVisibleIdentifier = getIdentifyingAttribute(calendarAccount);
		if (!persisted.getCalendarUniqueId().equals(calendarAccount.getCalendarUniqueId()) && persisted.getUsername().equals(accountVisibleIdentifier)) {
			LOG.warn("PersistedScheduleOwner(username=" + persisted.getUsername() + ") has different calendarUniqueId than calendarAccount; persisted: " + persisted.getUsername() + ", new value: " + accountVisibleIdentifier);
			int rows = this.simpleJdbcTemplate.update("update owners set external_unique_id=? where username=?", 
					calendarAccount.getCalendarUniqueId(),
					calendarAccount.getUsername());
			persisted.setCalendarUniqueId(calendarAccount.getCalendarUniqueId());
			if (rows == 1) {
				LOG.warn("change to calendarUniqueId persisted for " + persisted);
			} else {
				LOG.error("failed to persist calendarUniqueId update for " + calendarAccount + ", rows " + rows);
				throw new ScheduleOwnerUpdateFailureException("failed to persist calendarUniqueId update for " + calendarAccount);
			}
		} else if (!persisted.getUsername().equals(accountVisibleIdentifier) && persisted.getCalendarUniqueId().equals(calendarAccount.getCalendarUniqueId())) {
			final String oldUsername = persisted.getUsername();
			
			LOG.warn("PersistedScheduleOwner(calendarUniqueId=" + persisted.getCalendarUniqueId() + ") has different username than calendarAccount; persisted: " + oldUsername + ", new value: " + accountVisibleIdentifier);
			int rows = this.simpleJdbcTemplate.update("update owners set username=? where external_unique_id=?", 
					accountVisibleIdentifier,
					calendarAccount.getCalendarUniqueId());
			persisted.setUsername(calendarAccount.getUsername());
			if (rows == 1) {
				LOG.warn("change to username persisted for " + persisted);
			} else {
				LOG.error("failed to persist username update for " + calendarAccount + ", rows " + rows);
				throw new ScheduleOwnerUpdateFailureException("failed to persist username update for " + calendarAccount);
			}
			
			rows = this.simpleJdbcTemplate.update("update owner_adhoc_authz set owner_username=? where owner_username=?",
					accountVisibleIdentifier,
					oldUsername);
		 	if (rows > 0) {
		 		LOG.warn("updated " + rows + " rows in owner_adhoc_authz for " + persisted);
		    }
		}
		
		return persisted;
	}
	
	
	/**
	 * 
	 * @param internalId
	 * @return the corresponding {@link ScheduleOwner}, or null if non-existent
	 * @throws IncorrectResultSizeDataAccessException if more than 1 corresponding {@link ScheduleOwner} is stored
	 */
	protected ScheduleOwner internalLookup(final long internalId)  {
		List<PersistenceScheduleOwner> matching =  this.simpleJdbcTemplate.query(
				"select * from owners where internal_id = ?",
				new PersistenceScheduleOwnerRowMapper(), 
				internalId);
		
		PersistenceScheduleOwner internal = (PersistenceScheduleOwner) DataAccessUtils.singleResult(matching);
		if (null != internal){
			
			// ask calendarUserDao for more information
			ICalendarAccount calendarAccount = calendarAccountDao.getCalendarAccount(this.identifyingAttributeName, internal.getUsername());
			if (null == calendarAccount) {
				// try by uniqueId
				calendarAccount = calendarAccountDao.getCalendarAccountFromUniqueId(internal.getCalendarUniqueId());
				if (null != calendarAccount) {
					// we failed a lookup by username but succeeded on uniqueid
					// means our record needs update
					internal = updateScheduleOwnerIfNecessary(calendarAccount, internal);
				} else {
					LOG.error("schedule owner record found, but calendarUserDao reports user not found for " + internal);
					// return null instead of returning an incomplete record
					return null;
				}
			}
			
			DefaultScheduleOwnerImpl owner = new DefaultScheduleOwnerImpl(calendarAccount, internal.getId());
			Map<Preferences, String> prefs = retrievePreferences(owner);
			owner.setPreferences(prefs);
		
			LOG.debug("found owner " + owner);
			return owner;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param owner
	 * @param preference
	 * @param value
	 */
	protected void replacePreference(final ScheduleOwner owner, final Preferences preference, final String value) {
		int rowsUpdated = this.simpleJdbcTemplate.update(
				"delete from preferences where owner_id = ? and preference_key = ?",
				owner.getId(),
				preference.getKey());
		
		LOG.debug("deleted existing, rowsUpdated: " + rowsUpdated);
		rowsUpdated = this.simpleJdbcTemplate.update(
				"insert into preferences(owner_id, preference_key, preference_value) values (?, ?, ?)",
				owner.getId(),
				preference.getKey(),
				value);
		
		LOG.info("stored preference " + preference.getKey() + ", value " + value + ", owner " + owner);
	}
}
