package winner;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageConsumer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import Interface.WinnerDisplay;

import filtering.ConnectedComponent;
import filtering.ImageConstants;
import filtering.OtherFilters;


public class BubbleSpinnerWinner {
	
	public static int bench_x = 0;
	public static int bench_y = 0;
	
	public static boolean DEBUG = false;
	
	public static final int BLUE = 0;
	public static final int PINK = 0;
	public static final int RED = 0;
	public static final int GREEN = 0;
	public static final int YELLOW = 0;
	
	public enum Side{
		TOP, BUTTOM, LEFT, RIGHT
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		
		WinnerDisplay winner_display = new WinnerDisplay();
		
		winner_display.DrawDisplay();
		
		/*BufferedImage screen_cap = null;
		try {
			Robot robot = new Robot();
			
			screen_cap = new Robot().createScreenCapture(
			           new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );
			ImageIO.write( screen_cap, "bmp" , new File ( "screen.bmp" ));
			
		} catch (HeadlessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		
		
	}
	
	public static void PlayRound(WinnerDisplay winner_display){
		BufferedImage image = null;
		BufferedImage orig_image = null;
		BufferedImage search_image = null;
		File file = new File("last_screen.bmp");
		try {
			image = ImageIO.read(file);
			orig_image = ImageIO.read(file);
			search_image = ImageIO.read(file);
			
			
			
			if(!DEBUG){
				GameWindowFinder.GetPlayScreen(search_image, bench_x, bench_y);
				//saving for debug purposes later
				ImageIO.write( search_image, "bmp" , new File ( "last_screen.bmp" ));
				for(int i = 0; i < image.getWidth(); i++){
					for(int j = 0; j < image.getHeight(); j++){
						image.setRGB(i, j, search_image.getRGB(i,j));
						orig_image.setRGB(i, j, search_image.getRGB(i,j));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		//lets find connected components
		int [] connected_component_count_array = {0};
		int [] shooter_coord = {0,0};
		int [][] connected_components = ConnectedComponent.GetConnectedComponents(image, connected_component_count_array);
		
		System.out.println("We have :"+ connected_component_count_array[0] + " connected components");

		
		//filter based on size
		ConnectedComponent.ConnectedComponentSizeFilter(connected_components, image, connected_component_count_array);
		
		DebugSave(image, "output1.bmp");
		
		//color stuff black according to component number
		OtherFilters.ColorComponentsFilter(connected_components, image);
		
		DebugSave(image, "output2.bmp");
		
		//find connected components again
		connected_components = ConnectedComponent.GetConnectedComponents(image, connected_component_count_array);
		
		System.out.println("We have :"+ connected_component_count_array[0] + " connected components");
		
		//filter based on connected component that is entirely enclosed
		OtherFilters.EntirelyEnclosedFilter(connected_components, image, connected_component_count_array);

		//filter based on size
		ConnectedComponent.ConnectedComponentSizeFilter(connected_components, image, connected_component_count_array);

		//filter based on non circular objects by determining if component is a circle
		OtherFilters.CircleComponentFilter(connected_components, image, connected_component_count_array);
		
		//filter out bottom left components, and dump it to zero
		OtherFilters.FilterBottomLeftComponents(image, connected_components);
		
		//color stuff black according to component number
		OtherFilters.ColorComponentsFilter(connected_components, image);
		
		DebugSave(image, "output6.bmp");
		
		//now we can create a graph from the centroids of the remaining connected components
		connected_components = ConnectedComponent.GetConnectedComponents(image, connected_component_count_array);
		
		ArrayList<Integer> centroid_X = new ArrayList<Integer>();
		ArrayList<Integer> centroid_Y = new ArrayList<Integer>();
		
		OtherFilters.GetCentroid(connected_components, image, connected_component_count_array, centroid_X, centroid_Y);
		
		//filter out the centroids that are not in the center
		OtherFilters.FilterBottomLeftCentroids(centroid_X, centroid_Y);
		
		//PaintCentroids(connected_components, image, connected_component_count_array, centroid_X, centroid_Y);
		
		ArrayList<BubbleGraphNode> bubble_graph = BubbleGraph.ConstructElementGraph(image, orig_image, centroid_X, centroid_Y, connected_components);
		
		DebugSave(image, "output7.bmp");
		
		//now we will rank in order which nodes have the least connectivity
		ArrayList<BubbleGraphNode> bubble_graph_sorted = (ArrayList<BubbleGraphNode>) bubble_graph.clone();
		
		BubbleGraph.SortBubbleGraph(bubble_graph_sorted);
		
		
		
		ShotFinder.FindFireLocation(image, bubble_graph_sorted, shooter_coord);
		int shooter_x = ShotFinder.SHOOTER_X;
		int shooter_y = ShotFinder.SHOOTER_Y;
		int shooter_color = image.getRGB(shooter_x+2, shooter_y-1);
		int shooter_color2 = BubbleGraph.GetUnconnectedNodeColor(bubble_graph);
		
		System.out.println("Shooter Color:" + shooter_color );
		System.out.println("Shooter Color2:" + shooter_color2 );
		System.out.println("Shooter Color:" + image.getRGB(shooter_x+2, shooter_y));
		System.out.println("Shooter Color:" + image.getRGB(shooter_x+1, shooter_y));
		System.out.println("Shooter Color:" + image.getRGB(shooter_x, shooter_y-1));
		
		
		//RayTrace(image, shooter_x, shooter_y, 10, 300, bubble_graph_sorted);
		//RayTrace(image, shooter_x, shooter_y, 500, 300, bubble_graph_sorted);
		//RayTrace(image, shooter_x, shooter_y, 220, 300, bubble_graph_sorted, orig_image);
		//int clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 220, 300, bubble_graph_sorted, orig_image, shooter_color2, 2);
		//int clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 10, 220, bubble_graph_sorted, orig_image, shooter_color2, 3);
		//int clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 10, 300, bubble_graph_sorted, orig_image, shooter_color2, 3);
		int clear_number_max_right = 0;
		int clear_max_height_right = 0;
		int clear_number_max_left = 0;
		int clear_max_height_left = 0;
		int clear_number_max_bottom = 0;
		int clear_max_height_bottom = 0;
		Side side;
		
		/*
		image.setRGB(SHOOTER_X, SHOOTER_Y, image.getRGB(SHOOTER_X, SHOOTER_Y-1));
		System.out.println("Ming: " + RayTraceRecursive(image, shooter_x, shooter_y, 10, 298, bubble_graph_sorted, orig_image, shooter_color, 3, true));
		System.out.println(image.getRGB(SHOOTER_X, SHOOTER_Y));
		DebugSave(image, "output8.bmp");
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}*/
		
