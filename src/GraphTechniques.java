package src;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by Ghadeer on 8/30/19.
 *
 * The main task of this file is computing different graph topological techniques that used for comparsion with motifs
 * The techniques are:
 * 1) Common Neighbour
 * 2) Adamic Adar.
 * 3) Jaccard coefficient
 * 4) Preferential Attachment
 * 5) Rooted Page Rank (in the code the implementation of "Random Walk" approach.
 * 6) Katz (in the code the implementation between two nodes based on the number of paths).
 *
 *  The input contains i) The original Graph. ii) the sampleset (realList for example).
 *  The output includes:
 *  1) techniques_results file which contains the results in the format:
 *   edge1 , Value1, Value2, Value3.....ValueN (As N is the number of different techniques computed and valuei is the result)
 *   edge2,..
 *   edgeM  (As M is the number of edges in the sampleset).
 *
 *  Compile:
 *          javac GraphTechniques.java
 *  Run:
 *          java GraphTechniques <Graph file>  <edge sample file>
 *  Example:
 *          java GraphTechniques Graph.text  sample_edges
 *
 */
public class GraphTechniques {
    // the graph
    public static HashMap<Integer, ArrayList<Integer>> hash = new HashMap<Integer, ArrayList<Integer>>();
    // the sample
    public static HashMap<Integer, ArrayList<Integer>> edgeSet = new HashMap<Integer, ArrayList<Integer>>();
    // nodes & number of connections
    public static HashMap<Integer, Integer> nodes = new HashMap<Integer, Integer>();
   // nodes & its degree
   public static  HashMap<Integer, Double> nodes_value = new HashMap<Integer, Double>();

   // keep track of the processed edges
    public static HashMap<Integer, Integer> process_edges = new HashMap<Integer, Integer>();
   //
   public static ArrayList<Integer> DegreeReal = new ArrayList<Integer>();


   // variables used for paths search
    public static List<Integer>[] pathToNode = null;
    public static LinkedList<Integer> queue = null;
    static boolean  visited[]  = new boolean[hash.size()];
    static int countLevel =0;
    public static  boolean[] includedInPath= null;



