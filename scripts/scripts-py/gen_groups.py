import json
import random
import sys
import os

PATH = os.getcwd()
os.chdir(PATH)
projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
covLevel = ["statement", "method", "branch"]
projectId = int(sys.argv[1])


versions = open(projects[projectId]+"/coverage/sorted_version.txt")
print projects[projectId]
for version in versions:
    version = version.replace("\n", "")
    mutantsKillend = set()
    with open(projects[projectId]+"/faults-groups/"+version+"/test-mut.data", "r") as testsFile:
        for line in testsFile:
            line = line.replace("\n", "")
            splitLine = line.split(" ")
            for mut in splitLine[1:]:
                mutantsKillend.add(mut)
        mutsAleatory = list(mutantsKillend)
        random.shuffle(mutsAleatory)
        rest = len(mutsAleatory) % 5
        if rest != 0:
            mutsAleatory = mutsAleatory[:len(mutsAleatory)-rest]
        groups = []
        outputFile = open(projects[projectId]+"/faults-groups/"+version+"/faults-groups.data", "w")
        for i in range(0,len(mutsAleatory),5):
            value = ""
            for mut in mutsAleatory[i:i+5]:
                value += mut+" "
            groups.append(mutsAleatory[i:i+5])
            outputFile.write(str(value+"\n"))
        outputFile.close()
