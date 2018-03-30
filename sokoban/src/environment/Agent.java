package environment;

public class Agent {
	private Point point;
	private char value;
	private String color;

	public Agent(int x, int y, char val, String color) {
		this.point = new Point(x,y);
		this.value = val;
		this.color = color;
	}

	public int getX() {
		return this.point.getX();
	}

	public int getY() {
		return this.point.getY();
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
