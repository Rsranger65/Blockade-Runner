package net.kopeph.ld31.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import net.kopeph.ld31.spi.PointPredicate;

/** @author alexg */
public class Paths {
	private Paths() {
		throw new AssertionError("No Instantiation of: " + getClass().getName()); //$NON-NLS-1$
	}

	public static Node aStar(Node startNode, Node endNode, PointPredicate isOpen, int width, int height) {
		try {
			return aStar(startNode, endNode, isOpen, width, height, false);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static Node aStarI(Node startNode, Node endNode, PointPredicate isOpen, int width, int height) throws InterruptedException {
		return aStar(startNode, endNode, isOpen, width, height, true);
	}

	public static boolean isAccessible(Node startNode, Node endNode, PointPredicate isOpen, int width, int height) {
		try {
			return isAccessible0(startNode, endNode, isOpen, width, height, false);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isAccessibleI(Node startNode, Node endNode, PointPredicate isOpen, int width, int height) throws InterruptedException {
		return isAccessible0(startNode, endNode, isOpen, width, height, true);
	}

	private static Node aStar(Node startNode, Node endNode, PointPredicate isOpen, int sizeX, int sizeY, boolean shouldInterrupt) throws InterruptedException {
		PriorityQueue<Node> opened = new PriorityQueue<>();
		Node[] closed = new Node[sizeX * sizeY];
		opened.add(startNode);

		while (opened.size() > 0) {
			//Make interruptible
			if (shouldInterrupt && Thread.interrupted())
				throw new InterruptedException();

			Node curNode = opened.poll();

			if (curNode.equals(endNode))
				return curNode;

			List<Node> nextNodes = getNextNodes(curNode, 10, 14, isOpen);

			for (Node nextNode : nextNodes) {
				nextNode.setH(endNode, 10);
				nextNode.parent = curNode;

				if (isBetter(opened, nextNode) && isBetter(closed, sizeX, nextNode)) {
					while (opened.remove(nextNode)){/* Remove all */}
					closed[nextNode.y*sizeX + nextNode.x] = null;
					opened.add(nextNode);
				}
			}
			closed[curNode.y*sizeX + curNode.x] = curNode;
		}
		return null;
	}

	private static boolean isBetter(Iterable<Node> nodes, Node newNode) {
		for (Node node : nodes) {
			if (node.x == newNode.x && node.y == newNode.y)
				return node.g > newNode.g;
		}
		return true;
	}

	private static boolean isBetter(Node[] nodes, int sizeX, Node newNode) {
		Node existingNode = nodes[newNode.y*sizeX + newNode.x];
		if (existingNode == null)
			return true;
		return existingNode.g > newNode.g;
	}

	private static List<Node> getNextNodes(Node curNode, int orthoCost, int diagCost, PointPredicate isOpen) {
		List<Node> sucessorNodes = new ArrayList<>(8);
		boolean nw = false, ne = false, sw = false, se = false;

		if (isOpen.on(curNode.x-1, curNode.y)) {
			sucessorNodes.add(new Node(curNode.x-1, curNode.y, curNode.g+orthoCost));
			nw = sw = true;
		}
		if (isOpen.on(curNode.x, curNode.y-1)) {
			sucessorNodes.add(new Node(curNode.x, curNode.y-1, curNode.g+orthoCost));
			nw = ne = true;
		}
		if (isOpen.on(curNode.x+1, curNode.y)) {
			sucessorNodes.add(new Node(curNode.x+1, curNode.y, curNode.g+orthoCost));
			ne = se = true;
		}
		if (isOpen.on(curNode.x, curNode.y+1)) {
			sucessorNodes.add(new Node(curNode.x, curNode.y+1, curNode.g+orthoCost));
			se = sw = true;
		}

		if (nw && isOpen.on(curNode.x-1, curNode.y-1))
			sucessorNodes.add(new Node(curNode.x-1, curNode.y-1, curNode.g+diagCost));
		if (ne && isOpen.on(curNode.x+1, curNode.y-1))
			sucessorNodes.add(new Node(curNode.x+1, curNode.y-1, curNode.g+diagCost));
		if (sw && isOpen.on(curNode.x-1, curNode.y+1))
			sucessorNodes.add(new Node(curNode.x-1, curNode.y+1, curNode.g+diagCost));
		if (se && isOpen.on(curNode.x+1, curNode.y+1))
			sucessorNodes.add(new Node(curNode.x+1, curNode.y+1, curNode.g+diagCost));

		return sucessorNodes;
	}

	//This is basically the lovechild of A* and flood fill,
	//optimizing out the part which guarantees the shortest path and providing a fast best-case runtime
	private static boolean isAccessible0(Node startNode, Node endNode, PointPredicate isOpen, int sizeX, int sizeY, boolean shouldInterrupt) throws InterruptedException {
		PriorityQueue<Node> opened = new PriorityQueue<>();
		Node[] closed = new Node[sizeX * sizeY];
		opened.add(startNode);

		while (opened.size() > 0) {
			//Make interruptible
			if (shouldInterrupt && Thread.interrupted())
				throw new InterruptedException();

			Node curNode = opened.poll();

			if (curNode.equals(endNode))
				return true;

			List<Node> nextNodes = getNextNodes(curNode, 0, 0, isOpen);

			for (Node nextNode : nextNodes) {
				nextNode.setH(endNode, 1);
				nextNode.parent = curNode;

				if (!opened.contains(nextNode) && closed[nextNode.y*sizeX + nextNode.x] == null) {
					opened.add(nextNode);
					closed[nextNode.y*sizeX + nextNode.x] = nextNode;
				}
			}
		}
		return false;
	}
}
