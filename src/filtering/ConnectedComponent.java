package filtering;

import java.awt.image.BufferedImage;
import java.util.Stack;

public class ConnectedComponent {
	
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
				if(connected_component_tally[connected_components[i][j]] > ImageConstants.MAX_CONNECTED_SIZE){
					connected_components[i][j] = 0;
				}
				if(connected_component_tally[connected_components[i][j]] < ImageConstants.MIN_CONNECTED_SIZE){
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
}
