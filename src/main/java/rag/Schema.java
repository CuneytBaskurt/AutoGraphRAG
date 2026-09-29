package rag;

import connection.graph.Neo4jConnection;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

public class Schema {
	private final Neo4jConnection conn;

	public Schema(Neo4jConnection conn) {
		this.conn = conn;
	}
	public String getSchema() {
		String cypherQuery = """
            CALL apoc.meta.schema() YIELD value
            UNWIND keys(value) AS label
            WITH label, value[label] AS info
            WHERE info.type = 'node'
            WITH collect(
                label + "(" + 
                apoc.text.join(keys(info.properties), ", ") + 
                ")" + 
                CASE WHEN size(keys(info.relationships)) > 0 
                     THEN " -[:" + apoc.text.join([rel IN keys(info.relationships) | rel + "]->(" + info.relationships[rel].labels[0] + ")"], ", ")
                     ELSE "" END
            ) AS schemaRows
            RETURN apoc.text.join(schemaRows, "\n") AS schema
            """;

		try (Session session = conn.getSession()) {
			return session.executeRead(tx -> {
				Result result = tx.run(cypherQuery);
				if (result.hasNext()) {
					Record record = result.next();
					return record.get("schema").asString();
				}
				return "";
			});
		}
	}
}