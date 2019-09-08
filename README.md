# Link Prediction via Higher-Order Motif Features

This repository contains the code and scripts required to replicate the experiments that are reported in the paper titled "Link Prediction via Higher-Order Motif Features".
 The code includes:
1. Sample a set of positive edges from the dataset.
2. Sample a set of negative edges from the dataset.
3. Preparing the graph dataset in the input format of the <strong> Arabesque </strong> framework (http://arabesque.io/). 

These steps can be done using src/Sampling.java file using the following commands:
     ### Compile:
              javac Sampling.java
     ### Run:
              java Sampling <input graph file>  <number of edges in the sample>
     ### Example:
              java Sampling Graph.txt 1000

4. Extracting motifs. In this project, we use <strong> Arabesque </strong> framework (http://arabesque.io/). Any framework can be used for extracting the motifs from the graph.
5. Aggregating motifs for each edge in the sample (positive and negative).
6. Computing different graph techniuqes for link prediction including: (Common Neighbors, Jaccard Coefficient, Adamic/Adar,    Preferential Attachment,  Rooted PageRank, Katz Index).
