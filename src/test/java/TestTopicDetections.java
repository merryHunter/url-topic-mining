import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import detection.Location;
import detection.QuadManagerImpl;
import org.apache.log4j.Logger;
import smile.data.SparseDataset;
import util.HtmlUtil;
import util.sequential.BagOfWordsTopicDetector;
import util.sequential.CountVectorizer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;

import com.mongodb.spark.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import util.sequential.LDATopicDetector;

/**
 * @author Ivan Chernukha on 12.02.17.
 */

public class TestTopicDetections {
    private static final Logger logger = Logger.getLogger(TestTopicDetections.class);
    @Test
    public void onTestSparkMongodbConnector() {
        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("MongoSparkConnectorIntro")
                .config("spark.mongodb.input.uri", "mongodb://127.0.0.1/morphia_test.quad")
                .config("spark.mongodb.output.uri", "mongodb://127.0.0.1/morphia_test.quad")
                .getOrCreate();

        // Create a JavaSparkContext using the SparkSession's SparkContext object
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        /*Start Example: Read data from MongoDB************************/
        JavaMongoRDD<Document> rdd = MongoSpark.load(jsc);
        /*End Example**************************************************/

        // Analyze data from MongoDB
        System.out.println(rdd.count());
        System.out.println(rdd.first().toJson());

        jsc.close();
    }


    @Test
    public void onTestPrecomputingGetTopics(){
        QuadManagerImpl quadManager = new QuadManagerImpl( new LDATopicDetector());
//        QuadManagerImpl quadManager = new QuadManagerImpl( new BagOfWordsTopicDetector());
        long start = System.nanoTime();
        quadManager.partitionMapIntoQuads(
                new Location(47.185257, 8.206737), new Location(0.0,0.0), 2);
        long partitionTime = System.nanoTime() - start;
        start = System.nanoTime();
        quadManager.partitionUrls();
        long urlsTime = System.nanoTime() - start;
        start = System.nanoTime();
        quadManager.computeTopicStatsSmallestQuads();
        long smallest = System.nanoTime() - start;
        logger.info("Time partitionMapIntoQuad qSide==2048: " + partitionTime);
        logger.info("Time partitionUrls: " + urlsTime);//TODO:compare urls with synchronous?
        logger.info("Time computeTopicsSmallest: " + smallest);
    }

    @Test
    public void onTestGetTopics(){
        QuadManagerImpl quadManager = new QuadManagerImpl(new LDATopicDetector());
//        quadManager.computeTopicStatsSmallestQuads();

        quadManager.displayTopics(new Location(47.185257, 8.206737),
                new Location(43.171934, 18.449864), 150, "custom");
    }

    @Test
    public void onTestPrecomputingGetTopicsByRerun(){
        QuadManagerImpl quadManager = new QuadManagerImpl(new LDATopicDetector());
        quadManager.partitionMapIntoQuads(
                new Location(47.185257, 8.206737), new Location(0.0,0.0), 2);
        quadManager.partitionUrls();
    }

    @Test
    public void onTestGetTopicsByRerun(){
        QuadManagerImpl quadManager = new QuadManagerImpl(new LDATopicDetector());
        quadManager.displayTopics(new Location(46.064322, 11.123587),
                new Location(43.171934, 18.449864), 570, "rerun");
    }

