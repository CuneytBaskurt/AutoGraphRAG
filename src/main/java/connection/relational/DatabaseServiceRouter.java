package connection.relational;

import connection.relational.database.MssqlConnection;
import connection.relational.database.MysqlConnection;
import connection.relational.database.PostgresqlConnection;

public class DatabaseServiceRouter {

	public DbConnectionService getConnectionService(DatabaseType type) {
		if (type == null) {
			throw new IllegalArgumentException("Database type can not be null!");
		}

		return switch (type) {
			case MSSQL -> new MssqlConnection();
			case POSTGRESQL -> new PostgresqlConnection();
			case MYSQL -> new MysqlConnection();
		};
	}
}