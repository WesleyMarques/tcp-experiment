import threading
import time

SSH_PW = "150794"

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
      print "Starting Slave",self.threadID
      process_data(self.queue, self.queueLock, self.exitFlag, self.slavesAlive, self.slavesFree)
      print "Exiting",self.threadID


def process_data(queue, queueLock, exitFlag, slavesAlive, slavesFree):
    while not exitFlag[0]:
        queueLock.acquire()
        if not queue.empty():
            data = queue.get()
            nextSlave = slavesFree.index(True)
            slavesFree[nextSlave] = False
            sshClient = ssh_client.start_session(slavesAlive[nextSlave], "wesleynunes", SSH_PW)
            ssh_client.run_command()
            slavesFree[nextSlave] = True
            queueLock.release()
            print "processing %s" % (data)
        else:
            queueLock.release()
        time.sleep(1)
