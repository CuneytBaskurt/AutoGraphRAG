package connection.graph;

import config.ConfigReader;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

public class Neo4jConnection implements AutoCloseable {

	private Driver driver;
	private String dbName;

	public void connect() {
		String uri = ConfigReader.getProperty("neo4j.url");
		String username = ConfigReader.getProperty("neo4j.username");
		String password = ConfigReader.getProperty("neo4j.password");
		this.dbName = ConfigReader.getProperty("neo4j.database");

		this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
		this.driver.verifyConnectivity();

		System.out.println("Neo4j connection successful. Active DB: " + this.dbName);
	}

	public Session getSession() {
		if (driver == null) {
			throw new IllegalStateException("Connection is not ready. First call connect()");
		}
		if (dbName != null && !dbName.isBlank()) {
			return driver.session(SessionConfig.forDatabase(this.dbName));
		}

		return driver.session();
	}

	public Driver getDriver() {
		return this.driver;
	}

	@Override
	public void close() {
		if (driver != null) {
			driver.close();
			System.out.println("Neo4j connection has closed.");
		}
	}
}