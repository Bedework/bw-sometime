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
package org.bedework.sometime.model;

import org.bedework.base.ToString;
import org.bedework.database.db.InterceptorDbEntity;
import org.bedework.database.db.NoDump;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collection;

/** Base type for a database entity. We require an id and the subclasses must
 * implement hashcode and compareTo.
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public abstract class Dbentity<T>
        extends UnversionedDbentity<T>
        implements InterceptorDbEntity {
  /* Hibernate does not implicitly delete db entities during update or
   * save, except for those referenced as part of a Collection.
   *
   * These lists allows us to do explicit deletes when we delete or
   * update the entry.
   */

  private Collection<Dbentity<?>> deletedEntities;

  /* db version number */
  private int seq;

  /* For quota'd dbentities. */
  private int byteSize;

  /** No-arg constructor
   *
   */
  public Dbentity() {
  }

  /** The last calculated byte size should be stored with the entity. On update
   * call calculateByteSize to get a new value and use the difference to adjust
   * the quota.
   *
   * @param val The last calculated byte size
   */
  public void setByteSize(final int val) {
    byteSize = val;
  }

  /**
   * @return int last byte size
   */
  @JsonIgnore
  public int getByteSize() {
    return byteSize;
  }

  /** Set the seq for this entity
   *
   * @param val    int seq
   */
  public void setSeq(final int val) {
    seq = val;
  }

  /** Get the entity seq
   *
   * @return int    the entity seq
   */
  @JsonIgnore
  public int getSeq() {
    return seq;
  }

  /* ====================================================
   *                   Action methods
   * ==================================================== */

  /** Add a deleted entity - these may appear as a result of updates.
   * A null parameter is a noop.
   *
   * @param val a deleted entity
   */
  public void addDeletedEntity(final Dbentity<?> val) {
    if ((val == null) || val.unsaved()) {
      return;
    }

    if (deletedEntities == null) {
      deletedEntities = new ArrayList<>();
    }

    deletedEntities.add(val);
  }

  /**
   * @return deleted entities or null
   */
  @NoDump
  public Collection<Dbentity<?>> getDeletedEntities() {
    return deletedEntities;
  }

  /** Size to use for quotas.
   * @return int
   */
  public int length() {
    return 8;  // overhead
  }

  /** Add our stuff to the ToString object
   *
   * @param ts    ToString for result
   */
  protected ToString toStringSegment(final ToString ts) {
    return super.toStringSegment(ts)
                .append("seq", getSeq())
                .append("byteSize", getByteSize())
                .append("href", getHref());
  }
}
