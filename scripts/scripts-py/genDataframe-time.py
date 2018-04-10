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

resultFile = open("dataframe_time.data", "w")
resultFile.write("project,version,time,covLevel,algorithm\n");
for project in projects:
    versionsAux = versionOfProject(project)
    lenVersion = len(versionsAux)
    for idx, version in enumerate(versionsAux):
        for covType in covLevel:
            with open(project+"/faults-groups/"+version+"/exec-time_"+covType+".json") as data_file:
                jsonData = json.load(data_file)
            for algorithm in jsonData:
                for trash, time in enumerate(jsonData[algorithm]):
                    if time == 0: continue
                    result = project+",v"+str(lenVersion-idx)+","+str(time)+","+covType+","+algorithm
                    resultFile.write(result+"\n")

resultFile.close()
