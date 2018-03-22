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

public class World {

	private BufferedReader in;
	private int width;
	private int height;

	private List<Box> boxes = new ArrayList<Box>();
	private List<Agent> agents = new ArrayList<Agent>();

	private static List<Goal> goals = new ArrayList<Goal>();
	private static Set<Point> walls = new HashSet<Point>();

	public World(InputStream in) throws IOException {
		this.in = new BufferedReader( new InputStreamReader( in ) );
		this.width = 0;
		this.height = 0;

		readMap();
	}

	private void readMap() throws IOException {
		Map< Character, String > colors = new HashMap< Character, String >();

		String line, color;

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
				//if ( '0' <= id && id <= '9' ) //Agent
				//if ( 'a' <= id && id <= 'z' ) //Goal
				//if ( 'A' <= id && id <= 'Z' ) //Box
				if ( id == '+' ) {
					Point wall = new Point(this.height,i); 
					walls.add(wall);
				}
			}
			if(i > this.width) this.width = i;
			this.height++;

			line = in.readLine();
		}
	}

	@Override
	public String toString() {
		String s = "";
		char[][] world = new char[height][width];

		for (char[] row: world)
			Arrays.fill(row, ' ');

		for (Point wall: walls)
			world[wall.getX()][wall.getY()] = '+';

		for (Box box: boxes) 
			//world[box.getX()][box.getY()] = box.value;

		for (Agent agent: agents)
			//world[agent.getX()][agent.getY()] = agent.value;

		for (Goal goal: goals)
			//world[goal.getX()][goal.getY()] = goal.value;

		for (Point wall: walls)
			world[wall.getX()][wall.getY()] = '+';

		s += String.format("Dimensions: (%d,%d)\n",height,width);

		for (char[] row: world) {
			for (char point: row) {
				s += point;
			}
			s += '\n';
		}
		return s;
	}

}
