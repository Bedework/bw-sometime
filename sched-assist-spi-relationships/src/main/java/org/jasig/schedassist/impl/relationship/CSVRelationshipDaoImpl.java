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

package org.jasig.schedassist.impl.relationship;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bedework.sometime.ICalendarAccountDao;
import org.bedework.sometime.RelationshipDao;
import org.bedework.sometime.impl.owner.OwnerDao;
import org.bedework.sometime.impl.visitor.NotAVisitorException;
import org.bedework.sometime.impl.visitor.VisitorDao;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.IScheduleVisitor;
import org.bedework.sometime.model.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * Spring JDBC {@link RelationshipDao} backed by the same table that is
 * populated by the {@link CSVRelationshipDataSourceImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id: CSVRelationshipDaoImpl.java 147 2011-06-10 15:03:02Z npblair $
 */
@Service
public class CSVRelationshipDaoImpl implements RelationshipDao {

	private Log LOG = LogFactory.getLog(this.getClass());
	
	private JdbcTemplate simpleJdbcTemplate;
	private String identifyingAttributeName = "uid";
	private ICalendarAccountDao calendarAccountDao;
	private OwnerDao ownerDao;
	private VisitorDao visitorDao;
	
	/**
	 * 
	 * @param dataSource
	 */
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
	}
	/**
	 * @param calendarAccountDao the calendarAccountDao to set
	 */
	@Autowired
	public void setCalendarAccountDao(@Qualifier("people") ICalendarAccountDao calendarAccountDao) {
		this.calendarAccountDao = calendarAccountDao;
	}
	/**
	 * @param ownerDao the ownerDao to set
	 */
	@Autowired
	public void setOwnerDao(OwnerDao ownerDao) {
		this.ownerDao = ownerDao;
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
	 * @see org.jasig.schedassist.RelationshipDao#forOwner(org.jasig.schedassist.model.ScheduleOwner)
	 */
	@Override
	public List<Relationship> forOwner(ScheduleOwner owner) {
		String ownerId = getIdentifyingAttribute(owner.getCalendarAccount());
		List<CSVRelationship> csvRecords = this.simpleJdbcTemplate.query(
				"select * from csv_relationships where owner_id = ?",
				new CSVRelationshipRowMapper(),
				ownerId);
		List<Relationship> results = new ArrayList<Relationship>();

		for(CSVRelationship record : csvRecords) {
			ICalendarAccount visitorCalendarAccount = calendarAccountDao.getCalendarAccount(this.identifyingAttributeName, record.getVisitorIdentifier());
			if (null == visitorCalendarAccount) {
				LOG.debug("no visitor calendarAccount found for " + record);
				continue;
			}
			try {
				IScheduleVisitor visitor = visitorDao.toVisitor(visitorCalendarAccount);
					
				Relationship relationship = new Relationship();
				relationship.setOwner(owner);
				relationship.setVisitor(visitor);
				relationship.setDescription(record.getRelationshipDescription());
				results.add(relationship);
				if (LOG.isDebugEnabled()) {
					LOG.debug("found relationship " + relationship);
				}			
			} catch (NotAVisitorException e) {
				LOG.debug("calendarAccount found but not a visitor in " + record);
			} 
		}

		return results;
	}

	/* (non-Javadoc)
	 * @see org.jasig.schedassist.RelationshipDao#forVisitor(org.jasig.schedassist.model.IScheduleVisitor)
	 */
	@Override
	public List<Relationship> forVisitor(IScheduleVisitor visitor) {
		String visitorId = getIdentifyingAttribute(visitor.getCalendarAccount());
		List<CSVRelationship> csvRecords = this.simpleJdbcTemplate.query(
				"select * from csv_relationships where visitor_id = ?",
				new CSVRelationshipRowMapper(),
				visitorId);
		List<Relationship> results = new ArrayList<Relationship>();
		for(CSVRelationship record : csvRecords) {
			ICalendarAccount ownerCalendarAccount = calendarAccountDao.getCalendarAccount(this.identifyingAttributeName, record.getOwnerIdentifier());
			if (null == ownerCalendarAccount) {
				LOG.debug("no owner calendarAccount found for " + record);
				continue;
			}
			ScheduleOwner owner = ownerDao.locateOwner(ownerCalendarAccount);
			if (null != owner) {
				Relationship relationship = new Relationship();
				relationship.setOwner(owner);
				relationship.setVisitor(visitor);
				relationship.setDescription(record.getRelationshipDescription());
				results.add(relationship);
				if (LOG.isDebugEnabled()) {
					LOG.debug("found relationship " + relationship);
				}
			} else {
				LOG.debug("calendarAccount not registered as owner for " + record);
			}
		}
		
		return results;
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
