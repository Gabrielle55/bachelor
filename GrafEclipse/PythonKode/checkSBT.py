import sys
import io
import os


path1 = r"C:\Users\gabri\Documents\Bachelor\Eclipse\GrafEclipse\SBTresults"
path2 = r"C:\Users\gabri\Documents\Bachelor\Eclipse\GrafEclipse\Dijkresults"

dir_SBT = os.listdir(path1)
dir_dijk = os.listdir(path2)
list_of_result = []
for i in range(len(dir_SBT)):
    full_path_SBT = path1 + "\\" + dir_SBT[i]
    full_path_dijk = path2 + "\\" + dir_dijk[i]
    fp_result_SBT = open(full_path_SBT, 'r')
    fp_result_dijk = open(full_path_dijk, 'r')
    
    lines_result_dijk = fp_result_dijk.readlines()
    lines_result_SBT = fp_result_SBT.readlines()      

    list_of_result.append(lines_result_dijk == lines_result_SBT)
    
    fp_result_dijk .close()
    fp_result_SBT.close()
    
if False not in list_of_result:
    print("All pairs of files are equal")
else:
    print("Not all pairs of files are equal")
    for i in range(0, len(list_of_result)):
        if list_of_result[i] == False:
            print(i)