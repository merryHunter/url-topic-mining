import org.junit.Test;
import org.junit.*;

import javax.ws.rs.core.MultivaluedHashMap;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
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
}