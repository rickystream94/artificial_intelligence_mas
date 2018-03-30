package environment;

public class Goal {
	private Point point;
	private char value;
	private int priority = 0;

	public Goal(int x, int y, char val) {
		this.point = new Point(x,y);
		this.value = val;
	}

	public int getX() {
		return this.point.getX();
	}

	public int getY() {
		return this.point.getY();
	}

	public char getValue() {
		return this.value;
	}
}
