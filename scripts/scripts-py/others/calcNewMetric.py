import json
import random
import sys
import io
import os
try:
    to_unicode = unicode
except NameError:
    to_unicode = str

import calc_metrics

def versionOfProject(projectName):
    versionsFile = open(projectName+"/coverage/sorted_version.txt")
    versions = []
    for version in versionsFile:
        version = version.replace("\n", "")
        versions.append(version)
    return versions

PATH = os.getcwd()
os.chdir(PATH)
projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
covLevel = ["statement", "method", "branch"]

resultFile = open("dataframe_apfd_mSprading.data", "w")
resultFile.write("project,version,apfd,covLevel,algorithm\n");
for project in projects:
    versionsAux = versionOfProject(project)
    lenVersion = len(versionsAux)
    for idx, version in enumerate(versionsAux):
        for covType in covLevel:
            with open(project+"/faults-groups/"+version+"/apfd_"+covType+".json") as data_file:
                jsonDataApfd = json.load(data_file)
            with open(project+"/faults-groups/"+version+"/mean-spreading_"+covType+".json") as data_file:
                jsonDataSpreading = json.load(data_file)
            for algorithm in jsonDataApfd:
                newMetricValue = calc_metrics.genNewMetric(jsonDataApfd[algorithm], jsonDataSpreading[algorithm])
                for apfdSpredingValue in newMetricValue:
                    result = project+",v"+str(lenVersion-idx)+","+str(apfdSpredingValue)+","+covType+","+algorithm
                    resultFile.write(result+"\n")
resultFile.close()
