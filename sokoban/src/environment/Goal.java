package environment;

public class Goal {
	private Coordinate coordinate;
	private char value;

	public Goal(int x, int y, char val) {
		this.coordinate = new Coordinate(x,y);
		this.value = val;
	}

	public int getX() {
		return this.coordinate.getX();
	}

	public int getY() {
		return this.coordinate.getY();
	}

	public Coordinate getCoordinate() {
		return this.coordinate;
	}

	public char getValue() {
		return this.value;
	}
}
