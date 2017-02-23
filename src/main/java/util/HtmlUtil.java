/**
 * @author Ivan Chernukha on 13.02.17.
 */
package util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import detection.Quad;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {

    private static final Logger logger = Logger.getLogger(HtmlUtil.class);

    private static final int CONNECTION_TIMEOUT = 5000;  //5s

    public static enum PAGE_TYPE{
        TITLE,
        BODY,
        URL_LOCATION
    }

    private static volatile int nValidUrls;

    public static String getUrlLocationTokenized(String url){
        Matcher m = Pattern.compile("([a-z]*)").matcher(url);
        List<String> words = new LinkedList<>();
        while(m.find()) {
            String temp = m.group(1).trim();
            if (!temp.equals(""))
                words.add(temp);
        }
        //remove extension - may be "" and thus throw exception
        if (words.size() > 1) {
            words.remove(words.size() - 1);
        } else return "";

        return words.toString();
    }

    public static List<String> getHtmlPages(List<String> urls, PAGE_TYPE page_type){
        List<String> htmlList = new LinkedList<>();
        if (page_type == PAGE_TYPE.URL_LOCATION){
            for (String s : urls) {
                String html = getUrlLocationTokenized(s);
                if ( !html.equals(""))
                    htmlList.add(html);
            }
        } else if (page_type == PAGE_TYPE.TITLE){
            for (String s : urls) {
                try {
                String html = getTitles(s);
                    //TODO: ensure we do not add empty lines!
                    htmlList.add(html);
                }catch (Exception e){
                    logger.error("Unable to fetch url:" + s + "\n" +
                            e.getMessage());
                    //add to list tokenized url location
                    htmlList.add(getUrlLocationTokenized(s));
                }
            }
        } else if (page_type == PAGE_TYPE.BODY){
            for (String s : urls) {
                try {
                    String html = HtmlUtil.getRawText(s);
                    //TODO: ensure we do not add empty lines!
                    htmlList.add(html);
                } catch (Exception e){
                    logger.error("Unable to fetch url:" + s + "\n" +
                            e.getMessage());
                }
            }
        }
        return htmlList;
    }

    private static void saveToFile(String html) {
        String hashTextName = Integer.toString(html.hashCode());
        File file = new File("data/urls2/fetched_pages/" + hashTextName + ".txt");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(html.getBytes());
            fos.close();
        } catch(Exception e){
            logger.error("unable to create new file for " + e.getMessage());
        }
    }

    public static String getRawText(String urlToRead) throws Exception {
        try{ //try to get saved html from file
            FileInputStream inputStream = new FileInputStream(
                    "data/urls2/fetched_pages/" +
                            Integer.toString(urlToRead.hashCode()) + ".txt");
             return IOUtils.toString(inputStream);
        }catch (Exception e){ //fetch url
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            String html = Jsoup.parse(result.toString()).text();
            try {
                saveToFile(html);
            }catch (Exception ioe) {
                logger.error("Unable to save html to file. " +
                                              ioe.getMessage());
            }
            return html;
        }

    }

    public static String getTitles(String s)throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(s);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        Document doc = Jsoup.parse(result.toString());
        Element link = doc.select("a").first();

        return doc.title();
    }

    public static List<String> getCleanedUrlsByMimeType(String urlsFromDB){
        List<String> cleanedUrls = Collections.synchronizedList(new ArrayList<>());
        if(urlsFromDB != null) {
            String[] urls = urlsFromDB.split("\\|");

            for (String u : urls) {
                try {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(u);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("HEAD");
                                connection.setConnectTimeout(5000);
                                connection.connect();
                                String contentType = connection.getContentType();
                                contentType = contentType.trim();
                                if (contentType.contains("text/html") ||
                                        contentType.contains("text/plain")) {
                                    cleanedUrls.add(u);
                                    nValidUrls++;
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                    t.start();
                    t.join();
                } catch (Exception e) {
                    logger.error("test:unable to fetch head " + e.getMessage());
                }
            }
        }
        return cleanedUrls;
    }

    public static void fetchAndSaveUrls(){
        MongoClient mongoClient = MongoUtil.getOrCreateMongoClient();
        MongoDatabase mongoDatabase = MongoUtil.getDatabase("morphia_test");
        Morphia morphia = new Morphia();
        morphia.map(Quad.class);
        Datastore quadDataStore = morphia.createDatastore(mongoClient, "morphia_test");

        Query<Quad> queryQuad = quadDataStore
                .createQuery(Quad.class)
                .filter("urls exists", true);
        List<Quad> quadList = queryQuad.asList();
        int size = quadList.size();
        int nUrlsDeleted = 0;

        for (int i = 0; i < size; i++) {
            Quad q = quadList.get(i);
            for(String u :  q.getUrls()) {
                int nUrls = q.getUrls().size();
                List<String> cleanedUrls = new LinkedList<>();
                try {
                    URL url = new URL(u);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);
                    connection.connect();
                    String contentType = connection.getContentType();
                    if ( contentType.equals("text/html") ||
                            contentType.equals("text/plain")){
                        cleanedUrls.add(u);
                    }
                }catch (Exception e){
                    logger.error("test:unable to fetch head " + e.getMessage());
                }

                q.setUrls(cleanedUrls);
                try {
                    quadDataStore.save(q);
                }catch (Exception e){logger.error("unable to save " + e.getMessage());}
                nUrlsDeleted += nUrls - cleanedUrls.size();
            }

            if(i % 1000 == 0){
                logger.info("Processed " + Integer.toString(i) + " quads.");
            }
        }
        logger.info("Deleted urls:" + Integer.toString(nUrlsDeleted));

    }

    public static int getnValidUrls() {
        return nValidUrls;
    }
}
