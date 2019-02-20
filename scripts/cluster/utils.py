import json
import os.path, subprocess
from subprocess import STDOUT, PIPE
import os
import io
import sys

try:
    to_unicode = unicode
except NameError:
    to_unicode = str

def run_bash (command, params):
    cmd=[command]
    if len(params) > 0:
        cmd.extend(params)
    proc=subprocess.Popen(cmd, stdout = PIPE, stderr = STDOUT)
    return proc.stdout.read()
