import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AdditionalTotal {
	String Directory;
	String matrixFile;
	String coverageFile, complexityFile, codeLinesFile;
	char[][] CoverageMatrix;
	final String sep = File.separator;
	char[] currentCovered;
	int[] prob, priority;
	List<Integer> methodComplexity;
	int maxComplexity;
	private boolean[] selected;

	private ArrayList<Integer> getCodeComplexity(int methodSize) throws IOException{
		BufferedReader br_code;
		BufferedReader br_complexity;
		Map <String, Integer> complexityMap = new HashMap<String, Integer>();
		ArrayList<Integer> result = new ArrayList<Integer>(Collections.nCopies(methodSize, 0));
		try{
			br_code = new BufferedReader(new FileReader(codeLinesFile));
			br_complexity = new BufferedReader(new FileReader(codeLinesFile));
		}catch(Exception ex){
			return result;
		}
		maxComplexity = 0;
		int complexityValue = 0;
		String methodName;
		String[] lineSplit;
		String line;
		while((line = br_complexity.readLine()) != null){
			lineSplit = line.split(":");
			complexityValue = Integer.parseInt(lineSplit[lineSplit.length-1]);
			methodName = String.join(":", Arrays.copyOfRange(lineSplit, 0, lineSplit.length - 1));
			complexityMap.put(String.join(":", methodName), complexityValue);
			if(complexityValue > maxComplexity){
				maxComplexity = complexityValue;
			}
		}
		while((line = br_code.readLine()) != null){
			lineSplit = line.split(":");
			methodName = String.join(":", Arrays.copyOfRange(lineSplit, 0, lineSplit.length - 1));
			if(complexityMap.containsKey(methodName)){
				result.add(complexityMap.get(methodName));
			}else{
				result.add(0);
			}
		}
		return result;
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


	public AdditionalTotal(String Directory, String matrixFile) {
		this.Directory = Directory; // get the directory to Create a output file
									// for Statistic Data.
		this.matrixFile = matrixFile; // Create a new file use the same file
										// prefix for Statistic Data.
		this.coverageFile = Directory+this.sep+matrixFile+"_matrix.txt";
		this.codeLinesFile = Directory+this.sep+matrixFile+"_index.txt";
		this.complexityFile = Directory+this.sep+"method-complexity.txt";
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
			this.CoverageMatrix = new char[tempAl.size()][columnNum];

			// Store the information in the ArrayList to the Array.
			for (int i = 0; i < tempAl.size(); i++) {
				CoverageMatrix[i] = tempAl.get(i).toCharArray();
			}

			this.currentCovered = new char[columnNum]; // Initialized the global
														// currentCovered.
			this.methodComplexity = getCodeComplexity(columnNum);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initialize(int n, int m){
		this.prob = new int[m];
		this.selected = new boolean[n];
		this.priority = new int[n];
		for (int i = 0; i < m; i++) {
			this.prob[i] = 1;
		}
		for (int i = 0; i < n; i++) {
			this.selected[i] = false;
		}
	}

	public int[] getSelectedTestSequence(){
		this.getCoverageMatrix(coverageFile);
		int n = this.CoverageMatrix.length, m = this.CoverageMatrix[0].length; //m = numero de coverageType, n = número de testes
		this.initialize(n, m);
		int k, sum, s;
		for (int i = 0; i < n; i++) {
			k = 0;
			while(k < n && this.selected[k]){
				k++;
			}
			sum = 0;
			for (int j = 0; j < m; j++) {
				if(this.CoverageMatrix[k][j] == '1'){
					sum += this.prob[j];
				}
			}

			for (int l = k+1; l < n; l++) {
				if(!this.selected[l]){
					s = 0;
					for (int j = 0; j < m; j++) {
						if(this.CoverageMatrix[l][j] == '1'){
							s += this.prob[j];
						}
					}
					if(s > sum){
						sum = s;
						k = l;
					}
				}
			}
			this.priority[i] = k;
			this.selected[k] = true;
			for (int j = 0; j < n; j++) {
				if(this.CoverageMatrix[j][k] == '1'){
					this.prob[j] *= (getProb(k));
				}
			}
		}
		return this.priority;
	}

	private float getProb(int methodIndex){
		return (float)1 - (float)(0.5 * (this.methodComplexity.get(methodIndex)/Math.max(this.maxComplexity,1)));
	}

	public void print(int[] a){
		for(int i=0; i<a.length; i++){
			if(i == a.length-1){
				System.out.print(a[i]);
			}else{
				System.out.print(a[i]+",");
			}
		}
	}

	public static void main(String[] args) {
		AdditionalTotal ga = new AdditionalTotal(args[0], args[1]);
//
//		String path = "/home/wesleynunes/Documentos/workspaceMastering/tcp-experiment/data/scribe-java/coverage/6ae769e35e8319747a702a4962df31650303a98e";
//		String coverageStatement = "method";
//		AdditionalTotal ga = new AdditionalTotal(path, coverageStatement);
		int[] priorization = ga.getSelectedTestSequence();
		ga.print(priorization);
	}

}
