package winner;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;



import javax.imageio.ImageIO;

public class GameWindowFinder {
	
	public static final int CALIBRATION_SEARCH_RADIUS = 10;
	
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
					ImageUtilities.GetScreenCapCrop(screen_cap, croppedImage, horizontal_pos + i, vertical_pos + j);
					int difference = ImageUtilities.GetImageDifference(croppedImage, referenceImage);
					if(difference < min_dif){
						min_coord_x = horizontal_pos + i;
						min_coord_y = vertical_pos + j;
						min_dif = difference;
					}
					System.out.println((horizontal_pos+i) + " " + (vertical_pos+j) + " " + difference);
				}
			}
			System.out.println("Min: " + min_coord_x + " " + min_coord_y + "Min Dif: " + min_dif );
			ImageUtilities.GetScreenCapCrop(screen_cap, croppedImage, min_coord_x, min_coord_y);
			
			/*
			for(int i = 0 ; i < croppedImage.getWidth(); i++){
				for(int j = 0; j < 50; j++){
					if(Math.abs(croppedImage.getRGB(i, j) - referenceImage.getRGB(i, j)) != 0){
						croppedImage.setRGB(i, j, 0);
					}
				}
			}*/
			
			ImageIO.write(croppedImage, "bmp" , new File ("croppedScreen.bmp"));
			BubbleSpinnerWinner.bench_x = min_coord_x;
			BubbleSpinnerWinner.bench_y = min_coord_y;
			
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
}
