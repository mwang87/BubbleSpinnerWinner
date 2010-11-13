package winner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import winner.BubbleSpinnerWinner.Side;

public class ShotFinder {
	
	public static final int LEFT_SIDE_OFFSET = 10;
	public static final int RIGHT_SIDE_OFFSET = 491;
	public static final int BOTTOM_SIDE_OFFSET = 531;
	public static final int TOP_SIDE_OFFSET = 52;
	
	public static final int BALL_RADIUS_INT = 12;
	public static final double BALL_RADIUS = (double)BALL_RADIUS_INT;
	
	public static final int SHOOTER_Y_OFFSET = 4;
	
	public static final int SHOOTER_X = 250;
	public static final int SHOOTER_Y = 43;
	
	public static boolean DRAW_SHOOTER_PATH = false;
	
	public static final int MIN_TRAVEL_TRACE = 20;
	public static final int MIN_DISTANCE_FROM_START = 20;
	
	public static final int PATH_COLOR = -16757216;

	public static final int MAX_NEIGHBOR_HIT_LIMIT = 5;
	public static final int MAX_GUIDE_LINE_HIT_DISTANCE = 300;

	
	public static int FindBottomBound(BufferedImage image){
		int bottom_side = BOTTOM_SIDE_OFFSET - BALL_RADIUS_INT;
		for(int i = 0; i < image.getWidth(); i++){
			image.setRGB(i, bottom_side, 100000);
		}
		return bottom_side;
	}
	
	public static int FindTopBound(BufferedImage image){
		int top_side = TOP_SIDE_OFFSET + BALL_RADIUS_INT;
		for(int i = 0; i < image.getWidth(); i++){
			if(i > 150  && i < 350)
				image.setRGB(i, top_side-50, 100000);
			else
				image.setRGB(i, top_side, 100000);
		}
		for(int i = top_side-50; i < top_side; i++){
			image.setRGB(150, i, 100000);
			image.setRGB(350, i, 100000);
		}
		return top_side;
	}
	
	public static int FindRightBound(BufferedImage image){
		int right_side = RIGHT_SIDE_OFFSET - BALL_RADIUS_INT;
		for(int i = 0; i < image.getHeight(); i++){
			image.setRGB(right_side, i, 100000);
		}
		return right_side;
	}
	
	public static int FindLeftBound(BufferedImage image){
		int left_side = LEFT_SIDE_OFFSET + BALL_RADIUS_INT;
		for(int i = 0; i < image.getHeight(); i++){
			image.setRGB(left_side, i, 100000);
		}
		return left_side;
	}
	
	public static void FindFireLocation(BufferedImage image,  ArrayList<BubbleGraphNode> bubble_graph, int[] centroid_coord){
		int centroid_x = 0;
		int centroid_y = 0;
		for(BubbleGraphNode node : bubble_graph){
			if(node.GetNeighbors().size() == 0){
				//only the top one will have a connectivity of zero
				centroid_x = node.x;
				centroid_y = node.y;
			}
		}
		centroid_y += SHOOTER_Y_OFFSET;
		System.out.println("Shooter: (" + centroid_x + "," + centroid_y + ")");
		//image.setRGB(centroid_x, centroid_y, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y+1, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y+2, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y+3, 100000);
		centroid_coord[0] = centroid_x;
		centroid_coord[1] = centroid_y;
	}
	
	public static int RecursiveSearchNumberConnected(BubbleGraphNode node){
		node.traversed = true;
		int my_color = node.color;//need to reset the traversed flag
		int connection_count = 1;
		for(BubbleGraphNode neighbor_node : node.GetNeighbors()){
			if(neighbor_node.color == my_color && neighbor_node.traversed == false){
				connection_count += RecursiveSearchNumberConnected(neighbor_node);
			}
		}
		return connection_count;
	}
	
	public static boolean HitSide(int hit_x, int hit_y, int left_side, int right_side, int bottom_side, int top_side){
		if(hit_x == left_side || hit_x == right_side){
			return true;
		}
		if(hit_y == bottom_side || hit_y == top_side){
			return true;
		}
		return false;
	}
	
