package winner;

import java.util.ArrayList;

public class BubbleGraphNode {
	ArrayList<BubbleGraphNode> neighbors;
	int x;
	int y;
	
	public BubbleGraphNode(int x, int y){
		this.x = x;
		this.y = y;
		neighbors = new ArrayList<BubbleGraphNode>();
	}
	
	public void AddNeighbor(BubbleGraphNode neighbor){
		neighbors.add(neighbor);
	}
	
	public ArrayList<BubbleGraphNode> GetNeighbors(){
		return neighbors;
	}

}
