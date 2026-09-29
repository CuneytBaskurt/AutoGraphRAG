
import graph_generate.CreateGraph;
import rag.GenerateAnswer;
import rag.GenerateCypher;

public class Main {
	public static void main(String[] args) {
		String question = "What are the albums of AC/DC and what is the songs in the albums?";

		GenerateAnswer answerService = new GenerateAnswer();
		String answer = answerService.generateAnswer(question);

		System.out.println("--- Answer ---");
		System.out.println(answer);

	}
}