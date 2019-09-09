package src;

import java.io.*;
import java.util.*;

/**
 * Created by Ghadeer on 8/29/19.
 *
 * The main task of this file is preparing the feature set of motifs for the set of the edges in the experiment sample. The preparation includes:
 * 1) Reading the output from Arabesque. The output contains the patterns found in the graph that associated with edges in the experiment sample.
 * 2) Reading the edge sampleset. (In this example, the sample of real edges has been read from file 'realList;)
 * 3) Parsing the detected motifs and generating a representation for each pattren.
 * 4) Aggregating the counting of the motifs for the same pattren if it occurs multiple times for the same edge.
 * 5) Generating a file for each edge, set of pattrens, and their counts.
 *
 *  The input contains i) The output of Aarabesque. ii) the sampleset (realList).
 *  The output includes:
 *  1) The sampleset and the aggregated count for each edge for each pattren in the format:
 *   edge1 , Num1, Num2, Num3.....NumN (As N is the number of different pattrens found for the whole sample set).
 *   edge2,..
 *   edgeM  (As M is the number of edges in the sampleset).
 *  2) The pattrens found for edges sample in the form: prt1,ptr2,ptr3..
 *     For example: pattren 1202 represents the wedge of two edges: 1-2, 2-0 of the threee vertices (0,1,2).
 *
 *
 *  Compile:
 *          javac Aggregation.java
 *  Run:
 *          java Aggregation <arabesque output file>  <pattren file output>  <features output file>
 *  Example:
 *          java Aggregation arabesque_output pattren_found motifs_realEdges
 *
 */
public class Aggregation {

    static ArrayList<Edge> edgeSet = new ArrayList<Edge>();
    // this is the map of each edge and the list of patterns that appeared on it at least one time
    static HashMap<String, ArrayList<pattern>> edge2pattren = new HashMap<String, ArrayList<pattern>>();
    // list of all pattren found in the motifs.
    static ArrayList<pattern> all_pattrens = new ArrayList<pattern>();

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

