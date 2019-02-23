import Queue
import os,sys
import utils
import time
import threading


from MyThread import myThread
import ssh_client

projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
covLevel = ["statement", "method", "branch"]

PATH = os.getcwd()
os.chdir(PATH)

hostConfig = {
    "name": "monogatari"
}

javaFile = PATH+"/scripts/cluster/GreedyAdditionalSimilarity.class"

queueSlaves = []
queuePackages = []
slavesAlive = []
slavesFree = []
SUCCESS = "success"

def buildPackageName(project, coverage, version):
    return project+"_"+coverage+"_"+version

def resetExperimentData():
    hasDir = "No such file or directory" not in utils.run_bash("ls", ["/tmp/similatiryExperiment"])
    if hasDir:
        utils.run_bash("rm", ["-rf", "/tmp/similatiryExperiment"])
    utils.run_bash("mkdir", ["/tmp/similatiryExperiment"])

def createPackages():
    resetExperimentData()
    for project in projects:
        for coverageLevel in covLevel:
            versions = open(PATH+"/data/"+project+"/coverage/sorted_version.txt")
            for version in versions:
                version = version.replace("\n", "")
                currentPackagePath = "/tmp/similatiryExperiment/"+buildPackageName(project, coverageLevel, version)
                utils.run_bash("mkdir", [currentPackagePath])
                utils.run_bash("cp", [PATH+"/data/"+project+"/coverage/"+version+"/"+coverageLevel+"_matrix.txt", currentPackagePath])
                utils.run_bash("cp", [PATH+"/data/"+project+"/coverage/"+version+"/"+coverageLevel+"_index.txt", currentPackagePath])
                utils.run_bash("cp", [javaFile, currentPackagePath])
                utils.run_bash("cp", [PATH+"/scripts/cluster/run-priorization.py", currentPackagePath])
                tempFile = open(currentPackagePath+"/input.txt", "w")
                tempFile.write(project+","+coverageLevel+","+version)
                tempFile.close()
                hostConfig["unit"] = currentPackagePath
                queuePackages.append(hostConfig)

def getSlavesAlive():
    result = []
    with open(PATH+"/scripts/cluster/slaves.txt", 'r') as outfile:
        for slave in outfile:
            slaveHost = slave.split(',')
            result = utils.run_bash("bash", [PATH+"/scripts/cluster/pingTest.sh", slaveHost[0]])
            if result.strip() == SUCCESS:
                result.append(slaveHost[0])
    return result


# slavesAlive = getSlavesAlive()
slavesFree = [True] * len(slavesAlive)
# createPackages()

queueLock = threading.Lock()
workQueue = Queue.Queue(len(queuePackages))
slavesRunning = []
threadID = 1
exitFlag = [False]
# Create new threads
for tName in slavesFree:
   thread = myThread(tName, workQueue, queueLock, exitFlag, slavesAlive, slavesFree)
   thread.start()
   slavesRunning.append(thread)

# Fill the queue
queueLock.acquire()
for package in queuePackages:
    workQueue.put(package)
    if workQueue.qsize() == len(queueSlaves):
        queueLock.release()
        while workQueue.qsize() > len(queueSlaves) - 2:
            time.sleep(10)
        queueLock.acquire()
queueLock.release()

while not workQueue.empty():
   pass

exitFlag[0] = True

for t in slavesRunning:
   t.join()
