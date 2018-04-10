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
algorithms = ["ARTMaxMin", "Genetic", "GreedyTotal", "GreedyAdditional"]
covLevel = ["statement", "method", "branch"]

# compile_java("./algoritmos2priorize/ARTMaxMin.java")
# compile_java("./algoritmos2priorize/Genetic.java")
# compile_java("./algoritmos2priorize/GreedyTotal.java")
# compile_java("./algoritmos2priorize/GreedyAdditional.java")

project = projects[int(sys.argv[1])]
coverage = covLevel[int(sys.argv[2])]
print "START",project, coverage
versions = open(project+"/coverage/sorted_version.txt")
for version in versions:
    version = version.replace("\n", "")
    values = {}
    for algorithm in algorithms[:4]:
        priorization = execute_java(algorithm, project+"/coverage/"+version+"/", coverage+"_matrix.txt")
        values[algorithm] = priorization

    with open(project+"/coverage/"+version+"/priorization_"+coverage+".json", 'w+') as outfile:
        str_ = json.dumps(values,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
        outfile.write(to_unicode(str_))
        outfile.close()
print "FINISH", project, coverage
