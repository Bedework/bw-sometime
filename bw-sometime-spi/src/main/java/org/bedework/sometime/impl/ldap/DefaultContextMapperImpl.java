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

package org.bedework.sometime.impl.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bedework.sometime.model.ICalendarAccount;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

import javax.naming.Name;
import javax.naming.NamingException;

/**
 * @author Nicholas Blair
 *
 */
public class DefaultContextMapperImpl extends
				AbstractContextMapper<ICalendarAccount> {

	private final LDAPAttributesKey ldapAttributesKey;
	protected final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @param ldapAttributesKey
	 */
	public DefaultContextMapperImpl(LDAPAttributesKey ldapAttributesKey) {
		super();
		this.ldapAttributesKey = ldapAttributesKey;
	}

	@Override
	protected ICalendarAccount doMapFromContext(
			DirContextOperations contextOperations) {
		DefaultAttributesMapperImpl attributesMapper = new DefaultAttributesMapperImpl(ldapAttributesKey);
		Name dn = contextOperations.getDn();
		try {
			LDAPPersonCalendarAccountImpl account = (LDAPPersonCalendarAccountImpl) attributesMapper.mapFromAttributes(contextOperations.getAttributes());
			account.setDistinguishedName(dn);
			return account;
		} catch (NamingException e) {
			log.error("caught NamingException attempting to map attributes for " + dn, e);
		}
		return null;
	}



}
