from ast import For
import networkx as nx
import matplotlib.pyplot as plt
import re
from networkx.drawing.nx_pydot import write_dot
import string
import sys
        

# https://www.geeksforgeeks.org/python-visualize-graphs-generated-in-networkx-using-matplotlib/
# The tutorial linked above has been used to write the following program

H = nx.DiGraph()

filename = sys.argv[1]
fp = open(filename,'r')
edges = []
number_of_nodes = fp.readline()

lines = fp.readlines()
for line in lines:
    edgeLine = [int(i) for i in line.split()]
    edges.append(edgeLine)
print(edges)
    
fp.close()
edge_letters = string.ascii_uppercase
edge_letters += string.ascii_lowercase
sum = 0
number_of_edges = 0

for i in range(0, len(edges)):
    for j in range(0, len(edges[i]) - 2):
        H.add_edge(edges[i][j], edge_letters[i], weight = edges[i][-1])
        sum += 1
    H.add_edge(edge_letters[i], edges[i][-2], weight = 0)
    sum += 1
    number_of_edges += 1

pos = nx.spring_layout(H, seed= sum)

# nodes
nx.draw_networkx_nodes(H, pos, node_size=200)

#edges
nx.draw_networkx_edges(H,pos, H.edges, width=2)

#labels
nx.draw_networkx_labels(H,pos,font_size=10, font_family='sans-serif')

# edge weight labels
edge_labels = nx.get_edge_attributes(H, "weight")
nx.draw_networkx_edge_labels(H, pos, edge_labels)

ax = plt.gca()
ax.margins(0.08)
plt.axis("off")
plt.tight_layout()
plt.show()

plt.savefig("fig.png")