		for(int i = 100; i < 400; i++){
			System.out.println("left i = " + i);
			int temp_clear_number = 0;
			try{
				temp_clear_number = ShotFinder.RayTraceRecursive(image, shooter_x, shooter_y, 10, i, bubble_graph_sorted, orig_image, shooter_color2, 3, false);
			}
			catch(RuntimeException e){
				e.printStackTrace();
			}
			if(temp_clear_number > clear_number_max_left){
				clear_number_max_left = temp_clear_number;
				clear_max_height_left = i;
				side = Side.LEFT;
			}
		}
		for(int i = 100; i < 400; i++){
			System.out.println("right i = " + i);
			int temp_clear_number = 0;
			try{
				temp_clear_number = ShotFinder.RayTraceRecursive(image, shooter_x, shooter_y, 490, i, bubble_graph_sorted, orig_image, shooter_color2, 3, false);
			}
			catch(RuntimeException e){
				e.printStackTrace();
			}
			if(temp_clear_number > clear_number_max_right){
				clear_number_max_right = temp_clear_number;
				clear_max_height_right = i;
				side = Side.RIGHT;
			}
		}
		
		for(int i = -100; i < 600; i++){
			System.out.println("i = " + i);
			if(i == shooter_x)
				continue;
			int temp_clear_number = 0;
			try{
				temp_clear_number = ShotFinder.RayTraceRecursive(image, shooter_x, shooter_y, i, 490 , bubble_graph_sorted, orig_image, shooter_color2, 1, false);
			}
			catch(RuntimeException e){
				e.printStackTrace();
			}
			System.out.println("temp: " + temp_clear_number);
			if(temp_clear_number > clear_number_max_bottom){
				clear_number_max_bottom = temp_clear_number;
				clear_max_height_bottom = i;
				side = Side.BUTTOM;
			}
		}
		
		System.out.println("Total Clear Number Max Left: " + clear_number_max_left + " Max Height: " + clear_max_height_left);
		System.out.println("Total Clear Number Max Right: " + clear_number_max_right + " Max Height: " + clear_max_height_right);
		System.out.println("Total Clear Number Max Bottom: " + clear_number_max_bottom + " Max Height: " + clear_max_height_bottom);

		
		//deciding which side to take
		if(clear_number_max_bottom >= clear_number_max_right && clear_number_max_bottom >= clear_number_max_left){
			side = Side.BUTTOM;
		}
		else{
			if(clear_number_max_right > clear_number_max_left){
				side = Side.RIGHT;
			}
			else{
				side = Side.LEFT;
			}
		}
		
