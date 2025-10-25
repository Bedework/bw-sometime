/**
 * 
 */
package org.bedework.sometime.database.migrations;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FlywayDatabaseMigrations}.
 * 
 * @author Nicholas Blair
 */
public class FlywayDatabaseMigrationsTest {
	/**
	 * Executes {@link FlywayDatabaseMigrations#runMigrations()}.
	 */
	@Test
	public void runMigrations_test() {
		final FlywayDatabaseMigrations migrations = new FlywayDatabaseMigrations();
		migrations.setFlyway(new TestConfiguration().flyway());
		
		migrations.runMigrations();
	}
}
