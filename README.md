# Link Prediction via Higher-Order Motif Features

This repository contains the code and scripts required to replicate the experiments that are reported in the paper titled "Link Prediction via Higher-Order Motif Features".
 The code includes:
1. Sample a set of positive edges from the dataset.
2. Sample a set of negative edges from the dataset.
3. Preparing the graph dataset in the input format of the Arabesque framework (http://arabesque.io/). 
4. The code used to extract motifs using the Arabesque framework (http://arabesque.io/).
5. Aggregating motifs for each edge in the sample (positive and negative).
6. Computing different graph techniuqes for link prediction including: (Common Neighbors, Jaccard Coefficient, Adamic/Adar,    Preferential Attachment,  Rooted PageRank, Katz Index).
