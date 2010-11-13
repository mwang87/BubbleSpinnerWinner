package winner;

import java.awt.image.BufferedImage;

public class ImageUtilities {

	public static void GetScreenCapCrop(BufferedImage input, BufferedImage output, int horizontal_pos, int vertical_pos){
		for(int i = 0; i < output.getWidth(); i++){
			for(int j = 0; j < output.getHeight(); j++){
				output.setRGB(i, j, input.getRGB(i+horizontal_pos, j+vertical_pos));
			}
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
}
