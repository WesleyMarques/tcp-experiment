import os,sys

# sys.path.insert(0,'../scripts-py')

import utils

PATH = os.getcwd()
os.chdir(PATH)

hostConfig = {
    "name": "monogatari",
    "path": ""
}

def init_slave(slave, hostConfig):
    # copy project files

def build_tasks():
    # Create all possible experiment



slavesAlive = []

SUCCESS = "success"

with open(PATH+"/scripts/cluster/slaves.txt", 'r') as outfile:
    for slave in outfile:
        slaveHost = slave.split(',')
        result = utils.run_bash("bash", [PATH+"/scripts/cluster/pingTest.sh", slaveHost[0]])
        if result.strip() == SUCCESS:
            slavesAlive.append(slaveHost[0])
