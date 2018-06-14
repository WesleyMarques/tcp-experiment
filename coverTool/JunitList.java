package util;

import java.util.ArrayList;

public class JunitList {

	private ArrayList<JunitClass> junitList;
	
	public JunitList(){
		this.junitList = new ArrayList<JunitClass>();
	}

	public ArrayList<JunitClass> getJunitList() {
		return junitList;
	}

	public void setJunitList(ArrayList<JunitClass> junitList) {
		this.junitList = junitList;
	}
	
	public void addJunitClass(JunitClass jClass){
		this.getJunitList().add(jClass);
	}
}
