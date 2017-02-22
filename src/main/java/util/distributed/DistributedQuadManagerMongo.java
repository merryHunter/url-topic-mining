/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.distributed;

import com.google.gson.Gson;
import com.mongodb.spark.MongoSpark;
import detection.Location;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.*;
import scala.Tuple2;
import util.HtmlUtil;
import util.MongoUtil;
import util.sequential.LDATopicDetector;

import java.util.*;

public class DistributedQuadManagerMongo {

    static final Logger logger = Logger.getLogger(DistributedQuadManagerMongo.class);

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
            logger.info("DistributedQuadManagerMongo started!");
            jsc = createJavaSparkContext();
            logger.info("SPARK conf created successfully!");

            sparkSession = SparkSession.builder().getOrCreate();
            logger.info("SPARKsession has been built successfully!");
//
            dsQuad = MongoSpark.load(jsc).toDF();

            logger.info("SPARK quads from mongodb retrieved successfully!");

//            if (args[0].equals("true")){
                computeTopicStatsSmallestQuads();
//            }

            //TODO: select quads inside given area
//            JavaRDD<Tuple2<String,Integer>> computed = getStatsByMapReduce(smallestQuads);
//            List<Tuple2<String, Integer>> compList = computed.collect();
            computeAllTopics();

            //TODO: aggregate stats by mongodb/spark

            jsc.close();
        } catch (Exception e){
            logger.error("Failed " + e.getMessage());
        }
    }


    private static JavaSparkContext createJavaSparkContext() {
        String uri = getMongoClientURI();
        SparkConf conf = new SparkConf()
                .setMaster("spark://" + MongoUtil.HOST +":7077")
                .setAppName("UrlMining")
                .set("spark.mongodb.input.uri", uri)
                .set("spark.mongodb.output.uri", uri);
        return new JavaSparkContext(conf);
    }


    private static String getMongoClientURI() {
        String uri;
        uri = "mongodb://"+ MongoUtil.HOST + "/test.quad"; // default
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
            smallestQuads.collect();
            logger.info("SUCCESS");
            //save to mongodb
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
//            for (int i = Quad.QUAD_SIDE_MIN; i < 2048; i *= 2 ) {
//                int j = i * 2;
//                Dataset<Row> quadsOnSameLevel = sparkSession.sql(
//                        "SELECT * from quadsByLevel WHERE qSide = " + Integer.toString(j));
//                JavaRDD<Tuple2<String,Integer>> computed = getStatsByMapReduce(smallestQuads);
                List<Tuple2<String, Integer>> computed = getStatsByMapReduce(smallestQuads);

//                List<Tuple2<String, Integer>> compList = computed.collect();
//

//            }

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
                                .getTopicStatsByUrls(urls, HtmlUtil.PAGE_TYPE.URL_LOCATION);
                        String json = new Gson().toJson(topicStats);

//                        Document doc = Document.parse(json);
//                        doc.append("qId", row.getLong(4));// qId
//                        doc.append("stats", topicStats);
                        Row r =  RowFactory.create(row.getLong(4), topicStats); //4 поле - qId
                        return r;
                    }
                });
        return computedDS;
    }

    /**
     * Compute stats for all quads in quads by map reducing
     */
    private static List<Tuple2<String, Integer>> getStatsByMapReduce(JavaRDD<Row> quads) {
        JavaPairRDD<String,Integer>  pairs = quads.flatMapToPair(new PairFlatMapFunction<Row, String, Integer>() {
            @Override
            public Iterator<Tuple2<String, Integer>> call(Row row) throws Exception {
                HashMap<String,Integer> hashMap = (HashMap<String,Integer>)row.get(1);
                Tuple2<String,Integer>[] tupleStats = new Tuple2[hashMap.size()];
                int i = 0;
                for(Map.Entry e: hashMap.entrySet()){
                    Tuple2<String,Integer> t = new Tuple2<>(
                            (String)e.getKey(),(Integer)e.getValue());
                    tupleStats[i] = t;
                    i++;
                }

                return Arrays.asList(tupleStats).iterator();
            }
        });
        JavaPairRDD<String, Integer> counts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        List<Tuple2<String, Integer>> output = counts.collect();
        return output;
    }

}
