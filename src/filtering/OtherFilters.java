package filtering;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class OtherFilters {

	public static void PaintCentroids(int [][] connected_components, BufferedImage image, int [] connected_component_count_array, ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y){
		for(int i = 0; i < Centroid_X.size(); i++){
			image.setRGB(Centroid_X.get(i), Centroid_Y.get(i), 10000);
		}
	}
	
	public static void FilterBottomLeftCentroids(ArrayList<Integer> Centroid_X, ArrayList<Integer> Centroid_Y){
		for(int i = 0; i < Centroid_X.size(); i++){
			if(Centroid_X.get(i) < ImageConstants.BOTTOM_LEFT_CENTROID_FILTER_VERT && Centroid_Y.get(i) > ImageConstants.BOTTOM_LEFT_CENTROID_FILTER_HORIZ){
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
				if(component_size_count[i] < ImageConstants.MAX_CONNECTED_SIZE){
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
				if(component_moments[connected_components[i][j]] > ImageConstants.MAX_MOMENT){
					//connected_components[i][j] = 0;
				}
			}
		}
		
		
		//filter based upon moment/area
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				if(component_moments[connected_components[i][j]]/component_size_count[connected_components[i][j]] > ImageConstants.MAX_MOMENT_OVER_SIZE){
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
	
	
	public static void FilterBottomLeftComponents(BufferedImage image, int [][] connected_components){
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				if(i < ImageConstants.BOTTOM_LEFT_CENTROID_FILTER_VERT && j > ImageConstants.BOTTOM_LEFT_CENTROID_FILTER_HORIZ){
					connected_components[i][j] = 0;
				}
			}
		}
	}
}
