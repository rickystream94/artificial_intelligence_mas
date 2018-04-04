package environment;

import java.util.Objects;

public class Wall {
	private Coordinate coordinate;

	public Wall(int x, int y) {
		this.coordinate = new Coordinate(x,y);
	}

	public Coordinate getCoordinate() {
		return this.coordinate;
	}

	public int getX() {
		return this.coordinate.getX();
	}

	public int getY() {
		return this.coordinate.getY();
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) return true;
		if (!(o instanceof Wall)) {
			return false;
		}
		Wall other = (Wall) o;
		return 
			Objects.equals(this.coordinate, other.coordinate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.coordinate);
	}
}
