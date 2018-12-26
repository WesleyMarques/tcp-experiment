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

def convertTime(value):
    return int(value.replace("ms", ""))

PATH = os.getcwd()
os.chdir(PATH)
projects = [ "scribe-java", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor"]
covLevel = ["statement", "method", "branch"]
algorithms = ["ARTMaxMin", "Genetic", "GreedyAdditionalNew", "GreedyTotal", "GreedyAdditional", "GreedyAdditionalSimilarity", "AdditionalTotal"][3:]
projectName = projects[int(sys.argv[1])]
covType = covLevel[int(sys.argv[2])]
METRIC = sys.argv[3]

print "START===> "+projectName
versions = open(PATH+"/data/"+projectName+"/coverage/sorted_version.txt")
for version in versions:
    version = version.replace("\n", "")
    with open(PATH+"/data/"+projectName+"/coverage/"+version+"/priorization_"+covType+".json") as data_file:
        priorizationTests = json.load(data_file)

    faultGroups = open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/faults-groups.data", "r")
    faultGroupsLen = sum(1 for line in faultGroups)

    #Get schedule of test execution
    testsFile = open(PATH+"/data/"+projectName+"/coverage/"+version+"/running_time.txt", "r")
    testMat = []
    testMatTime = []
    testLen = 0;
    for line in testsFile:
        line = line.replace("\n", "").strip().split(' ')
        testName = line[0]
        testMat.append(testName)
        testLen += 1
        testTimes = map(lambda valueTime: int(valueTime.replace("m", "").replace("s", "")), line[1:])
        testMatTime.append(median(testTimes))
    #end

    fullMat = [[0 for x in range(faultGroupsLen)] for y in range(testLen)]

    #Get all groups of Faults
    faultGroups = open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/faults-groups.data", "r")
    fGroups = []
    for index,faults in enumerate(faultGroups):
        faults = faults.replace("\n", "")
        splitLine = faults.split(" ")
        if len(splitLine[-1]) == 0: splitLine = splitLine[0:-1]
        fGroups.append(splitLine)
    #end

    tests2Mut = {}
    testsFile = open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/test-mut.data", "r")
    for testMut in testsFile:
        testMut = testMut.replace("\n", "")
        splitLine = testMut.split(" ")
        testName = splitLine[0]
        mutants = splitLine[1:]
        if testName not in testMat:
            continue
        tests2Mut[testName] = mutants #testName identify mutants

    gSize = len(fGroups[0])
    metricResult = {}
    for groupTime in fGroups:
        tempArr = [[0 for x in range(gSize)] for y in range(testLen)]
        flag = False
        for test in tests2Mut:
            for mutante in tests2Mut[test]:
                if mutante in groupTime:
                    flag = True
                    testIdx = testMat.index(test)
                    tempArr[testIdx][groupTime.index(mutante)] = 1
        mutsTranspose = zip(*tempArr)
        if not flag:
            continue
        for alg in algorithms:
            if alg not in metricResult:
                    metricResult[alg] = []
            additional = priorizationTests[alg]
            TESTES_PRIOR = list(map(int, additional.split(",")))
            if METRIC == "apfd":
                metricResult[alg].append((calc_metrics.genAPFDValue(mutsTranspose, TESTES_PRIOR, projectName, version)))
            elif METRIC == "spreading":
                metricResult[alg].append((calc_metrics.genSpreading(mutsTranspose, TESTES_PRIOR, projectName, version)))
            elif METRIC == "mean-spreading":
                metricResult[alg].append((calc_metrics.genMeanSpreading(mutsTranspose, TESTES_PRIOR, projectName, version)))
            elif METRIC == "exec-time":
                time = calc_metrics.genTimeByGroupOfFaults(mutsTranspose, TESTES_PRIOR, projectName, version, testMatTime)
                metricResult[alg].append(time)
            elif METRIC == "group-spreading":
                metricResult[alg].append((calc_metrics.genGroupSpreading(mutsTranspose, TESTES_PRIOR, projectName, version)))
    with open(PATH+"/data/"+projectName+"/faults-groups/"+version+"/"+METRIC+"_"+covType+".json", 'w') as outfile:
        str_ = json.dumps(metricResult,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
        outfile.write(to_unicode(str_))
        outfile.close()
print "FINISH===> "+projectName
