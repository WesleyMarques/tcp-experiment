import Queue
import os,sys
import utils
import time
import threading
import datetime


from MyThread import myThread
import ssh_client

projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "metrics-core", "vraptor", "la4j", "assertj-core"]
covLevel = ["statement", "method", "branch"]

PATH = os.getcwd()
os.chdir(PATH)


# javaFile = PATH+"/scripts/cluster/GreedyAdditionalSimilarity.class"
javaFile = PATH+"/scripts/cluster/GreedyAdditionalSelection.class"

queueSlaves = []
queuePackages = []
slavesAlive = []
slavesFree = []
SUCCESS = "success"

base_path = "/tmp/similatiryExperiment/"

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
                hostConfig = {
                    "name": "monogatari"
                }
                version = version.replace("\n", "")
                currentPackagePath = buildPackageName(project, coverageLevel, version)
                utils.run_bash("mkdir", [base_path+currentPackagePath])
                utils.run_bash("cp", [PATH+"/data/"+project+"/coverage/"+version+"/"+coverageLevel+"_matrix.txt", base_path+currentPackagePath])
                utils.run_bash("cp", [PATH+"/data/"+project+"/coverage/"+version+"/"+coverageLevel+"_index.txt", base_path+currentPackagePath])
                utils.run_bash("cp", [javaFile, base_path+currentPackagePath])
                utils.run_bash("cp", [PATH+"/scripts/cluster/run-priorization.py", base_path+currentPackagePath])
                hostConfig["unit"] = currentPackagePath
                hostConfig["input"] = project+","+coverageLevel+","+version
                queuePackages.append(hostConfig)

def getSlavesAlive():
    slaves = []
    with open(PATH+"/scripts/cluster/slaves.txt", 'r') as outfile:
        for slave in outfile:
            slaveHost = slave.split(',')
            result = utils.run_bash("bash", [PATH+"/scripts/cluster/pingTest.sh", slaveHost[0]])
            if result.strip() == SUCCESS:
                slaves.append(slaveHost[0])
    return slaves

currentDT = datetime.datetime.now()
slavesAlive = getSlavesAlive()
slavesFree = [True] * len(slavesAlive)
createPackages()
queueLock = threading.Lock()
packAmount = len(queuePackages)
workQueue = Queue.Queue(len(queuePackages))
slavesRunning = []
threadID = 1
exitFlag = [False]
# Create new threads
for tName in range(len(slavesFree)):
   thread = myThread(slavesAlive[tName], workQueue, queueLock, exitFlag, slavesAlive, slavesFree)
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

currTime = packAmount-workQueue.qsize()
while not workQueue.empty():
    newCurrTime = (packAmount-workQueue.qsize())/(packAmount*1.0)
    if newCurrTime != currTime:
        currTime = newCurrTime
        print "========> Finished %s of %s - %s" % (str(packAmount-workQueue.qsize()), str(packAmount), (newCurrTime))
    pass

exitFlag[0] = True

for t in slavesRunning:
   t.join()
print str(currentDT),str(datetime.datetime.now())
