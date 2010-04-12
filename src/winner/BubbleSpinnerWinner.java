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
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class BubbleSpinnerWinner {
	
	public static final int MAX_CONNECTED_SIZE = 600;
	public static final int MIN_CONNECTED_SIZE = 150;
	public static final int MAX_MOMENT = 40000;
	public static final int MAX_MOMENT_OVER_SIZE = 100;
	public static final int MIN_MOMENT = 150;
	
	public static final int BOTTOM_LEFT_CENTROID_FILTER_HORIZ = 490;
	public static final int BOTTOM_LEFT_CENTROID_FILTER_VERT = 170;
	
	public static final int NODE_MAX_DISTANCE_CUTOFF = 800;
	
	public static final int SHOOTER_Y_OFFSET = 4;
	
	public static final int LEFT_SIDE_OFFSET = 10;
	public static final int RIGHT_SIDE_OFFSET = 491;
	public static final int BOTTOM_SIDE_OFFSET = 531;
	
	public static final int BALL_RADIUS_INT = 10;
	public static final double BALL_RADIUS = (double)BALL_RADIUS_INT;
	
	
	public static final int CALIBRATION_SEARCH_RADIUS = 4;
	
	public static final int SHOOTER_X = 250;
	public static final int SHOOTER_Y = 43;
	
	public static int bench_x = 0;
	public static int bench_y = 0;
	
	public static boolean DEBUG = false;
	
	public static boolean DRAW_CENTROID_COLOR = false;
	public static boolean DRAW_CONNECTED_COLOR = true;
	public static boolean DRAW_SHOOTER_PATH = false;
	
	
	public static final int BLUE = 0;
	public static final int PINK = 0;
	public static final int RED = 0;
	public static final int GREEN = 0;
	public static final int YELLOW = 0;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		
		DrawDisplay();
		
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
	
	public static void PlayRound(){
		BufferedImage image = null;
		BufferedImage orig_image = null;
		BufferedImage search_image = null;
		File file = new File("spinner2.bmp");
		try {
			image = ImageIO.read(file);
			orig_image = ImageIO.read(file);
			search_image = ImageIO.read(file);
			
			GetPlayScreen(search_image, bench_x, bench_y);
			
			if(!DEBUG){
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
		
		//filter out bottom left components, and dump it to zero
		FilterBottomLeftComponents(image, connected_components);
		
		//color stuff black according to component number
		ColorComponentsFilter(connected_components, image);
		
		//now we can create a graph from the centroids of the remaining connected components
		connected_components = GetConnectedComponents(image, connected_component_count_array);
		
		ArrayList<Integer> centroid_X = new ArrayList<Integer>();
		ArrayList<Integer> centroid_Y = new ArrayList<Integer>();
		
		GetCentroid(connected_components, image, connected_component_count_array, centroid_X, centroid_Y);
		
		//filter out the centroids that are not in the center
		FilterBottomLeftCentroids(centroid_X, centroid_Y);
		
		//PaintCentroids(connected_components, image, connected_component_count_array, centroid_X, centroid_Y);
		
		ArrayList<BubbleGraphNode> bubble_graph = ConstructElementGraph(image, orig_image, centroid_X, centroid_Y, connected_components);
		
		//now we will rank in order which nodes have the least connectivity
		ArrayList<BubbleGraphNode> bubble_graph_sorted = (ArrayList<BubbleGraphNode>) bubble_graph.clone();
		
		SortBubbleGraph(bubble_graph_sorted);
		
		
		
		FindFireLocation(image, bubble_graph_sorted, shooter_coord);
		int shooter_x = SHOOTER_X;
		int shooter_y = SHOOTER_Y;
		int shooter_color = orig_image.getRGB(shooter_x+2, shooter_y-1);
		int shooter_color2 = GetUnconnectedNodeColor(bubble_graph);
		
		System.out.println("Shooter Color:" + shooter_color );
		System.out.println("Shooter Color2:" + shooter_color2 );
		System.out.println("Shooter Color:" + orig_image.getRGB(shooter_x+2, shooter_y));
		System.out.println("Shooter Color:" + orig_image.getRGB(shooter_x+1, shooter_y));
		System.out.println("Shooter Color:" + orig_image.getRGB(shooter_x, shooter_y-1));
		
		
		//RayTrace(image, shooter_x, shooter_y, 10, 300, bubble_graph_sorted);
		//RayTrace(image, shooter_x, shooter_y, 500, 300, bubble_graph_sorted);
		//RayTrace(image, shooter_x, shooter_y, 220, 300, bubble_graph_sorted, orig_image);
		//int clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 220, 300, bubble_graph_sorted, orig_image, shooter_color2, 2);
		//int clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 10, 220, bubble_graph_sorted, orig_image, shooter_color2, 3);
		//int clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 10, 300, bubble_graph_sorted, orig_image, shooter_color2, 3);
		int clear_number_max = 0;
		int clear_max_height = 0;
		int side = 0;
		for(int i = 100; i < 400; i++){
			System.out.println("i = " + i);
			int temp_clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 10, i, bubble_graph_sorted, orig_image, shooter_color2, 3, false);
			if(temp_clear_number > clear_number_max){
				clear_number_max = temp_clear_number;
				clear_max_height = i;
				side = 1;
			}
		}
		for(int i = 100; i < 400; i++){
			System.out.println("i = " + i);
			int temp_clear_number = RayTraceRecursive(image, shooter_x, shooter_y, 490, i, bubble_graph_sorted, orig_image, shooter_color2, 3, false);
			if(temp_clear_number > clear_number_max){
				clear_number_max = temp_clear_number;
				clear_max_height = i;
				side = 2;
			}
		}
		
		for(int i = 100; i < 300; i++){
			System.out.println("i = " + i);
			if(i == shooter_x)
				continue;
			int temp_clear_number = RayTraceRecursive(image, shooter_x, shooter_y, i, 490 , bubble_graph_sorted, orig_image, shooter_color2, 1, false);
			if(temp_clear_number > clear_number_max){
				clear_number_max = temp_clear_number;
				clear_max_height = i;
				side = 3;
			}
		}
		
		if(side == 1){
			RayTraceRecursive(image, shooter_x, shooter_y, 10, clear_max_height, bubble_graph_sorted, orig_image, shooter_color2, 3, true);
		}
		if(side == 2){
			RayTraceRecursive(image, shooter_x, shooter_y, 490, clear_max_height, bubble_graph_sorted, orig_image, shooter_color2, 3, true);
		}
		if(side == 3){
			RayTraceRecursive(image, shooter_x, shooter_y, clear_max_height, 490, bubble_graph_sorted, orig_image, shooter_color2, 2, true);
		}
		
		
		System.out.println("Total Clear Number Max Left: " + clear_number_max + " Max Height: " + clear_max_height);
		//Make actual Movement
		Robot robot;
		try {
			PointerInfo a = MouseInfo.getPointerInfo();
			java.awt.Point b  = a.getLocation();
			int orig_mouse_x = (int)b.getX();
			int orig_mouse_y = (int)b.getY();
			
			robot = new Robot();
			
			if(side == 1){
				robot.mouseMove(10+bench_x, clear_max_height+bench_y);
			}
			if(side == 2){
				robot.mouseMove(490+bench_x, clear_max_height+bench_y);
			}
			if(side == 3){
				robot.mouseMove(clear_max_height+bench_x, 490+bench_y);
			}
			
			
			robot.mousePress(InputEvent.BUTTON1_MASK);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			robot.mouseMove(orig_mouse_x, orig_mouse_y);
			
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
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
	
	public static void DrawDisplay(){
		Display display = new Display();
		//org.eclipse.swt.graphics.Image image = display.getSystemImage(SWT.ICON_QUESTION);
		Shell shell = new Shell(display);
		shell.setLayout (new GridLayout());
		
		final Label output_label = new Label (shell, SWT.CHECK);
		
		final Text horizontal_enter = new Text (shell, SWT.BORDER);
		horizontal_enter.setBounds (10, 10, 200, 200);
		
		final Text vertical_enter = new Text (shell, SWT.BORDER);
		vertical_enter.setBounds (10, 10, 200, 200);
		final Button enter_button = new Button(shell, SWT.PUSH);
		enter_button.setText("Enter Top Left Spinner");
		final Button play_button  = new Button(shell, SWT.PUSH);
		
		play_button.setText("Play Round");
		play_button.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event e) {
				if(bench_x == 0 && bench_y ==0){
					output_label.setSize(300, 12);
					output_label.setText("Benchmark Location First");
				}
				else{
					(new Thread(new PlayRoundThread())).start();
				}
			}
		});
		
		enter_button.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event e) {
				String horizontal_pos_string = horizontal_enter.getText();
				String vertical_pos_string = vertical_enter.getText();
				output_label.setSize(300, 12);
				output_label.setText(horizontal_pos_string+ " " + vertical_pos_string);
				
				int horizontal_pos = Integer.parseInt(horizontal_pos_string);
				int vertical_pos = Integer.parseInt(vertical_pos_string);
				
				GetBenchmarkLoc(horizontal_pos, vertical_pos);
			}
			
		});
		
		
		
		shell.setSize(300, 300);
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
	
	public static void GetBenchmarkLoc(int horizontal_pos, int vertical_pos){
		try {
			Robot robot = new Robot();
			BufferedImage screen_cap = new Robot().createScreenCapture(
			           new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );
			
			File file = new File("spinner.bmp");
			BufferedImage croppedImage = ImageIO.read(file);
			BufferedImage referenceImage = ImageIO.read(file);
			int min_coord_x = 0;
			int min_coord_y = 0;
			int min_dif = 100000000;
			System.out.println(-CALIBRATION_SEARCH_RADIUS);
			for(int i = -CALIBRATION_SEARCH_RADIUS; i <= CALIBRATION_SEARCH_RADIUS; i++){
				for(int j = -CALIBRATION_SEARCH_RADIUS; j <= CALIBRATION_SEARCH_RADIUS; j++){
					GetScreenCapCrop(screen_cap, croppedImage, horizontal_pos + i, vertical_pos + j);
					int difference = GetImageDifference(croppedImage, referenceImage);
					if(difference < min_dif){
						min_coord_x = horizontal_pos + i;
						min_coord_y = vertical_pos + j;
						min_dif = difference;
					}
					System.out.println((horizontal_pos+i) + " " + (vertical_pos+j) + " " + difference);
				}
			}
			System.out.println("Min: " + min_coord_x + " " + min_coord_y );
			GetScreenCapCrop(screen_cap, croppedImage, min_coord_x, min_coord_y);
			
			/*
			for(int i = 0 ; i < croppedImage.getWidth(); i++){
				for(int j = 0; j < 50; j++){
					if(Math.abs(croppedImage.getRGB(i, j) - referenceImage.getRGB(i, j)) != 0){
						croppedImage.setRGB(i, j, 0);
					}
				}
			}*/
			
			ImageIO.write(croppedImage, "bmp" , new File ("croppedScreen.bmp"));
			bench_x = min_coord_x;
			bench_y = min_coord_y;
			
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	public static void GetScreenCapCrop(BufferedImage input, BufferedImage output, int horizontal_pos, int vertical_pos){
		for(int i = 0; i < output.getWidth(); i++){
			for(int j = 0; j < output.getHeight(); j++){
				output.setRGB(i, j, input.getRGB(i+horizontal_pos, j+vertical_pos));
			}
		}
	}
	
	public static void GetPlayScreen(BufferedImage output, int horizontal_pos, int vertical_pos){
		try {
			BufferedImage screen_cap = new Robot().createScreenCapture(
			           new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );
			
			for(int i = 0; i < output.getWidth(); i++){
				for(int j = 0; j < output.getHeight(); j++){
					output.setRGB(i, j, screen_cap.getRGB(i+horizontal_pos, j+vertical_pos));
				}
			}
			
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static int GetImageDifference(BufferedImage input1, BufferedImage input2){
		int total_dif = 0;
		for(int i = 0 ; i < input1.getWidth(); i++){
			for(int j = 0; j < 50; j++){
				if(Math.abs(input1.getRGB(i, j) - input2.getRGB(i, j)) != 0)
					total_dif ++; 
			}
		}
		return total_dif;
	}
	
	public static void FireBubbleHit(BufferedImage image, int startx, int starty, int targetx, int targety, int left_bound, int right_bound, int bottom_bound){
		
	}
	
	public static void RayTrace(BufferedImage image, int startx, int starty, int endx, int endy, ArrayList<BubbleGraphNode> bubble_graph, BufferedImage orig_image){
		int shooter_color = orig_image.getRGB(startx+2, starty-1);
		int left_side = FindLeftBound(image);
		int right_side = FindRightBound(image);
		int bottom_side = FindBottomBound(image);
		
		int delta_x = (endx - startx);
		int delta_y = (endy - starty);
		
		double angle = Math.atan2((double)(delta_y), (double)delta_x);
		
		
		double up = (Math.cos(angle)*BALL_RADIUS);
		double right = (Math.sin(angle)*BALL_RADIUS);
		
		//double complementary_angle = Math.PI/2 - angle;
		
		//double hypoteneus = BALL_RADIUS/Math.sin(complementary_angle);
		
		
		System.out.println("Angle: " + angle);
		System.out.println("Up: "+ up);
		System.out.println("Right: " + right);
		//System.out.println("Angle: " + complementary_angle);
		//System.out.println("Hypo: "+ hypoteneus);
		//DebugPaintLine(startx, endx, starty, endy, image);
		//DebugPaintLine((int)(startx+right), (int)(endx+right), (int)(starty-up), (int)(endy-up), image);
		//DebugPaintLine((int)(startx-right), (int)(endx-right), (int)(starty+up), (int)(endy+up), image);
		
		int[] intersec_location_mid = new int[2];
		int[] intersec_location_top = new int[2];
		int[] intersec_location_bot = new int[2];
		
		TraceLine(startx, endx, starty, endy, image, intersec_location_mid, false);
		//System.out.println(intersec_location_mid[0] + " " + intersec_location_mid[1]);
		TraceLine((int)(startx-right), (int)(endx-right), (int)(starty+up), (int)(endy+up), image, intersec_location_top, false);
		//System.out.println(intersec_location_top[0] + " " + intersec_location_top[1]);
		TraceLine((int)(startx+right), (int)(endx+right), (int)(starty-up), (int)(endy-up), image, intersec_location_bot, false);
		//System.out.println(intersec_location_bot[0] + " " + intersec_location_bot[1]);
		int clear_number = 0;
		
		if(HitSide(intersec_location_mid[0], intersec_location_mid[1], left_side, right_side, bottom_side)){
			System.out.println("Hit Side");
			//so now we want to bounce, but we have to calculate where it will hit, since the size is not zero
			//so we can take where the center hits, and assume that is where it will actually bounce even though this breaks down in the corners
			if(intersec_location_mid[0] == left_side){
				delta_x = - delta_x;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				angle = Math.atan2((double)(delta_y), (double)delta_x);
				up = (Math.cos(angle)*BALL_RADIUS);
				right = (Math.sin(angle)*BALL_RADIUS);
				
				TraceLine(startx, endx, starty, endy, image, intersec_location_mid, false);
				TraceLine((int)(startx-right), (int)(endx-right), (int)(starty+up), (int)(endy+up), image, intersec_location_top, false);
				TraceLine((int)(startx+right), (int)(endx+right), (int)(starty-up), (int)(endy-up), image, intersec_location_bot, false);
				
				System.out.println("Hit Left Side");
				
				if(!HitSide(intersec_location_mid[0], intersec_location_mid[1], left_side, right_side, bottom_side)){
					clear_number = GetNumberCleared(image, intersec_location_mid[0], intersec_location_mid[1], bubble_graph, shooter_color);
				}
			}
			else if(intersec_location_mid[0] == right_side){
				delta_x = - delta_x;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				angle = Math.atan2((double)(delta_y), (double)delta_x);
				up = (Math.cos(angle)*BALL_RADIUS);
				right = (Math.sin(angle)*BALL_RADIUS);
				
				TraceLine(startx, endx, starty, endy, image, intersec_location_mid, false);
				TraceLine((int)(startx-right), (int)(endx-right), (int)(starty+up), (int)(endy+up), image, intersec_location_top, false);
				TraceLine((int)(startx+right), (int)(endx+right), (int)(starty-up), (int)(endy-up), image, intersec_location_bot, false);
				
				System.out.println("Hit Right Side");
				
				if(!HitSide(intersec_location_mid[0], intersec_location_mid[1], left_side, right_side, bottom_side)){
					clear_number = GetNumberCleared(image, intersec_location_mid[0], intersec_location_mid[1], bubble_graph, shooter_color);
				}
			}
			else if(intersec_location_mid[1] == bottom_side){
				delta_y = - delta_y;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				angle = Math.atan2((double)(delta_y), (double)delta_x);
				up = (Math.cos(angle)*BALL_RADIUS);
				right = (Math.sin(angle)*BALL_RADIUS);
				
				TraceLine(startx, endx, starty, endy, image, intersec_location_mid, false);
				TraceLine((int)(startx-right), (int)(endx-right), (int)(starty+up), (int)(endy+up), image, intersec_location_top, false);
				TraceLine((int)(startx+right), (int)(endx+right), (int)(starty-up), (int)(endy-up), image, intersec_location_bot, false);
				
				System.out.println("Hit Bottom Side");
				
				if(!HitSide(intersec_location_mid[0], intersec_location_mid[1], left_side, right_side, bottom_side)){
					clear_number = GetNumberCleared(image, intersec_location_mid[0], intersec_location_mid[1], bubble_graph, shooter_color);
				}
			}
		}
		else{
			clear_number = GetNumberCleared(image, intersec_location_mid[0], intersec_location_mid[1], bubble_graph, shooter_color);
		}
		System.out.println("Actually cleared: " + clear_number);
	}
	
	
	public static int RayTraceRecursive(BufferedImage image, int startx, int starty, int endx, int endy, ArrayList<BubbleGraphNode> bubble_graph, BufferedImage orig_image, int shooter_color, int recursion_level, boolean DRAW_OVERRIDE){
		if(recursion_level == 0)
			return 0;
		
		int left_side = FindLeftBound(image);
		int right_side = FindRightBound(image);
		int bottom_side = FindBottomBound(image);
		
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
		
		if(HitSide(intersec_location_mid[0], intersec_location_mid[1], left_side, right_side, bottom_side)){
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
				System.out.println(startx + " " + starty + " " + endx + " " + endy + " " + delta_x + " " + delta_y);
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);
			}
			else if(intersec_location_mid[0] == right_side){
				System.out.println("Hit Right Side");
				delta_x = - delta_x;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);
			}
			else if(intersec_location_mid[1] == bottom_side){
				System.out.println("Hit Bottom Side");
				delta_y = - delta_y;
				startx = intersec_location_mid[0];
				starty = intersec_location_mid[1];
				endx = delta_x + startx;
				endy = delta_y + starty;
				clear_number = RayTraceRecursive(image, startx, starty, endx, endy, bubble_graph, orig_image, shooter_color, recursion_level-1, DRAW_OVERRIDE);
			}
		}
		else{
			clear_number = GetNumberCleared(image, intersec_location_mid[0], intersec_location_mid[1], bubble_graph, shooter_color);
		}
		
		return clear_number;
	}
	
	public static int GetNumberCleared(BufferedImage image, int hit_x, int hit_y, ArrayList<BubbleGraphNode> bubble_graph, int shooter_color){
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
		}
		System.out.println("Hit: " + hit_x + " " + hit_y + " node centroid " +  min_node.x + " "  + min_node.y);
		if(min_node.GetNeighbors().size() >= 6){
			return 1;
		}
		int number_connected = RecursiveSearchNumberConnected(min_node);
		System.out.println("Number Connected: " + number_connected);
		if(min_node.color != shooter_color){
			System.out.println("Different Colored, Shooter Color: " + shooter_color + " Node Color: " + min_node.color);
			return 1;
		}
		else{
			return number_connected+1;
		}
		
	} 
	
	public static int RecursiveSearchNumberConnected(BubbleGraphNode node){
		node.traversed = true;
		int my_color = node.color;
		int connection_count = 1;
		for(BubbleGraphNode neighbor_node : node.GetNeighbors()){
			if(neighbor_node.color == my_color && neighbor_node.traversed == false){
				connection_count += RecursiveSearchNumberConnected(neighbor_node);
			}
		}
		return connection_count;
	}
	
	public static boolean HitSide(int hit_x, int hit_y, int left_side, int right_side, int bottom_side){
		if(hit_x == left_side || hit_x == right_side){
			return true;
		}
		if(hit_y == bottom_side){
			return true;
		}
		return false;
	}
	
	public static void TraceLine(int startx, int endx, int starty, int endy, BufferedImage image, int [] intersec_location, boolean DRAW_OVERRIDE){
		System.out.println(startx + " " + starty);
		System.out.println(endx + " " + endy);
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
			System.out.println("Slope: " + slope);
			for(int i = 0; i < abs_delta_x; i++){
				int stepping_x = startx + i*sign_x;
				int stepping_y = starty + (int)(slope*(double)i*sign_y);
				
				//System.out.println(stepping_x + " " + stepping_y + " " + image.getRGB(stepping_x, stepping_y) + " " + i);
				
				if((image.getRGB(stepping_x, stepping_y) != -16777216 && (image.getRGB(stepping_x, stepping_y) != 20000) ) && i > 50){
					intersec_location[0] = stepping_x;
					intersec_location[1] = stepping_y;
					break;
				}
				if(DRAW_SHOOTER_PATH || DRAW_OVERRIDE)
					image.setRGB(stepping_x, stepping_y, 20000);
			}
		}
		else{
			//we will want to walk with y
			double slope = (double)(abs_delta_x)/((double)(abs_delta_y));
			for(int i = 0 ; i < abs_delta_y; i++){
				int stepping_x = startx + (int)(slope*(double)i*sign_x);
				int stepping_y = starty + i*sign_y;
				
				//System.out.println(stepping_x + " " + stepping_y + " " + image.getRGB(stepping_x, stepping_y) + " " + i);
				
				if(image.getRGB(stepping_x, stepping_y) != -16777216 && (image.getRGB(stepping_x, stepping_y) != 20000) && i > 50){
					intersec_location[0] = stepping_x;
					intersec_location[1] = stepping_y;
					break;
				}
				if(DRAW_SHOOTER_PATH || DRAW_OVERRIDE)
					image.setRGB(stepping_x, stepping_y, 20000);
			}
		}
	}
	
	public static void FilterBottomLeftComponents(BufferedImage image, int [][] connected_components){
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				if(i < BOTTOM_LEFT_CENTROID_FILTER_VERT && j > BOTTOM_LEFT_CENTROID_FILTER_HORIZ){
					connected_components[i][j] = 0;
				}
			}
		}
	}
	
	public static int FindBottomBound(BufferedImage image){
		int bottom_side = BOTTOM_SIDE_OFFSET - BALL_RADIUS_INT;
		for(int i = 0; i < image.getWidth(); i++){
			image.setRGB(i, bottom_side, 100000);
		}
		return bottom_side;
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
		image.setRGB(centroid_x, centroid_y, 100000);
		image.setRGB(SHOOTER_X, SHOOTER_Y, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y+1, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y+2, 100000);
		//image.setRGB(SHOOTER_X, SHOOTER_Y+3, 100000);
		centroid_coord[0] = centroid_x;
		centroid_coord[1] = centroid_y;
	}
	
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
