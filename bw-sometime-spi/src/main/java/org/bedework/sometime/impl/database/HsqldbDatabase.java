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
import org.springframework.jdbc.support.incrementer.HsqlSequenceMaxValueIncrementer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * {@link Configuration} that provides HSQLDB specific {@link Bean}s.
 * This configuration is activated with the "hsqldb" Spring {@link Profile}.
 * 
 * @author Nicholas Blair
 * @author Mike Douglass
 */
@Component("database")
@Profile("hsqldb")
public class HsqldbDatabase implements Database {
	private final DataSource dataSource;

	/**
	 * @param dataSource the dataSource to set
	 */
	@Autowired
	public HsqldbDatabase(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public DataFieldMaxValueIncrementer ownerIdSequence() {
		return new HsqlSequenceMaxValueIncrementer(dataSource, "ownerid_seq");
	}

	@Override
	public DataFieldMaxValueIncrementer eventIdSequence() {
		return new HsqlSequenceMaxValueIncrementer(dataSource, "eventid_seq");
	}

	@Override
	public DataFieldMaxValueIncrementer reminderIdSequence() {
		return new HsqlSequenceMaxValueIncrementer(dataSource, "reminderid_seq");
	}
}
