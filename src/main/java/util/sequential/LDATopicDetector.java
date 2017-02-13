/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.sequential;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import org.apache.log4j.Logger;
import util.HtmlUtil;

public class LDATopicDetector {
    private static final Logger logger = Logger.getLogger(LDATopicDetector.class);

    /**
     * Number of topics to detect.
     */
    private static final int NUM_TOPICS = 2;

    /**
     *  Compute topic statistics for all urls in the list.
     *  @param urls: Each element contains a url.
     *  @param page_type: fast processing depending on page titles,
     *              depending on page body or url location itself.
     */
    public static Hashtable<String, Integer> getTopicStatsByUrls(
            List<String> urls, HtmlUtil.PAGE_TYPE page_type) throws Exception {
        // curl url's
        List<String> htmlList = null;
        if (page_type == HtmlUtil.PAGE_TYPE.URL_LOCATION){
            htmlList = HtmlUtil
                    .getHtmlPages(urls, HtmlUtil.PAGE_TYPE.URL_LOCATION);
        }
        else if (page_type == HtmlUtil.PAGE_TYPE.TITLE) {
            htmlList = HtmlUtil
                    .getHtmlPages(urls, HtmlUtil.PAGE_TYPE.TITLE);
        } else{
            htmlList = HtmlUtil
                    .getHtmlPages(urls, HtmlUtil.PAGE_TYPE.BODY);
        }
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(
                Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(
                new File("stoplist-en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        instances.addThruPipe(new ArrayIterator(htmlList));

//        int numTopics = htmlList.size();
        //TODO: compute NUM_TOPICS depending on the number of urls!
        ParallelTopicModel model = new ParallelTopicModel(NUM_TOPICS, 1.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(1);
        model.setNumIterations(50);
        model.estimate();

        // compute stats
        String topWords = model.displayTopWords(5, true);
        Matcher m = Pattern.compile("\\n(([a-z]*)\\t)").matcher(topWords);
        Hashtable<String, Integer> topicsStats = new Hashtable<>();
        while(m.find()) {
            //TODO: extract topic count from topWords!
            topicsStats.put(m.group(1).trim(), 1);
        }
        logger.info(topWords);
        return topicsStats;
    }

}
