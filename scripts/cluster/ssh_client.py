import paramiko

SSH_PORT = 22

ssh = None

def setup():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    return ssh

def start_session(host, username, password):
    sshClient = setup();
    sshClient.connect(host, SSH_PORT, username, password)
    return sshClient

def run_command(cmd, sshClient):
    stdin, stout, stderr = sshClient.exec_command(cmd)
    outlines = stout.readlines()
    resp = "".join(outlines)
    return resp
