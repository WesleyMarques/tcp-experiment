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

def compile_java (java_file):
    subprocess.check_call(['javac', java_file])

def execute_java (java_file, path, filename):
    cmd=['java', java_file, path, filename]
    proc=subprocess.Popen(cmd, stdout = PIPE, stderr = STDOUT)
    #input = subprocess.Popen(cmd, stdin = PIPE)
    return proc.stdout.read()

PATH = os.getcwd()
os.chdir(PATH)

projects = [ "scribe-java", "jasmine-maven-plugin", "java-apns", "jopt-simple", "la4j", "metrics-core", "vraptor","assertj-core"]
algorithms = [str(sys.argv[3])] if len(sys.argv) > 3 else ["GreedyTotal", "GreedyAdditional", "ARTMaxMin", "Genetic"]
covLevel = ["statement", "method", "branch"]

project = projects[int(sys.argv[1])]
coverage = covLevel[int(sys.argv[2])]
print "START",project, coverage
versions = open(PATH+"/data/"+project+"/coverage/sorted_version.txt")
for version in versions:
    version = version.replace("\n", "")
    values = {}
    for algorithm in algorithms:
        priorization = execute_java(algorithm, PATH+"/data/"+project+"/coverage/"+version+"/", coverage)
        values[algorithm] = priorization

    with open(PATH+"/data/"+project+"/coverage/"+version+"/priorization_"+coverage+".json", 'w+') as outfile:
        str_ = json.dumps(values,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
        outfile.write(to_unicode(str_))
        outfile.close()
print "FINISH", project, coverage
