package com.skywatcher.extend;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Panorama {
	private final String TAG = "Panorama";
	private static Angle FovX, FovY; // between 10 ~ 100

	public static Angle getFovX() {
		return FovX;
	}

	public static void setFovX(Angle fovX) {
		FovX = fovX;
	}

	public static Angle getFovY() {
		return FovY;
	}

	public static void setFovY(Angle fovY) {
		FovY = fovY;
	}

	public static double getOverlap() {
		return Overlap;
	}

	public static void setOverlap(double overlap) {
		Overlap = overlap;
	}

	public static double getStepDelay() {
		return StepDelay;
	}

	public static void setStepDelay(double stepDelay) {
		StepDelay = stepDelay;
	}

	public static double getTriggerDelay() {
		return TriggerDelay;
	}

	public static void setTriggerDelay(double triggerDelay) {
		TriggerDelay = triggerDelay;
	}

	public static boolean isPortrait() {
		return Portrait;
	}

	public static void setPortrait(boolean portrait) {
		Portrait = portrait;
	}

	private static double Overlap; // should between 0.2 ~ 0.5
	private static double StepDelay;	// should between 1 ~ 20 sec
	private static double TriggerDelay; // should between 1 ~ 5 sec
	private static boolean Portrait; // FovX always bigger than FovY

	// MinAlt must smaller than MaxAlt
	// MinAlt -90 ~ 90
	// MaxAlt -90 ~ 90
	// MinAz -180 ~ 180
	// MaxAz -180 ~ 180
	public static List<CroodAxis1Axis2> GenerateShotPoint(Angle MinAz, Angle MaxAz, Angle MinAlt, Angle MaxAlt) {					
		// Need to check the books
		

		double delta_y = 0, delta_x = 0;
		if (Portrait == false) {
			delta_x = FovX.getRad();
			delta_y = FovY.getRad();
		} else {
			delta_x = FovY.getRad();
			delta_y = FovX.getRad();
		}

		double max_alt_rad = MaxAlt.getRad();
		double min_alt_rad = MinAlt.getRad();
		
		double max_az_rad = MaxAz.getRad();
		double min_az_rad = MinAz.getRad();
		
		double ratio = 1 - Overlap;
		
		if(max_az_rad < min_az_rad) max_az_rad = max_az_rad + Angle.RAD360;
		
		List<CroodAxis1Axis2> list = new LinkedList<CroodAxis1Axis2>();
		List<CroodAxis1Axis2> list2 = new LinkedList<CroodAxis1Axis2>();
		int direction = 0;
		// az_position, alt_position is the center of camera
		for(double alt_position = min_alt_rad + delta_y * ratio / 2; ; alt_position += delta_y * ratio){
			double alt_position_min = alt_position - delta_y / 2;
			double alt_position_max = alt_position + delta_y / 2;			
			
			double delta_angle = 0;
			if(alt_position_min < 0 && alt_position_max < 0)
				delta_angle = - alt_position_max;
			if(alt_position_min < 0 && alt_position_max > 0)
				delta_angle = 0;
			if(alt_position_min > 0 && alt_position_max > 0)
				delta_angle = alt_position_min; 
			
			double delta_x_with_alt_fixed = delta_x / Math.cos(delta_angle);
			list2.clear();							
			for(double az_position = min_az_rad + delta_x_with_alt_fixed * ratio / 2; ; az_position += delta_x_with_alt_fixed * ratio) {											
				list2.add(new CroodAxis1Axis2(Angle.FromRad(az_position), Angle.FromRad(alt_position)));
								
				if(max_az_rad < az_position + delta_x_with_alt_fixed / 2)
					break;
			}
			
			if(direction % 2 == 0)
			{
				list.addAll(list2);
			}
			else {
				Collections.reverse(list2);
				list.addAll(list2);
			}
			
			direction ++;
			// if covered the max_alt_rad already
			if(max_alt_rad < alt_position_max)
				break;
			
		}
				
		return list;

	}

}
