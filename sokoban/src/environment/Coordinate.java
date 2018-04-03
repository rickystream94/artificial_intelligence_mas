package environment;

public class Coordinate {
	private int x;
	private int y;

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Coordinate(Coordinate other) {
		this.x = other.x;
		this.y = other.y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
