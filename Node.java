import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 
    
    public static final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between this node and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/
    ArrayList<Integer> neighbors; /* list of direct neighbors to this node */
    int[][] minCostPaths;
    int[][][] paths;
     
    /* Class constructor */
    public Node() { }

    // sends min cost array to neighbors
    void tellTheNeighbors() {
        int lkcostLength = this.lkcost.length;
        if(lkcostLength > 0 && this.neighbors.size() > 0){
            for(int neighbor : neighbors){
                int[] poisonedlkcost = new int[lkcostLength];
                int[][] poisonedmcpaths = new int[lkcostLength][lkcostLength];
                for(int i = 0; i < lkcostLength; i++){ 
                    boolean poisonpath = false;
                    //if(this.costs[i][neighbor] == this.lkcost[i] && i != this.nodename){
                    for(int j = 0; j < lkcostLength; j++){
                        if(this.minCostPaths[i][j] == neighbor && this.minCostPaths[i][j] != i){ 
                            poisonpath = true;
                        }
                    }
                    if(poisonpath){
                        poisonedlkcost[i] = this.INFINITY;         
                        for(int j = 0; j < lkcostLength; j++){
                            poisonedmcpaths[i][j] = -1;
                        }
                    } else {
                        poisonedlkcost[i] = this.lkcost[i]; 
                        for(int j = 0; j < lkcostLength; j++){
                            poisonedmcpaths[i][j] = this.minCostPaths[i][j];
                        }
                    }
                }
                System.out.println("Node " + this.nodename + " sending update to Node " + neighbor + " at " + NetworkSimulator.clocktime);
                printpsnd(poisonedlkcost, neighbor);
                NetworkSimulator.tolayer2(new Packet(this.nodename, neighbor, poisonedlkcost, poisonedmcpaths));
            } 
        }
    }    

    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost, int[][] initial_lkpath) {
        
        // initialize nodename and distance table 
        int size = initial_lkcost.length;
        this.nodename = nodename; 
        this.costs = new int[size][size];
        this.lkcost = new int[size];
        this.neighbors = new ArrayList<Integer>();
        this.minCostPaths = new int[size][size];
        this.paths = new int[size][size][size];
        
        // initialize all distances in table to INFINITY
        NetworkSimulator.fill2d(this.costs, this.INFINITY);
        // initialize all paths to -1
        NetworkSimulator.fill3d(this.paths, -1);
        
        // process the initial cost array into the distance table and min cost array
        for(int i = 0; i < size; i++){
            this.costs[this.nodename][i] = initial_lkcost[i];
            this.costs[i][this.nodename] = initial_lkcost[i];
            this.lkcost[i] = initial_lkcost[i];
            if(initial_lkcost[i] != this.INFINITY && initial_lkcost[i] != 0){
                this.neighbors.add(i); 
            }
        }

        for(int i  = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                this.paths[this.nodename][i][j] = initial_lkpath[i][j];
                this.paths[i][this.nodename][j] = initial_lkpath[i][j];
                this.minCostPaths[i][j] = initial_lkpath[i][j];
            }
        }
        
        System.out.println("Node " + this.nodename + " initialized at " + NetworkSimulator.clocktime);
        printdt();
        printlkc();
        printPaths();
        // send min cost array to direct neighbors 
        tellTheNeighbors();        

        
    }    

    
    void rtupdate(Packet rcvdpkt) { 

        if(rcvdpkt.destid == this.nodename){
                    
            boolean changed = false;
            int src = rcvdpkt.sourceid;  

            // update distance table
            for(int i = 0; i < rcvdpkt.mincost.length; i++){
                if(rcvdpkt.mincost[i] == this.INFINITY) {
                    this.costs[i][src] = this.INFINITY;
                    this.costs[src][i] = this.INFINITY;
                    for(int j = 0; j < rcvdpkt.mincost.length; j++){
                        this.paths[i][src][j] = -1; 
                        this.paths[src][i][j] = -1;
                    }
                } else {
                    this.costs[i][src] = rcvdpkt.mincost[i] + this.costs[src][this.nodename];
                    this.paths[i][src][0] = src;  // because src will always be a neighbor, there will always be only one hop in this path
                    if(i == src){
                        for(int j = 1; j < rcvdpkt.mincost.length; j++){
                            this.paths[i][src][j] = -1;
                        }           
                    } else {
                        for(int j = 1; j < rcvdpkt.mincost.length; j++){
                            this.paths[i][src][j] = rcvdpkt.pathVector[i][j-1]; // add the path from this node to the neighbor to the path from the neighbor to node i
                        }
                    }   
                }
            }

            // update this node's min cost array
            for(int i = 0; i < this.lkcost.length; i++){
                int minCost = this.INFINITY;
                int[] newPath = new int[this.lkcost.length];
                for(int j = 0; j < this.lkcost.length; j++){
                    // find the shortest path to the node
                    if(this.costs[i][j] < minCost){
                        minCost = this.costs[i][j];
                        for(int k = 0; k < this.lkcost.length; k++){
                           newPath[k] = this.paths[i][j][k];  
                           //System.out.println("k = " + k + ": " + newPath[k]);
                        }
                    } 
                }
                // if the shortest path has changed for that node, update the min cost array
                if(this.lkcost[i] != minCost){
                    this.lkcost[i] = minCost;
                    for(int l = 0; l < this.lkcost.length; l++){
                        this.minCostPaths[i][l] = newPath[l]; 
                    }
                    changed = true;
                }
            } 

            System.out.println("Node " + this.nodename + " updated by Node " + src + " at " + NetworkSimulator.clocktime);
            printdt();
            printlkc();
            printPaths();
            printmcp();

            // if the min cost array has been changed, update the neighbors
            if(changed){
                tellTheNeighbors();
            }
            else {
                System.out.println("update by Node " + src + " did not change state of Node " + this.nodename + "'s min cost array");
            }

        } else {
            System.out.println("received someone else's packet!");
        }

    }
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) { 
   
        this.costs[linkid][this.nodename] = newcost; 
        this.costs[this.nodename][linkid] = newcost;
        this.costs[linkid][linkid] = newcost;
        
        boolean changed = false;
        // update this node's min cost array
        for(int i = 0; i < this.lkcost.length; i++){
            int minCost = this.INFINITY;
            for(int j = 0; j < this.lkcost.length; j++){
                // find the shortest path to the node
                if(this.costs[i][j] < minCost){
                    minCost = this.costs[i][j];
                } 
            }
            // if the shortest path has changed for that node, update the min cost array
            if(this.lkcost[i] != minCost){
                this.lkcost[i] = minCost;
                changed = true;
            }
        } 

        System.out.println("Distance between Node " + this.nodename + " and Node " + linkid + " changed to " + newcost + " at " + NetworkSimulator.clocktime);
        printdt();
        printlkc();

        // if the min cost array has been changed, update the neighbors
        if(changed){
            tellTheNeighbors();
        }
        else {
            System.out.println("updated distance to Node " + linkid + " did not change state of Node " + this.nodename + "'s min cost array");
        }
    }    



    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }

    void printlkc(){
        System.out.printf("min cost array for Node " + this.nodename + ": "); 
        for(int mincost : lkcost){
            System.out.printf(mincost + " ");
        }
        System.out.println("");
    }

    void printpsnd(int[] psnArray, int dest){
        System.out.printf("min cost array  for Node " + this.nodename + " sent to Node " + dest + " after poisoning: "); 
        for(int mincost : psnArray){
            System.out.printf(mincost + " ");
        }
        System.out.println("");

    }

    void printPaths() {

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                System.out.printf("Path from Node " + this.nodename + " to Node " + i + " through Node " + j + ":"); 
                for(int k = 0; k < 4; k++){
                    if(this.paths[i][j][k] > -1){
                        System.out.printf(" " + this.paths[i][j][k]);
                    } else {
                        System.out.printf(" -");
                    }
                }
                System.out.println("");
            }
        } 
    }

    void printmcp(){
        for(int i = 0; i < 4; i++){
            System.out.printf("Min cost path from Node " + this.nodename + " to Node " + i + ": "); 
            for(int j = 0; j < 4; j++){
                if(this.minCostPaths[i][j] > -1){
                    System.out.printf(" " + this.minCostPaths[i][j]);
                } else {
                    System.out.printf(" -");
                }
            }
            System.out.println("");
        }
    }
}
