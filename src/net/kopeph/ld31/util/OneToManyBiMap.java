package net.kopeph.ld31.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author alexg
 */
public class OneToManyBiMap<L, R> implements Map<L, List<R>> {
	private Map<L, List<R>> fwdMap = new HashMap<>();
	private Map<R, L> backMap = new HashMap<>();

	@Override
	public int size() {
		return fwdMap.size();
	}

	@Override
	public boolean isEmpty() {
		return fwdMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return fwdMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return backMap.containsKey(value);
	}

	@Override
	public List<R> get(Object key) {
		return fwdMap.get(key);
	}

	public R getIndex(Object key, int index) {
		return get(key).get(index);
	}

	public L getRev(Object value) {
		return backMap.get(value);
	}

	@Override
	public List<R> put(L key, List<R> values) {
		List<R> prevList = fwdMap.put(key, values);

		for (R value : values) {
			L prevKey = backMap.put(value, key);
			if (prevKey != null)
				fwdMap.get(prevKey).remove(value);
		}

		return prevList;
	}

	public R putIndex(L key, int index, R value) {
		return putIndex(key, index, value, null);
	}

	public R putIndex(L key, int index, R value, R fillValue) {
		//ensure sized properly
		while (index >= fwdMap.get(key).size())
			fwdMap.get(key).add(fillValue);

		R oldValue = fwdMap.get(key).set(index, value);
		L prevKey = backMap.put(value, key);
		if (prevKey != null)
			fwdMap.get(prevKey).remove(value);

		return oldValue;
	}

	@Override
	public List<R> remove(Object key) {
		List<R> removeds = fwdMap.remove(key);
		for (R removed : removeds)
			backMap.remove(removed);

		return removeds;
	}

	public R removeIndex(Object key, int index) {
		R removed = fwdMap.get(key).remove(index);
		backMap.remove(removed);

		return removed;
	}

	public L removeValue(Object value) {
		L removed = backMap.remove(value);
		fwdMap.get(removed).remove(value);

		return removed;
	}

	@Override
	public void putAll(Map<? extends L, ? extends List<R>> map) {
		for (Entry<? extends L, ? extends List<R>> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public void clear() {
		fwdMap.clear();
		backMap.clear();
	}

	@Override
	public Set<L> keySet() {
		return fwdMap.keySet();
	}

	@Override
	public Collection<List<R>> values() {
		return fwdMap.values();
	}

	@Override
	public Set<Entry<L, List<R>>> entrySet() {
		return fwdMap.entrySet();
	}

}
