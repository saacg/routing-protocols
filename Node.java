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
     
    /* Class constructor */
    public Node() { }

    // sends min cost array to neighbors
    void tellTheNeighbors() {
        int lkcostLength = this.lkcost.length;
        if(lkcostLength > 0 && this.neighbors.size() > 0){
            for(int neighbor : neighbors){
                int[] poisonedlkcost = new int[lkcostLength];
                for(int i = 0; i < lkcostLength; i++){ 
                    if(this.costs[i][neighbor] == this.lkcost[i] && i != this.nodename){
                        poisonedlkcost[i] = this.INFINITY;         
                    } else {
                        poisonedlkcost[i] = this.lkcost[i]; 
                    }
                }
                System.out.println("Node " + this.nodename + " sending update to Node " + neighbor + " at " + NetworkSimulator.clocktime);
                printpsnd(poisonedlkcost, neighbor);
                NetworkSimulator.tolayer2(new Packet(this.nodename, neighbor, poisonedlkcost));
            } 
        }
    }    

    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) {
        
        // initialize nodename and distance table 
        int size = initial_lkcost.length;
        this.nodename = nodename; 
        this.costs = new int[size][size];
        this.lkcost = new int[size];
        this.neighbors = new ArrayList<Integer>();
        
        // initialize all distances in table to INFINITY
        for(int[] row : this.costs){ 
            Arrays.fill(row, this.INFINITY);
        }

        // process the initial cost array into the distance table and min cost array
        for(int i = 0; i < size; i++){
            this.costs[this.nodename][i] = initial_lkcost[i];
            this.costs[i][this.nodename] = initial_lkcost[i];
            this.lkcost[i] = initial_lkcost[i];
            if(initial_lkcost[i] != this.INFINITY && initial_lkcost[i] != 0){
                this.neighbors.add(i); 
            }
        }
        
        System.out.println("Node " + this.nodename + " initialized at " + NetworkSimulator.clocktime);
        printdt();
        printlkc();
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
                } else {
                    this.costs[i][src] = rcvdpkt.mincost[i] + this.costs[src][this.nodename];
                }
            }

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

            System.out.println("Node " + this.nodename + " updated by Node " + src + " at " + NetworkSimulator.clocktime);
            printdt();
            printlkc();

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
}
