package com.github.borisskert.keycloak.config.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MultiValueMapTest {

    private MultiValueMap<String, String> emptyMap;
    private MultiValueMap<String, String> xaMap;
    private MultiValueMap<String, String> xabcMap;
    private MultiValueMap<String, String> xaybzcMap;

    @BeforeEach
    public void setup() throws Exception {
        emptyMap = new MultiValueMap<>();

        xaMap = createXaMultiValueMap();
        xabcMap = createXabcMultiValueMap();
        xaybzcMap = createXaYbZcMultiValueMap();
    }

    @Test
    public void shouldPutValue() throws Exception {
        MultiValueMap<String, String> multiValueMap = new MultiValueMap<>();
        List<String> values = multiValueMap.put("X", "A");

        assertThat(values, is(equalTo(listOf("A"))));

        values = multiValueMap.put("X", "B");

        assertThat(values, is(equalTo(listOf("A", "B"))));

        values = multiValueMap.put("X", "C");

        assertThat(values, is(equalTo(listOf("A", "B", "C"))));

        values.add("D");

        assertThat(multiValueMap.get("X"), is(equalTo(listOf("A", "B", "C"))));
    }

    @Test
    public void shouldProvideSize() throws Exception {
        assertThat(emptyMap.size(), is(equalTo(0)));
        assertThat(xaMap.size(), is(equalTo(1)));
        assertThat(xabcMap.size(), is(equalTo(1)));
        assertThat(xaybzcMap.size(), is(equalTo(3)));
    }

    @Test
    public void shouldProvideValueForKey() throws Exception {
        assertThat(emptyMap.get("X"), is(nullValue()));
        assertThat(xaMap.get("X"), equalTo(listOf("A")));
        assertThat(xabcMap.get("X"), equalTo(listOf("A", "B", "C")));
        assertThat(xaybzcMap.get("X"), equalTo(listOf("A")));
        assertThat(xaybzcMap.get("Y"), equalTo(listOf("B")));
        assertThat(xaybzcMap.get("Z"), equalTo(listOf("C")));
    }

    @Test
    public void shouldProvideValueClone() throws Exception {
        List<String> values = xabcMap.get("X");
        assertThat(values, equalTo(listOf("A", "B", "C")));

        values.add("D");

        assertThat(xabcMap.get("X"), is(equalTo(listOf("A", "B", "C"))));
    }

    @Test
    public void shouldProvideKeySet() throws Exception {
        assertThat(emptyMap.keySet(), is(equalTo(new HashSet<>())));
        assertThat(xaMap.keySet(), is(equalTo(setOf("X"))));
        assertThat(xabcMap.keySet(), is(equalTo(setOf("X"))));
        assertThat(xaybzcMap.keySet(), is(equalTo(setOf("X", "Y", "Z"))));
    }

    @Test
    public void shouldProvideValues() throws Exception {
        assertThat(emptyMap.values(), is(equalTo(new ArrayList<>())));
        assertThat(xaMap.values(), is(equalTo(listOf("A"))));
        assertThat(xabcMap.values(), is(equalTo(listOf("A", "B", "C"))));
        assertThat(xaybzcMap.values(), is(equalTo(listOf("A", "B", "C"))));
    }

    @Test
    public void shouldProvideEntrySet() throws Exception {
        assertThat(emptyMap.entrySet(), is(equalTo(new HashSet<>())));
        assertThat(xaMap.entrySet(), is(equalTo(setOf(entryOf("X", listOf("A"))))));
        assertThat(xabcMap.entrySet(), is(equalTo(setOf(entryOf("X", listOf("A", "B", "C"))))));
        assertThat(xaybzcMap.entrySet(), is(equalTo(setOf(
                entryOf("X", listOf("A")),
                entryOf("Y", listOf("B")),
                entryOf("Z", listOf("C"))
        ))));
    }

    @Test
    public void shouldIndicateIfContainsKey() throws Exception {
        assertThat(emptyMap.containsKey("X"), is(equalTo(false)));
        assertThat(emptyMap.containsKey("Y"), is(equalTo(false)));
        assertThat(emptyMap.containsKey("Z"), is(equalTo(false)));

        assertThat(xaMap.containsKey("X"), is(equalTo(true)));
        assertThat(xaMap.containsKey("Y"), is(equalTo(false)));
        assertThat(xaMap.containsKey("Z"), is(equalTo(false)));

        assertThat(xabcMap.containsKey("X"), is(equalTo(true)));
        assertThat(xabcMap.containsKey("Y"), is(equalTo(false)));
        assertThat(xabcMap.containsKey("Z"), is(equalTo(false)));

        assertThat(xaybzcMap.containsKey("X"), is(equalTo(true)));
        assertThat(xaybzcMap.containsKey("Y"), is(equalTo(true)));
        assertThat(xaybzcMap.containsKey("Z"), is(equalTo(true)));
    }

    @Test
    public void shouldIndicateIfContainsValue() throws Exception {
        assertThat(emptyMap.containsValue("A"), is(equalTo(false)));
        assertThat(emptyMap.containsValue("B"), is(equalTo(false)));
        assertThat(emptyMap.containsValue("C"), is(equalTo(false)));

        assertThat(xaMap.containsValue("A"), is(equalTo(true)));
        assertThat(xaMap.containsValue("B"), is(equalTo(false)));
        assertThat(xaMap.containsValue("C"), is(equalTo(false)));

        assertThat(xabcMap.containsValue("A"), is(equalTo(true)));
        assertThat(xabcMap.containsValue("B"), is(equalTo(true)));
        assertThat(xabcMap.containsValue("C"), is(equalTo(true)));

        assertThat(xaybzcMap.containsValue("A"), is(equalTo(true)));
        assertThat(xaybzcMap.containsValue("B"), is(equalTo(true)));
        assertThat(xaybzcMap.containsValue("C"), is(equalTo(true)));
    }

    @Test
    public void shouldIndicateIfEmpty() throws Exception {
        assertThat(emptyMap.isEmpty(), is(equalTo(true)));
        assertThat(xaMap.isEmpty(), is(equalTo(false)));
        assertThat(xabcMap.isEmpty(), is(equalTo(false)));
        assertThat(xaybzcMap.isEmpty(), is(equalTo(false)));
    }

    @Test
    public void shouldClearMap() throws Exception {
        emptyMap.clear();
        assertThat(emptyMap.isEmpty(), is(equalTo(true)));

        xaMap.clear();
        assertThat(xaMap.isEmpty(), is(equalTo(true)));

        xabcMap.clear();
        assertThat(xabcMap.isEmpty(), is(equalTo(true)));

        xaybzcMap.clear();
        assertThat(xaybzcMap.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void shouldRemoveKeys() throws Exception {
        List<String> removedValues = emptyMap.remove("X");
        assertThat(emptyMap.get("X"), is(nullValue()));
        assertThat(removedValues, is(nullValue()));

        removedValues = xaMap.remove("X");
        assertThat(xaMap.get("X"), is(nullValue()));
        assertThat(removedValues, is(equalTo(listOf("A"))));

        removedValues = xabcMap.remove("X");
        assertThat(xabcMap.get("X"), is(nullValue()));
        assertThat(removedValues, is(equalTo(listOf("A", "B", "C"))));

        removedValues = xaybzcMap.remove("X");
        assertThat(xaybzcMap.get("X"), is(nullValue()));
        assertThat(removedValues, is(equalTo(listOf("A"))));
    }

    @Test
    public void shouldPutOtherMultiValueMap() throws Exception {
        MultiValueMap<String, String> map = new MultiValueMap<>();

        map.putAll(emptyMap);
        assertThat(map, is(equalTo(emptyMap)));

        map = new MultiValueMap<>();
        map.putAll(xaMap);
        assertThat(map, is(equalTo(xaMap)));

        map = new MultiValueMap<>();
        map.putAll(xabcMap);
        assertThat(map, is(equalTo(xabcMap)));

        map = new MultiValueMap<>();
        map.putAll(xaybzcMap);
        assertThat(map, is(equalTo(xaybzcMap)));

        map = new MultiValueMap<>();
        map.putAll(xaybzcMap);
        map.putAll(createXdYeZfMultiValueMap());
        assertThat(map, is(equalTo(createXadYbeZcfMultiValueMap())));
    }

    @Test
    public void shouldPutAllOfMap() throws Exception {
        MultiValueMap<String, String> map = new MultiValueMap<>();

        map.putAll(new HashMap<>());
        assertThat(map, is(equalTo(emptyMap)));

        map = new MultiValueMap<>();
        map.putAll(createXaMap());
        assertThat(map, is(equalTo(xaMap)));

        map = new MultiValueMap<>();
        map.putAll(createXabcMap());
        assertThat(map, is(equalTo(xabcMap)));

        map = new MultiValueMap<>();
        map.putAll(createXaYbZcMap());
        assertThat(map, is(equalTo(xaybzcMap)));

        map = new MultiValueMap<>();
        map.putAll(createXaYbZcMap());
        map.putAll(createXdYeZfMap());
        assertThat(map, is(equalTo(createXadYbeZcfMultiValueMap())));
    }

    @Test
    public void shouldPutAllOfKeyWithCollection() throws Exception {
        MultiValueMap<String, String> map = new MultiValueMap<>();

        map.putAll("X", new ArrayList<>());
        assertThat(map, is(equalTo(emptyMap)));

        map = new MultiValueMap<>();
        map.putAll("X", listOf("A"));
        assertThat(map, is(equalTo(xaMap)));

        map = new MultiValueMap<>();
        map.putAll("X", listOf("A", "B", "C"));
        assertThat(map, is(equalTo(xabcMap)));

        map = new MultiValueMap<>();
        map.putAll("X", listOf("A"));
        map.putAll("Y", listOf("B"));
        map.putAll("Z", listOf("C"));
        assertThat(map, is(equalTo(xaybzcMap)));
    }

    @Test
    public void shouldEqualOtherMap() throws Exception {
        assertThat(emptyMap, is(equalTo(new MultiValueMap<>())));
        assertThat(xaMap, is(equalTo(createXaMultiValueMap())));
        assertThat(xabcMap, is(equalTo(createXabcMultiValueMap())));
        assertThat(xaybzcMap, is(equalTo(createXaYbZcMultiValueMap())));
    }

    @Test
    public void shouldProvideStringRepresentation() throws Exception {
        assertThat(emptyMap.toString(), is(equalTo("{}")));
        assertThat(xaMap.toString(), is(equalTo("{X=[A]}")));
        assertThat(xabcMap.toString(), is(equalTo("{X=[A, B, C]}")));
        assertThat(xaybzcMap.toString(), is(equalTo("{X=[A], Y=[B], Z=[C]}")));
    }

    @Test
    public void shouldConvertToMap() throws Exception {
        assertThat(emptyMap.toMap(), is(equalTo(new HashMap<>())));
        assertThat(xaMap.toMap(), is(equalTo(createXaMap())));
        assertThat(xabcMap.toMap(), is(equalTo(createXabcMap())));
        assertThat(xaybzcMap.toMap(), is(equalTo(createXaYbZcMap())));
    }

    @Test
    public void shouldConvertToClonedMap() throws Exception {
        Map<String, List<String>> convertedMap = xaybzcMap.toMap();
        assertThat(convertedMap, is(equalTo(createXaYbZcMap())));

        convertedMap.put("W", listOf("A"));

        // should not be modified
        assertThat(xaybzcMap.size(), is(equalTo(3)));
    }

    private MultiValueMap<String, String> createXaMultiValueMap() {
        MultiValueMap<String, String> map = new MultiValueMap<>();
        map.put("X", "A");

        return map;
    }

    private Map<String, Collection<String>> createXaMap() {
        Map<String, Collection<String>> map = new HashMap<>();
        map.put("X", listOf("A"));

        return map;
    }

    private MultiValueMap<String, String> createXabcMultiValueMap() {
        MultiValueMap<String, String> map = new MultiValueMap<>();
        map.put("X", "A");
        map.put("X", "B");
        map.put("X", "C");

        return map;
    }

    private Map<String, List<String>> createXabcMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("X", listOf("A", "B", "C"));

        return map;
    }

    private MultiValueMap<String, String> createXaYbZcMultiValueMap() {
        MultiValueMap<String, String> map = new MultiValueMap<>();
        map.put("X", "A");
        map.put("Y", "B");
        map.put("Z", "C");

        return map;
    }

    private Map<String, List<String>> createXaYbZcMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("X", listOf("A"));
        map.put("Y", listOf("B"));
        map.put("Z", listOf("C"));

        return map;
    }

    private MultiValueMap<String, String> createXdYeZfMultiValueMap() {
        MultiValueMap<String, String> map = new MultiValueMap<>();
        map.put("X", "D");
        map.put("Y", "E");
        map.put("Z", "F");

        return map;
    }

    private Map<String, List<String>> createXdYeZfMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("X", listOf("D"));
        map.put("Y", listOf("E"));
        map.put("Z", listOf("F"));

        return map;
    }

    private MultiValueMap<String, String> createXadYbeZcfMultiValueMap() {
        MultiValueMap<String, String> map = new MultiValueMap<>();

        map.put("X", "A");
        map.put("Y", "B");
        map.put("Z", "C");

        map.put("X", "D");
        map.put("Y", "E");
        map.put("Z", "F");

        return map;
    }

    private static <T> List<T> listOf(T item, T... otherItems) {
        List<T> list = new ArrayList<>();
        list.add(item);

        if (otherItems != null) {
            list.addAll(Arrays.asList(otherItems));
        }

        return list;
    }

    private static <T> Set<T> setOf(T item, T... otherItems) {
        Set<T> list = new HashSet<>();
        list.add(item);

        if (otherItems != null) {
            list.addAll(Arrays.asList(otherItems));
        }

        return list;
    }

    private static <K, V> Map.Entry<K, List<V>> entryOf(K key, List<V> values) {
        return new TestEntry<>(key, values);
    }

    private static class TestEntry<K, V> implements Map.Entry<K, List<V>> {

        private final K key;
        private final List<V> values;

        public TestEntry(K key, List<V> values) {
            this.key = key;
            this.values = values;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public List<V> getValue() {
            return values;
        }

        @Override
        public List<V> setValue(List<V> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;

            if (getClass() == o.getClass()) {
                TestEntry<?, ?> testEntry = (TestEntry<?, ?>) o;
                return Objects.equals(key, testEntry.key) &&
                        Objects.equals(values, testEntry.values);
            }

            if (o instanceof Map.Entry) {
                Map.Entry other = (Map.Entry) o;
                return Objects.equals(key, other.getKey()) &&
                        Objects.equals(values, other.getValue());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, values);
        }

        @Override
        public String toString() {
            return "[" + key + "=" + values + "]";
        }
    }
}
