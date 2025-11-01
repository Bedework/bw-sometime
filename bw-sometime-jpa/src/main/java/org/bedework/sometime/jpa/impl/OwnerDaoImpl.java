/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.sometime.jpa.impl;

import org.bedework.database.db.DbSession;
import org.bedework.sometime.model.ICalendarAccount;
import org.bedework.sometime.model.ScheduleOwner;
import org.bedework.sometime.model.Preferences;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import org.jasig.schedassist.impl.owner.OwnerDao;

import java.util.Map;

/**
 * User: mike Date: 10/27/25 Time: 16:25
 */
public class OwnerDaoImpl extends DAOBaseImpl
        implements OwnerDao, Logged {
  public OwnerDaoImpl(final DbSession sess) {
    super(sess);
  }

  @Override
  public String getName() {
    return OwnerDaoImpl.class.getName();
  }

  @Override
  public ScheduleOwner register(
          final ICalendarAccount calendarUser) {
    return null;
  }

  @Override
  public void removeAccount(final ScheduleOwner owner) {

  }

  @Override
  public ScheduleOwner locateOwner(
          final ICalendarAccount calendarUser) {
    return null;
  }

  @Override
  public ScheduleOwner locateOwnerByAvailableId(
          final long internalId) {
    return null;
  }

  @Override
  public ScheduleOwner updatePreference(final ScheduleOwner owner,
                                         final Preferences preference,
                                         final String value) {
    return null;
  }

  @Override
  public String retreivePreference(final ScheduleOwner owner,
                                   final Preferences preference) {
    return "";
  }

  @Override
  public ScheduleOwner removePreference(final ScheduleOwner owner,
                                         final Preferences preference) {
    return null;
  }

  @Override
  public Map<Preferences, String> retrievePreferences(
          final ScheduleOwner owner) {
    return Map.of();
  }

  @Override
  public String lookupUsername(final long internalId) {
    return "";
  }

  @Override
  public String lookupUniqueId(final long internalId) {
    return "";
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
