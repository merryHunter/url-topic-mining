/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.sequential;


import util.HtmlUtil;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;


public class BagOfWordsTopicDetector {
    public static Hashtable<String, Integer> getTopicStatsByUrls(List<String> urls) throws Exception{
        List<String> htmlList = new LinkedList<>();

        for (String s : urls) {
            try {
                String html = HtmlUtil.getTitles(s);
                //TODO: ensure we do not add empty lines!
                htmlList.add(html);
            }catch (Exception e){
//                logger.error("Unable to fetch url:" + s + "\n" + e.getMessage());
                System.out.println("error on fetching urls");
            }
        }
        CountVectorizer countVectorizer = CountVectorizer.withDefaultSettings();
        countVectorizer.fitTransform(htmlList);
        return countVectorizer.getTokenToIndex();
    }
}
