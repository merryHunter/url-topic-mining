/**
 * @author Ivan Chernukha on 14.02.17.
 */
package util;

import java.util.*;

public class Util {

    /**
     *
     * @param table topic stats
     * @param n minimum rating
     * @return
     */
    public static Hashtable<String, Integer> findGreaterThan(Hashtable<String,Integer> table, int n){
        Hashtable<String, Integer> results = new Hashtable<>();
        for(String s:table.keySet()){
            if (table.get(s) > n){
                results.put(s, table.get(s));
            }
        }
        return results;
    }

    public static <K, V extends Comparable<? super V>> List<Map.Entry<String, Integer>>
    findGreatest(Hashtable<String, Integer> map, int n)
    {
        Comparator<? super Map.Entry<K, V>> comparator =
                new Comparator<Map.Entry<K, V>>()
                {
                    @Override
                    public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1)
                    {
                        V v0 = e0.getValue();
                        V v1 = e1.getValue();
                        return v0.compareTo(v1);
                    }
                };
        PriorityQueue<Map.Entry<String, Integer>> highest =
                new PriorityQueue<Map.Entry<String, Integer>>(n, (Comparator<? super Map.Entry<String, Integer>>) comparator);
        for (Map.Entry<String, Integer> entry : map.entrySet())
        {
            highest.offer(entry);
            while (highest.size() > n)
            {
                highest.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        while (highest.size() > 0)
        {
            result.add(highest.poll());
        }
        return result;
    }

    public static String getColour(int i) {
        switch (i%7) {
            case 0: {
                return "default";
            }
            case 1: {
                return "green";
            }
            case 2: {
                return "pink";
            }
            case 3: {
                return "blue";
            }
            case 4: {
                return "tan";
            }
            case 5: {
                return "gray";
            }
            case 6: {
                return "yellow";
            }
        }
        return "default";
    }

}
