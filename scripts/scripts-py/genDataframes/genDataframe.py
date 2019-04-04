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
    versions = []
    with open("%s/data/%s/coverage/sorted_version.txt" % (PATH, projectName)) as versionsFile:
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
    "AdditionalTotal": "AdditionalTotal"
}


def getAlgorithmName(alg, covType):
    if not mapAlgorithm.has_key(alg):
        return alg + '-' + covType
    return mapAlgorithm[alg] + '-' + covType


PATH = os.getcwd()
os.chdir(PATH)

dataFrameType = sys.argv[1]
lineOfFile = "%s,v%s,%s,%s,%s,%s,%s"

projects = ["scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor", "assertj-core"][:-1]
covLevel = ["statement", "method", "branch"]

resultFile = open("dataframe_" + dataFrameType + ".data", "w")
resultFile.write("project,version,metric,covLevel,algorithm,limiar,maxTests\n")
for project in projects:
    versionsAux = versionOfProject(project)
    lenVersion = len(versionsAux)
    for idx, version in enumerate(versionsAux):
        for covType in covLevel:
            metricFile = "%s/data/%s/faults-groups/%s/%s_%s.json" % (PATH,project,version,dataFrameType,covType)
            with open(metricFile) as data_file:
                jsonData = json.load(data_file)
            for algorithm in jsonData:
                try:
                    simpleAlg, limiar, maxTests = algorithm.split("_")#GreedyAdditionalSimilarity_0.05_0.0
                except:
                    print metricFile,algorithm
                    continue
                for idxS, metricValue in enumerate(jsonData[algorithm]):
                    if metricValue == 0:
                        continue
                    result = lineOfFile % (project, str(lenVersion - idx), str(metricValue), covType, getAlgorithmName(algorithm, covType), limiar, maxTests)
                    resultFile.write(result + "\n")
resultFile.close()
