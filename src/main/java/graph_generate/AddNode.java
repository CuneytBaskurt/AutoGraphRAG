package graph_generate;

import connection.graph.Neo4jConnection;
import org.neo4j.driver.Session;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNode {

	private final Neo4jConnection neo4jConn;

	public AddNode(Neo4jConnection neo4jConn) {
		this.neo4jConn = neo4jConn;
	}

	public void add(Connection connection) throws SQLException {
		if (connection == null || connection.isClosed()) {
			throw new IllegalArgumentException("There is no active MySql connection.");
		}

		DatabaseMetaData metaData = connection.getMetaData();
		String currentDb = connection.getCatalog();

		try (ResultSet tables = metaData.getTables(currentDb, null, "%", new String[]{"TABLE"});
			 Session session = neo4jConn.getSession()) {

			while (tables.next()) {
				String tableName = tables.getString("TABLE_NAME");
				System.out.println("Transfering: " + tableName + "...");

				List<Map<String, Object>> batch = new ArrayList<>();

				try (Statement stmt = connection.createStatement();
					 ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {

					ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();

					while (rs.next()) {
						Map<String, Object> row = new HashMap<>();
						for (int i = 1; i <= columnCount; i++) {
							String colName = rsmd.getColumnLabel(i);
							Object colValue = rs.getObject(i);

							if (colValue != null) {
								if (colValue instanceof BigDecimal) {
									row.put(colName, ((BigDecimal) colValue).doubleValue());
								}
								else if (colValue instanceof java.sql.Date || colValue instanceof java.sql.Timestamp) {
									row.put(colName, colValue.toString());
								}
								else {
									row.put(colName, colValue);
								}
							}
						}
						batch.add(row);
					}
				}

				if (batch.isEmpty()) {
					continue;
				}

				String cypherQuery = String.format("""
                    UNWIND $batch AS row
                    CREATE (n:%s)
                    SET n = row
                    """, tableName);

				session.executeWrite(tx -> {
					tx.run(cypherQuery, Collections.singletonMap("batch", batch));
					return null;
				});

				System.out.println("From [" + tableName + "] " + batch.size() + " Node created.");
			}
		}
	}
}