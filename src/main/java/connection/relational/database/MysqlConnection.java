package connection.relational.database;

import config.ConfigReader;
import connection.relational.DbConnectionService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlConnection implements DbConnectionService {

	private Connection connection;
	private Statement statement;

	@Override
	public void connect() throws SQLException {

		String url = ConfigReader.getProperty("mysql.url");
		String username = ConfigReader.getProperty("mysql.username");
		String password = ConfigReader.getProperty("mysql.password");

		this.connection = DriverManager.getConnection(url, username, password);
		this.statement = this.connection.createStatement();

		System.out.println("MySQL connection has success.");
	}

	@Override
	public void disconnect() throws SQLException {
		if (statement != null && !statement.isClosed()) {
			statement.close();
		}
		if (connection != null && !connection.isClosed()) {
			connection.close();
			System.out.println("MySQL connection has closed.");
		}
	}

	@Override
	public boolean isConnected() {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}
}
