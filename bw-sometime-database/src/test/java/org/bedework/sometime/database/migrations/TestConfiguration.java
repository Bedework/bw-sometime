/**
 * 
 */
package org.bedework.sometime.database.migrations;

import org.flywaydb.core.Flyway;

/**
 * Configuration for FlywayDatabaseMigrationsTest.
 * 
 * @author Nicholas Blair
 */
class TestConfiguration {
	public Flyway flyway() {
		return new Flyway(Flyway.configure()
														.dataSource("jdbc:hsqldb:mem:bw", "sa", "")
														.locations("db/hsqldb", "db/migration"));
	}
}