    /* read the graph*/
    public static void read_edges(String fileName) {

        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                String[] splited = line.split("\\s+");

                int sourceID = Integer.parseInt(splited[0]);
                int destID = Integer.parseInt(splited[1].trim());


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
            //System.out.println("all real" + hash.keySet().size());
            // System.out.println("all 0 " + hash.get(0).toString());


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


    /* read the sample edge*/

    public static void read_edgeSample(String fileName) {

        String line = null;
        HashSet<String> fake = new HashSet<String>();
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                String[] splited = line.split("\\s+");
                int sourceID = Integer.parseInt(splited[0]);
                int destID = Integer.parseInt(splited[1].trim());

                if (edgeSet.containsKey(sourceID)) {
                    if (!edgeSet.get(sourceID).contains(destID))
                        edgeSet.get(sourceID).add(destID);

                } else {
                    edgeSet.put(sourceID, new ArrayList<Integer>() {{
                        add(destID);
                    }});
                }

                if (edgeSet.containsKey(destID)) {
                    if (!edgeSet.get(destID).contains(sourceID))
                        edgeSet.get(destID).add(sourceID);

                } else {
                    edgeSet.put(destID, new ArrayList<Integer>() {{
                        add(sourceID);
                    }});
                }
            }
            bufferedReader.close();
            System.out.println("all edges" + edgeSet.size());
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");

        }

    }

    /* compute degree based on number of connections the node has*/

    public static void compute_node_connection() {
        //degree based on the number of current edges of the node.
        for (Map.Entry<Integer, ArrayList<Integer>> entry : hash.entrySet()) {
            ArrayList<Integer> neighbors = entry.getValue();
            if (neighbors == null || neighbors.size() == 0)
                nodes.put(entry.getKey(), 1);
            else
                nodes.put(entry.getKey(), neighbors.size());
        }
    }

    /* compute the degree of each node.*/
    public static void compute_node_degree() {
        //degree based on adamic
        for (Map.Entry<Integer, ArrayList<Integer>> entry : hash.entrySet()) {
            ArrayList<Integer> neighbors = entry.getValue();
            //System.out.println(entry.getKey());
            if (neighbors == null || neighbors.size() == 0)
                nodes_value.put(entry.getKey(), 0.0);
            else
                nodes_value.put(entry.getKey(), Math.log10(1 / (float)neighbors.size()));

        }
    }

  /**/

    public static boolean ProcessedBefore(int v1, int v2) {

        if (process_edges.containsKey(v1))
            if (process_edges.get(v1) == v2)
                return true;

        if (process_edges.containsKey(v2))
            if (process_edges.get(v2) == v1)
                return true;


        return false;

    }


    /* compute the number of time the destination has been visited
     (starting from specific source) by random walk
     The reset paramter used to re-initialize the random walk from the source if the destination was mot reached within specific number of steps.
     */


    static double random_walk_pageRank(int v1, int v2){
        int source = v1;
        int dest= v1;
        int visitTime = 0;
        double reset_prop = 0.2; // B
        int steps [] = new int[100];
        Random rand = new Random();
        int count = 0;
        while (count < 100 ) { // can be changed
            visitTime=1;
            dest = v1;
            while (dest != v2) {
                double randomValue = (0.8) * rand.nextDouble();
                if (randomValue <= reset_prop) {
                    dest = source;
                } else {
                    ArrayList<Integer> items = hash.get(dest);
                    int item = rand.nextInt(items.size());
                    dest = items.get(item);  //one of the neighbours
                }
                visitTime++;
            }
            steps[count]= visitTime;
            count++;
        }


        int sum = 0;
        for(int i=0; i < steps.length;i++) {
            sum += steps[i];
        }
        // one possible answer can by the average by 100 steps. it can be changed.
        return (double)sum/100;
    }

    /* mark visited nodes*/


    public static  boolean allVisited() {

        for( boolean b : visited) {
            if(! b ) return false;
        }
        return true;
    }


    /* as we search for multiple paths between nodes, we need to initializeSearch whenever a new path found */
    public static void initializeSearch(int source) {

        queue = new LinkedList<>();
        queue.add(source);
        countLevel=0;
        visited = new boolean[hash.size()];
        pathToNode= new ArrayList[hash.size()];
        for (int i = 0; i < hash.size(); i++) {
            pathToNode[i]= new ArrayList<>();
        }
    }

    /* The method used to search for multiple paths (with different distance) between two nodes in the graph.*/


    public static int [] findpaths(int source, int destination , int k) {

        int paths [] = new int [k+1];
        includedInPath = new boolean[hash.size()];
        initializeSearch(source);
        int pathCounter = 0;
        int length=0;
        int count=0;
        int level= 0;

        while(! allVisited() && !queue.isEmpty()) {

            while (!queue.isEmpty()) {

                // Dequeue a vertex from queue and print it
                int src = queue.poll();
                visited[src] = true;
                length = pathToNode[src].size();

                if (src == destination &&  length == k) {
                    for (int i = 1; i < pathToNode[src].size(); i++) {
                        includedInPath[pathToNode[src].get(i)] = true;
                    }
                    System.out.println("Path " + pathCounter + " from "+source+" to "
                            + destination+ " :- "+ pathToNode[src]);
                    paths[length] +=1;
                    initializeSearch(source); //initialize before restarting
                    ++pathCounter;
                    break; //exit loop if target found

                }

                // when neither the source or destination have neighbours

                Iterator<Integer> i = hash.get(src).listIterator();
                int nsize = hash.get(src).size();

                if(length > k) {  // stop if you reach k distance during the search
                    return paths;
                }
                while (i.hasNext()) {
                    int n = i.next();
                    if (!queue.contains(n) && !visited[n]
                            && !includedInPath[n] /*ignore nodes already in a path*/) {
                        length= pathToNode[src].size();
                        if(n==destination && length!=k-1) {
                            visited[n] = false;
                            continue;
                        }

                        queue.add(n);
                        pathToNode[n].addAll(pathToNode[src]);
                        pathToNode[n].add(src);
                        length= pathToNode[src].size();

                    }
                }

            }

        }

        return paths;
    }
   /**/

    /*     Katz method - using the number of paths between two nodess*/

    static double compute_Katz(int u, int v ){
        boolean[] isVisited = new boolean[hash.size()];
        double score=0.0;
        double factor= 0.5;
        int diam= 4; //14
     //   int pre= 0;
        int [] counts = findpaths(u,v,diam);
        for(int i =0; i < counts.length;i++ ) {
            double value = Math.pow(counts[i], (i+1)) * Math.pow(factor, (i+1));
            score+=value;
        }

        return score;
    }

    /* just save the values into a file.*/

    public static void save_computation(String fileName ,int v1, int v2, int common, double jacard, double PA, double sum, double HD,double katz){

        // write the values on file

        BufferedWriter bw = null;
        FileWriter fw = null;

        process_edges.put(v1, v2);
        process_edges.put(v2, v1);

        try {
            File file = new File(fileName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write("\n");
            bw.write(v1+ "-"+v2 +" "+ HD + " " + katz +" "+1);


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


    /**/
    public static double getSum(List<Integer> commonList){
        double sum = 0.0;
        if(commonList==null)
            return 0.0;
        if(commonList.size()==0)
            return 0.0;
        for(int i=0;i<commonList.size();i++){
            sum+=nodes_value.get(commonList.get(i));

        }

        return  sum;
    }


    /* compute:
    *   Common Neighbors, Jaccard Coefficient, Adamic/Adar, Preferential Attachment, Rooted PageRank, Katz Index
     *  */

    static void  compute_techniques(String fileName) {


        for (Map.Entry<Integer, ArrayList<Integer>> entry : hash.entrySet()) {

            ArrayList<Integer> neighborslist = entry.getValue();
            for (int i = 0; i < neighborslist.size(); i++) {
                ArrayList<Integer> Nneighbors = hash.get(neighborslist.get(i));
                List<Integer> common = Nneighbors.stream().filter(neighborslist::contains).collect(toList());
                int TotalFriends = Nneighbors.size() + neighborslist.size();
                int PA= Nneighbors.size() * neighborslist.size(); //  nodesValuesReal
                //save the commonFriends task
                 double sum= getSum(common);

                //check if it is in the sample:
                if (edgeSet.containsKey(entry.getKey())) {
                    if (edgeSet.get(entry.getKey()).contains(neighborslist.get(i))) {
                        if (!ProcessedBefore(entry.getKey(), neighborslist.get(i))) {
                            double AvgSteps = random_walk_pageRank(entry.getKey(), neighborslist.get(i));  // steps
                            // for bidirectional edge you can compute the page rank in two directions.
                           // double AvgSteps2 = random_walk_pageRank(neighborslist.get(i),entry.getKey());
                            double score = compute_Katz(entry.getKey(), neighborslist.get(i));
                            save_computation(fileName, entry.getKey(), neighborslist.get(i), common.size(),(float) common.size() / (float) TotalFriends, PA, getSum(common) , -AvgSteps * (1 / (float) nodes.get(neighborslist.get(i))),score);
                           // saveCN(entry.getKey(), neighborslist.get(i), common.size(), (float) common.size() / (float) TotalFriends, PA, sum , -AvgSteps*(1/(float)nodesValuesReal.get(neighborslist.get(i)) ),score);


                        }
                    }
                }
            }
        }
    }


    public static void main(String args []){


        if (args.length > 0) {
            try {
                read_edges(args[0].trim()); // read the whole graph. args[0] is the name of the file.
                read_edgeSample(args[1].trim()); // the sample
                compute_node_connection();
                compute_node_degree();
                compute_techniques("techniques_results");


            } catch (Exception e) {
                System.exit(1);
            }
        } else System.out.println("No parameters inserted");


    }



}
