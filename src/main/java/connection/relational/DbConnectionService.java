package connection.relational;

import java.sql.Connection;
import java.sql.SQLException;

public interface DbConnectionService {
	void connect() throws SQLException;
	void disconnect() throws SQLException;
	boolean isConnected();
	Connection getConnection();
}
