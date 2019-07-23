import xmltodict
import sys
import json
import re


try:
    to_unicode = unicode
except NameError:
    to_unicode = str

with open(sys.argv[1]) as data_file_xml:
    jacocoFile = xmltodict.parse(data_file_xml, process_namespaces=True)
with open("./jacoco.json", 'w') as jsonFile:
    packages = jacocoFile["report"]["package"]
    str_ = json.dumps(packages,indent=4,sort_keys=True,separators=(',',':'), ensure_ascii=False)
    jsonFile.write(to_unicode(str_))
    jsonFile.close()

result = []

for package in packages:
    classes = package["class"]
    if not isinstance(classes, list):
        classes = [classes]
    for packClass in classes:
        className = packClass["@name"].replace("/", ".")
        if not isinstance(packClass["method"], list):
            packClass["method"] = [packClass["method"]]
        for method in packClass["method"]:
            params = re.sub("[L]([a-z]+[/])+", "Q", method["@desc"], flags=re.IGNORECASE)
            methSign = "%s.%s:%s" % (className, method["@name"], params)
            result.append("%s:%s" % (methSign, int(method["counter"][2]["@covered"]) + int(method["counter"][2]["@missed"])))


with open("./method-complexity.txt", 'w') as methComplex:
    for value in result:
        methComplex.write("%s\n" % value)
    methComplex.close()
