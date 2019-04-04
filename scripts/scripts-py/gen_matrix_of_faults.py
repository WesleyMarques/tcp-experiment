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

def init_matrix(row, column):
    result = []
    for i in range(row):
        result.append([])
        for j in range(column):
            result[i].append(0)
    return result

def convertTime(value):
    return int(value.replace("ms", ""))

def get_fault_groups(faultGroupsFile):
    fGroups = []
    for index,faults in enumerate(faultGroupsFile):
        faults = faults.replace("\n", "")
        splitLine = faults.split(" ")
        if len(splitLine[-1]) == 0: splitLine = splitLine[0:-1]
        fGroups.append(splitLine)
    return fGroups

def get_info_test_execution(testsFile):
    testMat = []
    testMatTime = []
    testLen = 0
    for line in testsFile:
        line = line.replace("\n", "").strip().split(' ')
        testName = line[0]
        testMat.append(testName)
        testLen += 1
        testTimes = map(lambda valueTime: int(valueTime.replace("m", "").replace("s", "")), line[1:])
        testMatTime.append(median(testTimes))
    return testMat, testMatTime, testLen

def get_tests_by_mutants(testsFile):
    tests2Mut = {}
    for testMut in testsFile:
        testMut = testMut.replace("\n", "")
        splitLine = testMut.split(" ")
        testName = splitLine[0]
        mutants = splitLine[1:]
        if testName not in testMat:
            continue
        tests2Mut[testName] = mutants #testName identify mutants
    return tests2Mut

def tests_mutants_transpose(tests2Mut, currentGroup, testMat, resultMatrix):
    flag = False
    contF = contT = 0
    for test in tests2Mut:
        for mutante in tests2Mut[test]:
            if mutante in currentGroup:
                flag = True
                testIdx = testMat.index(test)
                resultMatrix[testIdx][currentGroup.index(mutante)] = 1
                contT += 1
            else:
                contF += 1
    return flag and zip(*resultMatrix)

PATH = os.getcwd()
os.chdir(PATH)

projects = [ "scribe-java", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor", "jasmine-maven-plugin", "assertj-core"][6:]
covLevel = ["statement", "method", "branch"]
# algorithms = ["ARTMaxMin", "Genetic", "GreedyTotal", "GreedyAdditional", "GreedyAdditionalSimilarity", "AdditionalTotal", "GreedyAdditionalNew"][:4]
algorithms = ["GreedyAdditionalSimilarity"]
projectName = projects[int(sys.argv[1])]
covType = covLevel[int(sys.argv[2])]
METRIC = sys.argv[3]

print "START===> "+projectName
versions = open(PATH+"/data/"+projectName+"/coverage/sorted_version.txt")
for version in versions:
    version = version.replace("\n", "")
    # with open(PATH+"/data/"+projectName+"/coverage/"+version+"/priorization_"+covType+".json") as data_file:
    print PATH+"/data/priorizationData/%s_%s_%s_priorization.json" % (projectName, covType, version)
    try:
        with open(PATH+"/data/priorizationData/%s_%s_%s_priorization.json" % (projectName, covType, version)) as data_file:
            priorizationTests = json.load(data_file)
    except Exception as e:
        print e
        continue
    faultGroupsFile = open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/faults-groups.data", "r")
    faultGroupsLen = sum(1 for line in faultGroupsFile)

    #Get schedule of test execution
    with open(PATH+"/data/"+projectName+"/coverage/"+version+"/running_time.txt", "r") as testsFile:
            testMat,testMatTime,testLen = get_info_test_execution(testsFile)
    #end

    #Get all groups of Faults
    with open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/faults-groups.data", "r") as faultGroupsFile:
        faultGroups = get_fault_groups(faultGroupsFile)
    #end

    #get Matriz of tests by faults
    with open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/test-mut.data", "r") as testsFile:
        tests2Mut = get_tests_by_mutants(testsFile)

    faultGroupSize = len(faultGroups[0])
    metricResults = {}
    for currentGroup in faultGroups:
        resultMatrix = init_matrix(testLen, faultGroupSize)
        mutsTranspose = tests_mutants_transpose(tests2Mut, currentGroup, testMat, resultMatrix)
        if not mutsTranspose:
            continue
        # here I need to create all possibility GreedyAdditionalSimilarity_[0-1]_[0-1]
        limiares = range(0,101,5)
        maxTests = range(0,101,5)
        for alg in algorithms:
            for limiar in limiares:
                limiar = limiar/100.0
                for maxTest in maxTests:
                    maxTest = maxTest/100.0
                    algFormat = "%s_%s_%s" % (alg,limiar,maxTest)
                    if algFormat not in metricResults:
                            metricResults[algFormat] = []
                    testsPrior = priorizationTests[algFormat]
                    testsPrioritizated = list(map(int, testsPrior.split(",")))
                    metricResult = calc_metrics.chooseMetric(METRIC, mutsTranspose, testsPrioritizated, projectName, version)
                    metricResults[algFormat].append(metricResult)

    with open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/"+METRIC+"_"+covType+".json", 'w') as outfile:
        str_ = json.dumps(metricResults,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
        outfile.write(to_unicode(str_))
        outfile.close()
print "FINISH===> "+projectName
