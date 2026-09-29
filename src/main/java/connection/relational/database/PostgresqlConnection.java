package connection.relational.database;

import connection.relational.DbConnectionService;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresqlConnection implements DbConnectionService {
	@Override
	public void connect() throws SQLException {

	}

	@Override
	public void disconnect() throws SQLException {

	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public Connection getConnection() {
		return null;
	}
}
