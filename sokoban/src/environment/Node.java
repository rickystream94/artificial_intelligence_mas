package environment;

import java.util.List; 
import java.util.Arrays; 
import java.util.ArrayList; 

public class Node {
	private SokobanMap map;
	private List<Agent> agents;
	private List<Box> boxes;

	public Node(SokobanMap map) {
		this.agents = new ArrayList<Agent>();
		this.boxes = new ArrayList<Box>();
	}

	public List<Agent> getAgents() {
		return this.agents;
	}

	public List<Box> getBoxes() {
		return this.boxes;
	}
}
