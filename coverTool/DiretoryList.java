package diretorytool;


import util.JunitClass;
import util.JunitList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class DiretoryList {
	List<MethodDeclaration> otherMethods;
	List<MethodDeclaration> testMethods;
	NodeList<ImportDeclaration> imports;
	Optional<PackageDeclaration> packDeclaration;
    List<FieldDeclaration> fields;
	String className;
	String bar = System.getProperty("file.separator");
    JunitList newJunitList;

	public DiretoryList (){
		this.otherMethods = new ArrayList<MethodDeclaration>();
		this.testMethods = new ArrayList<MethodDeclaration>();
		this.imports = new NodeList<>();
		this.packDeclaration = null ;
		String className = null;
		newJunitList = new JunitList();
	}

	public JunitList searchClasses(File testDir, String srcPath, String testPath, String projPath) {
		newJunitList = new JunitList();
		new DiretoryExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			try {
				new VoidVisitorAdapter<Object>() {

					@Override
					public void visit(CompilationUnit cu, Object arg) {
						otherMethods = new ArrayList<MethodDeclaration>();
						testMethods = new ArrayList<MethodDeclaration>();
						imports = new NodeList<>();
						packDeclaration = null ;
						className = null;
						
						className = cu.getType(0).getName().toString();
						imports = cu.getImports();
						packDeclaration = cu.getPackageDeclaration();
					
						NodeList<TypeDeclaration<?>> types = cu.getTypes();
						for (TypeDeclaration<?> typeDeclaration : types) {
							if (typeDeclaration instanceof ClassOrInterfaceDeclaration){
								ClassOrInterfaceDeclaration foundClass = (ClassOrInterfaceDeclaration) typeDeclaration;
								if(isJunit3(foundClass)){
									fields = foundClass.getFields();
									for(MethodDeclaration method : foundClass.getMethods()){
										if(method.getNameAsString().contains("test")){
											testMethods.add(method);
										}else otherMethods.add(method);
									}

								}else if(isJunit4(foundClass)){
									for(MethodDeclaration method : foundClass.getMethods()){
										fields = foundClass.getFields();
										if(method.getAnnotations().size() > 0){
											NodeList<AnnotationExpr> annotationList = method.getAnnotations();
											if(findAnnotation(annotationList, "@Test"))
												testMethods.add(method);
											else otherMethods.add(method);
										}else otherMethods.add(method);
									}
								}
							}
						}

						try {
							createTempClasses (srcPath, testPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						super.visit(cu, arg);

					}

				}.visit(JavaParser.parse(file), null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();	
			} 
			
		}).explore(testDir); 
		
		return newJunitList;
	}

	//	private void createTempClasses(CompilationUnit cu) {
	//		for (MethodDeclaration test : testMethods) {
	//			CompilationUnit cuClone = cu.clone();
	//			NodeList<TypeDeclaration<?>> types = cu.getTypes();
	//			for (TypeDeclaration<?> typeDeclaration : types) {
	//				if (typeDeclaration instanceof ClassOrInterfaceDeclaration){
	//					ClassOrInterfaceDeclaration foundClass = (ClassOrInterfaceDeclaration) typeDeclaration;
	//					if(isJunit3(foundClass)){
	//						for(MethodDeclaration method : foundClass.getMethods()){
	//							if(method.getNameAsString().contains("test") && method.toString().equals(test.toString())){
	//								cuClone.remove(method);
	//							}
	//						}
	//					}else if(isJunit4(foundClass)){
	//						for(MethodDeclaration method : foundClass.getMethods()){
	//							if(method.getAnnotations().size() > 0){
	//								NodeList<AnnotationExpr> annotationList = method.getAnnotations();
	//								if(findAnnotation(annotationList, "@Test") && method.toString().equals(test.toString())){
	//									method.remove();
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}
	//		}
	//		
	//	}

	private void createTempClasses(String srcPath, String testPath) throws IOException {
		for (MethodDeclaration test : testMethods) {
			String tempTestClassName = className + "$" + test.getNameAsString();

			try {
				String pathTempClass = getRemainedPath(new File(srcPath + bar + testPath),className + ".java");
				String newClassName = tempTestClassName +".java";
				
				JunitClass newJunit = new JunitClass(tempTestClassName,pathTempClass);
				newJunit.getJunitMethods().add(test);
				newJunitList.addJunitClass(newJunit);

				File f = new File (pathTempClass,newClassName);
				f.createNewFile();
				System.out.println(pathTempClass);

				PrintWriter writer = new PrintWriter(f, "UTF-8");
				if (packDeclaration.isPresent())
					writer.println(packDeclaration.get().toString());
				for (ImportDeclaration importDeclaration : imports) {
					writer.println(importDeclaration.toString());
				}
				writer.println("import static org.junit.Assert.*;");

				writer.println("public class "+ tempTestClassName + "{");

				for (FieldDeclaration field : fields) {
					writer.println(field.toString());
				}
				
				writer.println(test.toString());

				for (MethodDeclaration otherMeth : otherMethods) {
					writer.println(otherMeth.toString());
				}
				writer.println("}");
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private String getRemainedPath(File testPath,String name) {
		System.out.println(name);
		File[] list = testPath.listFiles();
	    if (list != null) {
	        for (File fil : list) {
	            String path = null;
	            if (fil.isDirectory()) {
	                path = getRemainedPath(fil, name);
	                if (path != null) {
	                    return path;
	                }
	            } else if (fil.getName().contains(name)) {
	                path =fil.getParent();
	                if (path != null) {
	                    return path;
	                }
	            }
	        }
	    }
	    return null; 
	}
	
	
	private boolean isJunit3(ClassOrInterfaceDeclaration foundClass){
		if(foundClass.getExtendedTypes().size() > 0){
			for(ClassOrInterfaceType classf : foundClass.getExtendedTypes()){
				if(classf.getNameAsString().equals("TestCase"))
					return true;
			}
		}
		return false;
	}

	private boolean isJunit4(ClassOrInterfaceDeclaration foundClass){
		for(MethodDeclaration method : foundClass.getMethods()){
			if(method.getAnnotations().size() > 0){
				NodeList<AnnotationExpr> annotationList = method.getAnnotations();
				if(findAnnotation(annotationList, "@Test"))
					return true;
			}
		}
		return false;
	}

	private boolean findAnnotation(NodeList<AnnotationExpr> annotationList, String string) {
		for (AnnotationExpr annotationExpr : annotationList) {
			if (annotationExpr.toString().equals(string))
				return true;
		}
		return false;
	}

	public void makeTempDirectory(String path){
		File file = new File(path + "/" + "Temp");
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}

	}





}
