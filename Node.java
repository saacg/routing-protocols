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

    void tellTheNeighbors() {
        if(this.lkcost.length > 0 && this.neighbors.size() > 0){
            for(int neighbor : neighbors){
                NetworkSimulator.tolayer2(new Packet(this.nodename, neighbor, this.lkcost)); 
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

        for(int i = 0; i < size; i++){
            this.costs[nodename][i] = initial_lkcost[i];
            this.lkcost[i] = initial_lkcost[i];
            if(initial_lkcost[i] != this.INFINITY && initial_lkcost[i] != 0){
                this.neighbors.add(i); 
            }
        }
        
        tellTheNeighbors();        

        System.out.println("Node " + this.nodename + "initialized at " + NetworkSimulator.clocktime);
        printdt();
        
    }    

    
    void rtupdate(Packet rcvdpkt) { 
            if(rcvdpkt.destid == this.nodename){
                    
                boolean changed = false;
                int src = rcvdpkt.sourceid;  
                int distToSrc = this.costs[this.nodename][src];

                // update distance table
                for(int i = 0; i < rcvdpkt.mincost.length; i++){
                    if(rcvdpkt.mincost[i] == this.INFINITY) {
                        this.costs[src][i] = this.INFINITY;
                    } else {
                        this.costs[src][i] = rcvdpkt.mincost[i] + this.costs[this.nodename][i];
                    }
                }

                // update this node's min cost vector
                for(int i = 0; i < this.lkcost.length; i++){
                    for(int j = 0; j < this.lkcost.length; j++){
                        if(this.costs[i][j] < this.lkcost[i]){
                            this.lkcost[i] = this.costs[i][j];
                            changed = true;
                        }
                    }
                } 

                System.out.println("Node " + this.nodename + "updated by Node " + src + "at " + NetworkSimulator.clocktime);
                printdt();

                // if the min cost vector has been changed, update the neighbors
                if(changed){
                    tellTheNeighbors();
                }

            } else {
                System.out.println("received someone else's packet!");
            }
    }
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {  }    


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
    
}
