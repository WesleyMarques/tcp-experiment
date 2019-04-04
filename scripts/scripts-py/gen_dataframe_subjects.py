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

PATH = os.getcwd()
os.chdir(PATH)

projects = ["scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor", "assertj-core"][:-1]
covLevel = ["statement", "method", "branch"]

resultFile = open("dataframe_subjects.data", "w")
resultFile.write("project,version,coverage,covered,uncovered,amount\n")
for project in projects:
    versionsAux = versionOfProject(project)
    lenVersion = len(versionsAux)
    for covType in covLevel:
        metricFile = "%s/subjects_info/%s_%s.json" % (PATH,project,covType)
        with open(metricFile) as data_file:
            jsonData = json.load(data_file)
            for version in jsonData:
                currVersion = version.split("_")[1]
                for data in jsonData[version]:
                    result = "%s,%s,%s,%s,%s,%s" % (project, currVersion, covType, data["um"], data["zero"], (int(data["um"])+int(data["zero"])))
                    resultFile.write(result + "\n")
resultFile.close()
