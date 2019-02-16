package test;

import org.junit.Test;

import main.GreedyAdditionalSimilarity;

public class GreedyAdditionalTest {
	
	private final String directory = "/Users/wesley/Documents/workspaceJava/experimento-techniques/src/test/resources";
	private final String matrixFile = "method";
	private final double similarityFactor = 0.5;
	private final double testSimilar2Move = 0.5;
	
	GreedyAdditionalSimilarity gAddSimilarity;
	

	@Test
	public void test() {
		
		gAddSimilarity = new GreedyAdditionalSimilarity(this.directory, this.matrixFile, (float)this.similarityFactor, (float)this.testSimilar2Move);
		gAddSimilarity.Print(gAddSimilarity.getSelectedTestSequence());
	}

}
