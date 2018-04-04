package board;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SokobanMap {
	private int width;
	private int heigth;

	private List<Goal> goals;
	private HashMap<Coordinate, Wall> walls;

	public SokobanMap() {
		this.goals = new ArrayList<Goal>();
		this.walls = new HashMap<Coordinate, Wall>();
	}
	
	public List<Goal> getGoals() {
		return this.goals;
	}

	public HashMap<Coordinate, Wall> getWalls() {
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