                /// add the two end points
                edgeSet.add(new Edge(sourceID, destID));

            }
            bufferedReader.close();
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
    /*
    *    read arabesque output in the following format
     *   [1,1-2,1],[0,1-2,1]|[0-1, 1-2] which refers to (pattern | edges)
         saperate into [1,1-2,1],[0,1-2,1] and [0-1, 1-2]
    * */

    public static void read_arabesque_Output(String fileName, String firstLine, String FeatureOut) {

        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                StringTokenizer stk = new StringTokenizer(line, "|");
                int size = 0;
                String[] splited = new String[4];
                while (stk.hasMoreTokens()) {
                    splited[size] = stk.nextToken();
                    size++;

                }
                splited[0] = splited[0].replace("]", "").replace("[", "").replace("-", ",").replace(",", "");
                //do this: [0-1, 1-2] = 0-1,1-2 to extract the list of edges
                if (size <= 1) {
                    System.out.println(stk);
                    continue;
                }

                splited[1] = splited[1].replace("]", "").replace("[", "").
                        replace(" ", "");
                //  }
                // do this to extract the pattern:
                // 11210121 -> 1202
                char[] chrs = splited[0].toCharArray();
                StringBuilder bl = new StringBuilder();
                for (int j = 0; j < chrs.length; j += 2) {
                    bl.append(chrs[j]);

                }
                // prepare the pattern obj
                pattern pat = new pattern();
                pat.setPattern(bl.toString().trim());
                pat.setCount(1); // count the pattren.
                StringTokenizer str2 = new StringTokenizer(splited[1], ",");
                // get the edges from the edges list:
                //  0-1,1-2=
                // loop
                //    0-1
                //    1-2
                while (str2.hasMoreTokens()) { // for each edge

                    String edge = str2.nextToken().trim();
                    StringTokenizer vstr = new StringTokenizer(edge, "-");
                    String v1 = vstr.nextToken();
                    String v2 = vstr.nextToken();
                    boolean found = false;
                    boolean f = false;

                    // check if the edge  exists in the hashmap
                    if (edge2pattren.containsKey(edge) || edge2pattren.containsKey(v2 + "-" + v1)) {
                        f = true;
                        String search = (edge2pattren.containsKey(edge)) ? edge : v2 + "-" + v1;
                        // get the current patterns of this edge
                        ArrayList<pattern> pts = edge2pattren.get(search); //edge
                        for (int i = 0; i < pts.size(); i++) {
                            // if this pattern appeared before for this edge
                            if (pat.getPattern().trim().equals(pts.get(i).getPattern().trim())) {
                                pattern new_pattren = new pattern();
                                new_pattren.setPattern(pts.get(i).getPattern().trim());
                                new_pattren.setCount(pts.get(i).getCount() + 1); // increase count
                                pts.set(i, new_pattren); // update the list of patterns
                                found = true;
                                edge2pattren.get(search).set(i, new_pattren); // update the value of that edge in the hashmap
                                break;
                            }
                        }

                        if (!found) { // pattern doesn't exist before for this edge
                            edge2pattren.get(search).add(pat); // add the pattern to the edge's list with count 1

                        }

                    } else {
                        edge2pattren.put(edge, new ArrayList<pattern>() {{
                            add(pat);
                        }});
                        // add the edge as key and the pattern in the list of values in the hashmap

                    }

                }
            }
            bufferedReader.close();
            // to write the list of features for every edge in a file
            write_pattrens(firstLine, FeatureOut);

        } catch (java.lang.NullPointerException exception) {
            // Catch NullPointerExceptions.
            //  Logging.log(exception);

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


    /* the output will be:
     *  edge, count_ptr1, count_ptr2...
      * */


    public static void write_edges_features(String edge, ArrayList<pattern> ptscounts, String FeatureOut) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(FeatureOut);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(edge);
            for (pattern pp : all_pattrens) {
                boolean found = false;
                for (int i = 0; i < ptscounts.size(); i++) {
                    if (pp.getPattern().equals(ptscounts.get(i).getPattern())) {
                        found = true;
                        bw.write("," + ptscounts.get(i).getCount());
                        // break;
                    }
                }
                if (!found) {
                    // set 0 for the edge if it does not have the pattren.
                    bw.write("," + 0);
                }

            }
            bw.write("\n");
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

    /* check if the edge in the graph.*/

    public static String checkForEdge(String edge) {
        for (Edge e : edgeSet) {
            if (e.Is_Edge(edge)) {
                return e.getEdge();

            }

        }
        return null;

    }

    /*  write all pattrens (motifs) found in the graph in a file
    *  Then, save (in file) for each edge the count of each associate pattren (motifs)
    * */


    public static void write_pattrens(String firstline, String FeatureOut) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            File file = new File(firstline);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            // header E \t p1 \t p2
            bw.write("edge");

            for (pattern pp : all_pattrens) {
                boolean found = false;
                bw.write("," + pp.getPattern());
            }
            bw.write("\n");
            for (Map.Entry<String, ArrayList<pattern>> entry : edge2pattren.entrySet()) {

                ArrayList<pattern> pts = entry.getValue();
                //here I would compare with the list of Edge that has been chosen in the sample.
                String s = checkForEdge(entry.getKey());
                // not needed at training dataset
                if (s != null) {
                    write_edges_features(s, pts, FeatureOut); //entry.getKey()
                    continue;
                }
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

    /* example test*/
    public static void main(String args[]) {

        String fileName = "realList"; //args[0]; // read real edges
        String motifs_output = args[0].trim(); // motifs output from arabesque.
        String pattrens = args[1].trim(); // save the pattren detected in the graph.
        String edge_features = args[2].trim(); // where to save the output (edge, [ptr1,ptr2..]
        read_edges(fileName);
        read_arabesque_Output(motifs_output, pattrens, edge_features);
    }


}
