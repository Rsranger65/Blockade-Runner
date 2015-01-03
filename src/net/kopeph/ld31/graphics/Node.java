package net.kopeph.ld31.graphics;

public class Node implements Comparable<Node> {
	public Node parent = null;
	public final int x;
	public final int y;
	public int g = 0; //traveled distance
	public int h = 0; //absolute distance (from target) aka the heuristic

	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Node(int x, int y, int g) {
		this(x, y);
		this.g = g;
	}

	public void setH(Node targetNode, int unitDistance) {
		h = Math.abs(targetNode.x - x) + Math.abs(targetNode.y - y);
		h *= unitDistance;
	}

	//combined heuristic
	public int f() {
		return g + h;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Node)) return false;

		Node nOther = (Node) other;
		return x == nOther.x && y == nOther.y;
	}

	@Override
	public int hashCode() {
		return new Long((long) x << 32 | y).hashCode();
	}

	@Override
	public String toString() {
		return String.format("(%d, %d) f=%d+%d",x , y, f(), g , h); //$NON-NLS-1$
	}

	@Override
	public int compareTo(Node other) {
		return f() - other.f();
	}
}
