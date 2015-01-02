package net.kopeph.ld31.util;

public class Pointer<T> {
	public T value;

	public Pointer() {
		//Space for rent
	}

	public Pointer(T value) {
		this.value = value;
	}
}
