import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import detection.Location;
import detection.QuadManagerImpl;
import smile.data.SparseDataset;
import util.HtmlUtil;
import util.sequential.CountVectorizer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ivan Chernukha on 12.02.17.
 */

public class TestTopicDetections {

    @Test
    public void onTestSequential(){
//        ITopicDetector topicDetector = new LDATopicDetector();
//        topicDetector.getTopics(new Location(47.185257, 8.206737), new Location(0.0,0.0), 2);
        QuadManagerImpl quadManager = new QuadManagerImpl();
        quadManager.partitionMapIntoQuads(
                new Location(47.185257, 8.206737), new Location(0.0,0.0), 2);
        quadManager.partitionUrls();
        quadManager.computeTopicStatsSmallestQuads();
    }

    @Test
    public void onTestGetTopics(){
        QuadManagerImpl quadManager = new QuadManagerImpl();
        quadManager.getTopics(new Location(46.064322, 11.123587), new Location(0.0,0.0), 44);
    }

    @Test
    public void onTestMalletTopicDetection() throws IOException {

        List<String> htmlList = new LinkedList<>();
        String[] urls = {"https://github.com/mimno/Mallet",
        "https://docs.mongodb.com/manual/reference/mongo-shell/"};
        for (String s : urls) {
            try {
//                String html = LDATopicDetector.getRawText(s);
                String html = HtmlUtil.getTitles(s);
                //TODO: ensure we do not add empty lines!
                htmlList.add(html);
            }catch (Exception e){
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
//        int numTopics = htmlList.size() != 0 ? htmlList.size()/5 : 5;
        ParallelTopicModel model = new ParallelTopicModel(2, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(1);

        // Run the model for 50 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.estimate();

        String topWords = model.displayTopWords(5, true);
        Matcher m = Pattern.compile("\\n(([a-z]*)\\t)").matcher(topWords);
        List<String> topicsList = new LinkedList<>();
        while(m.find()) {
            topicsList.add(m.group(1).trim());
        }
        System.out.println(topicsList);
    }

    @Test
    public void onTestMalletTopicPerformance() throws IOException{

    }

    @Test
    public void onTestBoWTopicDetection()throws Exception{
        String[] aurls = {"https://github.com/mimno/Mallet",
                "https://docs.mongodb.com/manual/reference/mongo-shell/",};
        List<String> htmlList = new LinkedList<>();

        for (String s : aurls) {
            try {
                String html = HtmlUtil.getTitles(s);
                //TODO: ensure we do not add empty lines!
                htmlList.add(html);
            }catch (Exception e){
//                logger.error("Unable to fetch url:" + s + "\n" + e.getMessage());
                System.out.println("error on fetching urls");
            }
        }
        CountVectorizer countVectorizer = new CountVectorizer(2, true, true, false);
        SparseDataset sparseDataset = countVectorizer.fitTransform(htmlList);
        Hashtable<String, Integer> stats = countVectorizer.getTokenToIndex();
        ArrayList<Object> maxKeys= new ArrayList<Object>();
        Integer maxValue = Integer.MIN_VALUE;
        for(Map.Entry<String,Integer> entry : stats.entrySet()) {
            if(entry.getValue() > maxValue) {
                maxKeys.clear(); /* New max remove all current keys */
                maxKeys.add(entry.getKey());
                maxValue = entry.getValue();
            }
            else if(entry.getValue() == maxValue)
            {
                maxKeys.add(entry.getKey());
            }
        }
        System.out.println("ok");
    }
}