	public static void TraceLine(int startx, int endx, int starty, int endy, BufferedImage image, int [] intersec_location, boolean DRAW_OVERRIDE){
		System.out.println("start: " + startx + " " + starty);
		System.out.println("end: " + endx + " " + endy);
		int delta_x = (endx - startx)*2;
		int delta_y = (endy - starty)*2;
		//lets take one start and end point and then walk to it
		int abs_delta_x = Math.abs(delta_x);
		int abs_delta_y = Math.abs(delta_y);
		intersec_location[0] = endx;
		intersec_location[1] = endy;
		int sign_x = delta_x / abs_delta_x;
		int sign_y = delta_y / abs_delta_y;
		if(abs_delta_x > abs_delta_y){
			//we will want to walk with x
			double slope = (double)(abs_delta_y)/((double)(abs_delta_x));
			//System.out.println("Slope: " + slope);
			for(int i = 0; i < abs_delta_x; i++){
				int stepping_x = startx + i*sign_x;
				int stepping_y = starty + (int)(slope*(double)i*sign_y);
				
				//System.out.println(stepping_x + " " + stepping_y + " " + image.getRGB(stepping_x, stepping_y) + " " + i);
				int distance_from_shooter = (Math.abs(stepping_x - SHOOTER_X) + Math.abs(stepping_y - SHOOTER_Y));
				if((image.getRGB(stepping_x, stepping_y) != -16777216 && (image.getRGB(stepping_x, stepping_y) != PATH_COLOR) ) && ( distance_from_shooter > MIN_DISTANCE_FROM_START) &&i > MIN_TRAVEL_TRACE){
					intersec_location[0] = stepping_x;
					intersec_location[1] = stepping_y;
					break;
				}
				if(DRAW_SHOOTER_PATH || DRAW_OVERRIDE && ( distance_from_shooter > MIN_DISTANCE_FROM_START)){
					image.setRGB(stepping_x, stepping_y, 20000);
				}
			}
		}
		else{
			//we will want to walk with y
			double slope = (double)(abs_delta_x)/((double)(abs_delta_y));
			for(int i = 0 ; i < abs_delta_y; i++){
				int stepping_x = startx + (int)(slope*(double)i*sign_x);
				int stepping_y = starty + i*sign_y;
				
					//System.out.println(stepping_x + " " + stepping_y + " " + image.getRGB(stepping_x, stepping_y) + " " + i);
				int distance_from_shooter = (Math.abs(stepping_x - SHOOTER_X) + Math.abs(stepping_y - SHOOTER_Y));
				if(image.getRGB(stepping_x, stepping_y) != -16777216 && (image.getRGB(stepping_x, stepping_y) != PATH_COLOR) && ( distance_from_shooter > MIN_DISTANCE_FROM_START) && i > MIN_TRAVEL_TRACE){
					intersec_location[0] = stepping_x;
					intersec_location[1] = stepping_y;
					break;
				}
				if(DRAW_SHOOTER_PATH || DRAW_OVERRIDE && ( distance_from_shooter > MIN_DISTANCE_FROM_START))
					image.setRGB(stepping_x, stepping_y, 20000);
			}
		}
	}
	