    @Test
    public void onTestMalletTopicDetection() throws IOException {

        List<String> htmlList = new LinkedList<>();
        String[] urls = {        "https://docs.mongodb.com/manual/reference/mongo-shell/"};
        for (String s : urls) {
            try {
//                String html = LDATopicDetector.getRawText(s);
                String html = HtmlUtil.getRawText(s);
                //TODO: ensure we do not add empty lines!
//                htmlList.add(html);
            }catch (Exception e){
            }
        }
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        String text = "\n" +
                "\n" +
                "Donald Trump’s first budget proposal will spare big social welfare programs such as social security and Medicare from cuts, the treasury secretary, Steven Mnuchin, said in an interview broadcast on Sunday.\n" +
                "Analysis CPAC conservatives drink the Trump Kool-Aid, but who will pick up the tab?\n" +
                "A once-fringe presidential candidate has turned his doubters into cheerleaders. But the spiritual and financial cost of Trumpism has yet to be tallied\n" +
                "Read more\n" +
                "\n" +
                "Mnuchin said Trump would use his first address to Congress on Tuesday night to preview some elements of his sweeping plans to cut taxes for the middle class, simplify the tax system and make American companies more globally competitive with lower rates and changes to encourage manufacturing.\n" +
                "\n" +
                "Speaking on Fox News’ Sunday Morning Futures, Mnuchin, who has acknowledged that tax reform is his top policy priority, said the budget plan would not seek cuts to federal benefits programs known as “entitlements”.\n" +
                "\n" +
                "“We are not touching those now. So don’t expect to see that as part of this budget, OK,” Mnuchin said of the programs. “We are very focused on other aspects and that’s what’s very important to us. And that’s the president’s priority.”\n" +
                "\n" +
                "During the election campaign Trump promised not to cut social security, Medicare healthcare for seniors or Medicaid healthcare for the poor. Preservation of these programs, coupled with a middle-class tax cut, would aid the retirees and working-class Americans who make up a significant portion of Trump’s political base.\n" +
                "\n" +
                "Mnuchin said Trump “will be touching on tax reform” as part of his first state of the union speech to Congress.\n" +
                "\n" +
                "The plan will reduce the number of tax brackets for individuals and offer a “middle-income tax cut”, Mnuchin said. On the business side, Trump wants to “create a level playing field for US companies to be able to compete in the world”.\n" +
                "\n" +
                "Mnuchin said Trump was looking at a “reciprocal tax” that would help create more parity with other countries. Trump administration officials have complained that many countries charge value-added taxes on imports while exempting exports from taxation. The US mainly taxes corporate income.\n" +
                "\n" +
                "But Mnuchin again said he was only studying a House Republican border tax adjustment plan that would levy a 20% tax on imports to encourage more US-based production and exports. That plan aims to raise more than $1tn over a decade to offset lower tax rates for businesses.\n" +
                "White House plan to hire more border agents raises vetting fear, ex-senior official says\n" +
                "Read more\n" +
                "\n" +
                "“So let me just say this is something we are studying very carefully,” Mnuchin said. “There are certain aspects that the president likes about the concept of a border-adjusted tax, there are certain aspects that he’s very concerned about.”\n" +
                "\n" +
                "He added that the Trump administration would work with the House of Representatives and Senate to craft “a combined plan that takes the best of all of this when we bring it forward”.\n" +
                "\n" +
                "In a comment suggesting that Trump’s budget and tax plan may use aggressive revenue assumptions, Mnuchin said the administration “fundamentally believes in dynamic scoring” – a budget calculation method that assumes that a lower tax burden boosts revenues by encouraging economic activity.\n" +
                "\n" +
                "The Congressional Budget Office has previously used mainly “static” scoring methods that assume very conservative economic effects of budget and taxes.\n" +
                "\n" +
                "“If we make business taxes more competitive, people will do more business here and we’ll get more revenues,” Mnuchin said. “So although there may be an absolute lower rate, that doesn’t necessarily mean it’s a corresponding drop in revenues.”\n" +
                "Since you’re here …\n" +
                "\n" +
                "… we’ve got a small favour to ask. More people are reading the Guardian than ever, but far fewer are paying for it. Advertising revenues across the media are falling fast. And unlike some other news organisations, we haven’t put up a paywall – we want to keep our journalism open to all. So you can see why we need to ask for your help. The Guardian’s independent, investigative journalism takes a lot of time, money and hard work to produce. But we do it because we believe our perspective matters – because it might well be your perspective, too.\n" +
                "\n" +
                "If everyone who reads our reporting, who likes it, helps to support it, our future would be much more secure.";
        htmlList.add(text);
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
