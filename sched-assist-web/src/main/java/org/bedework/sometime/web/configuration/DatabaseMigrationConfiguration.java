/**
 * 
 */
package org.bedework.sometime.web.configuration;

import org.bedework.sometime.database.migrations.FlywayDatabaseMigrations;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * {@link Configuration} used to enable database migrations; active only when the "db-migration"
 * Spring {@link Profile} is active.
 *  
 * Expects a {@link DataSource} to be provided from some other part of the application context.
 * 
 * @see FlywayDatabaseMigrations#runMigrations()
 * @author Nicholas Blair
 */
@Configuration
@ComponentScan("org.bedework.sometime.database.migrations")
@Profile("db-migration")
public class DatabaseMigrationConfiguration {

	private static final List<String> SUPPORTED_DATABASES = Arrays.asList("h2", "hsqldb", "postgresql", "oracle");
	@Inject
	private Environment environment;
	/**
	 * 
	 * @param dataSource
	 * @return a {@link Flyway} bound to {@link #dataSource()}
	 */
	@Bean @Autowired
	public Flyway flyway(DataSource dataSource) {
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setLocations(getImplementationSpecificLocation(), "db/migration");
		return flyway;
	}
	/**
	 * {@link #flyway(DataSource)} invokes {@link Flyway#setLocations(String...)} with 2 values:
	 * <ol>
	 * <li>the default location "db/location"</li>
	 * <li>the database specific location that corresponds with our environment</li>
	 * </ol>
	 * This method sniffs {@link Environment#getActiveProfiles()} for a profile that is one of the databases we support.
	 * In the absence of a profile, this method will return the hsqldb location as a sensible default.
	 * 
	 * @return a value for {@link Flyway#setLocations(String...)} that is database implementation specific, never null
	 */
	protected String getImplementationSpecificLocation() {
		for(String active: environment.getActiveProfiles()) {
			if (SUPPORTED_DATABASES.contains(active)) {
				return "db/" + active;
			}
		}	
		return "db/hsqldb";
	}
}
