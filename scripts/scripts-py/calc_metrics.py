def calc_apfd(values, m, n):
    return 1-(sum(values)/(m*n*1.0))+(1/(2.0*n))

def calcSpreading(tests, m, n):
    if(m <= 1):
        return 0
    somatorio = 0
    tests.sort()
    for testIdx in range(1,m):
        somatorio += tests[testIdx] - tests[testIdx-1]
    return (somatorio*1.0)/n

def calcNewMetric(apfdValue, mSpreadingValue):
    return (apfdValue+(1 - mSpreadingValue))/2.0

def genNewMetric(apfd, mSpreading):
    result = []
    maxLen = max(len(apfd), len(mSpreading))
    for i in range(maxLen):
        if(apfd[i] == 0 or mSpreading[i] == 0): continue
        result.append(calcNewMetric(apfd[i],mSpreading[i]))
    return result


def genAPFDValue(mutsTranspose, testsPrio, project, version):
    mutLived = 0
    apfd_calc = []
    for mutant in mutsTranspose:
        indexs = [index for index, value in enumerate(mutant) if value == 1]
        if len(indexs) == 0:
            mutLived += 1
            continue
        for idx, i in enumerate(testsPrio):
            if i in indexs:
                apfd_calc.append(idx+1)
                break
    if len(mutsTranspose)-mutLived <= 0 or len(testsPrio) <= 0:
        print project, version, len(testsPrio), len(mutsTranspose)-mutLived
    return calc_apfd(apfd_calc, len(mutsTranspose)-mutLived, len(testsPrio))

def genSpreading(mutsTranspose, testsPrio, project, version):
    mutLived = 0
    result = []
    for mutant in mutsTranspose:
        spreadingTests = []
        indexs = [index for index, value in enumerate(mutant) if value == 1]
        if len(indexs) == 0:
            mutLived += 1
            continue
        for idx, i in enumerate(testsPrio):
            if i in indexs:
                spreadingTests.append(idx+1)
        result.append(calcSpreading(spreadingTests, len(spreadingTests)-mutLived, len(testsPrio)))
    return result

def genGroupSpreading(mutsTranspose, testsPrio, project, version):
    mutLived = 0
    for mutant in mutsTranspose:
        spreadingTests = set()
        indexs = [index for index, value in enumerate(mutant) if value == 1]
        if len(indexs) == 0:
            mutLived += 1
            continue
        for idx, i in enumerate(testsPrio):
            if i in indexs:
                spreadingTests.add(idx+1)
    return calcSpreading(list(spreadingTests), len(spreadingTests)-mutLived, len(testsPrio))

def genMeanSpreading(mutsTranspose, testsPrio, project, version):
    mutLived = 0
    result = []
    for mutant in mutsTranspose:
        spreadingTests = []
        indexs = [index for index, value in enumerate(mutant) if value == 1]
        if len(indexs) == 0:
            mutLived += 1
            continue
        for idx, i in enumerate(testsPrio):
            if i in indexs:
                spreadingTests.append(idx+1)
        result.append(calcSpreading(spreadingTests, len(spreadingTests)-mutLived, len(testsPrio)))#spreading per fault
    return sum(result) / float(max(len(result)-result.count(0), 1))#mean of spreading in group

def genTimeByGroupOfFaults(mutsTranspose, testsPrio, project, version, testTime):
    mutLived = 0
    result = 0
    first = 0
    for mutant in mutsTranspose:
        spreadingTests = []
        indexs = [index for index, value in enumerate(mutant) if value == 1]
        if len(indexs) == 0:
            mutLived += 1
            continue
        testsGetFaults = []
        for idx, i in enumerate(testsPrio):
            if i in indexs: #if test i are in list of test that detect current fault
                testsGetFaults.append(i)
        if min(testsGetFaults) > first:
            first = min(testsGetFaults)
    idx = testsPrio.index(first)
    amount = 0
    for timeIdx in testsPrio[:idx+1]:
        amount += testTime[timeIdx]
    return amount
