/*
 * Use Greedy Total Algorithm for Test Case Prioritization.
 * Yafeng.Lu@utdallas.edu
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;


public class GreedyTotal {
		String Directory;
		String matrixFile;
		String coverageFile;
		char[][] CoverageMatrix;
		final String sep = File.separator;

		public GreedyTotal(String Directory, String matrixFile){
			this.Directory = Directory; //get the directory to Create a output file for Statistic Data.
			this.matrixFile = matrixFile; //Create a new file use the same file prefix for Statistic Data.
			this.coverageFile = Directory+this.sep+matrixFile;
		}

	//Read the Coverage File and Store the value to the APBC, APDC or APSC Matrix.
	public void getCoverageMatrix(String coverageFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(coverageFile));
			ArrayList<String> tempAl = new ArrayList<String>();
			int columnNum = 0;
			String line;
			//Read all the rows from the Coverage Matrix and store then in an ArrayList for further process.
			while((line = br.readLine()) != null){
				if(columnNum == 0){
					columnNum = line.length();
				}else if(columnNum != line.length()){
					System.out.println("ERROR: The line from Coverage Matrix File is WORNG.\n"+line);
					System.exit(1);
				}
				tempAl.add(line);
			}
			this.CoverageMatrix = new char[tempAl.size()][columnNum]; //Initialize the Coverage Matrix.

			//Store the information in the ArrayList to the Array.
			for(int i=0; i<tempAl.size(); i++){
				CoverageMatrix[i] = tempAl.get(i).toCharArray();
			}

			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//Calculate the number of '1' in the array.
	public int getCoveredNumber(char[] a){
		int num = 0;
		for(int i=0; i<a.length; i++){
			if(a[i] == '1'){
				num++;
			}
		}
		return num;
	}

	public int[] getSelectedTestSequence(){

		this.getCoverageMatrix(this.coverageFile);
		int len = this.CoverageMatrix.length;
		int[] selectedTestSequence = new int[len];
		int[] coveredNum = new int[len];

		for(int i=0; i<len; i++){
			coveredNum[i] = this.getCoveredNumber(this.CoverageMatrix[i]);
		}
		int[] originalCoveredNum = Arrays.copyOf(coveredNum, len);

		Arrays.sort(coveredNum);

		for(int i=len-1; i>=0; i--){
			int max = coveredNum[i];
			for(int j=0; j<len; j++){
				if(originalCoveredNum[j] == max){
					selectedTestSequence[len-i-1] = j;
					originalCoveredNum[j] = -1;
					break;
				}
			}
		}
		return selectedTestSequence;
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
	//For Unit Test
	public static void main(String[] args){
		GreedyTotal gt = new GreedyTotal(args[0], args[1]);
		gt.Print(gt.getSelectedTestSequence());
	}
}
