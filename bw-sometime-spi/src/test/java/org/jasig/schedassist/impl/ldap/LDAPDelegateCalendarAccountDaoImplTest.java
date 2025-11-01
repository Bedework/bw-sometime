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
package org.jasig.schedassist.impl.ldap;

import org.bedework.sometime.impl.ldap.HasDistinguishedName;
import org.bedework.sometime.impl.ldap.LDAPDelegateCalendarAccountDaoImpl;
import org.bedework.sometime.model.IDelegateCalendarAccount;
import org.jasig.schedassist.model.mock.MockCalendarAccount;
import org.jasig.schedassist.model.mock.MockDelegateCalendarAccount;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.support.LdapUtils;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

/**
 * Tests for {@link LDAPDelegateCalendarAccountDaoImpl}.
 * 
 * @author Nicholas Blair
 */
public class LDAPDelegateCalendarAccountDaoImplTest {

	@Test
	public void testEnforceDistinguishNameControl() {
		final LdapName name = LdapUtils.newLdapName(
						"wwid=ABCDE12345,ou=people,o=domain,o=isp");
		final HasDistinguishedName owner = Mockito.mock(HasDistinguishedName.class);
		Mockito.when(owner.getDistinguishedName()).thenReturn(name);
		
		final MockCalendarAccountWithDistinguishedName accountOwner =
						new MockCalendarAccountWithDistinguishedName();
		accountOwner.setDistinguishedName(name);
		
		final LDAPDelegateCalendarAccountDaoImpl accountDao =
						new LDAPDelegateCalendarAccountDaoImpl();
		accountDao.setTreatOwnerAttributeAsDistinguishedName(true);
		
		final List<IDelegateCalendarAccount> delegates = new ArrayList<>();
		final MockDelegateCalendarAccount mockDelegate =
						new MockDelegateCalendarAccount();
		mockDelegate.setAccountOwnerAttribute(name.toString());
		delegates.add(mockDelegate);
		accountDao.enforceDistinguishedNameMatch(delegates, owner);
		Assert.assertEquals(1, delegates.size());
	}
	
	@Test
	public void testEnforceDistinguishNameNoMatch() {
		final LdapName name = LdapUtils.newLdapName(
						"wwid=ABCDE12345,ou=people,o=domain,o=isp");
		final HasDistinguishedName owner =
						Mockito.mock(HasDistinguishedName.class);
		Mockito.when(owner.getDistinguishedName()).thenReturn(name);
		
		final MockCalendarAccountWithDistinguishedName accountOwner =
						new MockCalendarAccountWithDistinguishedName();
		accountOwner.setDistinguishedName(name);
		
		final LDAPDelegateCalendarAccountDaoImpl accountDao =
						new LDAPDelegateCalendarAccountDaoImpl();
		accountDao.setTreatOwnerAttributeAsDistinguishedName(true);
		
		final List<IDelegateCalendarAccount> delegates = new ArrayList<>();
		final MockDelegateCalendarAccount mockDelegate =
						new MockDelegateCalendarAccount();
		// off by one
		mockDelegate.setAccountOwnerAttribute("wwid=ABCDE12346,ou=people,o=domain,o=isp");
		delegates.add(mockDelegate);
		accountDao.enforceDistinguishedNameMatch(delegates, owner);
		// verify removed from results
		Assert.assertEquals(0, delegates.size());
	}
	
	static class MockCalendarAccountWithDistinguishedName
					extends MockCalendarAccount implements HasDistinguishedName {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4368560472490091943L;
		private Name distinguishedName;
		
		/**
		 * @param distinguishedName the distinguishedName to set
		 */
		public void setDistinguishedName(final Name distinguishedName) {
			this.distinguishedName = distinguishedName;
		}

		@Override
		public Name getDistinguishedName() {
			return distinguishedName;
		}	
	}
}