	public static int RayTraceRecursive(BufferedImage image, int startx, int starty, int endx, int endy, ArrayList<BubbleGraphNode> bubble_graph, BufferedImage orig_image, int shooter_color, int recursion_level, boolean DRAW_OVERRIDE){
		if(recursion_level == 0)
			return 0;
		System.out.println("Cleared");
		int left_side = FindLeftBound(image);
		int right_side = FindRightBound(image);
		int bottom_side = FindBottomBound(image);
		int top_side = FindTopBound(image);
		
		int delta_x = (endx - startx);
		int delta_y = (endy - starty);
		
		double angle = Math.atan2((double)(delta_y), (double)delta_x);
		
		
		double up = (Math.cos(angle)*BALL_RADIUS);
		double right = (Math.sin(angle)*BALL_RADIUS);
		
		int[] intersec_location_mid = new int[2];
		int[] intersec_location_top = new int[2];
		int[] intersec_location_bot = new int[2];
		
		System.out.println(startx + " " + starty + " " + endx + " " + endy + " " + delta_x + " " + delta_y);
		
		TraceLine(startx, endx, starty, endy, image, intersec_location_mid, DRAW_OVERRIDE);
		//System.out.println(intersec_location_mid[0] + " " + intersec_location_mid[1]);
		TraceLine((int)(startx-right), (int)(endx-right), (int)(starty+up), (int)(endy+up), image, intersec_location_top, DRAW_OVERRIDE);
		//System.out.println(intersec_location_top[0] + " " + intersec_location_top[1]);
		TraceLine((int)(startx+right), (int)(endx+right), (int)(starty-up), (int)(endy-up), image, intersec_location_bot, DRAW_OVERRIDE);
		//System.out.println(intersec_location_bot[0] + " " + intersec_location_bot[1]);
		int clear_number = 0;
		
		if(HitSide(intersec_location_mid[0], intersec_location_mid[1], left_side, right_side, bottom_side, top_side)){
			System.out.println("Hit Side");
			//so now we want to bounce, but we have to calculate where it will hit, since the size is not zero
			//so we can take where the center hits, and assume that is where it will actually bounce even though this breaks down in the corners
			if(intersec_location_mid[0] == left_side){
				System.out.println("Hit Left Side");
				delta_x = - delta_x;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				//System.out.println(startx + " " + starty + " " + endx + " " + endy + " " + delta_x + " " + delta_y);
				if(!AllHitSameSide(intersec_location_mid, intersec_location_top, intersec_location_bot, Side.LEFT)){
					return 1;
				}
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);
			}
			else if(intersec_location_mid[0] == right_side){
				System.out.println("Hit Right Side");
				delta_x = - delta_x;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				if(!AllHitSameSide(intersec_location_mid, intersec_location_top, intersec_location_bot, Side.RIGHT)){
					return 1;
				}
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);
			}
			else if(intersec_location_mid[1] == bottom_side){
				System.out.println("Hit Bottom Side");
				delta_y = - delta_y;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				if(!AllHitSameSide(intersec_location_mid, intersec_location_top, intersec_location_bot, Side.BUTTOM)){
					return 1;
				}
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);
			}
			else if(intersec_location_mid[1] == top_side){
				System.out.println("Hit Top Side");
				delta_y = - delta_y;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				if(!AllHitSameSide(intersec_location_mid, intersec_location_top, intersec_location_bot, Side.TOP)){
					return 1;
				}
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);

			}
		}
		else{
			clear_number = GetNumberCleared(image, intersec_location_mid[0], intersec_location_mid[1], bubble_graph, shooter_color, intersec_location_top[0], intersec_location_top[1], intersec_location_bot[0], intersec_location_bot[1]);
		}
		
		return clear_number;
	}
	
	
	public static boolean AllHitSameSide(int[] intersec_location_mid, int[] intersec_location_top, int[] intersec_location_bot, Side side_enum){
		if(side_enum == Side.BUTTOM){
			if(intersec_location_mid[1] == intersec_location_top[1]&& intersec_location_top[1] == intersec_location_bot[1]){
				return true;
			}
			else{
				return false;
			}
		}
		if(side_enum == Side.TOP){
			if(intersec_location_mid[1] == intersec_location_top[1]&& intersec_location_top[1] == intersec_location_bot[1]){
				return true;
			}
			else{
				return false;
			}
		}
		if(side_enum == Side.LEFT){
			if(intersec_location_mid[0] == intersec_location_top[0]&& intersec_location_top[0] == intersec_location_bot[0]){
				return true;
			}
			else{
				return false;
			}
		}
		if(side_enum == Side.RIGHT){
			if(intersec_location_mid[0] == intersec_location_top[0]&& intersec_location_top[0] == intersec_location_bot[0]){
				return true;
			}
			else{
				return false;
			}
		}
		
		return false;
	}
	
	
	public static int GetNumberCleared(BufferedImage image, int hit_x, int hit_y, ArrayList<BubbleGraphNode> bubble_graph,
			int shooter_color, int hit_x_up, int hit_y_up, int hit_x_down, int hit_y_down){
		//finding the closest node
		int min_distance = 10000000;
		BubbleGraphNode min_node = null;
		for(BubbleGraphNode node : bubble_graph){
			int remote_x = node.x;
			int remote_y = node.y;
			int square_distance = (hit_x - remote_x)*(hit_x - remote_x) + (hit_y - remote_y)*(hit_y - remote_y);
			if(square_distance < min_distance){
				min_distance = square_distance;
				min_node = node;
			}
			node.traversed = false;
		}
		System.out.println("Hit: " + hit_x + " " + hit_y + " node centroid " +  min_node.x + " "  + min_node.y + "neighbot count: " + min_node.GetNeighbors().size());
		if(min_node.GetNeighbors().size() >= MAX_NEIGHBOR_HIT_LIMIT){
			return 1;
		}
		if((hit_x - hit_x_up)*(hit_x - hit_x_up) + (hit_y - hit_y_up)*(hit_y - hit_y_up) > MAX_GUIDE_LINE_HIT_DISTANCE){
			return 1;
		}
		if((hit_x - hit_x_down)*(hit_x - hit_x_down) + (hit_y - hit_y_down)*(hit_y - hit_y_down) > MAX_GUIDE_LINE_HIT_DISTANCE){
			return 1;
		}
			
		int number_connected = RecursiveSearchNumberConnected(min_node);
		System.out.println("Number Connected: " + number_connected);
		if(min_node.color != shooter_color){
			System.out.println("Node Centroid Colod: " + image.getRGB(min_node.x, min_node.y));
			System.out.println("Shooter Centroid Colod: " + image.getRGB(SHOOTER_X, SHOOTER_Y-3));
			System.out.println("Different Colored, Shooter Color: " + shooter_color + " Node Color: " + min_node.color);
			return 1;
		}
		else{
			return number_connected+1;
		}
		
	} 
	
	
}
