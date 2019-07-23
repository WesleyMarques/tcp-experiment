import threading
import time

import utils
import ssh_client

SSH_PW = "150794"

unit_path = "/tmp/similatiryExperiment"

class myThread (threading.Thread):
   def __init__(self, threadID, queue, queueLock, exitFlag, slavesAlive, slavesFree):
      threading.Thread.__init__(self)
      self.threadID = threadID
      self.queue = queue
      self.queueLock = queueLock
      self.exitFlag = exitFlag
      self.slavesAlive = slavesAlive
      self.slavesFree = slavesFree
   def run(self):
      # print "Starting Slave",self.threadID
      process_data(self.queue, self.queueLock, self.exitFlag, self.slavesAlive, self.slavesFree)
      # print "Exiting",self.threadID


def process_data(queue, queueLock, exitFlag, slavesAlive, slavesFree):
    while not exitFlag[0]:
        queueLock.acquire()
        if not queue.empty():
            data = queue.get()
            nextSlave = slavesFree.index(True)
            slavesFree[nextSlave] = False
            currentSlave = slavesAlive[nextSlave]
            queueLock.release()
            print "[STARTING] processing %s in %s" % (data["input"], slavesAlive[nextSlave])
            sshClient = ssh_client.start_session(currentSlave, "wesleynunes", SSH_PW)
            ssh_client.run_command("bash ~/experimentInit.sh %s" % SSH_PW, sshClient)
            utils.run_bash("sshpass", ("-p %s scp -r %s/%s %s:/local_home/wesleyExperiment" % (SSH_PW, unit_path, data["unit"], currentSlave)).split(" "))
            ssh_client.run_command("cd /local_home/wesleyExperiment/%s && python run-priorization.py %s" % (data["unit"], data["input"]), sshClient)
            utils.run_bash("sshpass", ("-p %s scp %s:/local_home/wesleyExperiment/%s/%s_priorization.json /local_home/wesleyExperiment/" % (SSH_PW, currentSlave, data["unit"], data["unit"])).split(" "))
            ssh_client.run_command("rm -rf /local_home/wesleyExperiment", sshClient)
            slavesFree[nextSlave] = True
            print "[FINISHING] processing %s in %s" % (data["input"], slavesAlive[nextSlave])
        else:
            queueLock.release()
        time.sleep(1)
