#!/usr/bin/python

#addtional ILP

from gurobipy import *
from operator import itemgetter
import os
import random
import linecache

VERSION_NUM = 5
pathindex = ["v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9"]
pathname = ""

FILENAME1 = "cov_submatrix.txt"
FILENAME2 = "common_set.txt"
FILENAME3 = "test_method_name.txt"
FILENAME4 = "statement_index.txt"
OUTPUTFILE = "order_ILP_addit_0.75_order_state_cov_method_level.txt"
TESTNUM = 0 # num of test case
UNITNUM = 0 # num of structure units
TIME_MAX = 0.75 # 0.05 0.25 0.5 0.75


# cd to the source directory
os.chdir("../../../../codingplace/empiricalstudyonRT/ontest/test-method/state_cov/source/v0")
for line in open("v.txt"):
	VERSION_NUM = int(line.strip())


for q in range(0, VERSION_NUM):
	TIME_MAX = 0.75
	pathname = "../"
	pathname += pathindex[q]
	os.chdir(pathname)
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
		test_name.append(line.strip())

	for i in range(0, TESTNUM):
		infile = open(FILENAME3)
		for line in infile:
			t_line = line.strip().split(' ')
			if t_line[0] == test_name[i]:
				time_matrix.append(float(t_line[1]))
				print("time")
				print(time_matrix[i])
				break
		infile.close()


	#calculating the sum of covered structure units for each test case
	for i in range(0, TESTNUM):
		tmp = 0
		for j in range(0, UNITNUM):
			tmp += cov_info[i][j]
		cov_matrix.append(tmp)
		#print (cov_info[i])



	#reading the time information#
	print("2.reading the time matrix and calculate the time budget ")############################################################
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
	#print("timemax")
	#print(TIME_MAX)
	#solving the ILP problem#
	print("3.solving the IPL problem")
	# Create a new model
	m = Model("addilp"+str(q))

	vari=[0 for col in range(TESTNUM)]
	# Create variables
	for i in range(0, TESTNUM):
		vari[i] = m.addVar(vtype=GRB.BINARY, name="x"+str(i))

	st = [0 for col in range(UNITNUM)]
	for i in range(0, UNITNUM):
		st[i] = m.addVar(vtype=GRB.BINARY, name="y"+str(i))
	# Integrate new variables
	m.update()

	# Set objective
	equ = 0
	for j in range(0, UNITNUM):
		equ += st[j]
	m.setObjective(equ, GRB.MAXIMIZE)


	#set time limitation
	time = 0
	for i in range(0, TESTNUM):
		time += time_matrix[i] * vari[i]

	# Add constraint
	m.addConstr(time <= TIME_MAX, "time_limit")

	for j in range(0, UNITNUM):
		tmp = 0
		for i in range(0, TESTNUM):
			tmp += vari[i] * cov_info[i][j]
		m.addConstr(st[j] <= tmp, "cove_limit" + str(j))


	m.optimize()

	for v in m.getVars():
		print('%s %d' % (v.varName, v.x))
	print('Obj: %d' % m.objVal)

	for v in m.getVars():
		selected.append(int(v.x))


	selected_set = []
	unselected_set = []
	second_test_num = 0
	used_time = 0


	for i in range(0, TESTNUM):
		if selected[i] == 1:
			selected_set.append(i)
			used_time += time_matrix[i]
		if selected[i] == 0:
			unselected_set.append(i)
			second_test_num += 1



	if second_test_num != 0:
		TIME_MAX = TIME_MAX - used_time
		print("need second solve ilp")
		m2 = Model("totalilp_two"+str(q))
		vari=[0 for col in range(TESTNUM)]
		# Create variable
		for i in unselected_set:
			vari[i] = m2.addVar(vtype=GRB.BINARY, name="x"+str(i))
		m2.update()
		# Set objective
		equ = 0
		for i in unselected_set:
			equ += cov_matrix[i] * vari[i]
		m2.setObjective(equ, GRB.MAXIMIZE)
		#set time limitation
		time = 0
		for i in unselected_set:
			time += time_matrix[i] * vari[i]
		# Add constraint
		m2.addConstr(time <= TIME_MAX, "time_limit")
		m2.optimize()
		#for v in m2.getVars():
		#	print('%s %d' % (v.varName, v.x))
		#print('Obj: %d' % m2.objVal)

		i = 0
		for v in m2.getVars():
			selected[unselected_set[i]] = int(v.x)


#for i in range(0, TESTNUM):
#	print(selected[i])



#################################################

	selected_set = []
	unselected_set = []
	left_test_name = []
	valid_num = 0
	for i in range(0, TESTNUM):
		if selected[i] == 1:
			selected_set.append(i)
			valid_num += 1
		if selected[i] == 0:
			unselected_set.append(i)
			line = linecache.getline(FILENAME2, i + 1)
			left_test_name.append(line.strip())
	linecache.clearcache()
	left_test_name = sorted(left_test_name)


	t_flag = []
	covernum = []
	for i in range(0, TESTNUM):
		t_flag.append(0)
		covernum.append(0)
		sorted_result.append(0)

	for i in range(0, TESTNUM):
		for j in range(0, UNITNUM):
			covernum[i] += cov_info[i][j]

	for k in range(0, valid_num):
		tmp = 0
		print k
		for i in selected_set:
			if t_flag[i] == 1:
				continue
			if t_flag[i] == 0:
				if tmp < covernum[i]:
					tmp = covernum[i]

		randomlist = []
		count = 0
		for i in selected_set:
			if t_flag[i] == 0 and tmp == covernum[i]:
				randomlist.append(i)
				count += 1
		m = random.randint(0, count - 1)
		m = randomlist[m]
		t_flag[m] = 1
		sorted_result[k] = m
		store_u = []
		if tmp == 0:
			continue
		for i in range(0, UNITNUM):
			if cov_info[m][i] == 1:
				store_u.append(i)

		for j in range(0, tmp):
			for i in range(0, valid_num):
				covernum[selected_set[i]] -= cov_info[selected_set[i]][store_u[j]]
				cov_info[selected_set[i]][store_u[j]] = 0


	# write the order
	print("write the order file")
	os.chdir(pathname)
	output = open(OUTPUTFILE, 'w')
	for i in range(0, valid_num):
		s_item  = linecache.getline(FILENAME2, sorted_result[i] + 1)
		output.write(s_item)
	linecache.clearcache()
	for item in left_test_name:
		output.write(item)
		output.write('\n')
	output.close()
