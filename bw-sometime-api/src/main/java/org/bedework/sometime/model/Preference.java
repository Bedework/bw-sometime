/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.sometime.model;

/**
 * User: mike Date: 10/31/25 Time: 22:23
 */
public class Preference
        extends Dbentity<Preference> {
  private String key;
  private String value;

  public String getKey() {
    return key;
  }

  public void setKey(final String val) {
    key = val;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String val) {
    value = val;
  }

  public Preferences getPref() {
    return Preferences.fromKey(getKey());
  }
}
