package src;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Created by Ghadeer on 2/13/18.
 *
 * An representation of the edge of
 */
public class Edge {

    int V1;
    int V2;

    public Edge(int V1, int V2){
        this.V1= V1;
        this.V2 = V2;

    }
    public boolean Find(int v1,int v2){
    if((this.V1==v1 && this.V2==v2 ) || (this.V2==v1 && this.V2==v2 ) )
        return true;
        return false;
    }

    public boolean Is_Edge(String edge){
        if(edge.equals("")) return false;
        StringTokenizer str = new StringTokenizer(edge,"-");
        int v1= Integer.parseInt(str.nextToken());
        int v2= Integer.parseInt(str.nextToken());

        if((this.V1==v1 && this.V2==v2 ) || (this.V2==v1 && this.V1==v2 ) )
            return true;
        return false;
    }

    public String getEdge(){
        return this.V1 +"-" + this.V2;

    }

    public void Print(){
       // return this.V1 +"-" + this.V2;
        System.out.print( this.V1 +"-" + this.V2);

    }



    public static void main (String args[]){
        ArrayList<Edge> realSet = new ArrayList<Edge>();
        realSet.add(new Edge(3,4));
        List<Edge> olderUsers = realSet.stream().filter(e -> e.Is_Edge("3-4")== true).collect(Collectors.toList());

        olderUsers.forEach(u -> u.Print());

    }
}
