# Link Prediction via Higher-Order Motif Features

This repository contains the code and scripts required to replicate the the experiments that reported in the paper titled "Link Prediction via Higher-Order Motif Features".
 The code includes:
1. Sample a set of real edges from the dataset.
2. Sample a set of negative edges from the dataset.
3. Preparing the graph dataset in the input format of Arabesque Framework (http://arabesque.io/). 
4. Modification used to extract motifs using Arabesque Framework (http://arabesque.io/)
5. Aggregates motifs for each edge in the sample (positve and negative).
6. Compute different graph teachniques for Link preidction including: (Common Neighbors, Jaccard Coefficient, Adamic/Adar,    Preferential Attachment,  Rooted PageRank, Katz Index).
