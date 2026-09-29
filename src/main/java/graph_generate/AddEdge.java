package graph_generate;

import connection.graph.Neo4jConnection;
import org.neo4j.driver.Session;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddEdge {

	private final Neo4jConnection neo4jConn;

	public AddEdge(Neo4jConnection neo4jConn) {
		this.neo4jConn = neo4jConn;
	}

	public void addEdges(Connection connection) throws SQLException {
		if (connection == null || connection.isClosed()) {
			throw new IllegalArgumentException("There is no active MySql connection.");
		}

		DatabaseMetaData metaData = connection.getMetaData();
		String currentDb = connection.getCatalog();

		try (ResultSet tables = metaData.getTables(currentDb, null, "%", new String[]{"TABLE"});
			 Session session = neo4jConn.getSession()) {

			while (tables.next()) {
				String fkTableName = tables.getString("TABLE_NAME");

				try (ResultSet foreignKeys = metaData.getImportedKeys(currentDb, null, fkTableName)) {
					while (foreignKeys.next()) {
						String pkTableName = foreignKeys.getString("PKTABLE_NAME");
						String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
						String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");

						String relationshipType = "HAS_" + fkTableName.toUpperCase();

						System.out.println(String.format("Establishing connection: (%s)-[:%s]->(%s) [%s.%s = %s.%s]...",
								pkTableName, relationshipType, fkTableName, pkTableName, pkColumnName, fkTableName, fkColumnName));
						
						String cypherQuery = String.format("""
                            MATCH (parent:%s), (child:%s)
                            WHERE toString(parent.%s) = toString(child.%s)
                            MERGE (parent)-[r:%s]->(child)
                            """, pkTableName, fkTableName, pkColumnName, fkColumnName, relationshipType);

						session.executeWrite(tx -> {
							tx.run(cypherQuery);
							return null;
						});

						System.out.println(String.format("(%s)-[:%s]->(%s) completed.",
								pkTableName, relationshipType, fkTableName));
					}
				}
			}
		}
	}
}