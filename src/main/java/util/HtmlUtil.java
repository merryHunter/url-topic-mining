/**
 * @author Ivan Chernukha on 13.02.17.
 */
package util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import detection.Quad;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.util.*;

import org.asynchttpclient.*;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {

    private static final Logger logger = Logger.getLogger(HtmlUtil.class);

    private static final int CONNECTION_TIMEOUT = 5000;  //5s
    private static final int MIN_HTML_LENGTH = 140; // like in Twitter
    private static final String DIRECTORY = "data/urls2/fetched_pages/";

    private static AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();

    public static enum PAGE_TYPE{
        BODY,
        URL_LOCATION
    }

    private static volatile int nValidUrls;

    public static List<String> getListSavedUrls(String urls) {
        List<String> savedUrls = new LinkedList<>();
        for (String s: Arrays.asList(urls.split("\\|"))){
            if (new File(DIRECTORY, Integer.toString(s.hashCode()) + ".txt")
                                                                    .exists()){
                savedUrls.add(s);
            }
        }
        return savedUrls;
    }

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
                if ( !html.equals("")) {
                    htmlList.add(html);
                }
            }
        } else if (page_type == PAGE_TYPE.BODY){
            for (String s : urls) {
                try {
                    String html = HtmlUtil.readHtmlFromSavedFile(s);
                    if ( html != null){
                        htmlList.add(html);
                    }
                } catch (Exception e){
                    logger.error("Unable to fetch url:" + s + "\n" +
                            e.getMessage());
                }
            }
        }
        return htmlList;
    }

    private static String readHtmlFromSavedFile(String url) {
        String html = null;
        try{
            FileInputStream inputStream = new FileInputStream(DIRECTORY
                    + Integer.toString(url.hashCode()) + ".txt");
            html = IOUtils.toString(inputStream);
        } catch (Exception e){
            logger.error("Could not open file with url: " + url);
        }
        return html.toLowerCase();
    }

    private static void saveToFile(String html, String url) {
        String hashTextName = Integer.toString(url.hashCode());
        File file = new File(DIRECTORY + hashTextName + ".txt");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(html.getBytes());
            fos.close();
        } catch(Exception e){
            logger.error("unable to create new file for " + e.getMessage());
        }
    }

    @Deprecated
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
                saveToFile(html, urlToRead);
            }catch (Exception ioe) {
                logger.error("Unable to save html to file. " +
                                              ioe.getMessage());
            }
            return html;
        }
    }

    @Deprecated
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

    public static void preprocessURLsToFindHtmlPages(final List<String> urls) {
        for(String url : urls) {
            if (urlLocationValid(url)) {
                try {
                    asyncHttpClient.prepareHead(url)
                            .setRequestTimeout(CONNECTION_TIMEOUT)
                            .execute(new AsyncCompletionHandler<Response>() {
                                @Override
                                public Response onCompleted(Response response) throws Exception {
                                    String contentType = response.getHeader("Content-Type");
                                    if (contentType.equalsIgnoreCase("text/html")) {
                                        downloadHtmlPage(response.getUri().toUrl());
                                    }
                                    return response;
                                }

                                @Override
                                public void onThrowable(Throwable t) {
                                    //TODO: log that smth wrong happened
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    private static boolean urlLocationValid(String url) {
        if (new File(DIRECTORY, Integer.toString(url.hashCode()) + ".txt")
                .exists()) {
            return false;
        }
        if ( url.length() < 15 ){
            return false;
        }
        if (url.endsWith(".jpg") || url.endsWith(".png") ||
                url.endsWith(".gif")  || url.endsWith(".json") || url.endsWith(".xml") ){
            return false;
        }
        return true;
    }

    private static void downloadHtmlPage(final String url) {
        asyncHttpClient
                .prepareGet(url)
                .setRequestTimeout(CONNECTION_TIMEOUT)
                .execute(new AsyncCompletionHandler<Response>(){
            @Override
            public Response onCompleted(Response response) throws Exception{
                String htmlPage = response.getResponseBody();
                htmlPage = Jsoup.parse(htmlPage).text();
                if(htmlPage.length() > MIN_HTML_LENGTH) {
                    saveToFile(htmlPage, url);
                }
                return response;
            }
            @Override
            public void onThrowable(Throwable t){
                //TODO: log that smth wrong happened
            }
        });
    }

    public static int getnValidUrls() {
        return nValidUrls;
    }
}
