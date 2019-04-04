import json
import os.path, subprocess
import os
import io
import sys

try:
    to_unicode = unicode
except NameError:
    to_unicode = str

PATH = os.getcwd()
os.chdir(PATH)

projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
covLevel = ["statement", "method", "branch"]

project = projects[int(sys.argv[1])]
coverage = covLevel[int(sys.argv[2])]

versions = open(PATH+"/data/"+project+"/coverage/sorted_version.txt")
coverageMatrix = []
projectResults = {}

for version in versions:
    coverageMatrix = []
    version = version.replace("\n", "")
    with open(PATH+"/data/"+project+"/coverage/"+version+"/"+coverage+"_matrix.txt", 'r') as file:
        for matrixLine in file:
            matrixLine = list(matrixLine)
            newResult = {
            "zero": matrixLine.count("0"),
            "um": matrixLine.count("1")
            }
            coverageMatrix.append(newResult)
        label = project+"_"+version+"_"+coverage
        projectResults[label] = coverageMatrix
with open(PATH+"/subjects_info/"+project+"_"+coverage+".json", 'w+') as outfile:
    str_ = json.dumps(projectResults,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
    outfile.write(to_unicode(str_))
    outfile.close()
