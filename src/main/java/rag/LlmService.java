package rag;

import com.google.genai.Client;
import com.google.genai.errors.ServerException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

public class LlmService {
	private final Client client;
	private static final String MODEL_NAME = "gemini-3.5-flash-lite";

	public LlmService(String apiKey) {
		this.client = Client.builder()
				.apiKey(apiKey)
				.build();
	}

	public String ask(String systemInstruction, String userInstruction) {
		GenerateContentConfig config = GenerateContentConfig.builder()
				.systemInstruction(
						Content.fromParts(
								com.google.genai.types.Part.fromText(systemInstruction)
						)
				)
				.temperature(0.0f)
				.build();

		int maxRetries = 3;
		int waitTimeMs = 2000;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				GenerateContentResponse response = client.models.generateContent(
						MODEL_NAME,
						userInstruction,
						config
				);

				return response.text();

			} catch (ServerException e) {
				System.err.println("Gemini server is busy (503). Trying again... (" + attempt + "/" + maxRetries + ")");
				if (attempt == maxRetries) {
					throw e;
				}
				try {
					Thread.sleep(waitTimeMs);
					waitTimeMs *= 2;
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Proccess stopped", ie);
				}
			}
		}

		return "";
	}
}