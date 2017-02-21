import org.junit.Test;
import org.junit.*;
import util.HtmlUtil;

import javax.ws.rs.core.MultivaluedHashMap;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    public void onTestPutAllHashtable(){
        Hashtable<String, Integer> table1 = new Hashtable<>();
        Hashtable<String, Integer> table2 = new Hashtable<>();
        table1.put("test", 1);
        table1.put("test1", 11);
        table1.put("lol", 100);
        table2.put("test", 2);
        table2.put("test1", 3);
        table2.put("lol", 100);
        table2.put("lol1", 100);
        table1.putAll(table2);
        System.out.println(table1);
    }

    @Test
    public  void onTestMergeHashtables(){
        Hashtable<String, Integer> table1 = new Hashtable<>();
        Hashtable<String, Integer> table2 = new Hashtable<>();
        Hashtable<String, Integer> t = new Hashtable<>();
//        table1.put("test", 1);
        table1.put("est1", 11);
        table1.put("lol", 100);
        table2.put("test", 2);
        table2.put("test1", 3);
        table2.put("lol", 100);
        table2.put("lol1", 100);
        t.putAll(table1);
        t.putAll(table2);
        Hashtable<String, Integer> table = new Hashtable<>();
            Map<String, Integer> combinedMap = Stream.concat(
                    table1.entrySet().stream(), table2.entrySet().stream())
                    .collect(Collectors.groupingBy(Map.Entry::getKey,
                                Collectors.summingInt(Map.Entry::getValue)));
        table2.forEach((k,v) -> table1.merge(k, v, (v1,v2) -> v1 + v2));

        System.out.println(table1);
    }

    private Hashtable<String, Integer> mergeHashTables(Hashtable<String, Integer> t1, Hashtable<String, Integer> t2){
        Hashtable<String, Integer> table = new Hashtable<>();
        for (Map.Entry<String,Integer> entry : t1.entrySet()) {
//            entry.getKey() + " " + entry.getValue();
        }
        return null;
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
    public void onTestFetchAndSaveUrls(){
        HtmlUtil.fetchAndSaveUrls();
    }


    @Test
    public void onTestAsyncRequests() throws IOException, InterruptedException {
        String[] urls = {"http://cstatic.weborama.fr/advertiser/426/43/238/319/wbrm_animation.html",
                "http://cstatic.weborama.fr/iframe/external.html",
                "http://cstatic.weborama.fr/advertiser/426/43/238/319/standard_300x250.html?scrrefstr=scr_23897040824banner1464470086420&scrdebug=0&scrwidth=300&scrheight=250&scrwebodomain=0&scrdevtype=mobile&vars=wuid%3D%26retargeting%3D%26&clicks=%5B%22http%3A%2F%2Fcnfm.ad.dotandad.com%2Fclick%3Fpar%3D21.88103.49513.190450.97933.noflash%257C%257Cm_ilgiornale_it_news_2016_05_28_e-morto-giorgio-albertazzi_1264768_%257Cm_ilgiornale_it_news_2016_05_28_e-morto-giorgio-albertazzi_1264768_..http%253A%252F%252Fm%25252eilgiornale%25252eit%252Fnews%252F2016%252F05%252F28%252Fe-morto-giorgio-albertazzi%252F1264768%252F.http%253A%252F%252Fm%25252efacebook%25252ecom%252F.360.1446..1.%3Blink%3Dhttp%253A%252F%252Fmediolanum.solution.weborama.fr%252Ffcgi-bin%252Fdispatch.fcgi%253Fa.A%253Dcl%2526a.si%253D426%2526a.te%253D701%2526a.aap%253D667%2526a.agi%253D75%2526g.lu%253D%22%5D",
                "http://gluservices.s3.amazonaws.com/PushNotifications2.0/com.glu.deerhunt2-google/notifications.txt", "http://ds.ssw.live.com/UploadData.aspx", "http://iphone.ilmeteo.it/android-app.php?method=situationAndForecast&type=0&id=609&x=6f9f4ece6c4e0e9dbd6782c8acddbe1b&lang=ita&v=3.5", "http://lgemobilewidget.accu-weather.com/widget/lgemobilewidget/weather-data.asp?slat=46.1394781&slon=12.2176508&langid=8",
                "http://ds.ssw.live.com/UploadData.aspx",
                "http://appexneuprodweather.blob.core.windows.net/img/phone/default/tile/wide/27b.jpg",
                "http://appexneuprodweather.blob.core.windows.net/img/phone/default/tile/medium/27b.jpg",
                "http://appexneuprodweather.blob.core.windows.net/img/phone/default/tile/small/27b.jpg",
                "http://ds.ssw.live.com/UploadData.aspx",
                "http://appexneuprodweather.blob.core.windows.net/img/phone/default/tile/wide/27b.jpg",
                "http://appexneuprodweather.blob.core.windows.net/img/phone/default/tile/medium/27b.jpg",
                "http://appexneuprodweather.blob.core.windows.net/img/phone/default/tile/small/27b.jpg",
                "http://ds.ssw.live.com/UploadData.aspx",
                "http://iphone.ilmeteo.it/android-app.php?method=situationAndForecast&type=0&id=609&x=6f9f4ece6c4e0e9dbd6782c8acddbe1b&lang=ita&v=3.5",
                "http://www.trenitalia.com/cms-file/common/js/themes/trenitalia_2014/001/list.json",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_mobile/299x130_hotel.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_mobile/299x130_infomobilita.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_mobile/299x130_stato-treno.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_mobile/299x130_tutte-le-offerte.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_2/440x170_dx_smartcard.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_2/440x170_nuova_Cartafreccia.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_1/299x171_elezioni-amministrative.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_1/299x171_bimbigratis_a.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Fascia_banner_1/299x171_treniextraEmiliaRomagna.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Flag-off.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Immagini_mobile/banner_form_mobileOnly.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Visual/1400x480/1400x480_sconto_biglietto_ZoomBioparco.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Visual/1400x480/1400x480_ar_weekend_Napoli.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Visual/1400x480/1400x480_leonardo_express.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/Visual/1400x480/1400x480_gioca_danticipo-4.jpg",
                "http://www.trenitalia.com/cms-file/immagini/trenitalia2014/Homepage/pulsante_menu_FC.jpg",
                "http://www.trenitalia.com/cms-file/common/css/themes/trenitalia_2014/001/i/china_flag.jpg",
                "http://mesu.apple.com/assets/com_apple_MobileAsset_SafariCloudHistoryConfiguration/com_apple_MobileAsset_SafariCloudHistoryConfiguration.xml",
                "http://dlcdnamax.asus.com/Rel/App/IME/theme/20151030/ime-20151030-it.json",
                "http://dlcdnamax.asus.com/Rel/App/IME/theme/20151030/ime-20151030.json",
                "http://dlcdnamax.asus.com/Rel/App/IME/index.json",
                "http://api.accuweather.com/alerts/v1/2549407.json?apikey=a33466bfa5b24f9f82aa7cf62d482f67&details=true&language=it",
                "http://api.accuweather.com/currentconditions/v1/2549407.json?apikey=a33466bfa5b24f9f82aa7cf62d482f67&language=it&details=true",
                "http://api.accuweather.com/locations/v1/cities/geoposition/search.json?q=46.1096405",
                "http://configuration.apple.com/configurations/pep/pipeline/pipeline3.html",
                "http://configuration.apple.com/configurations/pep/pipeline/pipeline2.html",
                "http://configuration.apple.com/configurations/pep/pipeline/pipeline1.html",
                "http://configuration.apple.com/configurations/pep/pipeline/pipeline0.html",
                "http://api-wp.geniale.com/v1/ita/flyers.json?key=249cc184-d73c-11e2-91f9-005056af0765&conditions=is_active:1",
                "is_visible:1",
                "is_highlight:1",
                "distance%20<=:30&sort=publish_at&direction=DESC&ll=46.11",
                "http://iphone.ilmeteo.it/android-app.php?method=situationAndForecast&type=0&id=609&x=6f9f4ece6c4e0e9dbd6782c8acddbe1b&lang=ita&v=3.5",
                "http://www.snai.it/mobilepage/richiestadeposito.php?carta=6031980010126789&token=94490e8a14b5b869a9529a20f1583644&pvend_sport=15215&tipo=sportGAME360&device=iphone&back_url=http://native_back",
                "http://www.rai.it/dl/portale/html/palinsesti/static/palinsestoOraInOnda.html?output=json&id=636000763494965056"
        };
       /* try(AsyncHttpClient asyncHttpClient = asyncHttpClient()) {
            for(String u: urls) {
                asyncHttpClient
                        .prepareGet(u)
                        .execute()
                        .toCompletableFuture()
                        .thenApply(Response::getContentType)
                        .thenAccept(System.out::println)
                        .join();
            }
        }*/
        for (String u : urls) {
//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
                    try {
                        URL url = new URL(u);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("HEAD");
                        connection.setConnectTimeout(20000);
                        connection.connect();
                        String contentType = connection.getContentType();
                        contentType = contentType.trim();
                        if (contentType.contains("text/html") ||
                                contentType.contains("text/plain")) {
                            System.out.println("YES");
                        }
                        else{
                            System.out.println(contentType);}
                    } catch (Exception e) {
                    }
//                }
//            });
//            t.start();
//            t.join();
        }

    }
}