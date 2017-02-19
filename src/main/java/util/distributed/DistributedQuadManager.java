/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.distributed;

import com.google.gson.Gson;
import com.mongodb.spark.MongoSpark;
import detection.Location;
import detection.Quad;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.bson.Document;
import util.HtmlUtil;
import util.sequential.LDATopicDetector;

import java.util.Hashtable;
import java.util.List;

public class DistributedQuadManager {

    static final Logger logger = Logger.getLogger(DistributedQuadManager.class);

    private static Dataset<Row> dsQuad;

    private static SparkSession sparkSession;

    private static JavaSparkContext jsc;

    private static JavaRDD<Row> smallestQuads;

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            jsc = createJavaSparkContext();
            sparkSession = SparkSession.builder().getOrCreate();
            dsQuad = MongoSpark.load(jsc).toDF();
//            if (args[0].equals("true")){
                computeTopicStatsSmallestQuads();
//            }

            //TODO: select quads inside given area
//            computeAllTopics();

            //TODO: aggregate stats by mongodb/spark

            jsc.close();
        } catch (Exception e){
            logger.error("Failed " + e.getMessage());
        }
    }


    private static JavaSparkContext createJavaSparkContext() {
        String uri = getMongoClientURI();
        SparkConf conf = new SparkConf()
                .setMaster("local")
                .setAppName("UrlMining")
                .set("spark.mongodb.input.uri", uri)
                .set("spark.mongodb.output.uri", uri);

        return new JavaSparkContext(conf);
    }


    private static String getMongoClientURI() {
        String uri;
        uri = "mongodb://localhost/test.quad"; // default
        return uri;
    }


    public Hashtable<Long, String> getTopics(Location topleft, double distanceToBottomRight, int S) {
//        JavaMongoRDD<Document> aggregatedRdd = rddQuads.withPipeline()
        return null;
    }


    public static void computeTopicStatsSmallestQuads() {
        logger.info("computeTopicStatsSmallestQuads started");
        try {
            dsQuad.createOrReplaceTempView("smallestQuads");
            Dataset<Row> smallestQuadsDS = sparkSession.sql(
                    "SELECT * from smallestQuads WHERE urls IS NOT NULL");
            smallestQuadsDS.head(2);
            smallestQuads = getRddWithTopics(smallestQuadsDS);

//            smallestQuads.collect();
//            smallestQuads.collect(); // replace with reduce later ?!
            //save to mongodb
//            MongoSpark.
//            MongoSpark.save(smallestQuads); // TODO:convert RDD<Row> to RDD<Document>
//            .option("collection", "allquads").mode("overwrite").save();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("computeTopicStatsSmallestQuads error " + e.getMessage());
        }

        logger.info("computeTopicStatsSmallestQuads finished");
    }

    private static void computeAllTopics() {
        logger.info("computeAllTopics started");
        try {
            dsQuad.createOrReplaceTempView("quadsByLevel");
            for (int i = 32; i < 2048; i *= 2 ) {
                Dataset<Row> quadsDS = sparkSession.sql(
                        "SELECT * from quadsByLevel WHERE qSide <= " + Integer.toString(i));
                JavaRDD<Row> computed = getStatsByMapReduce(quadsDS);
                computed.collect();


            }
            // if current quad side is the minimal one, so no quads inside this

        }catch (Exception e){
            logger.error("computeAllTopics error " + e.getMessage());
        }
        logger.info("computeAllTopics finished");
    }


    private static JavaRDD<Row> getRddWithTopics(Dataset<Row> smallestQuads){
        JavaRDD<Row> computedDS = smallestQuads.toJavaRDD().map(
                new Function<Row, Row>(){
                    @Override
                    public Row call(Row row) throws Exception {
                        List<String> urls =  row.getList(row.size() - 1);
                        Hashtable<String, Integer> topicStats = LDATopicDetector
                                .getTopicStatsByUrls(urls, HtmlUtil.PAGE_TYPE.BODY);
                        String json = new Gson().toJson(topicStats);

//                        Document doc = Document.parse(json);
//                        doc.append("qId", row.getLong(4));// qId
//                        doc.append("stats", topicStats);
                        Row r =  RowFactory.create(row.getLong(4), topicStats); //4 поле - qId
                        return r;
                    }
                });
        computedDS.collect();
        return computedDS;
    }

    private static JavaRDD<Row> getStatsByMapReduce(Dataset<Row> smallestQuads){
        JavaRDD<Row> computedDS = smallestQuads.toJavaRDD().map(
                new Function<Row, Row>(){
                    @Override
                    public Row call(Row row) throws Exception {
                        List<String> urls =  row.getList(row.size() - 1);
                        Hashtable<String, Integer> topicStats = LDATopicDetector
                                .getTopicStatsByUrls(urls, HtmlUtil.PAGE_TYPE.BODY);
                        String json = new Gson().toJson(topicStats);

//                        Document doc = Document.parse(json);
//                        doc.append("qId", row.getLong(3));// 3?
//                        doc.append("stats", topicStats);
                        Row r =  RowFactory.create(row.getLong(4), topicStats);
                        return r;
                    }
                });
        return computedDS;
    }
}
