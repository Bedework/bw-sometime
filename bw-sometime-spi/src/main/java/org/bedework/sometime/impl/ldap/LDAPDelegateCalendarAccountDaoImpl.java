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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bedework.sometime.IDelegateCalendarAccountDao;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.IDelegateCalendarAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.TimeLimitExceededException;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.support.LdapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.SearchControls;

/**
 * LDAP backed {@link IDelegateCalendarAccountDao} implementation.
 * Returns "Resource" (or delegate) calendar accounts, e.g. {@link LDAPDelegateCalendarAccountImpl} instances.
 *
 * @author Nicholas Blair
 */
public class LDAPDelegateCalendarAccountDaoImpl implements
		IDelegateCalendarAccountDao {

	private static final String OBJECTCLASS = "objectclass";
	private static final String WILDCARD = "*";

	private final Log log = LogFactory.getLog(this.getClass());
	
	private LdapOperations ldapTemplate;
	private LDAPAttributesKey ldapAttributesKey = new LDAPAttributesKeyImpl();
	private String baseDn = "o=isp";
	private long searchResultsLimit = 25L;
	private int searchTimeLimit = 10000;
	private boolean treatOwnerAttributeAsDistinguishedName = true;
	private boolean enforceSpecificObjectClass = false;
	private String requiredObjectClass = "inetresource";
	
	/**
	 * @param ldapTemplate the ldapTemplate to set
	 */
	@Autowired
	public void setLdapTemplate(final LdapOperations ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/**
	 * @param ldapAttributesKey the ldapAttributesKey to set
	 */
	@Autowired
	public void setLdapAttributesKey(final LDAPAttributesKey ldapAttributesKey) {
		this.ldapAttributesKey = ldapAttributesKey;
	}

	/**
	 * @param baseDn the baseDn to set
	 */
	@Value("${ldap.resourceAccountBaseDn:o=isp}")
	public void setBaseDn(final String baseDn) {
		this.baseDn = baseDn;
	}

	/**
	 * @param searchResultsLimit the searchResultsLimit to set
	 */
	@Value("${ldap.searchResultsLimit:25}")
	public void setSearchResultsLimit(final long searchResultsLimit) {
		this.searchResultsLimit = searchResultsLimit;
	}

	/**
	 * @param searchTimeLimit the searchTimeLimit to set (in milliseconds)
	 */
	@Value("${ldap.searchTimeLimitMillis:5000}")
	public void setSearchTimeLimit(final int searchTimeLimit) {
		this.searchTimeLimit = searchTimeLimit;
	}

	/**
	 * @return the treatOwnerAttributeAsDistinguishedName
	 */
	public boolean isTreatOwnerAttributeAsDistinguishedName() {
		return treatOwnerAttributeAsDistinguishedName;
	}

	/**
	 * @param treatOwnerAttributeAsDistinguishedName the treatOwnerAttributeAsDistinguishedName to set
	 */
	@Value("${ldap.treatResourceOwnerAttributeAsDistinguishedName:true}")
	public void setTreatOwnerAttributeAsDistinguishedName(
					final boolean treatOwnerAttributeAsDistinguishedName) {
		this.treatOwnerAttributeAsDistinguishedName = treatOwnerAttributeAsDistinguishedName;
	}

	/**
	 * @param enforceSpecificObjectClass the enforceSpecificObjectClass to set
	 */
	@Value("${ldap.resources.enforceSpecificObjectClass:false}")
	public void setEnforceSpecificObjectClass(
					final boolean enforceSpecificObjectClass) {
		this.enforceSpecificObjectClass = enforceSpecificObjectClass;
	}

	/**
	 * @param requiredObjectClass the requiredObjectClass to set
	 */
	@Value("${ldap.resources.requiredObjectClass:inetresource}")
	public void setRequiredObjectClass(final String requiredObjectClass) {
		this.requiredObjectClass = requiredObjectClass;
	}

	@Override
	public List<IDelegateCalendarAccount> searchForDelegates(
					final String searchText,
					final ICalendarAccount owner) {
		String searchTextInternal = searchText.replace(" ", WILDCARD);
		if (!searchTextInternal.endsWith(WILDCARD)) {
			searchTextInternal += WILDCARD;
		}

		final AndFilter searchFilter = new AndFilter();
		
		// inner orFilter searches on displayName and username
		final OrFilter orFilter = new OrFilter();
		orFilter.or(new LikeFilter(ldapAttributesKey.getDisplayNameAttributeName(), searchTextInternal));
		orFilter.or(new LikeFilter(ldapAttributesKey.getUsernameAttributeName(), searchTextInternal));
		
		// if the owner isn't null and owner attribute is NOT a DN, include the owner id in the search
		if (owner != null && !isTreatOwnerAttributeAsDistinguishedName()) {
			// TODO assumes delegateOwnerAttributeName has values of ICalendarAccount#getUsername
			searchFilter.and(new EqualsFilter(ldapAttributesKey.getDelegateOwnerAttributeName(), owner.getUsername()));
		}
		// and the orFilter with filter to assert our results have calendar unique ids 
		searchFilter.and(orFilter);
		searchFilter.and(new LikeFilter(ldapAttributesKey.getUniqueIdentifierAttributeName(), WILDCARD));

		if (enforceSpecificObjectClass) {
			searchFilter.and(new EqualsFilter(OBJECTCLASS, requiredObjectClass));
		}

		return new ArrayList<>(
						executeSearchReturnList(searchFilter, owner));
	}

	@Override
	public List<IDelegateCalendarAccount> searchForDelegates(
					final String searchText) {
		return searchForDelegates(searchText, null);
	}

	@Override
	public IDelegateCalendarAccount getDelegate(final String accountName) {
		return getDelegate(accountName, (ICalendarAccount) null);
	}

	@Override
	public IDelegateCalendarAccount getDelegate(
					final String accountName,
					final ICalendarAccount owner) {
		final AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(
						ldapAttributesKey.getDisplayNameAttributeName(),
						accountName));

		if (owner != null && !isTreatOwnerAttributeAsDistinguishedName()) {
			// TODO assumes delegateOwnerAttributeName has values of ICalendarAccount#getUsername
			searchFilter.and(new EqualsFilter(ldapAttributesKey.getDelegateOwnerAttributeName(), owner.getUsername()));
		}

		searchFilter.and(new LikeFilter(ldapAttributesKey.getUniqueIdentifierAttributeName(), WILDCARD));

		if (enforceSpecificObjectClass) {
			searchFilter.and(new EqualsFilter(OBJECTCLASS, requiredObjectClass));
		}

		final List<IDelegateCalendarAccount> results =
						executeSearchReturnList(searchFilter, owner);
		return DataAccessUtils.singleResult(results);
	}

	@Override
	public IDelegateCalendarAccount getDelegateByUniqueId(
					final String accountUniqueId) {
		return getDelegateByUniqueId(accountUniqueId, null);
	}

	@Override
	public IDelegateCalendarAccount getDelegateByUniqueId(
					final String accountUniqueId,
					final ICalendarAccount owner) {
		final AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(ldapAttributesKey.getUniqueIdentifierAttributeName(), accountUniqueId));
		if (owner != null && !isTreatOwnerAttributeAsDistinguishedName()) {
			// TODO assumes delegateOwnerAttributeName has values of ICalendarAccount#getUsername
			searchFilter.and(new EqualsFilter(ldapAttributesKey.getDelegateOwnerAttributeName(), owner.getUsername()));
		}
		if (enforceSpecificObjectClass) {
			searchFilter.and(new EqualsFilter(OBJECTCLASS, requiredObjectClass));
		}

		final List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter, owner);
		return DataAccessUtils.singleResult(results);
	}
	
	@Override
	public IDelegateCalendarAccount getDelegate(
					final String attributeName,
					final String attributeValue) {
		Filter filter = new EqualsFilter(attributeName, attributeValue);
		if (enforceSpecificObjectClass) {
			final AndFilter andFilter = new AndFilter();
			andFilter.and(filter);
			andFilter.and(new EqualsFilter(OBJECTCLASS, requiredObjectClass));
			filter = andFilter;
		}

		final List<IDelegateCalendarAccount> results = executeSearchReturnList(filter, null);
		return DataAccessUtils.singleResult(results);
	}

	/**
	 * 
	 * @param searchFilter to limit search
	 * @param owner
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<IDelegateCalendarAccount> executeSearchReturnList(
					final Filter searchFilter,
					final ICalendarAccount owner) {
		final SearchControls searchControls = new SearchControls();
		searchControls.setCountLimit(searchResultsLimit);
		searchControls.setTimeLimit(searchTimeLimit);
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		List<IDelegateCalendarAccount> results = Collections.emptyList();
		try {
			results = ldapTemplate.search(
				baseDn, 
				searchFilter.toString(), 
				searchControls, 
				new DefaultDelegateAccountAttributesMapperImpl(ldapAttributesKey, owner));
			if (log.isDebugEnabled()) {
				log.debug("search " + searchFilter + " returned " + results.size() + " results");
			}
			
			if (isTreatOwnerAttributeAsDistinguishedName() &&
							owner instanceof HasDistinguishedName) {
				final HasDistinguishedName ldapOwnerAccount = (HasDistinguishedName) owner;
				enforceDistinguishedNameMatch(results, ldapOwnerAccount);
			}
			results.sort(new DelegateDisplayNameComparator());
		} catch (final SizeLimitExceededException e) {
			log.debug("search filter exceeded size limit (" + searchResultsLimit + "): " + searchFilter);
		} catch (final TimeLimitExceededException e) {
			log.debug("search filter exceeded time limit(" + searchTimeLimit + " milliseconds): " + searchFilter);
		}
		return results;
	}

	/**
	 * Iterate over delegates, removing elements that have mismatched DN to owner
	 * @param delegates
	 * @param desiredOwnerAccount
	 */
	protected void enforceDistinguishedNameMatch(
					final List<IDelegateCalendarAccount> delegates,
					final HasDistinguishedName desiredOwnerAccount) {
		for (final Iterator<IDelegateCalendarAccount> i = delegates.iterator(); i.hasNext(); ) {
			final IDelegateCalendarAccount delegate = i.next();
			final String ownerAttributeValue = delegate.getAccountOwnerAttribute();
			if (ownerAttributeValue == null) {
				if (log.isDebugEnabled()) {
					log.debug("No ownerAttributeValue for " +
														delegate.getCalendarUniqueId());
				}
				i.remove();
				continue;
			}

			final var owner = LdapUtils.newLdapName(ownerAttributeValue);
			if (!desiredOwnerAccount.getDistinguishedName()
															.equals(owner)) {
				if (log.isDebugEnabled()) {
					log.debug(ownerAttributeValue +
														" does not match desired owner ICalendarAccount dn: " +
														desiredOwnerAccount.getDistinguishedName());
				}
				i.remove();
			}
		}
	}

	/**
	 * Simple {@link Comparator} for {@link IDelegateCalendarAccount} that compares
	 * on the displayName field.
	 *
	 * @author Nicholas Blair
	 * @version $Id: LDAPDelegateCalendarAccountDaoImpl $
	 */
	private static class DelegateDisplayNameComparator implements Comparator<IDelegateCalendarAccount>{
		@Override
		public int compare(final IDelegateCalendarAccount o1,
											 final IDelegateCalendarAccount o2) {
			return new CompareToBuilder()
							.append(o1.getDisplayName(), o2.getDisplayName())
							.toComparison();
		}
	}
}
