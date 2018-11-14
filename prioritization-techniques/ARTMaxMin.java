
/*
 * Use ART-MaxMin Algorithm for Test Case Prioritization.
 * Yafeng.Lu@cs.utdallas.edu
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ARTMaxMin {

	String Directory;
	String matrixFile;
	String coverageFile;
	String codeLinesFile;
	char[][] CoverageMatrix;
	final String sep = File.separator;
	ArrayList<Integer> coveredZero = new ArrayList<Integer>(); // Store the test
																// case in case
																// it covers 0
																// statements/methods/branches.

	public ARTMaxMin(String Directory, String matrixFile) {
		this.Directory = Directory; // Get the directory to Create a output file
									// for Statistic Data.
		this.matrixFile = matrixFile; // Create a new file use the same file
										// prefix for Statistic Data.
		this.coverageFile = Directory+File.separator+matrixFile+"_matrix.txt";
		this.codeLinesFile = Directory+File.separator+matrixFile+"_index.txt";
	}

	private ArrayList<Integer> removeTestLines() throws NumberFormatException, IOException{
		String line;
		ArrayList<Integer> result = new ArrayList<Integer>();
		BufferedReader br_index;
		try{
			br_index = new BufferedReader(new FileReader(codeLinesFile));
		}catch(Exception ex){
			return result;
		}
		while((line = br_index.readLine()) != null){
			if(line.matches("(.*)Test(.*)")){
				String[] lineSplit = line.split(":");
				result.add(Integer.parseInt(lineSplit[lineSplit.length-1]));
			}
		}
		return result;
	}

	private String removeColumns(String line, List<Integer> indexTest){
		StringBuilder sb = new StringBuilder(line);
		Collections.sort(indexTest);
		for (int i = 0;i < indexTest.size(); i++ ) {
			sb.deleteCharAt(indexTest.get(i)-i);
		}
		return line;
	}

	// Read the Coverage File and Store the value to the APBC, APDC or APSC
	// Matrix.
	public void getCoverageMatrix(String coverageFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(coverageFile));
			ArrayList<String> tempAl = new ArrayList<String>();
			ArrayList<Integer> indexTest = removeTestLines();
			int columnNum = 0;
			String line;
			// Read all the rows from the Coverage Matrix and store then in an
			// ArrayList for further process.
			while ((line = br.readLine()) != null) {
				line = removeColumns(line, indexTest);
				if (columnNum == 0) {
					columnNum = line.length();
				} else if (columnNum != line.length()) {
					System.out.println("ERROR: The line from Coverage Matrix File is WORNG.\n" + line);
					System.exit(1);
				}
				tempAl.add(line);
			}
			this.CoverageMatrix = new char[tempAl.size()][columnNum]; // Initialize
																		// the
																		// Coverage
																		// Matrix.

			// Store the information in the ArrayList to the Array.
			for (int i = 0; i < tempAl.size(); i++) {
				CoverageMatrix[i] = tempAl.get(i).toCharArray();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int[] getSelectedTestSequence() {

		this.getCoverageMatrix(this.coverageFile);

		int len = this.CoverageMatrix.length, columnNum = this.CoverageMatrix[0].length;
		int[] selectedTestSequence = new int[len];
		ArrayList<Integer> selected = new ArrayList<Integer>(); // Store the
																// current
																// selected test
																// cases.

		final int LIMIT = 10; // The size of candidate tests, WARNING: test case
								// number should be more than 10.
		// Generate procedure
		int first = (int) (len * Math.random()); // Randomly select the first
													// element.
		selected.add(first);

		while (selected.size() < len) {
			// Generate procedure
			ArrayList<Integer> candidate = new ArrayList<Integer>(); // Store
																		// the
																		// already
																		// selected
																		// candidate
																		// tests.

			char[] covered = new char[columnNum]; // Record the already covered
													// statements/methods/branches.
			this.clearArray(covered);
			int coveredNum = 0; // Store the number of
								// statements/methods/branches.
			boolean stop = false;
			// this.Print(selected);
			// Randomly select the first candidate.
			// int firstRandomCandidate = -1;
			ArrayList<Integer> tempList = new ArrayList<Integer>();
			for (int i = 0; i < len; i++) {
				if (!selected.contains(i)) {
					tempList.add(i);
				}
			}
			int firstRandom = (int) (Math.random() * tempList.size());
			candidate.add(tempList.get(firstRandom));
			// tempList.remove(firstRandom);
			this.mergeIntoCurrentArray(covered, this.CoverageMatrix[firstRandom]);
			coveredNum = this.getCoveredNumber(covered);

			// while(candidate.size() < LIMIT){
			while (true) {
				// int[] leftCandidate = new int[len - selected];

				ArrayList<Integer> leftToChoose = new ArrayList<Integer>(); // int[len-selected.size()-candidate.size()];
																			// //The
																			// left
																			// unselected
																			// candidates
																			// to
																			// choose.
				for (int i = 0; i < len; i++) {
					if (!selected.contains(i) && !candidate.contains(i)) {
						leftToChoose.add(i);
					}
				}
				if (leftToChoose.size() == 0) {
					// System.out.println("Nothing to choose.");
					break;
				}

				int selcetedRandom = (int) (Math.random() * leftToChoose.size()); // Randomly
																					// select
																					// the
																					// next
																					// candidate.
				// System.out.println(selcetedRandom+","+leftToChoose.size());
				int newCandiadteIndex = leftToChoose.get(selcetedRandom); // Get
																			// the
																			// index
																			// of
																			// new
																			// selected
																			// candidate.
				// if(this.getCoveredNumber(this.CoverageMatrix[newCandiadteIndex])
				// == 0) continue;
				this.mergeIntoCurrentArray(covered, this.CoverageMatrix[newCandiadteIndex]); // Merge
																								// the
																								// new
																								// statements/methods/branches
																								// coverage
																								// into
																								// the
																								// covered
																								// array.
				int currentCovered = this.getCoveredNumber(covered);
				if (currentCovered > coveredNum) {
					coveredNum = currentCovered;
					candidate.add(newCandiadteIndex); // Add the selected
														// candidate to the
														// candidate arraylist.
					// leftCandidates[newCandiadteIndex] = -1;
				} else {
					// System.out.println(newCandiadteIndex+" break
					// :");this.Print(this.CoverageMatrix[newCandiadteIndex]);
					break; // If the statements/methods/branches coverage is not
							// increase, then stop.
				}
			}
			// if(candidate.size() ==0) continue;
			// this.Print(candidate);
			// Select procedure
			// double[][] Distance = new
			// double[selected.size()][candidate.size()]; //Store the distances
			// of selected test cases to candidates.
			double[] MaxDistances = new double[candidate.size()]; // Get the
																	// maximum
																	// distance
																	// from the
																	// candidate
																	// minimum
																	// distances.
			for (int j = 0; j < candidate.size(); j++) {
				int candidateNo = candidate.get(j);
				double[] MinDistance = new double[selected.size()]; // Get the
																	// minimum
																	// distance
																	// from the
																	// selected
																	// minimum
																	// distances.
				for (int i = 0; i < selected.size(); i++) {
					int testCaseNo = selected.get(i);
					MinDistance[i] = this.getJaccardDistance(this.CoverageMatrix[testCaseNo],
							this.CoverageMatrix[candidateNo]);
					/*
					 * if(MinDistance[i] == Double.NEGATIVE_INFINITY ||
					 * MinDistance[i] == Double.NaN){ MinDistance[i] = 0;
					 * System.out.println("Got a MinDistance is Double.NaN"); }
					 */
				}
				int MinIndex = this.getMinIndex(MinDistance);
				if (MinIndex == -1) {
					System.out.println("ERROR: getSelectedTestSequence MinIndex == -1");
					this.Print(MinDistance);
					this.Print(selected);
					this.Print(candidate);
					System.exit(1);
				}
				MaxDistances[j] = MinDistance[MinIndex]; // Assign each
															// candidate's
															// minimum distance
															// to the
															// MaxDistances
															// array.
			}
			int MaxIndex = this.getMaxIndex(MaxDistances);
			if (MaxIndex == -1) {
				System.out.println("ERROR: getSelectedTestSequence MaxIndex == -1");
				this.Print(MaxDistances);
				this.Print(selected);
				this.Print(candidate);
				System.exit(1);
			}
			// Select the candidate to selected arraylist.
			selected.add(candidate.get(MaxIndex));
			// leftCandidates[newCandiadteIndex] = -1;
		}
		// Add the elements of selected arraylist to the test case sequence.
		for (int i = 0; i < selected.size(); i++) {
			selectedTestSequence[i] = selected.get(i);
		}
		return selectedTestSequence;
	}

	// Calculate the Jaccard distance between two vector.
	public double getJaccardDistance(char[] a, char[] b) {
		if (a.length != b.length) {
			System.out.println("ERROR: length not equal.");
			System.exit(0);
		}
		int len = a.length;
		double distance = 0;
		int join = 0, combine = 0;
		char[] combinedArray = new char[len]; // Store the combined result of a
												// and b.

		for (int i = 0; i < len; i++) {
			if (a[i] == '1' && b[i] == '1') {
				join++;
			}
			if (a[i] == '1') {
				combinedArray[i] = '1';
			}
			if (b[i] == '1') {
				combinedArray[i] = '1';
			}
		}
		combine = this.getCoveredNumber(combinedArray);
		if (combine == 0) {
			return 0;
		}
		distance = 1.0 - (join / (double) combine);
		return distance;
	}

	// Return the minimum element's index of the double[].
	public int getMinIndex(double[] a) {
		double min = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < a.length; i++) {
			if (a[i] < min) {
				min = a[i];
				index = i;
			}
		}
		return index;
	}

	// Return the maximum element's index of the double[].
	public int getMaxIndex(double[] a) {
		double max = -Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < a.length; i++) {
			if (a[i] > max) {
				max = a[i];
				index = i;
			}
		}
		return index;
	}

	// Calculate the number of '1' in the array.
	public int getCoveredNumber(char[] a) {
		int num = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == '1') {
				num++;
			}
		}
		return num;
	}

	// Set all elements '0' in the array.
	public void clearArray(char[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = '0';
		}
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

	public void Print(char[] a) {
		for (int i = 0; i < a.length; i++) {
			if(i == a.length-1){
				System.out.print(a[i]);
			}else{
				System.out.print(a[i]+",");
			}
		}
	}

	public void Print(int[] a) {
		for (int i = 0; i < a.length; i++) {
			if(i == a.length-1){
				System.out.print(a[i]);
			}else{
				System.out.print(a[i]+",");
			}
		}
	}

	public void Print(double[] a) {
		for (int i = 0; i < a.length; i++) {
			if(i == a.length-1){
				System.out.print(a[i]);
			}else{
				System.out.print(a[i]+",");
			}
		}
	}

	public void Print(ArrayList<Integer> a) {
		for (int i = 0; i < a.size(); i++) {
			if(i == a.size()-1){
				System.out.print(a.get(i));
			}else{
				System.out.print(a.get(i)+",");
			}
		}
	}

	// For Unit Test.
	public static void main(String[] args) {
		ARTMaxMin art = new ARTMaxMin(args[0], args[1]);
		art.Print(art.getSelectedTestSequence());

	}
}
