package environment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Map; 
import java.util.HashMap; 
import java.util.HashSet; 
import java.util.Set; 
import java.util.List; 
import java.util.Arrays; 
import java.util.ArrayList; 
import java.util.LinkedList; 
import utils.FibonacciHeap;
import algorithm.Strategy;

public class World {
	private SokobanMap map;
	private Node node;

	private List<Agent> agents;
	private FibonacciHeap<Goal> subGoals;

	private BufferedReader in;

	public World(InputStream in) throws IOException {
		this.in = new BufferedReader( new InputStreamReader( in ) );
		this.map = new SokobanMap();
		this.node = new Node(this.map);
		this.subGoals = new FibonacciHeap<Goal>();
		this.agents = new ArrayList<Agent>();

		readMap();
	}

	public LinkedList<Node> search(Strategy strategy, Node initialState) {
		strategy.addToFrontier(initialState);
		while (true) {

			if (strategy.frontierIsEmpty()) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

			if ( leafNode.isGoalState() ) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) {
				if ( !strategy.isExplored(n) && !strategy.inFrontier(n) ) {
					strategy.addToFrontier(n);
				}
			}
		}
	} 

	private void readMap() throws IOException {
		Map< Character, String > colors;
		String line, color;
		List<Goal> goals;
		List<Agent> agents;
		HashMap<Coordinate,Box> boxes;
		HashMap<Coordinate,Wall> walls;
		int width, heigth;

		boxes = this.node.getBoxes();
		agents = this.node.getAgents();
		goals = this.map.getGoals();
		walls = this.map.getWalls();
		colors = new HashMap< Character, String >();
		heigth = 0;
		width = 0;

		// Read lines specifying colors
		while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
			line = line.replaceAll( "\\s", "" );
			color = line.split( ":" )[0];

			for ( String id : line.split( ":" )[1].split( "," ) )
				colors.put( id.charAt( 0 ), color );
		}

		// Read lines specifying level layout
		while ( !line.equals( "" ) ) {
			int i;

			for ( i = 0; i < line.length(); i++ ) {

				char id = line.charAt( i );

				if ( '0' <= id && id <= '9' ) {
					Agent agent = new Agent(heigth,i,id, colors.get(id));
					if(agent.getColor() == null && colors.size() > 0)
						agent.setColor("blue");
					this.agents.add(agent);
					agents.add(agent);
				}

				if ( 'a' <= id && id <= 'z' ) {
					Goal goal = new Goal(heigth,i,id);	
					goals.add(goal);
				}

				if ( 'A' <= id && id <= 'Z' ) {
					Box box = new Box(heigth,i,id, colors.get(id));					
					if(box.getColor() == null && colors.size() > 0)
						box.setColor("blue");
					boxes.put(new Coordinate(heigth,i), box);
				}

				if ( id == '+' ) {
					walls.put(new Coordinate(heigth,i), new Wall(heigth,i)); 
				}
			}
			if(i > width) width = i;
			heigth++;

			line = in.readLine();
		}

		map.setHeigth(heigth);
		map.setWidth(width);
	}

	@Override
		public String toString() {
			String s;
			List<Agent> agents;
			List<Goal> goals;
			HashMap<Coordinate,Box> boxes;
			HashMap<Coordinate,Wall> walls;
			int heigth, width;
			char[][] world;

			s = "";
			agents = this.agents;
			boxes = this.node.getBoxes();
			goals = this.map.getGoals();
			walls = this.map.getWalls();
			heigth = this.map.getHeigth();
			width = this.map.getWidth();

			world = new char[heigth][width];

			for (char[] row: world)
				Arrays.fill(row, ' ');

			for (Wall wall: walls.values())
				world[wall.getX()][wall.getY()] = '+';

			for (Box box: boxes.values()) 
				world[box.getX()][box.getY()] = box.getValue();

			for (Agent agent: agents)
				world[agent.getX()][agent.getY()] = agent.getValue();

			for (Goal goal: goals)
				world[goal.getX()][goal.getY()] = goal.getValue();

			s += String.format("Dimensions: (%d,%d)\n",heigth,width);

			for (char[] row: world) {
				for (char coordinate: row) {
					s += coordinate;
				}
				s += '\n';
			}

			return s;
		}

}
