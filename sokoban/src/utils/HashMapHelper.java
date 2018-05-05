package utils;

import java.util.Map;

public class HashMapHelper {

    /**
     * Given a map with generic keys and integer values, returns the key that has the minimum value
     *
     * @param map Generic Object-Integer map
     * @return key corresponding to minimum value
     */
    public static Object getKeyByMinIntValue(Map<Object, Integer> map) {
        int minValue = Integer.MAX_VALUE;
        Object chosenKey = null;
        for (Object key : map.keySet()) {
            int value = map.get(key);
            if (value < minValue) {
                minValue = value;
                chosenKey = key;
            }
        }
        return chosenKey;
    }

    /**
     * Given a map with generic keys and integer values, returns the key that has the maximum value
     *
     * @param map Generic Object-Integer map
     * @return key corresponding to maximum value
     */
    public static Object getKeyByMaxIntValue(Map<Object, Integer> map) {
        int maxValue = Integer.MIN_VALUE;
        Object chosenKey = null;
        for (Object key : map.keySet()) {
            int value = map.get(key);
            if (value > maxValue) {
                maxValue = value;
                chosenKey = key;
            }
        }
        return chosenKey;
    }
}
