/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.sequential;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import org.apache.log4j.Logger;

public class NaiveTopicDetector {
    private static final Logger logger = Logger.getLogger(NaiveTopicDetector.class);
    private static final int NUM_TOPICS = 5;
    /**
     *  Compute topic statistics for all urls in the list.
     *  @param urls: Each element contains a url.
     */
    public static Hashtable<String, Integer> getTopicStatsByUrls(List<String> urls) throws Exception {
        // curl url's body
        // compute stats
        Hashtable<String, Integer> stats = new Hashtable<>();
        List<String> htmlList = new LinkedList<>();
        for (String s : urls) {
            try {
                String html = getRawText(s);
                //TODO: ensure we do not add empty lines!
                htmlList.add(html);

            }catch (Exception e){
                logger.error("Unable to fetch url:" + s + "\n" + e.getMessage());
            }
        }
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplist-en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        instances.addThruPipe(new ArrayIterator(htmlList));

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.

        ParallelTopicModel model = new ParallelTopicModel(NUM_TOPICS, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(1);

        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.estimate();

        model.displayTopWords(5, true);
        return null;
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
//    @Override
//    public List<String> getTopics(Location topleft, Location bottomright, int S) {
//        if (quadManager == null) {
//            quadManager = new QuadManagerImpl();
////        quadManager.partitionMapIntoQuads();
////        quadManager.partitionUrls();
//        }
//        /* ****************************************
//         * How to optimize this code? the logic is
//         * separated from db transactions, but performance go down
//         ****************************************/
//        for(Quad q: quadManager.getListSmallestQuads()){
//            List<String> topics = getTopicStatsByQuad(q);
//            quadManager.saveTopics(q, topics);
//
//        }
//
//        return topics;
//    }

}
