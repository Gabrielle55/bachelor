# -*- coding: UTF-8 -*-
# Programmet tager to filnavne som input og kører Dijkstra, som implementeret af NetworkX, på grafen i første argument.
# Resultatet af Dijkstra skrives i filen, der er det andet argument.

from ast import For
from re import T
import networkx as nx
import sys
import io

# Input filen skal være på formattet:
#   Antallet af knuder \n
#   Antallet af kanter \n
#   kant_1 \n
#   kant_2 \n
#   ...
#   kant_m \n

# Hver kant skal være på formattet:
#   knude_id,knude_id,vægt


filename = sys.argv[1]

fp = open(filename,'r')

number_of_nodes = int(fp.readline().replace("\r\n",""))
number_of_edges = int(fp.readline().replace("\r\n",""))

graph = []

edges = fp.readlines()
for edge in edges:
    tuple = ()
    new_line = str(edge).split(",")
    for char in new_line:
        tuple = tuple + (int(char),)
    graph.append(tuple)

    
g = nx.DiGraph()

g.add_weighted_edges_from(graph)


# Dette kan ændres, så Dijkstra kører med en anden startknude
source_node = 0

pred, dist = nx.dijkstra_predecessor_and_distance(g, source_node)


# Skriver resultatet i filen, der var andet argument
with io.open(sys.argv[2], 'w', encoding='utf8') as new_fp:

    # Hvis der ikke er nogen sti til knuden, da skrives -1, som den tidligere knude
    for i in range(0, number_of_nodes):
        new_fp.write( str(i) + " " + str(pred.get(i)).replace("[]","-1").removeprefix('[').removesuffix(']') + " " +  str(dist.get(i)) + "\n")


# Denne metode genererer de korteste veje fundet af Dijkstra
def generatePathsAndDistance(pred, dist, source_node, number_of_nodes):
    for i in range(0, number_of_nodes):
        if(pred.get(i) is not None):
            path = [i]
            if(i != source_node):
                predecessor = pred.get(i)
                while (predecessor[0] != source_node):
                    path.append(predecessor[0]) 
                    predecessor = pred.get(predecessor[0])
                path.append(predecessor[0]) 
                print("Node " + str(i) + ":")
                print("    distance: " + str(dist.get(i)))
                print("    path: ", end="")
                j = len(path) -1
                line = ""
                while (j >= 0):
                    line = line + str(path[j]) + "->"
                    j = j-1
                print(line.removesuffix("->"))
            else:
                print("Node " + str(i) + ":")
                print("    distance: " + str(dist.get(i)))
                print("    path: " + str(source_node))


# Dette metode-kald kan bruges til at printe den fulde sti
#generatePathsAndDistance(pred, dist, source_node, number_of_nodes) 