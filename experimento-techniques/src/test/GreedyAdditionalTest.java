package test;

import org.junit.Test;

import main.GreedyAdditionalSimilarity;

public class GreedyAdditionalTest {
	
	private final String directory = "/Users/wesley/Documents/workspaceMastering/tcp-experiment/data/scribe-java/coverage/9cb2af37c7c3f9fdcc3da63714ba801747df33b2/";
	private final String matrixFile = "statement";
	private final double similarityFactor = 0.05;
	private final double testSimilar2Move = 0.05;
	
	GreedyAdditionalSimilarity gAddSimilarity;
	

	@Test
	public void test() {
		gAddSimilarity = new GreedyAdditionalSimilarity(this.directory, this.matrixFile, (float)this.similarityFactor, (float)this.testSimilar2Move);
		gAddSimilarity.Print(gAddSimilarity.getSelectedTestSequence());
	}

}
