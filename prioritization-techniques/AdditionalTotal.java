import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdditionalTotal {
	String Directory;
	String matrixFile;
	String coverageFile;
	String codeLinesFile;
	char[][] CoverageMatrix;
	final String sep = File.separator;
	char[] currentCovered;
	int[] prob, priority;
	private boolean[] selected;

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
				if (columnNum == 0) {
					columnNum = line.length();
				} else if (columnNum != line.length()) {
					System.out.println("ERROR: The line from Coverage Matrix File is WORNG.\n" + line);
					System.exit(1);
				}
				tempAl.add(removeColumns(line, indexTest));
			}
			this.CoverageMatrix = new char[tempAl.size()][columnNum];

			// Store the information in the ArrayList to the Array.
			for (int i = 0; i < tempAl.size(); i++) {
				CoverageMatrix[i] = tempAl.get(i).toCharArray();
			}

			this.currentCovered = new char[columnNum]; // Initialized the global
														// currentCovered.
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
		int m = this.CoverageMatrix.length, n = this.CoverageMatrix[0].length; //len = numero de coverageType, columnNum = número de testes
		this.initialize(n, m);
		int k, sum, s;
		for (int i = 0; i < n; i++) {
			k = 0;
			while(k < n && this.selected[k]){
				k++;
			}
			sum = 0;
			for (int j = 0; j < m; j++) {
				if(this.CoverageMatrix[j][k] == '1'){
					sum += this.prob[j];
				}
			}

			for (int l = k+1; l < n; l++) {
				if(!this.selected[l]){
					s = 0;
					for (int j = 0; j < m; j++) {
						if(this.CoverageMatrix[j][l] == '1'){
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
			for (int j = 0; j < m; j++) {
				if(this.CoverageMatrix[j][k] == '1'){
					this.prob[j] *= (1-0.8);
				}
			}
		}
		return this.priority;

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
//		String path = "/home/wesleynunes/Documentos/workspaceMastering/tcp-experiment/data/java-apns/coverage/0c13a8626967d4b4cfacab4afbcd840ee714ead8";
//		String coverageStatement = "method_matrix.txt";
//		AdditionalTotal ga = new AdditionalTotal(path, coverageStatement);
		int[] priorization = ga.getSelectedTestSequence();
		ga.print(priorization);
	}

}
