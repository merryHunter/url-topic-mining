import org.junit.Test;
import org.junit.*;
import util.HtmlUtil;

import javax.ws.rs.core.MultivaluedHashMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Dmytro on 06/02/2017.
 */
public class RandomTestsFIleTest {

    @Test
    public void test_method_1() {
        for (int i=0; i<=11; i++) {
            System.out.println(2*Math.pow(2,i) + " ");

        }
    }

    @Test
    public void onTestHashMap(){
        MultivaluedHashMap<String,Long> map = new MultivaluedHashMap();
        Long l = 1L;
        List<Long> listlist = new LinkedList<>();
        listlist.add(l);
        map.add("sdf", l);

    }

    @Test
    public void onTestSplit(){
        String[] parts = "http://edge.sharethis.com/share4x/index.8b80d8d9046d002f5c79f52c9a27d602.html|http://images.taboola.com/taboola/image/fetch/dpr_2.0%2Cf_jpg%2Cq_80%2Ch_174%2Cw_209%2Cc_fill%2Cg_faces%2Ce_sharpen/http%3A//cdn.taboolasyndication.com/libtrc/static/thumbnails/ee27d47852a3424d49f4e0da85b7c04c.jpg|http://images.taboola.com/taboola/image/fetch/dpr_2.0%2Cf_jpg%2Cq_80%2Ch_174%2Cw_209%2Cc_fill%2Cg_faces%2Ce_sharpen/http%3A//cdn.taboolasyndication.com/libtrc/static/thumbnails/862566b4826e1473c05d548d2125e1f8.jpg|http://images.taboola.com/taboola/image/fetch/dpr_2.0%2Cf_jpg%2Cq_80%2Ch_174%2Cw_209%2Cc_fill%2Cg_faces%2Ce_sharpen/http%3A//cdn.taboolasyndication.com/libtrc/static/thumbnails/355a5f28c9a87227209b435f91a1477a.jpg|http://images.taboola.com/taboola/image/fetch/dpr_2.0%2Cf_jpg%2Cq_80%2Ch_174%2Cw_209%2Cc_fill%2Cg_faces%2Ce_sharpen/http%3A//www.ottopagine.it/public/thumb/658x370/5-2016/16/news77606.jpg|http://images.taboola.com/taboola/image/fetch/dpr_2.0%2Cf_jpg%2Cq_80%2Ch_174%2Cw_209%2Cc_fill%2Cg_faces%2Ce_sharpen/http%3A//www.ottopagine.it/public/thumb/658x370/5-2016/18/news77920.jpg|http://images.taboola.com/taboola/image/fetch/dpr_2.0%2Cf_jpg%2Cq_80%2Ch_174%2Cw_209%2Cc_fill%2Cg_faces%2Ce_sharpen/http%3A//www.ottopagine.it/public/thumb/658x370/5-2016/16/news77734.jpg|http://seg.sharethis.com/getSegment.php?purl=http%3A%2F%2Fwww.ottopagine.it%2Fav%2Fattualita%2F79293%2Fmoscati-un-altra-vittima-addio-al-bar-cortina.shtml&jsref=http%3A%2F%2Fm.facebook.com%2F&rnd=1464469628252|http://img.youtube.com/vi/s7sK8PJNtKA/hqdefault.jpg|http://www.ottopagine.it/public/thumb/200x112/5-2016/28/news79359.jpg|http://www.ottopagine.it/public/thumb/200x112/5-2016/28/news79363.jpg|http://www.ottopagine.it/public/thumb/200x112/5-2016/28/news79368.jpg|http://www.ottopagine.it/public/thumb/200x112/5-2016/28/news79293.jpg|http://www.ottopagine.it/public/thumb/658x370/5-2016/28/news79293.jpg"
                .split("|");
        for(String s : parts)
            System.out.println(s);
        System.out.println(parts);
    }

    @Test
    public void onTestMatch(){
        String x= "0\t0.5\n" +
                "manual\t1\n" +
                "mongodb\t1\n" +
                "reference\t1\n" +
                "quick\t1\n" +
                "shell\t1\n" +
                "1\t0.5\n" +
                "text\t1\n" +
                "applications\t1\n" +
                "learning\t1\n" +
                "machine\t1\n" +
                "extraction\t1\n";
        Matcher m = Pattern.compile("\\n(([a-z]*)\\t)").matcher(x);
        Matcher counts = Pattern.compile("\\t(([0-9]*)\\n)").matcher(x);
        List<String> topics = new LinkedList<>();
        Hashtable<String, Integer> table = new Hashtable<>();
        while(m.find() && counts.find()) {
            topics.add(m.group(1).trim());
            table.put(m.group(1).trim(), Integer.parseInt(counts.group(1).trim()));
        }
        while(counts.find()){
            String s = counts.group(1).trim();
            System.out.println(s);
        }
        System.out.println(table);
    }

    @Test
    public void onTestTokenizingUrl(){
        String url = "info.fuzzytrack.com/ux/it/1/WhatsApp_IT_1221_files/logo.jpg";
        Matcher m = Pattern.compile("([a-z]*)").matcher(url);
        List<String> words = new LinkedList<>();
        while(m.find()) {
            String s = m.group(1).trim();
            if (!s.equals(""))
                words.add(s);
        }
        //remove extension
        words.remove(words.size() - 1);
        System.out.println(words.toString());
    }

    @Test
    public void onTestPreviousPowerOfTwo(){
        int x = 1028;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
//        x = x | (x >> 8);
        x = x | (x >> 16);
        x =  x - (x >> 1);
        System.out.println(x);
    }

    @Test
    public void onTestLevel(){
        int bits = 1024;
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        log +=  ( bits >>> 1 );
        System.out.println(log);

    }

    @Test
    public void onTestGetTopFromHashtable(){
        Hashtable<String, Integer> map = new Hashtable<>();

        map.put("zip000", 1234);
        map.put("zip001", 2345);
        map.put("zip002", 3456);
        map.put("zip003", 4567);
        map.put("zip004", 5678);
        map.put("zip005", 6789);
        map.put("zip006", 123);
        map.put("zip007", 234);
        map.put("zip008", 456);
        map.put("zip009", 567);
        map.put("zip010", 7890);
        map.put("zip011", 678);
        map.put("zip012", 789);
        map.put("zip013", 890);

        int n = 5;
        List<Map.Entry<String, Integer>> greatest = findGreatest(map, 5);
        Hashtable<String, Integer> map1 = new Hashtable<>();
        map1.putAll((Map<? extends String, ? extends Integer>) greatest);
        System.out.println("Top "+n+" entries:");
        for (Map.Entry<String, Integer> entry : greatest)
        {
            System.out.println(entry);

        }
        System.out.println(map1);
    }

    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>>
    findGreatest(Map<K, V> map, int n)
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
        PriorityQueue<Map.Entry<K, V>> highest =
                new PriorityQueue<Map.Entry<K,V>>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            highest.offer(entry);
            while (highest.size() > n)
            {
                highest.poll();
            }
        }

        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K,V>>();
        while (highest.size() > 0)
        {
            result.add(highest.poll());
        }
        return result;
    }


    @Test
    public void onTestPreprocessCleanUrls(){
        HtmlUtil.preprocessCleanUrlsInDatabase();
    }

}