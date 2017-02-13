/**
 * @author Ivan Chernukha on 13.02.17.
 */
package util;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {

    private static final Logger logger = Logger.getLogger(HtmlUtil.class);

    public static enum PAGE_TYPE{
        TITLE,
        BODY,
        URL_LOCATION
    }
    public static String getUrlLocationTokenized(String url){
        Matcher m = Pattern.compile("([a-z]*)").matcher(url);
        List<String> words = new LinkedList<>();
        try {
            while(m.find()) {
                String temp = m.group(1).trim();
                if (!temp.equals(""))
                    words.add(temp);
            }
            //remove extension - may be "" and thus throw exception
            words.remove(words.size() - 1);
        }catch (Exception e){
            logger.error("Unable to get tokenized topics from url: " + url);
            return "";
        }
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
                }catch (Exception e){
                    logger.error("Unable to fetch url:" + s + "\n" +
                            e.getMessage());
                }
            }
        }
        return htmlList;
    }

    public static String getRawText(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return Jsoup.parse(result.toString()).text();
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
}
