import json
import random
import sys
import io
import os
try:
    to_unicode = unicode
except NameError:
    to_unicode = str

def versionOfProject(projectName):
    versionsFile = open(PATH+"/data/"+projectName+"/coverage/sorted_version.txt")
    versions = []
    for version in versionsFile:
        version = version.replace("\n", "")
        versions.append(version)
    return versions

mapAlgorithm = {
"ARTMaxMin": "ART",
"GreedyTotal": "Total",
"GreedyAdditional": "Additional",
"GreedyAdditionalNew": "AdditionalNew",
"GreedyAdditionalSimilarity": "AdditionalSimilarity",
"Genetic": "Search-Based",
"AdditionalTotal":"AdditionalTotal"
}

def getAlgorithmName(alg, covType):
    return mapAlgorithm[alg]+'-'+covType

PATH = os.getcwd()
os.chdir(PATH)
projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
covLevel = ["statement", "method", "branch"]

resultFile = open("dataframe_mean_spreading.data", "w")
resultFile.write("project,version,meanSpreading,covLevel,algorithm\n");
for project in projects:
    versionsAux = versionOfProject(project)
    lenVersion = len(versionsAux)
    for idx, version in enumerate(versionsAux):
        for covType in covLevel:
            with open(PATH+"/data/"+project+"/faults-groups/"+version+"/mean-spreading_"+covType+".json") as data_file:
                jsonData = json.load(data_file)
            for algorithm in jsonData:
                for idxS, meanSpreading in enumerate(jsonData[algorithm]):
                    if meanSpreading == 0: continue
                    result = project+",v"+str(lenVersion-idx)+","+str(meanSpreading)+","+covType+","+getAlgorithmName(algorithm,covType)
                    print result
                    resultFile.write(result+"\n")
resultFile.close()
