package winner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import filtering.ImageConstants;

public class BubbleGraph {
	
	public static boolean DRAW_CENTROID_COLOR = false;
	public static boolean DRAW_CONNECTED_COLOR = true;
	
	public static final int NODE_MAX_DISTANCE_CUTOFF = 800;
	
	public static void RemoveUnconnectedNodes(ArrayList<BubbleGraphNode> bubble_graph){
		for(int i = 0; i < bubble_graph.size(); i++){
			if(bubble_graph.get(i).GetNeighbors().size() == 0){
				bubble_graph.remove(i);
				i--;
			}
		}
	}
	
	public static int GetUnconnectedNodeColor(ArrayList<BubbleGraphNode> bubble_graph){
		for(int i = 0; i < bubble_graph.size(); i++){
			if(bubble_graph.get(i).GetNeighbors().size() == 0){
				return bubble_graph.get(i).color;
			}
		}
		return 0;
	}
	
	private static class byNodeConnectivity implements java.util.Comparator<BubbleGraphNode> {
		 public int compare(BubbleGraphNode first, BubbleGraphNode second) {
		  int first_connectivity = ((BubbleGraphNode)first).GetNeighbors().size();
		  int second_connectivity = ((BubbleGraphNode)second).GetNeighbors().size();
		  return first_connectivity - second_connectivity;
		 }
	} 
	
	public static void SortBubbleGraph(ArrayList<BubbleGraphNode> bubble_graph_sorted){
		Collections.sort(bubble_graph_sorted, new byNodeConnectivity());
		return;
	}

	public static ArrayList<BubbleGraphNode> ConstructElementGraph(BufferedImage image, BufferedImage orig_image, ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y, int [][] connected_components ){
		ArrayList<BubbleGraphNode> cur_bubble_graph = new ArrayList<BubbleGraphNode>();
		for(int i = 0; i < Centroid_X.size(); i++){
			BubbleGraphNode cur_Node = new BubbleGraphNode(Centroid_X.get(i), Centroid_Y.get(i));
			
			int connected_component_number = connected_components[Centroid_X.get(i)][Centroid_Y.get(i)];
			HashMap<Integer, Integer> color_count = new HashMap<Integer, Integer>();
			for(int p = 0; p < image.getWidth(); p++){
				for(int q = 0; q < image.getHeight(); q++){
					if(connected_component_number == connected_components[p][q]){
						if(color_count.containsKey(orig_image.getRGB(p, q))){
							int count = color_count.get(orig_image.getRGB(p, q));
							count++;
							color_count.put(orig_image.getRGB(p, q), count);
						}
						else{
							color_count.put(orig_image.getRGB(p, q), 1);
						}
					}
				}
			}
			//find the most frequent
			int most_frequent_color = 0;
			int max_freq = 0;
			for(Integer key : color_count.keySet()){
				int frequency = color_count.get(key);
				//System.out.println("Color: " + key + " Freq: " + frequency);
				if(frequency > max_freq){
					most_frequent_color = key;
					max_freq = frequency;
				}
			}
			
			//filter out big things
			if(max_freq > ImageConstants.MAX_CONNECTED_SIZE){
				continue;
			}
			
			cur_Node.color = most_frequent_color;
			System.out.println(cur_Node.color + " " + most_frequent_color);
			if(DRAW_CENTROID_COLOR){
				image.setRGB(Centroid_X.get(i)+2, Centroid_Y.get(i)-1,  orig_image.getRGB(Centroid_X.get(i)+2, Centroid_Y.get(i)-1));
			}
			if(DRAW_CONNECTED_COLOR){
				for(int p = 0; p < image.getWidth(); p++){
					for(int q = 0; q < image.getHeight(); q++){
						if(connected_component_number == connected_components[p][q]){
							image.setRGB(p, q, cur_Node.color);
						}
					}
				}
			}
			
			cur_bubble_graph.add(cur_Node);
		}
		
		System.out.println("We have " + cur_bubble_graph.size() + " nodes");
		
		//now we need to construct the graph
		for(int i = 0; i < cur_bubble_graph.size(); i++){
		//for(int i = 0; i < 1; i++){
			//if we comb through the points, we can see which points we are connected to and make a undirected graph
			BubbleGraphNode cur_node = cur_bubble_graph.get(i);
			int cur_x = cur_node.x;
			int cur_y = cur_node.y;
			for(int j = 0; j < cur_bubble_graph.size(); j++){
				int remote_x = cur_bubble_graph.get(j).x;
				int remote_y = cur_bubble_graph.get(j).y;
				
				//calculating distance
				int square_distance = (remote_x - cur_x)*(remote_x - cur_x) + (remote_y - cur_y)*(remote_y - cur_y);
				
				if(square_distance > NODE_MAX_DISTANCE_CUTOFF || square_distance == 0 ){
					//then it is out or range
					continue;
				}
					
				//adding the neighbor
				cur_node.AddNeighbor(cur_bubble_graph.get(j));
			}
		}
		
		
		for(int i = 0; i < cur_bubble_graph.size(); i++){
			BubbleGraphNode cur_node = cur_bubble_graph.get(i);
			int cur_x = cur_node.x;
			int cur_y = cur_node.y;
			for(int j = 0; j < cur_node.GetNeighbors().size(); j++){
				int remote_x = cur_node.GetNeighbors().get(j).x;
				int remote_y = cur_node.GetNeighbors().get(j).y;
				
				//debug drawing
				int start_x = Math.min(cur_x, remote_x);
				int end_x = Math.max(cur_x, remote_x);
				int start_y = Math.min(cur_y, remote_y);
				int end_y = Math.max(cur_y, remote_y);
				
				
				//DebugPaintLine(cur_x, remote_x, cur_y, remote_y, image);
				
			}
		}
		
		return cur_bubble_graph;
	}
}
