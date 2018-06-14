package xmltool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import coverage.ClassData;
import coverage.CounterTypeData;
import coverage.CoverageData;
import coverage.LineData;
import coverage.MethodData;
import coverage.PackageData;
import coverage.SourceFileData;

public class XmlTool {
	
	public static CoverageData buildCoveryObject(File xmlFile,File matrixData) throws ParserConfigurationException, SAXException, IOException{
		FileWriter fStream = new FileWriter(matrixData,true);
		BufferedWriter buffWriter = new BufferedWriter(fStream);
		//Para conseguir manipular o XML.
		  DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
		  Document doc = dBuilder.parse(xmlFile);
		 
				  CoverageData coverD = new CoverageData("");
				  //Retirando o valor da tag report
				  NodeList reportN = doc.getElementsByTagName("report");
				  for(int r = 0; r < reportN.getLength(); r++){
					  Node nodeAux = reportN.item(r);
					  if(nodeAux.getNodeType() == Node.ELEMENT_NODE){
						  Element report = (Element) nodeAux;
						  String reportName = xmlFile.getName().replaceAll("report", "").replaceAll(".xml", "");
						  coverD.setReportName(reportName);
						 
					  }
				  }
				  
				  NodeList resume = doc.getElementsByTagName("counter");
				  for(int temp = 0; temp < resume.getLength(); temp++){
					  Node nNode = resume.item(temp);
					  if(nNode.getNodeType() == Node.ELEMENT_NODE){
						  Element eElement = (Element) nNode;
						  String type = eElement.getAttribute("type");
						  String missed = eElement.getAttribute("missed");
						  String covered = eElement.getAttribute("covered");
						  CounterTypeData newCounter = new CounterTypeData(type,missed,covered);
						  coverD.addResumeCounters(newCounter);
					  }
				  }
				  
				  
				  
				  //Retirando informações de pacotes,classes,metodos e counters
				  NodeList packgList = doc.getElementsByTagName("package");
				  for(int f = 0; f < packgList.getLength(); f++){
					  Node node1 = packgList.item(f);
					  if(node1.getNodeType() == Node.ELEMENT_NODE){
						  Element packg = (Element) node1;
						  String packageName = packg.getAttribute("name");
						  PackageData pckgD = new PackageData(packageName);
						  coverD.addPackage(pckgD);
						  
						  NodeList sourceList = packg.getElementsByTagName("sourcefile");
						  for(int a = 0; a < sourceList.getLength(); a++){
								  Node node6 = sourceList.item(a);
								  if(node6.getNodeType() == Node.ELEMENT_NODE){
									  Element srcF = (Element) node6;
									  String sourceFileName = srcF.getAttribute("name");
									  if(sourceFileName.equals(""))break;
									  SourceFileData newSource = new SourceFileData(sourceFileName);
									  pckgD.addSourceFiles(newSource);
									  
									  NodeList lineList = srcF.getElementsByTagName("line");
									  NodeList countersList = srcF.getElementsByTagName("counter");
									  for(int b = 0; b < lineList.getLength(); b++){
										  Node node7 = lineList.item(b);
										  if(node7.getNodeType() == Node.ELEMENT_NODE){
											  Element line = (Element) node7;
											  String numberLines = line.getAttribute("nr");
											  String missedInfo = line.getAttribute("mi");
											  String coverInfo = line.getAttribute("ci");
											  String missedBranch = line.getAttribute("mb");
											  String coverBranch = line.getAttribute("cb");
											  if(numberLines.equals("")) break;
											  if(missedInfo.equals("")) break;
											  if(coverInfo.equals("")) break;
											  if(missedBranch.equals("")) break;
											  if(coverBranch.equals("")) break;
											  LineData newLine = new LineData(numberLines,missedInfo,coverInfo,missedBranch,coverBranch);
											  newSource.addLines(newLine);
										  }
									  }
									  for(int c = 0; c < countersList.getLength(); c++){
										  Node node8 = countersList.item(c);
										  if(node8.getNodeType() == Node.ELEMENT_NODE){
											   Element counterSource = (Element) node8;
											   String counterSType = counterSource.getAttribute("type");
											   String counterSMissed = counterSource.getAttribute("missed");
											   String counterSCovered = counterSource.getAttribute("covered");
											   if(counterSType.equals("")) break;
											   if(counterSMissed.equals("")) break;
											   if(counterSCovered.equals("")) break;
											   CounterTypeData newCounterS = new CounterTypeData(counterSType,counterSMissed,counterSCovered);
											   newSource.addCounterTypes(newCounterS);
										  }
									  }
								  }
						  }
						  
						  
						  NodeList classList = packg.getElementsByTagName("class");
						  for(int i = 0; i < classList.getLength(); i++){
							  Node node2 = classList.item(i);
							  if(node2.getNodeType() == Node.ELEMENT_NODE){
								  Element cls = (Element) node2;
								  String className = cls.getAttribute("name");
								  if(className.equals(""))break;
								  ClassData classD = new ClassData(className);
								  pckgD.addClasses(classD);
								  
						  
								  NodeList methodList = cls.getChildNodes();
								  String line = "";
								  for(int k = 0; k < methodList.getLength(); k++){
									  Node node3 = methodList.item(k);
									  if(node3.getNodeType() == Node.ELEMENT_NODE){
										  Element method = (Element) node3;
										  String methodLine = method.getAttribute("line");
										  String methodName = method.getAttribute("name");
										  String methodDesc = method.getAttribute("desc");
										  if(methodName.equals("")) break;
										  if(methodLine.equals("")) break;
										  if(methodDesc.equals("")) break;
										  MethodData methodD = new MethodData(methodName,methodDesc,methodLine);
										  classD.addMethods(methodD);
										  NodeList counterList = method.getChildNodes();
										  for(int j = 0; j < counterList.getLength(); j++){
											  Node node4 = counterList.item(j);
											  if(node4.getNodeType() == Node.ELEMENT_NODE){
												 
												  Element counter = (Element) node4;
												  String counterType = counter.getAttribute("type");
												  String counterMissed = counter.getAttribute("missed");
												  String counterCovered = counter.getAttribute("covered");
												  if(counterType.equals("")) break;
												  if(counterMissed.equals("")) break;
												  if(counterCovered.equals("")) break;
												  
												  if(!cls.getAttribute("name").contains("Test"))
													  if(counterType.equals("METHOD"))
														  if(!counter.getAttribute("covered").equals(""))
															  line += counter.getAttribute("covered");
												  
												  CounterTypeData counterD = new CounterTypeData(counterType,counterMissed,counterCovered);
												  methodD.addCounter(counterD);
											  }
										  }
										  
									  }
									  
								  }
								  if(!line.equals(""))
									  buffWriter.write(line + "\n");
							  }
						  }
					  }
				  }

		  buffWriter.close();
		  //Retorno do Objeto CoverD completo
		  return coverD;
		  
	 }
	
				  
}
	

