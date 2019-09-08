package src;

import java.io.*;
import java.util.*;

    /**
     * Created by Ghadeer on 8/28/19.
     * The main task of this file is preparing a sampleset from the graph which includes:
     * 1) Select a set of random edges from the graph to act as positive class edges.
     * 2) Prepare a set of 'Fake' edges to form the Negative class based on i) A pair of random unconnected vertices ii) A controllable distance between the unconnected vertices.
     * 3) Create a new graph with the 'negative' edges inserted.
     * 4) Optional: Prepare the graph in the Arabesque's input format to extract the motifs. You can replace this step by different method
     *
     * The input is the graph where the graph is a list of edges in the following format:
     *  edge1
     *  edge2
     *  ..
     *  edgeN
     *  The edge is V1 V2. where V1 is the source vertex and V2 is the destination Vertex.
     *  The output includes:
     *  1) 'realList' which is the set of positive edges in the format
     *   edge1
     *   edge2
     *  2) 'FakeList' which is the set of negative edges in the same format.
     *  3) 'arabesque-edges.graph' which is the the input graph for Arabesque. You can read more about the format here: https://github.com/qcri/Arabesque/tree/spark-2.0
     *  Compile:
     *          javac Sampling.java
     *  Run:
     *          java Sampling <input graph file>  <number of edges in the sample>
     *  Example:
     *          java Sampling Graph.txt 1000
     *
     */
    public class Sampling {

        // nodes' IDs
        public static HashMap<Integer, Integer> hashIDs = new HashMap<Integer, Integer>();
        // Adjacency List of the graph (bi-directional).
        public static HashMap<Integer, ArrayList<Integer>> hash = new HashMap<Integer, ArrayList<Integer>>();

        // The set of random edges from the graph - positive edges
        public static HashMap<Integer, ArrayList<Integer>> RandomEdges = new HashMap<Integer, ArrayList<Integer>>();

        // the new graph of the sampled positive edges with the added negative edges.
        public static HashMap<Integer, ArrayList<Integer>> newGraph = new HashMap<Integer, ArrayList<Integer>>();
        /*
        *   Get max node's ID in the graph
        *   Used in re-numbering the nodes in the Graph.
        * */

        public static int getMax() {
            int max = 0;

            for (int i : hashIDs.keySet()) {
                if (i >= max)
                    max = i;

            }
            return max;

        }

        /*
        *    Generate new ID for the new node.
        *
        * */
        public static int generateIDs(int vertex) {
            int candidateId = getMax() + 1;

            if (hashIDs.size() == 0)
                candidateId = 0;

            for (Map.Entry<Integer, Integer> entry : hashIDs.entrySet()) {
                if (entry.getValue() == vertex)
                    return entry.getKey();
            }
            if (hashIDs.get(candidateId) == null) {
                hashIDs.put(candidateId, vertex);

            }
            return candidateId;

        }

        /**
         * read the list of edges (source, destination)
         * create hashmap of the vertices
         * prepare the bidirectional edges.
         */

        public static void readDataset(String fileName) {

            String line = null;
            // HashMap<Integer, ArrayList<Integer>> tmp = new HashMap<Integer, ArrayList<
            try {
                // FileReader reads text files in the default encoding.
                FileReader fileReader =
                        new FileReader(fileName);

                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader =
                        new BufferedReader(fileReader);

                while ((line = bufferedReader.readLine()) != null) {
                    String[] splited = line.split("\\s+");

                    int sourceID = generateIDs(Integer.parseInt(splited[0]));
                    int destID = generateIDs(Integer.parseInt(splited[1].trim()));

                    if (!hash.containsKey(sourceID))
                        hash.put(sourceID, new ArrayList<Integer>() {{
                            add(destID);
                        }});
                    else
                        hash.get(sourceID).add(destID);

                    // because it is bidirectional graph.
                    if (!hash.containsKey(destID))
                        hash.put(destID, new ArrayList<Integer>() {{
                            add(sourceID);
                        }});
                    else
                        hash.get(destID).add(sourceID);


                }

                // Always close files.
                bufferedReader.close();


            } catch (FileNotFoundException ex) {
                System.out.println(
                        "Unable to open file '" +
                                fileName + "'");
            } catch (IOException ex) {
                System.out.println(
                        "Error reading file '"
                                + fileName + "'");
                // Or we could just do this:
                // ex.printStackTrace();
            }


        }


        /*
        *  Sampling random positive edges from the graph.
        *  Add the edge only once
        *  It is valid to add the same node twice in the sample (but it should be for different edges).
        * */

        public static void random_positive_edges(int m) {
            //  HashSet<Integer> res = new HashSet<Integer>(m);
            //   HashMap<Integer, Integer> RandomEdges = new HashMap<Integer, Integer>();

            Random rnd = new Random();
            int n = hash.size();//items.size();
            if (m > n) {
                throw new IllegalArgumentException(" sample size is larger than the number of all the edges");
            }
            int e = 0;
            while (e < m) {
                int pos = rnd.nextInt(n - 1);
                if (RandomEdges.containsKey(pos)) {
                    ArrayList<Integer> items = hash.get(pos);
                    int item = rnd.nextInt(items.size());
                    // add the edge only once.
                    if (RandomEdges.get(pos).contains(items.get(item)) || items.get(item) == pos) {
                        continue;
                    } else {

                        RandomEdges.get(pos).add(items.get(item));
                        if (RandomEdges.containsKey(items.get(item))) {
                            // if(!RandomEdges.get(items.get(item)).contains(pos))
                            RandomEdges.get(items.get(item)).add(pos);
                        } else {
                            RandomEdges.put(items.get(item), new ArrayList<Integer>() {{
                                add(pos);
                            }});
                        }
                        save_realList(pos, items.get(item));
                        e++;
                    }
                } else {
                    ArrayList<Integer> items = hash.get(pos);
                    int item = rnd.nextInt(items.size());
                    if (RandomEdges.containsKey(items.get(item))) {
                        if (RandomEdges.get(items.get(item)).contains(pos) || items.get(item) == pos)
                            continue;
                    } else {

                        ArrayList<Integer> l = new ArrayList<Integer>();
                        l.add(items.get(item));
                        RandomEdges.put(pos, l);//items.get(item));
                        if (RandomEdges.containsKey(items.get(item))) {
                            if (!RandomEdges.get(items.get(item)).contains(pos))
                                RandomEdges.get(items.get(item)).add(pos);
                        } else
                            RandomEdges.put(items.get(item), new ArrayList<Integer>() {{
                                add(pos);
                            }});

                        save_realList(pos, items.get(item));
                        e++;

                    }


                }
            }
        }


        /*
        *   This method used to find a path (with customized distance) between two nodes in the graph.
         *  It implements breadth first search and checks for max_distance when search for path.
        *   The method is used in sampling negative edges (unconnected nodes).
        * */

        public static int BFSPathMod(int S, int max_distance) {
            // Mark all the vertices as not visited(By default set as false)
            int d = 0;
            boolean visited[] = new boolean[hashIDs.size()];
            ArrayList<Integer> shortestPathList = new ArrayList<Integer>();
            // Create a queue for BFS
            LinkedList<Integer> queue = new LinkedList<Integer>();
            int[] countTo = new int[hash.size()];
            // Mark the current node as visited and enqueue it
            int s = S;
            visited[s] = true;
            queue.add(s);
            countTo[s] = 1;

            while (queue.size() != 0) {
                // Dequeue a vertex from queue and print it
                s = queue.poll();
                // Get all adjacent vertices of the dequeued vertex s
                // If a adjacent has not been visited, then mark it
                // visited and enqueue it
                ArrayList<Integer> sn = hash.get(s);
                Iterator<Integer> i = sn.listIterator();
                int n;
                while (i.hasNext()) {
                    n = i.next();

                    if (!visited[n]) {
                        visited[n] = true;
                        queue.add(n);
                        countTo[n] = countTo[s] + 1;
                        // System.out.println("node n= " +n);
                        if (countTo[n] == max_distance) { // max ==3
                            // System.out.println(" distances with node =" + n);
                            return n;
                        }

                    }
                }

            }
            return -1;
        }

        /*
        *  check if this edge is in the graph.
        * */
        public static boolean isNeighbour(int source, int dest) {

            ArrayList<Integer> nodes = hash.get(source);
            if (nodes.contains(dest)) {
                return true;
            }

            return false;
        }


        /*  The method used to sample a set of Fake edges to be used as negative class the training/testing datasets.
         *  The key idea is to randomly pick two (unconnected) nodes in the graph.
         *  Search for a path (two nodes or three nodes distance) between the two nodes.
         *  If yes, add it to the sample, otherwise choose different pair.
         * */
        public static void random_negative_edges(int edges, int max_distance) {
            // HashSet<Integer> res = new HashSet<Integer>(m);

            HashMap<Integer, ArrayList<Integer>> fakeEdges = new HashMap<Integer, ArrayList<Integer>>();

            Random rnd = new Random();
            int n = hash.size();//items.size();

            int e = 0;
            // int e2 = edges/2;

            if (e > n) {
                throw new IllegalArgumentException(" sample size larger than all edges");
            }
            outer:
            while (e < Math.ceil(3 * edges / 4)) {
                int pos = rnd.nextInt(n - 1);
                int pos2 = -1;
                if (fakeEdges.containsKey(pos)) {
                    // find path

                    pos2 = BFSPathMod(pos, max_distance);
                    if (fakeEdges.get(pos).contains(pos2) || pos2 == -1 || isNeighbour(pos, pos2)) continue;
                    if (fakeEdges.containsKey(pos2)) {
                        if (fakeEdges.get(pos2).contains(pos)) continue;
                    }
                    save_neg_List(pos, pos2, max_distance - 1);
                    e++;
                    if (!fakeEdges.get(pos).contains(pos2))
                        fakeEdges.get(pos).add(pos2);
                    if (fakeEdges.containsKey(pos2))// pos2);
                        fakeEdges.get(pos2).add(pos);
                    else {
                        ArrayList<Integer> l = new ArrayList<Integer>();
                        l.add(pos);
                        fakeEdges.put(pos2, l);
                    }

                } else {

                    pos2 = BFSPathMod(pos, max_distance);
                    // add the edge only once.
                    if (pos2 == -1 || isNeighbour(pos, pos2)) continue;
                    if (fakeEdges.containsKey(pos2)) {
                        if (fakeEdges.get(pos2).contains(pos)) continue;
                    }
                    e++;
                    save_neg_List(pos, pos2, max_distance - 1);
                    ArrayList<Integer> l = new ArrayList<Integer>();
                    l.add(pos2);
                    fakeEdges.put(pos, l);
                    if (fakeEdges.containsKey(pos2))// pos2);
                        //if(RandomEdges2.) {
                        fakeEdges.get(pos2).add(pos);
                        // }
                    else {

                        ArrayList<Integer> ll = new ArrayList<Integer>();
                        ll.add(pos);
                        fakeEdges.put(pos2, ll);

                    }
                }

                // For controlling the ratio of fake edges with 2 edges distance or 3 edges distance in the sample of the negative edges.

                if ((e == Math.ceil(edges / 4)) && max_distance == 3) {

                    max_distance = 4;
                    e = 0;
                    continue outer;
                }

            }

        }


        /* Combine the current graph with  the fake edges.*/

        public static HashMap<Integer, ArrayList<Integer>> combine() {
            ///
            HashMap<Integer, ArrayList<Integer>> hashmap3 = new HashMap<Integer, ArrayList<Integer>>();
            for (Map.Entry<Integer, ArrayList<Integer>> entry : hash.entrySet()) {
                ArrayList<Integer> arrayList = null;
                //  if(nonhash.containsKey(entry.getKey())) {
                List<Integer> list2 = entry.getValue();
                List<Integer> list3 = newGraph.get(entry.getKey());
                if (list3 != null) {
                    list3.addAll(list2);
                    arrayList = new ArrayList<>(list3);
                } else {
                    arrayList = new ArrayList<>(list2);
                }
                // arrayList = new ArrayList<>(list3);
                hashmap3.put(entry.getKey(), arrayList);


            }
            return hashmap3;


        }

        /*  Optional: Extra check for any self-loop in the graph (an edge from a node to itself)
        *
        * */

        public static HashMap<Integer, ArrayList<Integer>> checkSelfLoop() {
            HashMap<Integer, ArrayList<Integer>> uniq = new HashMap<Integer, ArrayList<Integer>>();

            for (Map.Entry<Integer, ArrayList<Integer>> entry : hash.entrySet()) {
                ArrayList<Integer> arrayList = entry.getValue();
                while (arrayList.contains(entry.getKey())) {
                    arrayList.remove(entry.getKey());

                }
                uniq.put(entry.getKey(), arrayList);

            }

            return uniq;
        }




    /* prepare the graph in the format of Arabesque
    *  node-id  neighbor1 neighbor2 neighbor3 ... neighbor-n
    * */

        public static void save_edged_Arabesque(String filename) {

            BufferedWriter bw = null;
            FileWriter fw = null;

            try {
                File file = new File(filename);

                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                /// write for edges file:
                for (Map.Entry<Integer, ArrayList<Integer>> entry : hash.entrySet()) {
                    ArrayList<Integer> nodes = entry.getValue();
                    bw.write(entry.getKey() + " " + 1);
                    for (int x : nodes) {
                        bw.write(" " + x);

                    }
                    bw.write("\n");
                }

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (bw != null)
                        bw.close();

                    if (fw != null)
                        fw.close();

                } catch (IOException ex) {

                    ex.printStackTrace();

                }
            }

        }


        /*
        *  keep track of the sample of real edges.
        * */


        public static void save_realList(int v1, int v2){
            // write the values on file

            BufferedWriter bw = null;
            FileWriter fw = null;

            try {

                File file = new File("realList");

                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }

                // true = append file
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.write("\n");
                bw.write(v1+ " "+v2);


            } catch (IOException e) {

                e.printStackTrace();
            } finally {

                try {

                    if (bw != null)
                        bw.close();

                    if (fw != null)
                        fw.close();

                } catch (IOException ex) {

                    ex.printStackTrace();

                }
            }



        }


        /*  save the collection of negative edges

        * */

        public static void save_neg_List(int v1, int v2, int pthLong){
            // write the values on file

            BufferedWriter bw = null;
            FileWriter fw = null;

            try {

                File file = new File("FakeList");

                // if file doesnt exists, then create it
                if (!file.exists()) {
                    file.createNewFile();
                }

                // true = append file
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);

                bw.write("\n");
                bw.write(v1+ " "+v2+" "+ pthLong);


            } catch (IOException e) {

                e.printStackTrace();
            } finally {

                try {

                    if (bw != null)
                        bw.close();

                    if (fw != null)
                        fw.close();

                } catch (IOException ex) {

                    ex.printStackTrace();

                }
            }



        }

        /*  test an example*/
        public static void main(String args[]) {



            if (args.length > 0) {
                try {
                    String filename = args[0].trim(); //"out.opsahl-collaboration"
                    readDataset(filename);
                    int  edges = Integer.parseInt(args[1]); //  number of edges
                    random_positive_edges(edges);
                    random_negative_edges(edges,  3); //3 will be the smallest one, i count vertices.
                    hash= combine();
                    hash = checkSelfLoop();
                    save_edged_Arabesque( "arabesque-edges.graph"); // "arabesque-with-all.graph"

                } catch (NumberFormatException e) {
                    System.err.println("Argument" + args[1] + " must be an integer.");
                    System.exit(1);
                }
            }

        }

    }

