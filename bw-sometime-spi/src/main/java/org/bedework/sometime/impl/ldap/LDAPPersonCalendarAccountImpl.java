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

import java.util.List;
import java.util.Map;

import javax.naming.Name;

/**
 * {@link AbstractCalendarAccount} implementation to represent a 
 * person sourced from LDAP.
 * 
 * @author Nicholas BlairNicholas Blair
 * @version $Id: LDAPPersonCalendarAccountImpl.java $
 */
class LDAPPersonCalendarAccountImpl extends AbstractCalendarAccount implements HasDistinguishedName {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1794331642591042311L;

	private Map<String, List<String>> attributesMap;
	private Name distinguishedName;
	
	/**
	 * Default implementation.
	 * 
	 * @param attributes
	 * @param ldapAttributesKey
	 */
	public LDAPPersonCalendarAccountImpl(
					final Map<String, List<String>> attributes,
					final LDAPAttributesKey ldapAttributesKey) {
		this.attributesMap = attributes;
		// populate fields first
		setCalendarUniqueId(
						getAttributeValue(
										ldapAttributesKey.getUniqueIdentifierAttributeName()));

		setDisplayName(
						getAttributeValue(
										ldapAttributesKey.getDisplayNameAttributeName()));

		setEmailAddress(
						getAttributeValue(
										ldapAttributesKey.getEmailAddressAttributeName()));

		setUsername(
						getAttributeValue(
										ldapAttributesKey.getUsernameAttributeName()));

		// set eligibility
		setEligible(
						ldapAttributesKey.evaluateEligibilityAttributeValue(attributes));
	}

	@Override
	public Map<String, List<String>> getAttributes() {
		return this.attributesMap;
	}

	@Override
	public String getCalendarLoginId() {
		return getUsername();
	}

	@Override
	public Name getDistinguishedName() {
		return distinguishedName;
	}

	/**
	 * @param distinguishedName the distinguishedName to set
	 */
	public void setDistinguishedName(final Name distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	@Override
	public String toString() {
		return "LDAPPersonCalendarAccountImpl " +
						"[attributesMap=" +	attributesMap +
						", distinguishedName=" + distinguishedName +
						", toString()=" + super.toString() + "]";
	}
}
