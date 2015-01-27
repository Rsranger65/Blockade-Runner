package net.kopeph.ld31.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* OPERATION INVARIANT:
 * At the end of every function call, the following must be true for proper operation:
 * (This class is responsible for not falling into a invalid state)
 *
 * - For every R element in fwdMap:
 *   - (element == nullValue) || (element != <any R element excepting itself>)
 *   - backMap.containsKey(element)
 * - For every L element in backMap:
 *   - (element != nullValue)
 *   - fwdMap.containsKey(element)
 */

/**
 * @author alexg
 */
public class OneToManyBiMap<L, R> implements Map<L, List<R>> {
	private Map<L, List<R>> fwdMap = new HashMap<>();
	private Map<R, L> backMap = new HashMap<>();
	private final R nullValue;

	public OneToManyBiMap(R nullValue) {
		this.nullValue = nullValue;
	}

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
		//Copy list to guard against read-only lists
		List<R> prevList = fwdMap.put(key, new ArrayList<>(values));
		if (prevList != null) {
			for (R value : prevList)
				backMap.remove(value);
		}

		for (R value : values) {
			if (!value.equals(nullValue)) {
				L prevKey = backMap.put(value, key);
				if (prevKey != null)
					fwdMap.get(prevKey).remove(value);
			}
		}

		return prevList;
	}

	public R putIndex(L key, int index, R value) {
		//ensure sized properly
		if (!fwdMap.containsKey(key))
			fwdMap.put(key, new ArrayList<>());
		while (index >= fwdMap.get(key).size())
			fwdMap.get(key).add(nullValue);

		//Add in value to fwdMap, and remove old value from backMap.
		R oldValue = fwdMap.get(key).set(index, value);
		backMap.remove(oldValue);

		//if we aren't adding a placeholder, add new value to backMap
		//and correct for uniqueness.
		if (!value.equals(nullValue)) {
			L prevKey = backMap.put(value, key);
			if (prevKey != null)
				fwdMap.get(prevKey).remove(value);
		}

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
