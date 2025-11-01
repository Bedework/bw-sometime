package org.bedework.sometime.jpa.impl.common;

import org.bedework.base.exc.BedeworkException;
import org.bedework.database.db.DbSession;
import org.bedework.util.logging.Logged;

import org.bedework.sometime.model.UnversionedDbentity;

public interface DAOBase extends Logged {
  /**
   * @param sess the session
   */
  void init(DbSession sess);

  /**
   *
   * @return a unique name for the instance.
   */
  String getName();

  void setSess(DbSession val);

  DbSession getSess();

  DbSession createQuery(String query);

  void rollback();

  DbSession add(UnversionedDbentity<?> val);

  DbSession update(UnversionedDbentity<?> val);

  DbSession delete(UnversionedDbentity<?> val);

  UnversionedDbentity<?> merge(UnversionedDbentity<?> val);

  void throwException(BedeworkException be);

  void throwException(String pname);

  void throwException(String pname, String extra);
}
