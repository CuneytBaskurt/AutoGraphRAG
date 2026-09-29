package rag;

import config.ConfigReader;
import connection.graph.Neo4jConnection;

public class GenerateCypher {

	public String generateCypher(String question) {
		Neo4jConnection conn = new Neo4jConnection();
		String schema;

		try {
			conn.connect();
			Schema sch = new Schema(conn);
			schema = sch.getSchema();
		} finally {
			conn.close();
		}

		String apiKey = ConfigReader.getProperty("gemini.api.key");
		LlmService gemini = new LlmService("apiKey");

		String systemInstruction = """
             Task: Generate Cypher statement to query a graph database. 
             Instructions: Use only the provided relationship types and properties in the schema. 
             Do not use any other relationship types or properties that are not provided in the schema. 
             Do not include any explanations or apologies in your responses. 
             Do not respond to any questions that might ask anything else than for you to construct a Cypher statement. 
             Do not include any text except the generated Cypher statement.
             """;

		String userInstructionTemplate = """
             Generate Cypher statement to query a graph database. 
             Use only the provided relationship types and properties in the schema.
             Schema: %s
             Question: %s
             Cypher output:
             """;

		String userInstruction = String.format(userInstructionTemplate, schema, question);

		return gemini.ask(systemInstruction, userInstruction);
	}
}