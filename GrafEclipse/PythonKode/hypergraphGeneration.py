from ast import For
import networkx as nx
import codecs

from networkx.utils import py_random_state
import random
import sys
import io

# Inputfilen skal være på formattet:
# Antal af knuder
# Gennemsnitligt antal af udadgående kanter for en knude
# Et seed der bruges til at generere kanter og vægte


# Åbner input filen og læser dataen
filename = sys.argv[1]
fp = open(filename,'r')
info = []
lines = fp.readlines()
for line in lines:
    info.append(int(line))


number_of_nodes = info[0]
average_outdegree = info[1]
seed = info[2]

number_of_edges = number_of_nodes * average_outdegree
random.seed(seed)
with io.open(sys.argv[2], 'w', encoding='utf8') as new_fp:
    new_fp.write(str(number_of_nodes ) + "\n" + str(number_of_edges))
    for i in range(0, number_of_edges):
        new_fp.write("\n")
        number_of_nodes_in_tail = random.randint(1, 5)
        #number_of_nodes_in_tail = random.randint(1, 3)
        new_fp.write(str(number_of_nodes_in_tail) + ",")
        numbers = []
        for j in range(0, number_of_nodes_in_tail + 1):
            new_node = random.randint(0, number_of_nodes - 1)
            while( new_node in numbers):
                new_node = random.randint(0, number_of_nodes - 1)
            numbers.append(new_node)         
            new_fp.write(str(new_node) + ",")
        new_fp.write(str(random.randint(1, 20))) 