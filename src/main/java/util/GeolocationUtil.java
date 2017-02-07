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
    public static final int GEOHASH_PRECISION = 5;

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

        double lat2 = Math.asin( Math.sin(lat)*Math.cos(d/R) +
                Math.cos(lat)*Math.sin(d/R)*Math.cos(bearing));

        double lon2 = lon + Math.atan2(Math.sin(bearing)*Math.sin(d/R)*Math.cos(lat),
                Math.cos(d/R)-Math.sin(lat)*Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new Location(lat2, lon2);
    }


}
