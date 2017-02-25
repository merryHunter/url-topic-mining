/**
 * @author Ivan Chernukha on 23.02.17.
 */
package util;

import detection.Quad;
import org.apache.log4j.Logger;

import java.util.*;

public class TopicAggregation {

    private static final Logger logger = Logger.getLogger(TopicAggregation.class);

    private static final int a = 10;
    private static final int b = 10;
    private static final int c = 10;
    private static final int d = 10;

    private static final float MIN_RATING = 1.0f;
    private static final float MAX_RATING = 100.0f;

    public static Hashtable<String, Integer> computeStatsAggregation(List<Quad> quads) {
        Hashtable<String, Float> newRatings = new Hashtable<>();
        float[][] par = getParametersForFourQuads(quads);

        for(int i = 0; i < quads.size(); i++){
            Hashtable<String, Integer> stats = quads.get(i).getStats();
            try {
                if(stats != null && !stats.isEmpty()) {
//                    logger.info(stats);
                    for (String t : stats.keySet()) {
                        newRatings.put(t,2 * stats.get(t) + c * par[i][2] + d * par[i][3]);
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage());
            }
        }
        if(!newRatings.isEmpty()) {
//            logger.info(newRatings);
            return getRescaledValues(newRatings);
        }
        return null;
    }

    private static Hashtable<String, Integer> getRescaledValues(Hashtable<String, Float> ratings) {
        float r_min = Collections.min(ratings.values());
        float r_max = Collections.max(ratings.values());
        Hashtable<String, Integer> result = new Hashtable<>();
        for ( String s: ratings.keySet()){
            result.put(s, (int) (
                    ((MAX_RATING - MIN_RATING) / (r_max - r_min)  ) *
                            (ratings.get(s) - r_max) + MAX_RATING
                    ));
        }
//        logger.info(result);
        return result;
    }

    private static float[][] getParametersForFourQuads(List<Quad> quads) {
        float[][] result = new float[4][4];
        int[] nUrls = new int[4];       //number of urls in each quad
        int[] nGeoPoints = new int[4];  //number of geopoints in each quad
        float[] urlsRation = new float[4];  //normalized ratio of urls
        float[] geoPointRatio = new float[4]; //normalized ratio of geopoints
        float[] pointUrlsRatio = new float[4];//normalized ratio of geopoints to urls
        for(int i = 0; i < quads.size(); i++){
            nUrls[i] =quads.get(i).getUrls().size();
            nGeoPoints[i] = quads.get(i).getGeoPoints();//initialized with 0
            result[i][0] = (float)  nUrls[i];
            result[i][1] = (float) nGeoPoints[i];
        }
        IntSummaryStatistics staturl = Arrays.stream(nUrls).summaryStatistics();
        IntSummaryStatistics statgeopoints = Arrays.stream(nGeoPoints).summaryStatistics();

        for(int i = 0; i < quads.size(); i++){
            urlsRation[i] = (float)(nUrls[i] - staturl.getMin()  ) / (staturl.getMax() - staturl.getMin() + 1);
            geoPointRatio[i] = (float)(nGeoPoints[i] - statgeopoints.getMin()  ) /
                    (statgeopoints.getMax() - statgeopoints.getMin() + 1);//avoid null division!
            result[i][2] = urlsRation[i];
            result[i][3] = geoPointRatio[i];
        }

        return result;
    }
}
