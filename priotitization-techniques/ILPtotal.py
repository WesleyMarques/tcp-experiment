#!/usr/bin/python

#total ILP

from gurobipy import *
from operator import itemgetter
import os
import random
import linecache
VERSION_NUM = 1
pathindex = ["v0", "v1", "v2", "v3"]
pathname = ""

FILENAME1 = "cov_submatrix.txt"
FILENAME2 = "common_set.txt"
FILENAME3 = "test_method_name.txt"#running_time.txt
FILENAME4 = "statement_index.txt"
OUTPUTFILE = "order_ILP_total_0.75_order_state_cov_method_level.txt"
TESTNUM = 0 # num of test case
UNITNUM = 0 # num of structure units
TIME_MAX = 0.75 # 0.05 0.25 0.5 0. 75


os.chdir("/Users/wesley/Documents/experiment/data/jopt-simple/coverage/0d308ab96e39254e42a8f9359760b91ab8e414d8")
# for line in open("v.txt"):
# 	VERSION_NUM = int(line.strip())



for q in range(0, VERSION_NUM):
	TIME_MAX = 0.75
	# pathname = "../"
	# pathname += pathindex[q]
	# os.chdir(pathname)
	TESTNUM = 0
	UNITNUM = 0
	for item in open(FILENAME2):
		TESTNUM += 1
	for item in open(FILENAME4):
		UNITNUM += 1
	#global variables#
	cov_info = [[0 for col in range(UNITNUM)] for row in range(TESTNUM)]
	time_matrix = []
	cov_matrix = []
	test_name = []
	selected= []
	sorted_result = []

	#reading the coverage information#
	print("1.reading the coverage matrix")
	i = 0
	for line in open(FILENAME1):
		t_line = line.strip()
		for j in range(0, UNITNUM):
			cov_info[i][j] = int(t_line[j])
		i = i + 1
	for line in open(FILENAME2):
		#line = line.split()[0]
		test_name.append(line.strip())

	for i in range(0, TESTNUM):
		infile = open(FILENAME3)
		for line in infile:
			t_line = line.strip().split(' ')
			if t_line[0] == test_name[i]:
				time_matrix.append(float(t_line[1]))
				print(time_matrix[i])
				break
		infile.close()


	#calculating the sum of covered structure units for each test case
	for i in range(0, TESTNUM):
		tmp = 0
		for j in range(0, UNITNUM):
			tmp += cov_info[i][j]
		cov_matrix.append(tmp)



	#reading the time information#
	print("2.reading the time matrix and calculate the time budget ") ######################################################################
	os.chdir("../"+pathindex[VERSION_NUM - 1])
	time_tmp = 0
	for i in range(0, TESTNUM):
		infile = open(FILENAME3)
		for line in infile:
			t_line = line.strip().split(' ')
			if t_line[0] == test_name[i]:
				time_tmp += float(t_line[1])
				break;
		infile.close()
	TIME_MAX = time_tmp * TIME_MAX
	#solving the ILP problem#
	print("3.solving the IPL problem")
	# Create a new model
	m = Model("totalilp" +str(q))
	vari=[0 for col in range(TESTNUM)]
	# Create variables
	for i in range(0, TESTNUM):
		vari[i] = m.addVar(vtype=GRB.BINARY, name="x"+str(i))

	# Integrate new variables
	m.update()

	# Set objective
	equ = 0
	for i in range(0, TESTNUM):
		equ += vari[i] * cov_matrix[i]
	m.setObjective(equ, GRB.MAXIMIZE)


	#set time limitation
	time = 0
	for i in range(0, TESTNUM):
		time += time_matrix[i] * vari[i]

	# Add constraint
	m.addConstr(time <= TIME_MAX, "time_limit")

	m.optimize()

	for v in m.getVars():
		print('%s %d' % (v.varName, v.x))

	print('Obj: %d' % m.objVal)


	print("4.do the total sort")
	for v in m.getVars():
		selected.append(v.x)


	selected_set= []
	unselected_set = []
	left_test_name = []



	for i in range(0, TESTNUM):
		if selected[i] == 1:
			selected_set.append(i)
		if selected[i] == 0:
			unselected_set.append(i)
			line = linecache.getline(FILENAME2, i + 1)
			left_test_name.append(line.strip())
	linecache.clearcache()
	left_test_name = sorted(left_test_name)


	flag = []
	for i in range(0, TESTNUM):
		cov_matrix[i] = 0
		flag.append(0)
		sorted_result.append(0)
		for j in range(0, UNITNUM):
			cov_matrix[i] += cov_info[i][j]


	validnum = 0
	for i in selected_set:
		tmp = 0
		for j in selected_set:
			if flag[j] == 0 and tmp < cov_matrix[j]:
				tmp = cov_matrix[j]
		randomlist = []
		count = 0
		for j in selected_set:
			if flag[j] ==0 and tmp == cov_matrix[j]:
				randomlist.append(j)
				count += 1
		k = random.randint(0, count - 1)
		k = randomlist[k]
		flag[k] = 1
		sorted_result[validnum] = k
		validnum += 1
		#print(k)

	# write the order
	print("write the order file")
	os.chdir(pathname)
	output = open(OUTPUTFILE, 'w')
	for i in range(0, validnum):
		s_item  = linecache.getline(FILENAME2, sorted_result[i] + 1)
		output.write(s_item)
	linecache.clearcache()
	for item in left_test_name:
		output.write(item)
		output.write('\n')
	output.close()
