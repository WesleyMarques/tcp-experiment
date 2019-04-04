import json
import io
import sys, os
import subprocess
from subprocess import STDOUT, PIPE

def execute_java (java_file, path, filename, limiar, maxTests):
    cmd=['java', java_file, path, filename, limiar, maxTests]
    proc=subprocess.Popen(cmd, stdout = PIPE, stderr = STDOUT)
    #input = subprocess.Popen(cmd, stdin = PIPE)
    return proc.stdout.read()

PATH = os.getcwd()

try:
    to_unicode = unicode
except NameError:
    to_unicode = str

project,coverage,version = sys.argv[1].split(",")

limiares = range(0,1,5)
maxTests = range(0,101,5)
values = {}
status = 0
algorithm = "GreedyAdditionalSimilarity"

for limiar in limiares:
    for maxTest in maxTests:
        limiar_str = str(limiar/100.)
        maxTest_str = str(maxTest/100.)
        priorization = execute_java(algorithm, PATH+"/", coverage, limiar_str, maxTest_str)
        values[algorithm+"_"+limiar_str+"_"+maxTest_str] = priorization

with open(project+"_"+coverage+"_"+version+"_priorization.json", 'w+') as outfile:
    str_ = json.dumps(values,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
    print str_
    outfile.write(to_unicode(str_))
    outfile.close()
