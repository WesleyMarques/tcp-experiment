import sys
import io
import os
try:
    to_unicode = unicode
except NameError:
    to_unicode = str

projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
covLevel = ["statement", "method", "branch"]

PATH = os.getcwd()
os.chdir(PATH)

project = projects[int(sys.argv[1])]
coverage = covLevel[int(sys.argv[2])]
versions = open(PATH+"/data/"+project+"/coverage/sorted_version.txt")
print "STARTING", project, coverage
for version in versions:
    version = version.replace("\n", "")
    testIndex = []
    with open(PATH+"/data/"+project+"/coverage/"+version+"/"+coverage+"_index.txt", 'r') as testFile:
        cont = 0
        for testLine in testFile:
            cont+=1
            testLine = testLine.replace("\n", "")
            if 'Test.' in testLine:
				lineSplit = testLine.split(":")
				testIndex.append(int(lineSplit[-1]))
    if len(testIndex) == 0:
        pass
    with open(PATH+"/data/"+project+"/coverage/"+version+"/"+coverage+"_matrix.txt", 'r') as matrixFile:
        testIndex = sorted(testIndex)
        fileLines = []
        for matrixLine in matrixFile:
            newLine = matrixLine
            for i in range(0,len(testIndex)):
                indexToRemove = testIndex[i]-i
                newLine = newLine[:indexToRemove]+newLine[indexToRemove+1:]
            fileLines.append(newLine)
        with open(PATH+"/data/"+project+"/coverage/"+version+"/"+coverage+"_matrix.txt", 'w+') as newMatrixFile:
            for newLine in fileLines:
                newMatrixFile.write(newLine)
print "FINISH", project, coverage
