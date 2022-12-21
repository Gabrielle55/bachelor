import sys
import io
# Dette program er en test om to filer er ens.

fp_result = open(sys.argv[1], 'r')
fp_result_Net = open(sys.argv[2], 'r')

lines_result = fp_result.readlines()
lines_result_Net = fp_result_Net.readlines()

is_equal = True

for i in range(len(lines_result)):    
    if (lines_result[i] == lines_result_Net[i]):
        pass
    else:
        # Split lines_result linje op i tre dele
        # split lines_result_Net op i flere dele og sæt nr 2 indtil - 2 sammen
        line_result = lines_result[i].split()
        line_result_Net = lines_result_Net[i].split()
        temp = ""
        for i in range(1, len(line_result_Net) - 1):
            temp += line_result_Net[i]
        line_result_Net_new = [line_result_Net[0], temp, line_result_Net[-1]]
        if ( line_result[0] != line_result_Net_new[0] and line_result[2] == line_result_Net_new[-1]):
            print(i)
            is_equal = False
        if line_result[1] not in line_result_Net_new[1]:
            print(i)
            is_equal = False



print("The two files are equal: " + is_equal)

fp_result.close()
fp_result_Net.close()