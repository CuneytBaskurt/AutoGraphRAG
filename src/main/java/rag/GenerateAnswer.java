package rag;

import config.ConfigReader;
import connection.graph.Neo4jConnection;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

public class GenerateAnswer {

	public String generateAnswer(String question) {
		GenerateCypher cypherGenerator = new GenerateCypher();
		String rawCypher = cypherGenerator.generateCypher(question);

		String cleanCypher = rawCypher.replaceAll("(?s)```cypher\\s*", "")
				.replaceAll("(?s)```\\s*", "")
				.trim();

		System.out.println("--- Cypher Query ---");
		System.out.println(cleanCypher + "\n");

		List<String> queryResults = new ArrayList<>();
		Neo4jConnection conn = new Neo4jConnection();

		try {
			conn.connect();
			try (Session session = conn.getSession()) {
				session.executeRead(tx -> {
					Result result = tx.run(cleanCypher);
					while (result.hasNext()) {
						Record record = result.next();
						queryResults.add(record.asMap().toString());
					}
					return null;
				});
			}
		} finally {
			conn.close();
		}

		String apiKey = ConfigReader.getProperty("gemini.api.key");
		LlmService gemini = new LlmService("apiKey");

		String systemInstruction = """
            You are a helpful assistant.
            Answer the user's question using ONLY the provided graph database query results.
            Be concise and clear. If the result is empty, state that no matching records were found.
            """;

		String userInstructionTemplate = """
            Question: %s

            Database Query Results:
            %s

            Answer:
            """;

		String resultsText = queryResults.isEmpty() ? "No records found." : String.join("\n", queryResults);
		String userInstruction = String.format(userInstructionTemplate, question, resultsText);

		return gemini.ask(systemInstruction, userInstruction);
	}
}