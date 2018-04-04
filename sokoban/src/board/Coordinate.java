package board;

import java.util.Objects;

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
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int x) {
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) return true;
		if (!(o instanceof Coordinate)) {
			return false;
		}
		Coordinate other = (Coordinate) o;
		return 
			this.x == other.x &&
			this.y == other.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.y);
	}

}