		if(side == Side.LEFT){
			ShotFinder.RayTraceRecursive(image, shooter_x, shooter_y, 10, clear_max_height_left, bubble_graph_sorted, orig_image, shooter_color2, 3, true);
			System.out.println("Total Clear Number Max Left: " + clear_number_max_left + " Max Height: " + clear_max_height_left);
		}
		if(side == Side.RIGHT){
			ShotFinder.RayTraceRecursive(image, shooter_x, shooter_y, 490, clear_max_height_right, bubble_graph_sorted, orig_image, shooter_color2, 3, true);
			System.out.println("Total Clear Number Max Right: " + clear_number_max_right + " Max Height: " + clear_max_height_right);
		}
		if(side == Side.BUTTOM){
			ShotFinder.RayTraceRecursive(image, shooter_x, shooter_y, clear_max_height_bottom, 490, bubble_graph_sorted, orig_image, shooter_color2, 2, true);
			System.out.println("Total Clear Number Max Bottom: " + clear_number_max_bottom + " Max Height: " + clear_max_height_bottom);
		}
		
		
		//Calculating Mouse Target Location
		int target_mouse_x = 0;
		int target_mouse_y = 0;
		
		if(side == Side.LEFT){
			target_mouse_x = 10 + bench_x;
			target_mouse_y = clear_max_height_left+bench_y;
			//robot.mouseMove(10+bench_x, clear_max_height_left+bench_y);
		}
		if(side == Side.RIGHT){
			target_mouse_x = 490 + bench_x;
			target_mouse_y = clear_max_height_right+bench_y;
			//robot.mouseMove(490+bench_x, clear_max_height_right+bench_y);
		}
		if(side == Side.BUTTOM){
			//in the event that it is off the screen, then we will walk back to the screen
			int delta_x = shooter_x - clear_max_height_bottom;
			int delta_y = shooter_y - 490;
			int new_target_x = clear_max_height_bottom;
			int new_target_y = 490;
			if(clear_max_height_bottom < 0){
				double slope = ((double)delta_y)/((double)delta_x);
				new_target_x = 5;
				new_target_y = (int)(slope*(-delta_x));
			}
			if(clear_max_height_bottom > 500){
				double slope = ((double)delta_y)/((double)delta_x);
				new_target_x = 400;
				new_target_y = (int)(slope*(-delta_x));
			}
			System.out.println("Corrected to be in screen: " + new_target_x + " " + new_target_y);
			
			target_mouse_x = new_target_x+bench_x;
			target_mouse_y = new_target_y+bench_y;
			//robot.mouseMove(new_target_x+bench_x, new_target_y+bench_y);
		}
		
		MouseMove mouse_move = new MouseMove(target_mouse_x, target_mouse_y);
		winner_display.SetMoveReady(mouse_move);
		
		
		
		//RayTrace(image, shooter_x, shooter_y, 400, 300);
		
				
		//So now we will filter out any unconnected nodes
		//RemoveUnconnectedNodes(bubble_graph_sorted);
		
		
		
		
		try {
			ImageIO.write( image, "bmp" , new File ( "output.bmp" ));
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}
	
	
	public static void MakeMouseMove(MouseMove mouse_move){
		int target_mouse_x = mouse_move.x_loc;
		int target_mouse_y = mouse_move.y_loc;
		
		//Make actual Movement
		Robot robot;
		try {
			if(!DEBUG){
				PointerInfo a = MouseInfo.getPointerInfo();
				java.awt.Point b  = a.getLocation();
				int orig_mouse_x = (int)b.getX();
				int orig_mouse_y = (int)b.getY();
				
				robot = new Robot();
				
				robot.mouseMove(target_mouse_x, target_mouse_y);
				robot.mousePress(InputEvent.BUTTON1_MASK);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				robot.mouseMove(orig_mouse_x, orig_mouse_y);
			}
			
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	//================================================================
	// Beyond this point is debugging code
	//================================================================
	public static void DebugSave(BufferedImage image, String filename){
		if(DEBUG){
			try {
				ImageIO.write( image, "bmp" , new File ( filename ));
			} catch (IOException e) {
				e.printStackTrace();
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
