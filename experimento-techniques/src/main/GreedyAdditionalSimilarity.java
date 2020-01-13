package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GreedyAdditionalSimilarity {
	String Directory;
	String matrixFile;
	String coverageFile;
	String codeLinesFile;
	char[][] coverageMatrix;
	float[][] testSimilarity;
	Map<Integer, List<Integer>> testMapSimilarity = new HashMap<>();
	final String sep = File.separator;
	char[] currentUnitsCovered; // Record the already covered
	private float similarityFactor;
								// statements/methods/branches.
	private float testSimilar2Move;

	public GreedyAdditionalSimilarity(String Directory, String matrixFile, float similarityFactor, float testSimilar2Move) {
		this.Directory = Directory; // get the directory to Create a output file
									// for Statistic Data.
		this.matrixFile = matrixFile; // Create a new file use the same file
										// prefix for Statistic Data.
		this.coverageFile = Directory + this.sep + matrixFile + "_matrix.txt";
		this.codeLinesFile = Directory + this.sep + matrixFile + "_index.txt";
		this.similarityFactor = similarityFactor;
		this.testSimilar2Move = testSimilar2Move;
	}

	/**
	 * Method to find indexes of unit code from test classes. 
	 */
	private ArrayList<Integer> findTestsCodeIndexes() throws NumberFormatException, IOException {
		String line;
		ArrayList<Integer> result = new ArrayList<Integer>();
		BufferedReader br_index;
		try {
			br_index = new BufferedReader(new FileReader(codeLinesFile));
		} catch (Exception ex) {
			return result;
		}
		while ((line = br_index.readLine()) != null) {
			if (line.matches("(.*)(.*)")) {
				String[] lineSplit = line.split(":");
				result.add(Integer.parseInt(lineSplit[lineSplit.length - 1]));
			}
		}
		br_index.close();
		return result;
	}
	
	/**
	 * Remove columns that are in the list of testeIndexes
	 * 
	 * @return new line without unit code from testsIndexes
	 */
	private String removeColumns(String line, List<Integer> indexTest) {
		StringBuilder sb = new StringBuilder(line);
		Collections.sort(indexTest);
		for (int i = 0; i < indexTest.size(); i++) {
			sb.deleteCharAt(indexTest.get(i) - i);
		}
		return line;
	}

	/**
	 * Read the Coverage File and Store the value to the APBC, APDC or APSC matrix
	 * 
	 * @param coverageFile
	 */
	public void getCoverageMatrix(String coverageFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(coverageFile));
			ArrayList<String> testsLines = new ArrayList<String>();
//			ArrayList<Integer> indexTestCode = findTestsCodeIndexes();
			int codeUnitCoveredLength = 0;
			String unitsCoveragedByTest = null;
			
			// Read all the rows from the Coverage Matrix and store then in an
			// ArrayList for further process.
			while ((unitsCoveragedByTest = br.readLine()) != null) {
				if (codeUnitCoveredLength == 0) {
					codeUnitCoveredLength = unitsCoveragedByTest.length();
				} else if (codeUnitCoveredLength != unitsCoveragedByTest.length()) {
					System.out.println("ERROR: The line from Coverage Matrix File is WORNG.\n" + unitsCoveragedByTest);
					System.exit(1);
				}
				testsLines.add(unitsCoveragedByTest);
			}
			// Initialize coverage matrix with number os tests vs units of code
			this.coverageMatrix = new char[testsLines.size()][codeUnitCoveredLength];
			this.testSimilarity = new float[testsLines.size()][testsLines.size()];
			// Store the information in the ArrayList to the Array.
			for (int i = 0; i < testsLines.size(); i++) {
				coverageMatrix[i] = testsLines.get(i).toCharArray();
				testMapSimilarity.put(new Integer(i), getCoveredIndexes(coverageMatrix[i]));
			}

			this.currentUnitsCovered = new char[codeUnitCoveredLength]; // Initialized the
															// global
															// currentCovered.
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Calculate the number of additional '1' in the array based on the global
	// array currentCovered.
	public int getAdditionalCoveredNumber(char[] a) {
		int num = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == '1' && this.currentUnitsCovered[i] == '0') {
				num++;
			}
		}
		return num;
	}

	// Calculate the number of additional '1' in the array.
	// o array recebido eh uma linha com o tipo de coverage vs testes
	// retorna o numero de testes que cobrem o trecho de codigo a
	public int getCoveredNumber(char[] a) {
		int num = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == '1') {
				num++;
			}
		}
		return num;
	}

	public List<Integer> getCoveredIndexes(char[] a) {
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < a.length; i++) {
			if (a[i] == '1') {
				indexes.add((int) i);
			}
		}
		return indexes;
	}

	// The main function that select the test sequence.
	public int[] getSelectedTestSequence() {

		this.getCoverageMatrix(this.coverageFile);
		this.getSimilarTests();

		int lenTests = this.coverageMatrix.length, 
			lenUnits = this.coverageMatrix[0].length;
		int[] selectedTestSequence = new int[lenTests];
		int[] unitsCoveredByTest = new int[lenTests];
		ArrayList<Integer> testsSelected = new ArrayList<Integer>();
		ArrayList<Integer> unitsNotCovered = new ArrayList<Integer>();
		boolean containAllZeroRow = false;

		getCoverageByTest(lenTests, unitsCoveredByTest, unitsNotCovered);
		
		int[] originalUnitsCoveredByTest = Arrays.copyOf(unitsCoveredByTest, lenTests); 
		
		this.currentUnitsCovered = new char[lenUnits];
		this.clearArrayWithZeros(this.currentUnitsCovered);
		
		while (testsSelected.size() < lenTests) {
			int maxTestCoveringIndex = this.selectMaxNonCovered(unitsCoveredByTest);
			if (maxTestCoveringIndex == -1) {
				// All the Units are covered, then use the same algorithm for
				// the left test cases.
				if (testsSelected.size() == lenTests)
					break;
				unitsCoveredByTest = Arrays.copyOf(originalUnitsCoveredByTest, lenTests);
				maxTestCoveringIndex = this.selectMaxNonCovered(unitsCoveredByTest);
				this.clearArrayWithZeros(this.currentUnitsCovered);
			}

			if (maxTestCoveringIndex == -1) {
				containAllZeroRow = true;
				break;
			}
			originalUnitsCoveredByTest[maxTestCoveringIndex] = 0;
			testsSelected.add(maxTestCoveringIndex);
			List<Integer> testsSimilar2move = new ArrayList<Integer>();
			int testsMoved = 0;
			for (int i = 0; i < this.testSimilarity[maxTestCoveringIndex].length; i++) {
				if (i == maxTestCoveringIndex || testsSelected.contains(i))
					continue;
				if (this.testSimilarity[maxTestCoveringIndex][i] >= similarityFactor) {
					testsSimilar2move.add(i);
				}
			}
			if(testsSimilar2move.size() > 0){
				orderByCoverage(testsSimilar2move, unitsCoveredByTest);
				double numberOfTests2Move = Math.ceil(testsSimilar2move.size()*this.testSimilar2Move/1);
				for (int i = 0; i < testsSimilar2move.size() && i < numberOfTests2Move; i++) {
					originalUnitsCoveredByTest[i] = 0;
					testsSelected.add(testsSimilar2move.get(i));
				}
			}
			
			this.mergeIntoCurrentArray(this.currentUnitsCovered, this.coverageMatrix[maxTestCoveringIndex]);

			for (int j = 0; j < lenTests; j++) {
				if (testsSelected.contains(j)) {
					unitsCoveredByTest[j] = 0;
				} else {
					unitsCoveredByTest[j] = this.getAdditionalCoveredNumber(this.coverageMatrix[j]);
				}
			}
		}

		if (containAllZeroRow) {// For this algorithm, put all the zero covered
								// test case to the end
			for (int i = 0; i < unitsNotCovered.size(); i++) {
				testsSelected.add(unitsNotCovered.get(i));
			}
		}
		for (int i = 0; i < lenTests; i++) {
			selectedTestSequence[i] = testsSelected.get(i);
		}
		return selectedTestSequence;
	}
	
	private void orderByCoverage(List<Integer> testsSimilar2move, int[] unitsCoveredByTest){
		Integer[] newArray = new Integer[unitsCoveredByTest.length];
		int i = 0;
		for (int value : unitsCoveredByTest) {
		    newArray[i++] = Integer.valueOf(value);
		}
		testsSimilar2move.sort((t1, t2) -> newArray[t2].compareTo(newArray[t1]));
	}

	private void getCoverageByTest(int lenTests, int[] unitsCoveredByTest, ArrayList<Integer> unitsNotCovered) {
		for (int testIndex = 0; testIndex < lenTests; testIndex++) {
			unitsCoveredByTest[testIndex] = this.getCoveredNumber(this.coverageMatrix[testIndex]);
			if (unitsCoveredByTest[testIndex] == 0) {
				unitsNotCovered.add(testIndex);
			}
		}
	}

	private void getSimilarTests() {
		Set<Integer> union;
		Set<Integer> intersection;
		for (int i = 0; i < coverageMatrix.length; i++) {
			for (int j = i; j < coverageMatrix.length; j++) {
				union = new HashSet<Integer>();
				intersection = new HashSet<Integer>();
				union.addAll(testMapSimilarity.get(i));
				union.addAll(testMapSimilarity.get(j));
				intersection.addAll(testMapSimilarity.get(i));
				intersection.retainAll(testMapSimilarity.get(j));
				float similatiryResult = (float) (intersection.size() * 1.0) / union.size();
				this.testSimilarity[i][j] = similatiryResult;
				this.testSimilarity[j][i] = similatiryResult;
			}
		}
	}

	// Select the maximum number in the array and return its index.
	public int selectMaxNonCovered(int[] a) {
		int index = -1;
		int max = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] > max) {
				max = a[i];
				index = i;
			}
		}

		return index;
	}

	// Merge all the '1's in the new array into the current array.
	public void mergeIntoCurrentArray(char[] current, char[] newArray) {
		if (current.length != newArray.length) {
			System.out.println("ERROR: mergeIntoCurrentArray: length is not equal.");
			System.exit(1);
		}
		int len = current.length;
		for (int i = 0; i < len; i++) {
			if (newArray[i] == '1') {
				current[i] = newArray[i];
			}
		}
	}

	// Set all elements '0' in the array.
	public void clearArrayWithZeros(char[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = '0';
		}
	}

	public void Print(char[] a) {
		for (int i = 0; i < a.length; i++) {
			if (i == a.length - 1) {
				System.out.print(a[i]);
			} else {
				System.out.print(a[i] + ",");
			}
		}
	}

	public void Print(int[] a) {
		for (int i = 0; i < a.length; i++) {
			if (i == a.length - 1) {
				System.out.print(a[i]);
			} else {
				System.out.print(a[i] + ",");
			}
		}
	}

	// For Unit Test.
	public static void main(String[] args) {
		GreedyAdditionalSimilarity ga = new GreedyAdditionalSimilarity(args[0], args[1], Float.parseFloat(args[2]), Float.parseFloat(args[3]));
		ga.Print(ga.getSelectedTestSequence());

	}
}
