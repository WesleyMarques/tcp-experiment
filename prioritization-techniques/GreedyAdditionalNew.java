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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class GreedyAdditionalNew {
		String Directory;
		String matrixFile;
		String coverageFile;
		String codeLinesFile;
		char[][] CoverageMatrix;
		final String sep = File.separator;
		char[] currentCovered; //Record the already covered statements/methods/branches.
		static Map<Integer, List<Integer>> testCoverageList = new HashMap<>();

		public GreedyAdditionalNew(String Directory, String matrixFile){
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

			//Store the information in the ArrayList to the Array.
			for(int i=0; i<tempAl.size(); i++){
				CoverageMatrix[i] = tempAl.get(i).toCharArray();
			}

			this.currentCovered = new char[columnNum]; //Initialized the global currentCovered.
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//Calculate the number of additional '1' in the array based on the global array currentCovered.
	public int getAdditionalCoveredNumber(char[] a){
		int num = 0;
		for(int i=0; i<a.length; i++){
			if(a[i] == '1' && this.currentCovered[i] == '0'){
				num++;
			}
		}
		return num;
	}
	//Calculate the number of additional '1' in the array.
	// o array recebido é uma linha com o tipo de coverage vs testes
	// retorna o número de testes que cobrem o trecho de código a[]
	public List<Integer> getCoveredNumber(char[] a){
		List<Integer> result = new ArrayList<Integer>();
		for(int i=0; i<a.length; i++){
			if(a[i] == '1'){
				result.add(i);
			}
		}
		return result;
	}
	//The main function that select the test sequence.
	public int[] getSelectedTestSequence(){

		this.getCoverageMatrix(this.coverageFile);

		int len = this.CoverageMatrix.length, columnNum = this.CoverageMatrix[0].length; //len = numero de coverageType, columnNum = numero de testes
		int[] selectedTestSequence = new int[len];
		int[] coveredNum = new int[len];
		ArrayList<Integer> selected = new ArrayList<Integer>(); //Store the elements that are already selected.
		ArrayList<Integer> coveredZero = new ArrayList<Integer>(); //Store the elements in case  it covers 0 statement/method/branch.
		boolean containAllZeroRow = false;
		List<Integer> coveredList = new ArrayList<Integer>();
		for(int i=0; i<len; i++){
			coveredList = this.getCoveredNumber(this.CoverageMatrix[i]);
			testCoverageList.put(i, coveredList);
			coveredNum[i] = coveredList.size();
			if(coveredNum[i] == 0){
				coveredZero.add(i);//trechos de codigos que nao sao cobertos
			}
		}
		int[] originalCoveredNum = Arrays.copyOf(coveredNum, len); //Copy of coveredNum, for the remaining elements.
		this.currentCovered = new char[columnNum];
		this.clearArray(this.currentCovered);
		//System.out.println("Before:");
		//this.Print(coveredNum);
		while(selected.size() < len){

			int maxIndex = this.selectMax(coveredNum);// retorna o testes que mais cobre o statement/branch/method
			if(maxIndex == -1){//All the statements/methods/branches are covered, then use the same algorithm for the left test cases.
				if(selected.size() == len) break;
				coveredNum = Arrays.copyOf(originalCoveredNum, len);
				maxIndex = this.selectMax(coveredNum);
				this.clearArray(this.currentCovered);
			}

			if(maxIndex == -1){
				/*coveredZero.add()
				System.out.println(this.coverageFile+", "+selected.size());
				this.Print(coveredNum);*/
				containAllZeroRow = true;
				// System.out.println(this.coverageFile+" contains all 0 row.");
				break;
			}
			originalCoveredNum[maxIndex] = 0;
			//selectedTestSequence[i] = maxIndex;
			selected.add(maxIndex);
			this.mergeIntoCurrentArray(this.currentCovered, this.CoverageMatrix[maxIndex]);

			for(int j=0; j<len; j++){
				if(selected.contains(j)){
					coveredNum[j] = 0;
				}else{
					coveredNum[j] = this.getAdditionalCoveredNumber(this.CoverageMatrix[j]);
				}
			}
			//this.Print(this.currentCovered);
		}

		if(containAllZeroRow){//For this algorithm, put all the zero covered test case to the end
			for(int i=0; i<coveredZero.size(); i++){
				selected.add(coveredZero.get(i));
			}
		}
		for(int i=0; i<len; i++){
			selectedTestSequence[i] = selected.get(i);
		}
		return selectedTestSequence;
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
	public void clearArray(char[] a){
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
	static Integer maxIntersection = -1, maxTest = -1;
	public static void main(String[] args){
		GreedyAdditionalNew ga = new GreedyAdditionalNew(args[0], args[1]);
		int[] priorization = ga.getSelectedTestSequence();
//		ga.Print(priorization);
		Map<Integer, Integer> testsSimilarity = new HashMap<>();
		testCoverageList.forEach((key, value) -> {
			int currentTest = key;
			List<Integer> currentList = value;
			maxIntersection = -1;
			maxTest = -1;
			testCoverageList.forEach((key2, value2) -> {
				if(key2 != key && !testsSimilarity.containsKey(key2)){
					Set<Integer> templist = new TreeSet<>();
					templist.addAll(value);
					templist.addAll(value2);
					int diffList = (value.size()+value.size()) - templist.size();
					if(diffList > 0){
						if(diffList > maxIntersection){
							maxIntersection = diffList;
							maxTest = key2;
						}else{
							if(testCoverageList.get(maxTest).size() < testCoverageList.get(key2).size()){
								maxIntersection = diffList;
								maxTest = key2;
							}
						}


					}

				}
			});
			testsSimilarity.put(key, maxTest);
		});
		Set<Integer> result = new LinkedHashSet();
		for (int i = 0; i < priorization.length; i++) {
			result.add(priorization[i]);
			int param = priorization[i];
			while(true){
				if(testsSimilarity.containsKey(param) && !testsSimilarity.get(param).equals(-1)){
					result.add(testsSimilarity.get(param));
					param = testsSimilarity.get(param);
				}else{
					break;
				}
			}

		}
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			Integer integer = (Integer) iterator.next();
			System.out.print(integer);
			if (iterator.hasNext()) {
				System.out.print(",");
			}
		}


	}
}
