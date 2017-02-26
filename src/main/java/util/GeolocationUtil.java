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
    public static final int NUMBER_TOPIC_TO_VIEW = 10;

    /** Number of characters for geohashing .*/
    public static final int GEOHASH_PRECISION = 4;

    /**
     * Compute new location given bearing nad distance from a location.
     * @param bearing: Bearing in degrees, north is 0 degrees.
     * @param d: distance to the new location in km.
     * @return <code>{@link Location}</code>  object cnotaining lat & lon.
     */


    public static Location getNewLocation(double lat, double lon, double bearing, double d){
        bearing = Math.toRadians(bearing);
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon);
        //d *= 0.7001; // convert km to pseudo-miles
        double lat2 = Math.asin( Math.sin(lat)*Math.cos(d/R) +
                Math.cos(lat)*Math.sin(d/R)*Math.cos(bearing));

        double lon2 = lon + Math.atan2(Math.sin(bearing)*Math.sin(d/R)*Math.cos(lat),
                Math.cos(d/R)-Math.sin(lat)*Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new Location(lat2, lon2);
    }

    public static double calculateDistance(Location StartP, Location EndP) {
        double lat1 = StartP.getLatitude();
        double lat2 = EndP.getLatitude();
        double lon1 = StartP.getLongitude();
        double lon2 = EndP.getLongitude();
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    public static Location getAlternativeNewLocation(double lat, double lon, double bearing, double d) {
        bearing = Math.toRadians(bearing); //transform degrees into radians
        lat = Math.toRadians(lat);
        lon = Math.toRadians(lon);

        double latNew = Math.asin(
                Math.sin(lat)*Math.cos(d)
                        + Math.cos(lat)*Math.sin(d)*Math.cos(bearing)
        );
        double intermediateLon = Math.atan2(
                Math.sin(bearing)*Math.sin(d)*Math.cos(lat),
                Math.cos(d) - Math.sin(lat)*Math.sin(latNew)
        );

        double lonNew = (lon-intermediateLon + Math.PI) % 2*Math.PI - Math.PI;

//        lat =asin(sin(lat1)*cos(d)+cos(lat1)*sin(d)*cos(tc))
//        dlon=atan2(sin(tc)*sin(d)*cos(lat1),cos(d)-sin(lat1)*sin(lat))
//        lon=mod( lon1-dlon +pi,2*pi )-pi

        latNew = Math.toDegrees(latNew);
        lonNew = Math.toDegrees(lonNew);
        return new Location(latNew, lonNew);
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
