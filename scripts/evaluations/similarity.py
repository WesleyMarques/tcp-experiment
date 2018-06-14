import json
import sys
from pprint import pprint
import os
import io

try:
    to_unicode = unicode
except NameError:
    to_unicode = str

PATH = os.getcwd()
os.chdir(PATH)

with open(projectName+"/coverage/sorted_version.txt") as versions:
    for version in versions:
        version = version.replace("\n", "")
        coveragePath = "%s/coverage/%s/priorization_%s.json" % (projectName, version, covType)
        with open(coveragePath) as data_file:
            priorizationTests = json.load(data_file)
