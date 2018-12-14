/*
 * Use Greedy Additional Algorithm for Test Case Prioritization.
 * Yafeng.Lu@cs.utdallas.edu
 */
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
import java.util.Map.Entry;
import java.util.Set;


public class GreedyAdditionalSimilarity {
		String Directory;
		String matrixFile;
		String coverageFile;
		String codeLinesFile;
		char[][] CoverageMatrix;
		float[][] testSimilarity;
		Map<Integer, List<Integer>> testMapSimilarity = new HashMap<>();
		final String sep = File.separator;
		char[] currentUnitsCovered; //Record the already covered statements/methods/branches.

		public GreedyAdditionalSimilarity(String Directory, String matrixFile){
			this.Directory = Directory; //get the directory to Create a output file for Statistic Data.
			this.matrixFile = matrixFile; //Create a new file use the same file prefix for Statistic Data.
			this.coverageFile = Directory+this.sep+matrixFile+"_matrix.txt";
			this.codeLinesFile = Directory+this.sep+matrixFile+"_index.txt";
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
			br_index.close();
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
	//Read the Coverage File and Store the value to the APBC, APDC or APSC Matrix.
	public void getCoverageMatrix(String coverageFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(coverageFile));
			ArrayList<String> tempAl = new ArrayList<String>();
			ArrayList<Integer> indexTest = removeTestLines();
			int columnNum = 0;
			String line;
			//Read all the rows from the Coverage Matrix and store then in an ArrayList for further process.
			while((line = br.readLine()) != null){
				line = removeColumns(line, indexTest);
				if(columnNum == 0){
					columnNum = line.length();
				}else if(columnNum != line.length()){
					System.out.println("ERROR: The line from Coverage Matrix File is WORNG.\n"+line);
					System.exit(1);
				}
				tempAl.add(line);
			}
			this.CoverageMatrix = new char[tempAl.size()][columnNum]; //Initialize the Coverage Matrix. #cov_criterio vs #tests
			this.testSimilarity = new float[tempAl.size()][tempAl.size()];
			//Store the information in the ArrayList to the Array.
			for(int i=0; i<tempAl.size(); i++){
				CoverageMatrix[i] = tempAl.get(i).toCharArray();
				testMapSimilarity.put(new Integer(i), getCoveredIndexes(CoverageMatrix[i]));
			}

			this.currentUnitsCovered = new char[columnNum]; //Initialized the global currentCovered.
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//Calculate the number of additional '1' in the array based on the global array currentCovered.
	public int getAdditionalCoveredNumber(char[] a){
		int num = 0;
		for(int i=0; i<a.length; i++){
			if(a[i] == '1' && this.currentUnitsCovered[i] == '0'){
				num++;
			}
		}
		return num;
	}
	//Calculate the number of additional '1' in the array.
	// o array recebido eh uma linha com o tipo de coverage vs testes
	// retorna o numero de testes que cobrem o trecho de codigo a
	public int getCoveredNumber(char[] a){
		int num = 0;
		for(int i=0; i<a.length; i++){
			if(a[i] == '1'){
				num++;
			}
		}
		return num;
	}

	public List<Integer> getCoveredIndexes(char[] a){
		List<Integer> indexes = new ArrayList<>();
		for(int i=0; i<a.length; i++){
			if(a[i] == '1'){
				indexes.add((int)i);
			}
		}
		return indexes;
	}
	//The main function that select the test sequence.
	public int[] getSelectedTestSequence(){

		this.getCoverageMatrix(this.coverageFile);
		this.getTestSimilarity();

		int lenTests = this.CoverageMatrix.length,
			lenUnits = this.CoverageMatrix[0].length;
		int[] selectedTestSequence = new int[lenTests];
		int[] unitsCoveredByTest = new int[lenTests];
		ArrayList<Integer> testsSelected = new ArrayList<Integer>(); //Store the elements that are already selected.
		ArrayList<Integer> coveredZero = new ArrayList<Integer>(); //Store the elements in case  it covers 0 statement/method/branch.
		boolean containAllZeroRow = false;

		for(int testIndex = 0; testIndex < lenTests; testIndex++){
			unitsCoveredByTest[testIndex] = this.getCoveredNumber(this.CoverageMatrix[testIndex]);
			if(unitsCoveredByTest[testIndex] == 0){
				coveredZero.add(testIndex);//trechos de codigos que nao sao cobertos
			}
		}
		int[] originalUnitsCoveredByTest = Arrays.copyOf(unitsCoveredByTest, lenTests); //Copy of coveredNum, for the remaining elements.
		this.currentUnitsCovered = new char[lenUnits];
		this.clearArrayWithZeros(this.currentUnitsCovered);
		while(testsSelected.size() < lenTests){

			int maxTestCoveringIndex = this.selectMax(unitsCoveredByTest);
			if(maxTestCoveringIndex == -1){
				//All the Units are covered, then use the same algorithm for the left test cases.
				if(testsSelected.size() == lenTests) break;
				unitsCoveredByTest = Arrays.copyOf(originalUnitsCoveredByTest, lenTests);
				maxTestCoveringIndex = this.selectMax(unitsCoveredByTest);
				this.clearArrayWithZeros(this.currentUnitsCovered);
			}

			if(maxTestCoveringIndex == -1){
				containAllZeroRow = true;
				break;
			}
			originalUnitsCoveredByTest[maxTestCoveringIndex] = 0;
			//selectedTestSequence[i] = maxIndex;
			testsSelected.add(maxTestCoveringIndex);
			int testsSelectedTemp[] = new int[lenTests];
			for (int i = 0; i < this.testSimilarity[maxTestCoveringIndex].length; i++) {
				if(i == maxTestCoveringIndex || testsSelected.contains(i)) continue;
				if(this.testSimilarity[maxTestCoveringIndex][i] >= 0.5){
					originalUnitsCoveredByTest[i] = 0;
					testsSelected.add(i);
				}
			}
			this.mergeIntoCurrentArray(this.currentUnitsCovered, this.CoverageMatrix[maxTestCoveringIndex]);

			for(int j=0; j<lenTests; j++){
				if(testsSelected.contains(j)){
					unitsCoveredByTest[j] = 0;
				}else{
					unitsCoveredByTest[j] = this.getAdditionalCoveredNumber(this.CoverageMatrix[j]);
				}
			}
		}

		if(containAllZeroRow){//For this algorithm, put all the zero covered test case to the end
			for(int i=0; i<coveredZero.size(); i++){
				testsSelected.add(coveredZero.get(i));
			}
		}
		for(int i=0; i<lenTests; i++){
			selectedTestSequence[i] = testsSelected.get(i);
		}
		return selectedTestSequence;
	}
	private void getTestSimilarity() {
		Set<List<Integer>> union;
		Set<List<Integer>> intersection;
		for (int i = 0; i < CoverageMatrix.length; i++) {
			for (int j = 0; j < CoverageMatrix.length; j++) {
				union = new HashSet<List<Integer>>();
				intersection = new HashSet<List<Integer>>();
				union.addAll((List)testMapSimilarity.get(i));
				union.addAll((List)testMapSimilarity.get(j));
				intersection.addAll((List)testMapSimilarity.get(i));
				intersection.retainAll((List<Integer>)testMapSimilarity.get(j));
				this.testSimilarity[i][j] =  (float)(intersection.size()*1.0)/union.size();
			}
		}
	}

	//Select the maximum number in the array and return its index.
	public int selectMax(int[] a){
		int index = -1;
		int max = 0;
		for(int i=0; i<a.length; i++){
			if(a[i] > max){
				max = a[i];
				index = i;
			}
		}

		return index;
	}

	//Merge all the '1's in the new array into the current array.
	public void mergeIntoCurrentArray(char[] current, char[] newArray){
		if(current.length != newArray.length){
			System.out.println("ERROR: mergeIntoCurrentArray: length is not equal.");
			System.exit(1);
		}
		int len = current.length;
		for(int i=0; i<len; i++){
			if(newArray[i] == '1'){
				current[i] = newArray[i];
			}
		}
	}
	//Set all elements '0' in the array.
	public void clearArrayWithZeros(char[] a){
		for(int i=0; i<a.length; i++){
			a[i] = '0';
		}
	}
	public void Print(char[] a){
		for(int i=0; i<a.length; i++){
			if(i == a.length-1){
				System.out.print(a[i]);
			}else{
				System.out.print(a[i]+",");
			}
		}
	}
	public void Print(int[] a){
		for(int i=0; i<a.length; i++){
			if(i == a.length-1){
				System.out.print(a[i]);
			}else{
				System.out.print(a[i]+",");
			}
		}
	}
	//For Unit Test.
	public static void main(String[] args){
		GreedyAdditionalSimilarity ga = new GreedyAdditionalSimilarity(args[0], args[1]);
		ga.Print(ga.getSelectedTestSequence());

	}
}
