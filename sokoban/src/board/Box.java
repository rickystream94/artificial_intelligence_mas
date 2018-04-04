package board;

import java.util.Objects;

public class Box {
	private Coordinate coordinate;
	private char value;
	private String color;

	public Box(int x, int y, char val, String color) {
		this.coordinate = new Coordinate(x,y);
		this.value = val;
		this.color = color;
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

	public String getColor() {
		return this.color;
	}

	public char getValue() {
		return this.value;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setCoordinate(int x, int y) {
		this.coordinate.setX(x);
		this.coordinate.setY(y);
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) return true;
		if (!(o instanceof Box)) {
			return false;
		}
		Box other = (Box) o;
		return 
			Objects.equals(this.coordinate, other.coordinate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.coordinate);
	}
}
