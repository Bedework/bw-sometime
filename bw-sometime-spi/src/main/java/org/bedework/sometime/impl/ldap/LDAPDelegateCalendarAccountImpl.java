/*
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

package org.bedework.sometime.impl.ldap;

import org.bedework.sometime.model.AbstractCalendarAccount;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.IDelegateCalendarAccount;

import java.util.List;
import java.util.Map;

/**
 * {@link IDelegateCalendarAccount} sourced by LDAP.
 * 
 * @author Nicholas Blair
 * @version $Id: LDAPDelegateCalendarAccountImpl.java $
 */
class LDAPDelegateCalendarAccountImpl
				extends AbstractCalendarAccount
				implements IDelegateCalendarAccount {
	/**
	 * 
	 */
	private static final long serialVersionUID = 53706L;

	private final String calendarUniqueId;
	private final String emailAddress;
	private final String displayName;
	private final String username;
	private final boolean eligible;
	private final String location;
	private final String contactInformation;
	private final Map<String, List<String>> attributesMap;
	private final String accountOwnerAttribute;
	private final ICalendarAccount accountOwner;
	
	/**
	 * Initializes a delegate account without setting the accountOwner.
	 * 
	 * @param attributes
	 * @param ldapAttributesKey
	 */
	public LDAPDelegateCalendarAccountImpl(
					final Map<String, List<String>> attributes,
					final LDAPAttributesKey ldapAttributesKey) {
		this(attributes, ldapAttributesKey, null);
	}
	
	/**
	 * Default implementation.
	 * 
	 * @param attributes
	 * @param ldapAttributesKey
	 * @param accountOwner
	 */
	public LDAPDelegateCalendarAccountImpl(
					final Map<String, List<String>> attributes,
					final LDAPAttributesKey ldapAttributesKey,
					final ICalendarAccount accountOwner) {
		this.attributesMap = attributes;
		this.accountOwner = accountOwner;
		// populate fields first
		calendarUniqueId = getAttributeValue(ldapAttributesKey.getUniqueIdentifierAttributeName());
		displayName = getAttributeValue(ldapAttributesKey.getDisplayNameAttributeName());
		emailAddress = getAttributeValue(ldapAttributesKey.getEmailAddressAttributeName());
		username = getAttributeValue(ldapAttributesKey.getUsernameAttributeName());
		location = getAttributeValue(ldapAttributesKey.getDelegateLocationAttributeName());
		contactInformation = getAttributeValue(ldapAttributesKey.getDelegateContactInformationAttributeName());
		accountOwnerAttribute = getAttributeValue(ldapAttributesKey.getDelegateOwnerAttributeName());
		// set eligibility
		eligible = ldapAttributesKey.evaluateEligibilityAttributeValue(attributes);
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public String getCalendarLoginId() {
		return getUsername();
	}

	@Override
	public String getCalendarUniqueId() {
		return calendarUniqueId;
	}

	@Override
	public Map<String, List<String>> getAttributes() {
		return this.attributesMap;
	}

	@Override
	public boolean isEligible() {
		return eligible;
	}

	@Override
	public ICalendarAccount getAccountOwner() {
		return accountOwner;
	}

	@Override
	public String getAccountOwnerAttribute() {
		return accountOwnerAttribute;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public String getContactInformation() {
		return contactInformation;
	}

	@Override
	public boolean isDelegate() {
		return true;
	}

	@Override
	public String toString() {
		return "LDAPDelegateCalendarAccountImpl [calendarUniqueId="
				+ calendarUniqueId + ", emailAddress=" + emailAddress
				+ ", displayName=" + displayName + ", username=" + username
				+ ", eligible=" + eligible + ", location=" + location
				+ ", contactInformation=" + contactInformation
				+ ", attributesMap=" + attributesMap + ", accountOwner="
				+ accountOwner + ", toString()=" + super.toString() + "]";
	}
}
