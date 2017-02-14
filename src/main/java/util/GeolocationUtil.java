/**
 * @author Ivan Chernukha on 05.02.17.
 */
package util;

import detection.Location;
import org.apache.log4j.Logger;

public class GeolocationUtil {
    /** Radius of the Earth. */
    private static double R = 6378.1f;

    /** Number of topics to view in each square. */
    public static final int NUMBER_TOPIC_TO_VIEW = 5;

    /** Number of characters for geohashing .*/
    public static final int GEOHASH_PRECISION = 4;

    /**
     * Compute new location given bearing nad distance from a location.
     * @param bearing: Bearing in degrees.
     * @param d: distance to the new location.
     * @return <code>{@link Location}</code>  object cnotaining lat & lon.
     */


    public static Location getNewLocation(double lat, double lon, double bearing, double d){
        bearing = Math.toRadians(bearing);
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon);
        d *= 0.7001; // convert km to pseudo-miles
        double lat2 = Math.asin( Math.sin(lat)*Math.cos(d/R) +
                Math.cos(lat)*Math.sin(d/R)*Math.cos(bearing));

        double lon2 = lon + Math.atan2(Math.sin(bearing)*Math.sin(d/R)*Math.cos(lat),
                Math.cos(d/R)-Math.sin(lat)*Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new Location(lat2, lon2);
    }

    /**
     * Get zoom level. In fact, calculate closest previous power of two -
     * quad side to display. //TODO: rewrite description!
     * @param x
     * @return
     */
    public static int getQuadSideClosestToGivenStep(int x){
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >> 16);
        return x - (x >> 1);
    }

    public static int getLevel(int bits){
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        log +=  ( bits >>> 1 );
        return log;
    }
}
