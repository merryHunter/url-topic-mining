/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.sequential;


import detection.ITopicDetector;
import util.HtmlUtil;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

//@Deprecated
public class BagOfWordsTopicDetector  implements ITopicDetector {

    @Override
    public Hashtable<String, Integer> getTopicStatsByUrls(List<String> urls, HtmlUtil.PAGE_TYPE page_type) throws Exception {
        List<String> htmlList = HtmlUtil.getHtmlPages(urls, HtmlUtil.PAGE_TYPE.BODY);
        CountVectorizer countVectorizer = CountVectorizer.withDefaultSettings();
        countVectorizer.fitTransform(htmlList);

        return countVectorizer.getTokenToIndex();
    }
}
