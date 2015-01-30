package net.kopeph.ld31.spi;

@FunctionalInterface
public interface KeyPredicate {
	public boolean press(int keyId, boolean down);
}
