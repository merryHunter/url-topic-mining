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
     * Minimum number of topics to detect.
     */
    private static final int MIN_NUM_TOPICS = 2;


    private static final int NUM_WORDS = 5;

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

        int num_topics = getNumTopics(htmlList.size());
        logger.info("URLS length:" + Integer.toString(htmlList.size()));
        logger.info("Topicsnumber: " + Integer.toString(num_topics));

        ParallelTopicModel model = new ParallelTopicModel(num_topics, 1.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(1);
        model.setNumIterations(50);
        model.estimate();

        // compute stats
        Hashtable<String, Integer> topicsStats = new Hashtable<>();
        String topWords = model.displayTopWords(NUM_WORDS, true);
        Matcher m = Pattern.compile("\\n(([a-z]*)\\t)").matcher(topWords);
        Matcher counts = Pattern.compile("\\t(([0-9]*)\\n)").matcher(topWords);
        try {
            while (m.find() && counts.find()) {
                topicsStats.put(m.group(1).trim(),
                        Integer.parseInt(counts.group(1).trim()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        logger.info(topicsStats);
        return topicsStats;
    }

    private static int getNumTopics(int length){
        int num_topics = 0;
        if ( length >= 3000 ){
            while(length >= 10) length /=10;
            num_topics = 15  + length;
        } else if ( length >= 100){
            while(length >= 10) length /=10;
            num_topics = length + MIN_NUM_TOPICS;
        }else if( length >= 10){
            while(length >= 10) length /=10;
            num_topics = length + MIN_NUM_TOPICS;
        } else{
            num_topics = MIN_NUM_TOPICS;
        }
        return num_topics;
    }
}
