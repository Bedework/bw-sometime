/**
 * 
 */
package org.bedework.sometime.database.migrations;

import org.flywaydb.core.Flyway;

import javax.annotation.PostConstruct;

/**
 * Flyway backed class responsible for executing database migration.
 * 
 * @author Nicholas Blair
 */
public class FlywayDatabaseMigrations {
	private Flyway flyway;

	/**
	 * @param flyway the {@link Flyway} to be used by {@link #runMigrations()}
	 */
	public void setFlyway(final Flyway flyway) {
		this.flyway = flyway;
	}
	/**
	 * @see Flyway#migrate()
	 */
	@PostConstruct
	public void runMigrations() {
		flyway.migrate();
	}
}
