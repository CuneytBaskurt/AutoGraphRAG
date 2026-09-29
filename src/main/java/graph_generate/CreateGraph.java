package graph_generate;

import connection.graph.Neo4jConnection;
import connection.relational.DatabaseServiceRouter;
import connection.relational.DatabaseType;
import connection.relational.DbConnectionService;

public class CreateGraph {
	public void create(){
		DatabaseServiceRouter router = new DatabaseServiceRouter();
		DbConnectionService mysqlService = router.getConnectionService(DatabaseType.MYSQL);

		Neo4jConnection neo4jConn = new Neo4jConnection();

		try {
			mysqlService.connect();
			neo4jConn.connect();

			AddNode addNode = new AddNode(neo4jConn);
			addNode.add(mysqlService.getConnection());

			System.out.println("\nThe nodes has created!");

			AddEdge addEdge = new AddEdge(neo4jConn);
			addEdge.addEdges(mysqlService.getConnection());

			System.out.println("\nThe edges has created!");


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mysqlService.disconnect();
			} catch (Exception ignored) {}
			neo4jConn.close();
		}
	}
}
