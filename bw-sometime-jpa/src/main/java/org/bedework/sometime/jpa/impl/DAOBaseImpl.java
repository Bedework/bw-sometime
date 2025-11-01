/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.sometime.jpa.impl;

import org.bedework.base.exc.BedeworkException;
import org.bedework.database.db.DbSession;
import org.bedework.sometime.jpa.impl.common.DAOBase;
import org.bedework.sometime.model.UnversionedDbentity;
import org.bedework.util.logging.BwLogger;

/** Class used as basis for a number of DAO classes.
 *
 * @author Mike Douglass   douglm  bedework.org
 */
public abstract class DAOBaseImpl implements DAOBase {
  private DbSession sess;

  /**
   */
  public DAOBaseImpl() {
  }

  /**
   * @param sess the session
   */
  public DAOBaseImpl(final DbSession sess) {
    init(sess);
  }

  public void init(final DbSession sess) {
    this.sess = sess;
  }
  /**
   * 
   * @return a unique name for the instance.
   */
  public abstract String getName();

  public void setSess(final DbSession val) {
    sess = val;
  }

  public DbSession getSess() {
    return sess;
  }

  public DbSession createQuery(final String query) {
    return sess.createQuery(query);
  }

  public void rollback() {
    getSess().rollback();
  }

  public DbSession add(final UnversionedDbentity<?> val) {
    getSess().add(val);
    return sess;
  }

  public DbSession update(final UnversionedDbentity<?> val) {
    getSess().update(val);
    return sess;
  }

  public DbSession delete(final UnversionedDbentity<?> val) {
    getSess().delete(val);
    return sess;
  }

  public UnversionedDbentity<?> merge(final UnversionedDbentity<?> val) {
    return (UnversionedDbentity)sess.merge(val);
  }

  public void throwException(final BedeworkException be) {
    getSess().rollback();
    throw be;
  }

  public void throwException(final String pname) {
    getSess().rollback();
    throw new BedeworkException(pname);
  }

  public void throwException(final String pname, final String extra) {
    getSess().rollback();
    throw new BedeworkException(pname, extra);
  }

  /* =================================================
   *                   Logged methods
   * ================================================= */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
