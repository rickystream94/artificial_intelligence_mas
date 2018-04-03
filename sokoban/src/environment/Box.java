package environment;

public class Box {
	private Coordinate coordinate;
	private char value;
	private String color;

	public Box(int x, int y, char val, String color) {
		this.coordinate = new Coordinate(x,y);
		this.value = val;
		this.color = color;
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
}
