#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
# Et seed der bruges til at generere kanter
# Et seed der bruges til at generere vægte

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
weight_seed = info[3]

# Benytter NetworkX samt "seed" til at generere en tilfældig, almindelig, orienteret graf
g = nx.DiGraph()
g = nx.gnm_random_graph(number_of_nodes, average_outdegree * number_of_nodes, seed, True)

random.seed(weight_seed)
with io.open(sys.argv[2], 'w', encoding='utf8') as new_fp:
    new_fp.write(str(number_of_nodes) + "\n")
    new_fp.write(str(average_outdegree * number_of_nodes) + "\n")

    for (i,j) in g.edges:
        new_fp.write(str(i) + "," + str(j) + "," + str(random.randint(0,20)) + "\n")
