import json
import sys
from pprint import pprint
import calc_metrics
import os
import io
from numpy import median

try:
    to_unicode = unicode
except NameError:
    to_unicode = str


def get_tests_by_mutants(testsFile):
    tests2Mut = {}
    mutantsSet = {}
    for testMut in testsFile:
        testMut = testMut.replace("\n", "")
        splitLine = testMut.split(" ")
        testName = splitLine[0]
        mutants = splitLine[1:]
        if testName not in testsName:
            continue
        tests2Mut[testName] = mutants  # testName identify mutants
        for mutant in mutants:
            mutantsSet[mutant] = 1
    return tests2Mut,len(mutantsSet)


def get_info_test_execution(testsFile):
    testMat = []
    testMatTime = []
    testLen = 0
    for line in testsFile:
        line = line.replace("\n", "").strip().split(' ')
        testName = line[0]
        testMat.append(testName)
        testLen += 1
        testTimes = map(lambda valueTime: int(
            valueTime.replace("m", "").replace("s", "")), line[1:])
        testMatTime.append(median(testTimes))
    return testMat, testMatTime, testLen

def get_coverage_matrix(coverageFile):
    coverageMatrix = [];
    for line in coverageFile:
        coverageMatrix.append(list(line))
    return coverageMatrix, len(coverageMatrix[0])

def covered_number(coverageTest):
    return coverageTest.count("1");

PATH = os.getcwd()
os.chdir(PATH)

projects = ["scribe-java", "jasmine-maven-plugin", "java-apns",
            "jopt-simple", "la4j", "metrics-core", "vraptor", "assertj-core"]
covLevels = ["statement", "method", "branch"]

with open(PATH+"/result.data", 'w+') as outfile:
    outfile.write("project, version, coverage, test, faults, faultsTax, covered, coveredTax\n")
    for project in projects:
        with open(PATH + "/data/" + project + "/coverage/sorted_version.txt") as versionFile:
            versions = [line.rstrip('\n') for line in versionFile]
        for version in versions:
            for coverage in covLevels:
                with open(PATH + "/data/" + project + "/coverage/" + version + "/running_time.txt", "r") as testsFile:
                    testsName, testsTime, testLen = get_info_test_execution(testsFile)
                with open(PATH + "/data/" + project + "/faults-groups/" + version + "/test-mut.data", "r") as testsFile:
                    tests2Mut, mutantsTotal = get_tests_by_mutants(testsFile)
                with open(PATH + "/data/" + project + "/coverage/" + version + "/"+ coverage +"_matrix.txt", "r") as coverageFile:
                    coverageMatrix, coverageTotal = get_coverage_matrix(coverageFile)
                cont = 0
                amount = []
                for test in tests2Mut:
                    faults = len(tests2Mut[test])
                    covered = covered_number(coverageMatrix[cont])
                    lineToWrite = "%s,%s,%s,%s,%s,%s,%s,%s\n" % (project, version, coverage, test, str(faults), str(1.0*faults/mutantsTotal), str(covered), str(1.0*covered/coverageTotal))
                    amount.append(len(tests2Mut[test]))
                    cont += 1
                    outfile.write(lineToWrite)
outfile.close()
# print median(amount)
