package com.github.borisskert.keycloak.config.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a multi-value map to store multiple values for each key
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MultiValueMap<K, V> {

    /* *****************************************************************************************************************
     * Readonly fields
     **************************************************************************************************************** */

    private final Map<K, List<V>> map = new HashMap<>();

    /* *****************************************************************************************************************
     * Public contract
     **************************************************************************************************************** */

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        Set<Map.Entry<K, List<V>>> entries = map.entrySet();

        for (Map.Entry<K, List<V>> entry : entries) {
            List<V> entryValue = entry.getValue();

            if (entryValue != null && entryValue.contains(value)) {
                return true;
            }
        }

        return false;
    }

    public List<V> get(K key) {
        List<V> values = map.get(key);
        return cloneOf(values);
    }

    public List<V> put(K key, V value) {
        List<V> values = map.computeIfAbsent(key, k -> new ArrayList<>());

        values.add(value);

        return cloneOf(values);
    }

    public List<V> remove(K key) {
        return map.remove(key);
    }

    public void putAll(Map<? extends K, ? extends Collection<V>> map) {
        putEntries(map.entrySet());
    }

    public void putAll(K key, Collection<V> values) {
        for (V value : values) {
            List<V> storedValues = map.computeIfAbsent(key, k -> new ArrayList<>());
            storedValues.add(value);
        }
    }

    public void putAll(MultiValueMap<K, V> map) {
        putEntries(map.entrySet());
    }

    public void clear() {
        map.clear();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        return map.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Set<Map.Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }

    public Map<K, List<V>> toMap() {
        return map.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                );
    }

    /* *****************************************************************************************************************
     * Overrides of Object
     **************************************************************************************************************** */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiValueMap<?, ?> that = (MultiValueMap<?, ?>) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /* *****************************************************************************************************************
     * Private methods
     **************************************************************************************************************** */

    private void putEntries(Set<? extends Map.Entry<? extends K, ? extends Collection<V>>> entries) {
        for (Map.Entry<? extends K, ? extends Collection<V>> entry : entries) {
            Collection<V> entryValues = entry.getValue();

            for (V entryValue : entryValues) {
                putValue(entry.getKey(), entryValue);
            }
        }
    }

    private void putValue(K key, V value) {
        List<V> values = map.computeIfAbsent(key, k -> new ArrayList<>());

        values.add(value);
    }

    private List<V> cloneOf(List<V> values) {
        if (values == null) {
            return null;
        }

        ArrayList<V> clone = new ArrayList<>(values.size());
        clone.addAll(values);

        return clone;
    }
}
