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
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.*;
import org.bson.Document;
import scala.Function1;
import scala.Tuple2;
import scala.util.parsing.json.JSONObject;
import util.HtmlUtil;
import util.sequential.LDATopicDetector;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class DistributedQuadManager {

    static final Logger logger = Logger.getLogger(DistributedQuadManager.class);

    private static Dataset<Row> dsQuad;

    private static SparkSession sparkSession;

    private static JavaSparkContext jsc;

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
            Dataset<Row> smallestQuads = sparkSession.sql(
                    "SELECT * from smallestQuads WHERE urls IS NOT NULL");
            Dataset<Row> computedDS = testDataset(smallestQuads);
//            JavaRDD<Row> computedDS = testRdd(smallestQuads);
            computedDS.collect(); // replace with reduce later ?!
//            computedDS.(5);
            System.out.println("test");
            //save to mongodb
            MongoSpark.save(computedDS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("computeTopicStatsSmallestQuads error " + e.getMessage());
        }

        logger.info("computeTopicStatsSmallestQuads finished");
    }

    private static Dataset<Row> testDataset(Dataset<Row> smallestQuads){
        Dataset<Row> computedDS = smallestQuads.map(
                new MapFunction<Row, Row>(){
                    @Override
                    public Row call(Row row) throws Exception {
                        List<String> urls =  row.getList(row.size() - 1);
                        System.out.println(urls);
                        Hashtable<String, Integer> topicStats = LDATopicDetector
                                .getTopicStatsByUrls(urls, HtmlUtil.PAGE_TYPE.URL_LOCATION);
                        logger.info(topicStats);
                        System.out.println(row.getLong(4));//qId
                        System.out.println(row.getLong(1));//
                        System.out.println(row.getInt(5));//qSide
                        List<String> topleft = row.getList(3);
                        Quad temp = new Quad();
                        String json = new Gson().toJson(temp);
//                        return Document.parse(json);
                        return RowFactory.create(row, topicStats);
                    }
                }, Encoders.kryo(Row.class));
        return computedDS;
    }

    private static JavaRDD<Document> testRdd(Dataset<Row> smallestQuads){
        JavaRDD<Document> computedDS = smallestQuads.toJavaRDD().map(
                new Function<Row, Document>(){
                    @Override
                    public Document call(Row row) throws Exception {
                        List<String> urls =  row.getList(row.size() - 1);
                        Hashtable<String, Integer> topicStats = LDATopicDetector
                                .getTopicStatsByUrls(urls, HtmlUtil.PAGE_TYPE.URL_LOCATION);
//                    List<String> topleft = row.getList(3);
//                    Quad temp = new Quad();
                    String json = new Gson().toJson(urls);
                        return Document.parse(json);
                    }
                });
        return computedDS;
    }
}
