package io.github.psm8.archunit.fixtures.valid.application;

import javax.sql.DataSource;

public final class JavaSeDataSourceService {
	private final DataSource dataSource;

	public JavaSeDataSourceService(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
