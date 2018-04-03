package environment;

public class Wall {
	private Coordinate coordinate;

	public Wall(int x, int y) {
		this.coordinate = new Coordinate(x,y);
	}

	public int getX() {
		return this.coordinate.getX();
	}

	public int getY() {
		return this.coordinate.getY();
	}
}
