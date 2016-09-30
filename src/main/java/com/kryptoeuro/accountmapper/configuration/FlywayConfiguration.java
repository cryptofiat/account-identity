package com.kryptoeuro.accountmapper.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@Import(DataSourceConfiguration.class)
public class FlywayConfiguration {
	private static final List<String> PROFILES_WITH_NO_FLYWAY_CLEAN = Arrays.asList("production", "no-flyway-clean");

	@Autowired
	Environment environment;

	String[] locations = {
			"db.migration"
	};

	@Autowired
	@Qualifier("flyway")
	DataSource dataSource;

	@Bean
	public Flyway flyway() {
		Flyway flyway = new Flyway();
		flyway.setDataSource(dataSource);
		flyway.setBaselineOnMigrate(true); //For Prod. If schema exists consider it as V1

		String[] migrationLocations = locations;

		flyway.setLocations(migrationLocations);    //Where do I look for migrations

		flyway.repair(); //If migration failed cleanup and retry
		flyway.migrate(); //Apply migrations
		return flyway;
	}
}

