/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.distributed;

import detection.Location;
import detection.Quad;
import detection.ITopicDetector;
import org.apache.spark.sql.SparkSession;
import util.HtmlUtil;

import java.util.Hashtable;
import java.util.List;

public class SparkTopicDetector{


    public static Hashtable<String, Integer> getTopicStatsByUrls(
            List<String> urls, HtmlUtil.PAGE_TYPE page_type) throws Exception {
        SparkSession sparkSession = SparkSession.builder().getOrCreate();
        return null;
    }


//    @Override
    public static Hashtable<String, Integer> getTopicStatsByQuad(Quad q) {
        return null;
    }
//    @Override
    public static List<String> getTopics(Location topleft, Location bottomright, int S) {
        return null;
    }
}
