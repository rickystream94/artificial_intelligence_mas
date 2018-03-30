package environment;

import java.util.HashMap; 
import java.util.HashSet; 
import java.util.Set; 
import java.util.List; 
import java.util.Arrays; 
import java.util.ArrayList; 

public class SokobanMap {
	private int width;
	private int heigth;

	private List<Goal> goals;
	private Set<Point> walls;

	public SokobanMap() {
		this.goals = new ArrayList<Goal>();
		this.walls = new HashSet<Point>();
	}
	
	public List<Goal> getGoals() {
		return this.goals;
	}

	public Set<Point> getWalls() {
		return this.walls;
	}

	public int getHeigth() {
		return this.heigth;
	}

	public int getWidth() {
		return this.width;
	}

	public void setHeigth(int heigth) {
		this.heigth = heigth;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
