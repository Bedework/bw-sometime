/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 * </br>
 * http://www.apache.org/licenses/LICENSE-2.0
 * </br>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.bedework.sometime.impl.owner;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import org.apache.commons.lang3.StringUtils;
import org.bedework.sometime.model.ICalendarAccount;

import java.util.regex.Pattern;

/**
 * {@link OwnerAuthorization} implementation that checks for a
 * non-empty value of a specific attribute on the {@link ICalendarAccount}.
 * </br>
 * If attributeValuePattern is set, the {@link #isEligible(ICalendarAccount)}
 * implementation also checks that the user's attribute value matches
 * the pattern.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Header: RequiredAttributeOwnerAuthorizationImpl.java Exp $
 */
public class RequiredAttributeOwnerAuthorizationImpl implements
		OwnerAuthorization, Logged {
	private String attributeName = "wisceduisisadvisoremplid";
	private Pattern attributeValuePattern;
	
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * @param attributeValuePattern the attributeValuePattern to set
	 */
	public void setAttributeValuePattern(final String attributeValuePattern) {
		this.attributeValuePattern = Pattern.compile(attributeValuePattern);
	}

	@Override
	public boolean isEligible(final ICalendarAccount user) {
		final var userAttributeValue = user.getAttributeValue(attributeName);
		if (null != attributeValuePattern) {
			// test for pattern equality
			final var m = attributeValuePattern.matcher(userAttributeValue);
			if (m.matches()) {
				debug("user is eligible " + user);
				return true;
			} 
		} else {
			// test just for existence (non-blank)
			if (!StringUtils.isBlank("userAttributeValue")) {
				debug("user is eligible " + user);
				return true;
			}
		}

		debug("user is not eligible " + user);
		return false;
	}

	/* ============================================================
	 *                   Logged methods
	 * ============================================================ */

	private final BwLogger logger = new BwLogger();

	@Override
	public BwLogger getLogger() {
		if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
			logger.setLoggedClass(getClass());
		}

		return logger;
	}
}
