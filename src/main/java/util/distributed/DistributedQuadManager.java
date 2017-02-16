/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.distributed;

import com.mongodb.spark.MongoSpark;
import detection.Location;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.*;
import scala.Tuple2;
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
                    "SELECT qId,urls from smallestQuads WHERE urls IS NOT NULL");
            JavaRDD<Row> quadsRdd = smallestQuads.toJavaRDD();
//            smallestQuads.show();
            Dataset<Row> rdd = smallestQuads.map(
                    new MapFunction<Row, Row>(){
                @Override
                public Row call(Row value) throws Exception {
                    List<String> urls = value.getList(1);
                    Hashtable<String, Integer> topicStats = LDATopicDetector
                            .getTopicStatsByUrls(urls, HtmlUtil.PAGE_TYPE.URL_LOCATION);
                    logger.info(topicStats);
                    return RowFactory.create(value.get(0), value.get(1), topicStats.toString());
                }
            }, Encoders.kryo(Row.class));
            rdd.collect(); // replace with reduce later ?!
            System.out.println("test");
            //save to mongodb

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("computeTopicStatsSmallestQuads error " + e.getMessage());
        }

        logger.info("computeTopicStatsSmallestQuads finished");
    }
}
