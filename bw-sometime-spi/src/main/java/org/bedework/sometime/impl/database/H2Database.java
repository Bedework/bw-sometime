/**
 * 
 */
package org.bedework.sometime.impl.database;

import org.jasig.schedassist.model.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * {@link Configuration} that provides H2 specific {@link Bean}s.
 * This bean is activated with the "h2" Spring {@link Profile}.
 * 
 * @author Nicholas Blair
 * @author Mike Douglass
 */
@Component("database")
@Profile("h2")
public class H2Database implements Database {
	private final DataSource dataSource;

	/**
	 * @param dataSource the dataSource to set
	 */
	@Autowired
	public H2Database(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public DataFieldMaxValueIncrementer ownerIdSequence() {
		return new H2SequenceMaxValueIncrementer(dataSource, "ownerid_seq");
	}

	@Override
	public DataFieldMaxValueIncrementer eventIdSequence() {
		return new H2SequenceMaxValueIncrementer(dataSource, "eventid_seq");
	}

	@Override
	public DataFieldMaxValueIncrementer reminderIdSequence() {
		return new H2SequenceMaxValueIncrementer(dataSource, "reminderid_seq");
	}
}
