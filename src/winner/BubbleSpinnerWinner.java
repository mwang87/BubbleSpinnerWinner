package winner;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageConsumer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class BubbleSpinnerWinner {
	
	public static final int MAX_CONNECTED_SIZE = 600;
	public static final int MIN_CONNECTED_SIZE = 150;
	public static final int MAX_MOMENT = 40000;
	public static final int MAX_MOMENT_OVER_SIZE = 100;
	public static final int MIN_MOMENT = 150;
	
	public static final int BOTTOM_LEFT_CENTROID_FILTER_HORIZ = 500;
	public static final int BOTTOM_LEFT_CENTROID_FILTER_VERT = 170;
	
	public static final int NODE_MAX_DISTANCE_CUTOFF = 800;
	
	public static void main(String[] args){
		BufferedImage image = null;
		BufferedImage orig_image = null;
		File file = new File("spinner.bmp");
		try {
			image = ImageIO.read(file);
			orig_image = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//lets find connected components
		int [] connected_component_count_array = {0};
		int [][] connected_components = GetConnectedComponents(image, connected_component_count_array);
		
		System.out.println("We have :"+ connected_component_count_array[0] + " connected components");

		
		

		
		//filter based on size
		ConnectedComponentSizeFilter(connected_components, image, connected_component_count_array);
		
		//color stuff black according to component number
		ColorComponentsFilter(connected_components, image);
		
		//find connected components again
		connected_components = GetConnectedComponents(image, connected_component_count_array);
		
		System.out.println("We have :"+ connected_component_count_array[0] + " connected components");
		
		//filter based on connected component that is entirely enclosed
		EntirelyEnclosedFilter(connected_components, image, connected_component_count_array);
		
		//filter based on size
		ConnectedComponentSizeFilter(connected_components, image, connected_component_count_array);
		
		//filter based on non circular objects by determining if component is a circle
		CircleComponentFilter(connected_components, image, connected_component_count_array);
		
		//color stuff black according to component number
		ColorComponentsFilter(connected_components, image);
		
		//now we can create a graph from the centroids of the remaining connected components
		connected_components = GetConnectedComponents(image, connected_component_count_array);
		
		ArrayList<Integer> centroid_X = new ArrayList<Integer>();
		ArrayList<Integer> centroid_Y = new ArrayList<Integer>();
		
		GetCentroid(connected_components, image, connected_component_count_array, centroid_X, centroid_Y);
		
		//filter out the centroids that are not in the center
		FilterBottomLeftCentroids(centroid_X, centroid_Y);
		
		PaintCentroids(connected_components, image, connected_component_count_array, centroid_X, centroid_Y);
		
		ConstructElementGraph(image, centroid_X, centroid_Y);
		
		
		try {
			ImageIO.write( image, "bmp" , new File ( "output.bmp" ) /* target */ );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("Done");
	}
	

	public static void ConstructElementGraph(BufferedImage image, ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y){
		ArrayList<Integer> Working_X = (ArrayList<Integer>) Centroid_X.clone();
		ArrayList<Integer> Working_Y = (ArrayList<Integer>) Centroid_Y.clone();
		ArrayList<BubbleGraphNode> cur_bubble_graph = new ArrayList<BubbleGraphNode>();
		for(int i = 0; i < Centroid_X.size(); i++){
			BubbleGraphNode cur_Node = new BubbleGraphNode(Centroid_X.get(i), Centroid_Y.get(i));
			cur_bubble_graph.add(cur_Node);
		}
		
		System.out.println("We have " + cur_bubble_graph.size() + "nodes");
		
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
				
				if(square_distance > NODE_MAX_DISTANCE_CUTOFF){
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
				
				
				DebugPaintLine(cur_x, remote_x, cur_y, remote_y, image);
				
			}
		}
	}
	
	public static void DebugPaintLine(int startx, int endx, int starty, int endy, BufferedImage image){
		//System.out.println("("+startx+","+starty+")"+" ("+endx+","+endy+")");
		/*try {
			//Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		if(((startx == endx)|| (startx == endx-1) || (startx == endx+1)) && ((starty == endy) || (starty == endy+1) || (starty == endy-1))){
			return;
		}
		image.setRGB(startx, starty, 70000);
		image.setRGB(endx, endy, 70000);
		DebugPaintLine((startx + endx)/2, endx, (starty + endy)/2, endy, image);
		DebugPaintLine(startx, (startx + endx)/2, starty, (starty + endy)/2,  image);
	}
	
	public static void PaintCentroids(int [][] connected_components, BufferedImage image, int [] connected_component_count_array, ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y){
		for(int i = 0; i < Centroid_X.size(); i++){
			image.setRGB(Centroid_X.get(i), Centroid_Y.get(i), 10000);
		}
	}
	
	public static void FilterBottomLeftCentroids(ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y){
		for(int i = 0; i < Centroid_X.size(); i++){
			if(Centroid_X.get(i) < BOTTOM_LEFT_CENTROID_FILTER_VERT && Centroid_Y.get(i) > BOTTOM_LEFT_CENTROID_FILTER_HORIZ){
				Centroid_X.remove(i);
				Centroid_Y.remove(i);
				i--;
			}
		}
	}
	
	public static void GetCentroid(int [][] connected_components, BufferedImage image, int [] connected_component_count_array, ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y){
		int connected_component_count = connected_component_count_array[0];
		int [] component_centroid_x = new int[connected_component_count+1];
		int [] component_centroid_y = new int[connected_component_count+1];
		int [] component_size_count = new int[connected_component_count+1];
		
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				component_centroid_x[connected_components[i][j]] += i;
				component_centroid_y[connected_components[i][j]] += j;
				component_size_count[connected_components[i][j]]++;
			}
		}
		
		for(int i = 0; i < connected_component_count+1; i++){
			if(component_size_count[i] != 0){
				component_centroid_x[i] = component_centroid_x[i]/component_size_count[i];
				component_centroid_y[i] = component_centroid_y[i]/component_size_count[i];
			}
		}
		
		//now we have the centroids, if they arent 0,0, then it is a valid point
		for(int i = 0; i < connected_component_count+1; i++){
			if(component_centroid_x[i] != 0){
				if(component_size_count[i] < MAX_CONNECTED_SIZE){
					//System.out.println("Centroid: (" + component_centroid_x[i] + "," + component_centroid_y[i] + ")" );
					Centroid_X.add(component_centroid_x[i]);
					Centroid_Y.add(component_centroid_y[i]);
				}
			}
		}
		
		
		
	}
	
	public static void CircleComponentFilter(int [][] connected_components, BufferedImage image, int [] connected_component_count_array){
		int connected_component_count = connected_component_count_array[0];
		int [] component_centroid_x = new int[connected_component_count+1];
		int [] component_centroid_y = new int[connected_component_count+1];
		int [] component_size_count = new int[connected_component_count+1];
		int [] component_moments = new int[connected_component_count+1];
		
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				component_centroid_x[connected_components[i][j]] += i;
				component_centroid_y[connected_components[i][j]] += j;
				component_size_count[connected_components[i][j]]++;
			}
		}
		
		for(int i = 0; i < connected_component_count+1; i++){
			if(component_size_count[i] != 0){
				component_centroid_x[i] = component_centroid_x[i]/component_size_count[i];
				component_centroid_y[i] = component_centroid_y[i]/component_size_count[i];
			}
		}
		
		//now we have centroids, we can calculate moment of inertia
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				int cur_centroid_x = component_centroid_x[connected_components[i][j]];
				int cur_centroid_y = component_centroid_y[connected_components[i][j]];
				component_moments[connected_components[i][j]] += (Math.abs(i - cur_centroid_x)*Math.abs(i - cur_centroid_x) + 
						Math.abs(j - cur_centroid_y)*Math.abs(j - cur_centroid_y));
			}
		}

		
		//now we have moments of inertia, we can filter the ones that are too big out
		//filtering based only on moment
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				if(component_moments[connected_components[i][j]] > MAX_MOMENT){
					//connected_components[i][j] = 0;
				}
			}
		}
		
		
		//filter based upon moment/area
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				if(component_moments[connected_components[i][j]]/component_size_count[connected_components[i][j]] > MAX_MOMENT_OVER_SIZE){
					connected_components[i][j] = 0;
				}
			}
		}
		
	}
	
	public static void ColorComponentsFilter(int [][] connected_components, BufferedImage image){
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				//char color = (char)connected_components[i][j];
				image.setRGB(i, j, connected_components[i][j]*500);
			}
		}
	}
	
	public static void EntirelyEnclosedFilter(int [][] connected_components, BufferedImage image, int [] connected_component_count_array){
		//we look at each connected component, and see if it is touching more than one other component
		int connected_component_count = connected_component_count_array[0];
		HashMap<Integer, Integer> [] connected_component_connectivity = new HashMap[connected_component_count+1];
		//init for array
		for(int i = 0; i < connected_component_count+1; i++){
			connected_component_connectivity[i] = new HashMap<Integer, Integer>();
		}
		
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				int cur_component = connected_components[i][j];
				int right = -1;
				int left = -1;
				int up = -1;
				int down = -1;
				if(i + 1 < image.getWidth()){
					int touching_comp = connected_components[i+1][j];
					if(touching_comp != cur_component){
						connected_component_connectivity[cur_component].put((Integer)touching_comp, 1);
					}
				}
				if(i - 1 > 0){
					int touching_comp = connected_components[i-1][j];
					if(touching_comp != cur_component){
						connected_component_connectivity[cur_component].put((Integer)touching_comp, 1);
					}
				}
				
				if(j + 1 > image.getHeight()){
					int touching_comp = connected_components[i][j+1];
					if(touching_comp != cur_component){
						connected_component_connectivity[cur_component].put((Integer)touching_comp, 1);
					}
				}
				
				if(j - 1 > 0){
					int touching_comp = connected_components[i][j-1];
					if(touching_comp != cur_component){
						connected_component_connectivity[cur_component].put((Integer)touching_comp, 1);
					}
				}
			}
		}
		
		//we shoudl find whihc components have only one touching, meaning its completely enclosed by something else
		/*for(int i = 0; i < connected_component_count+1; i++){
			Set<Integer> adjacent_components = connected_component_connectivity[i].keySet(); 
			if(adjacent_components.size() == 1){
				//System.out.println("Component " + i + " has " + adjacent_components.size() + " neighbors");
			}
		}*/
		
		//make things totally enclosed the same as the neighbor
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j <image.getHeight(); j++){
				//in here we will make it the same as the neighbor
				int cur_component = connected_components[i][j];
				if(connected_component_connectivity[cur_component].keySet().size() == 1){
					connected_components[i][j] = (Integer)connected_component_connectivity[cur_component].keySet().toArray()[0];
				}
			}
		}
	}
	
	public static void ConnectedComponentSizeFilter(int [][] connected_components, BufferedImage image, int [] connected_component_count_array){
		//now that we have connected components, lets count how big each connected component is
		int connected_component_count = connected_component_count_array[0];
		int [] connected_component_tally = new int[connected_component_count+1];
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				connected_component_tally[connected_components[i][j]]++;
			}
		}
		
		//make things outside of some define threshold go to component 0
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				if(connected_component_tally[connected_components[i][j]] > MAX_CONNECTED_SIZE){
					connected_components[i][j] = 0;
				}
				if(connected_component_tally[connected_components[i][j]] < MIN_CONNECTED_SIZE){
					connected_components[i][j] = 0;
				}
			}
		}
	}
	
	public static int[][] GetConnectedComponents(BufferedImage image, int[] connected_component_count){
		 int [][] connectedComponentsPicture = new int[image.getWidth()][image.getHeight()];
		 for(int i = 0; i < image.getWidth(); i++){
			 for(int j = 0; j < image.getHeight(); j++){
				 connectedComponentsPicture[i][j] = 0;
			 }
		 }
		 int connected_components = 0;
		 for(int i = 0; i < image.getWidth(); i++){
			 for(int j = 0; j < image.getHeight(); j++){
				if(connectedComponentsPicture[i][j] == 0){
					//we havent labeled this so lets go recursive on its ass
					connected_components++;
					//System.out.println("Connected Component: "+ connected_components);
					connectedComponentsPicture[i][j] = connected_components;
					IterativeConnect(connectedComponentsPicture, image, i, j);
				}
			 }
		 }
		 connected_component_count[0] = connected_components;

		 return connectedComponentsPicture;
	}
	
	public static void IterativeConnect(int[][] connectedComponentsPicture, BufferedImage image, int x, int y){
		Stack<Integer> X_Stack = new Stack<Integer>();
		Stack<Integer> Y_Stack = new Stack<Integer>();
		
		X_Stack.add(x);
		Y_Stack.add(y);
		//System.out.println(x+":"+y);
		
		while(!X_Stack.isEmpty()){
			int cur_x = X_Stack.pop();
			int cur_y = Y_Stack.pop();
			int current_color = image.getRGB(cur_x, cur_y);
			
			
			if(cur_x+1 < image.getWidth()){
				if(connectedComponentsPicture[cur_x+1][cur_y] == 0 && image.getRGB(cur_x+1, cur_y) == current_color){
					connectedComponentsPicture[cur_x+1][cur_y] = connectedComponentsPicture[cur_x][cur_y];
					X_Stack.add(cur_x+1);
					Y_Stack.add(cur_y);
				}
			}
			
			if(cur_x-1 >= 0){
				if(connectedComponentsPicture[cur_x-1][cur_y] == 0 && image.getRGB(cur_x-1, cur_y) == current_color){
					connectedComponentsPicture[cur_x-1][cur_y] = connectedComponentsPicture[cur_x][cur_y];
					X_Stack.add(cur_x-1);
					Y_Stack.add(cur_y);
				}
			}
			
			
			if(cur_y+1 < image.getHeight()){
				if(connectedComponentsPicture[cur_x][cur_y+1] == 0 && image.getRGB(cur_x, cur_y+1) == current_color){
					connectedComponentsPicture[cur_x][cur_y+1] = connectedComponentsPicture[cur_x][cur_y];
					X_Stack.add(cur_x);
					Y_Stack.add(cur_y+1);
				}
			}
			
			if(cur_y-1 >= 0){
				if(connectedComponentsPicture[cur_x][cur_y-1] == 0 && image.getRGB(cur_x, cur_y-1) == current_color){
					connectedComponentsPicture[cur_x][cur_y-1] = connectedComponentsPicture[cur_x][cur_y];
					X_Stack.add(cur_x);
					Y_Stack.add(cur_y-1);
				}
			}		
		}
		
	}
	
	public static void RecursiveConnect(int[][] connectedComponentsPicture, BufferedImage image, int x, int y){
		boolean right = false;
		boolean left = false;
		boolean up = false;
		boolean down = false;
		
		System.out.println(x+":"+y);
		
		int current_color = image.getRGB(x, y);
		if(x+1 < image.getWidth()){
			if(connectedComponentsPicture[x+1][y] == 0 && image.getRGB(x+1, y) == current_color){
				connectedComponentsPicture[x+1][y] = connectedComponentsPicture[x][y];
				RecursiveConnect(connectedComponentsPicture, image, x+1, y);
			}
		}
		
		if(x-1 >= 0){
			if(connectedComponentsPicture[x-1][y] == 0 && image.getRGB(x-1, y) == current_color){
				connectedComponentsPicture[x-1][y] = connectedComponentsPicture[x][y];
				RecursiveConnect(connectedComponentsPicture, image, x-1, y);
			}
		}
		
		if(y+1 < image.getHeight()){
			if(connectedComponentsPicture[x][y+1] == 0 && image.getRGB(x, y+1) == current_color){
				connectedComponentsPicture[x][y+1] = connectedComponentsPicture[x][y];
				RecursiveConnect(connectedComponentsPicture, image, x, y+1);
			}
		}
		
		if(y-1 >= 0){
			if(connectedComponentsPicture[x][y-1] == 0 && image.getRGB(x, y-1) == current_color){
				connectedComponentsPicture[x][y-1] = connectedComponentsPicture[x][y];
				RecursiveConnect(connectedComponentsPicture, image, x, y-1);
			}
		}		
	}
	
	public static void DrawVertLine(BufferedImage image, int y){
		for(int i = 0; i < image.getWidth(); i++){
			image.setRGB(i, y, 50000);
		}
	}
	
	public static void DrawHorizLine(BufferedImage image, int x){
		for(int i = 0; i < image.getHeight(); i++){
			image.setRGB(x, i, 50000);
		}
	}
	
}
