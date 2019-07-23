package util;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

public class JunitClass {
	
	private String className;
	private String classPath;
	private List<MethodDeclaration> junitMethods;
	
  
	public JunitClass(String name,String path){
		this.className = name;
		this.classPath = path;
		this.junitMethods = new ArrayList<MethodDeclaration>();
	}


	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}


	public String getClassPath() {
		return classPath;
	}


	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}


	public List<MethodDeclaration> getJunitMethods() {
		return junitMethods;
	}


	public void setJunitMethods(List junitMethods) {
		this.junitMethods = junitMethods;
	}

	
	
	
}
