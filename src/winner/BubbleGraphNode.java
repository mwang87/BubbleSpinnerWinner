package winner;

import java.util.ArrayList;

public class BubbleGraphNode {
	ArrayList<BubbleGraphNode> neighbors;
	int x;
	int y;
	int color;
	boolean traversed;
	
	public BubbleGraphNode(int x, int y){
		this.x = x;
		this.y = y;
		color = 0;	//need to change this later
		neighbors = new ArrayList<BubbleGraphNode>();
		traversed = false;
	}
	
	public void AddNeighbor(BubbleGraphNode neighbor){
		neighbors.add(neighbor);
	}
	
	public ArrayList<BubbleGraphNode> GetNeighbors(){
		return neighbors;
	}

}
