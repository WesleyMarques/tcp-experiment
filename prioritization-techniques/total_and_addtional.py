# by louyiling 


import os
import random
import linecache
#contest value when doing experiments only need to change there varibles

FILENAME = "cov_submatrix.txt"
FILENAME2 = "common_set.txt"
FILENAME3 = "branch_index.txt"
OUTPUTFILE1="order_total_branch_cov_method_level.txt"
OUTPUTFILE2="order_addit_branch_cov_method_level.txt"
VERSION_NUM = 8

TESTNUM = 0 # num of test case
UNITNUM = 0 # num of structure units
# variable
# store coverage information matrix; line represents test case; column represents structure unit
matrix = []
covernum = []# store each test case covers how many structure units
sorted_result = [] # store the sorted result 



# read coverage information matrix from file 
def read_in_matrix():
	global TESTNUM
	global UNITNUM
	global matrix
	global covernum
	global sorted_result
	i = 0
	for line in open(FILENAME):
		sorted_result.append(0)
		covernum.append(0)
		t_line = line.strip()
		for j in range(0, UNITNUM):
			matrix[i][j] = int (t_line[j])
		i = i + 1
		
		
# sorted test case in total 
def sort_total():
	global TESTNUM
	global UNITNUM
	global matrix
	global covernum
	global sorted_result
	for i in range(0, TESTNUM):
		covernum[i] = 0
		for j in range(0, UNITNUM):
			covernum[i] += matrix[i][j]
	flag = []
	for i in range(0, TESTNUM):
		flag.append(0)
	for i in range(0, TESTNUM):
		tmp = 0
		for j in range(0, TESTNUM):
			if flag[j] == 0 and tmp < covernum[j]:
				tmp = covernum[j]
		randomlist = []
		count = 0
		for j in range(0, TESTNUM):
			if flag[j] ==0 and tmp == covernum[j]:
				randomlist.append(j)
				count += 1
		k = random.randint(0, count - 1)
		k = randomlist[k]
		flag[k] = 1
		sorted_result[i] = k
	
#	sorted_pair = sorted(covernum.items(),key=lambda e:e[1],reverse=True)
##	for item in sorted_pair:
		#print item[0],
		#print item[1]
#		sorted_result[i] = item[0]
#		i = i + 1

		
# sorted test case in additional 
def sort_add():
	global TESTNUM
	global UNITNUM
	global matrix
	global covernum
	global sorted_result
	t_flag = []
	for i in range(0, TESTNUM):
		t_flag.append(0)
		covernum[i] = 0
		
	for k in range(0, TESTNUM):
		tmp = 0
		for i in range(0, TESTNUM):
			if t_flag[i] == 1:
				continue
			if t_flag[i] == 0:
				covernum[i] = 0
				for j in range(0, UNITNUM):
					covernum[i] +=matrix[i][j]
				if tmp < covernum[i]:
					tmp = covernum[i]
		randomlist = []
		count = 0
		for i in range(0, TESTNUM):
			if t_flag[i] == 0 and tmp == covernum[i]:
				randomlist.append(i)
				count += 1
		m = random.randint(0, count - 1)
		m = randomlist[m]
		t_flag[m] = 1
		sorted_result[k] = m
		for j in range(0, UNITNUM):
			if matrix[m][j] == 1:
				for i in range(0, TESTNUM):
					matrix[i][j] = 0
		#print m



		




#for test use 
def test1():

	output = open(OUTPUTFILE1, 'w')
	for item in sorted_result:
		s_item  = linecache.getline(FILENAME2, item+1)
		output.write(s_item)
	linecache.clearcache()
	output.close()
	
	print "total:",
	for i in range(0, TESTNUM):
		print sorted_result[i],
	print

#for test use 
def test2():
	output = open(OUTPUTFILE2, 'w')
	for item in sorted_result:
		s_item  = linecache.getline(FILENAME2, item+1)
		output.write(s_item)
	linecache.clearcache()
	output.close()
#	print "add:",
#	for i in range(0, TESTNUM):
#		print sorted_result[i],	
#	print 
		
		
if __name__ == '__main__':
	global TESTNUM
	global UNITNUM
	global matrix
	global covernum
	global sorted_result
	pathindex = ["v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9"]
	os.chdir("../source/v0")
	for i in range(0, VERSION_NUM):
		pathname ="../"
		pathname += pathindex[i]
		os.chdir(pathname)
		TESTNUM = 0
		UNITNUM = 0
		infile = open(FILENAME2)
		for item in infile:
			TESTNUM += 1
		infile.close()
		infile = open(FILENAME3)
		for item in infile:
			UNITNUM += 1
		infile.close()
		matrix = [[0 for col in range(UNITNUM)] for row in range(TESTNUM)]
		covernum = []# store each test case covers how many structure units
		sorted_result = [] # store the sorted result 
		read_in_matrix()
		sort_total()
		test1()
		sort_add()
		test2()
	











