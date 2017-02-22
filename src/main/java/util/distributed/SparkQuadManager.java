/**
 * @author Ivan Chernukha on 22.02.17.
 */
package util.distributed;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import detection.Quad;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.SparkSession;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import scala.Tuple2;
import util.HtmlUtil;
import util.MongoUtil;
import util.sequential.LDATopicDetector;

import java.util.List;

public class SparkQuadManager {

    private static final Logger logger = Logger.getLogger(SparkQuadManager.class);

    private static final String DATABASE_NAME = "morphia_test";

    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("UrlMining")
                .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //Read quads from mongodb
        MongoClient mongoClient = MongoUtil.getOrCreateMongoClient();
        MongoDatabase mongoDatabase = MongoUtil.getDatabase(DATABASE_NAME);
        Morphia morphia = new Morphia();
        morphia.map(Quad.class);
        Datastore quadDataStore = morphia.createDatastore(mongoClient, DATABASE_NAME);
        Query<Quad> queryQuad = quadDataStore
                .createQuery(Quad.class)
                .filter("urls exists", true);
        List<Quad> quadList = queryQuad.asList();

        //parallelize and send jobs to slaves
        JavaRDD<Quad> quadRdd = sc.parallelize(quadList);
        JavaRDD<Quad> computedQuadTopics = quadRdd.map(new Function<Quad, Quad>() {
            @Override
            public Quad call(Quad q) throws Exception {
                q.setStats(LDATopicDetector.getTopicStatsByUrls(q.getUrls(),
                                            HtmlUtil.PAGE_TYPE.URL_LOCATION));
                return q;
            }
        });
        List<Quad> quadSmallestListComputed = computedQuadTopics.collect();

        //save back
        for (Quad q: quadSmallestListComputed){
            try {
                quadDataStore.save(q);
            }catch (Exception e){
                logger.error(e.getMessage());
            }
        }

    }
}
