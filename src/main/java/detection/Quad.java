/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import ch.hsr.geohash.GeoHash;
import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.*;
import util.GeolocationUtil;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents physical squares of the map grid. Allows access to containing
 * URLs, computed topics, squares inside this square. */
//
//TODO: think about secondary indexes(commented out now)
@Entity("quad")
//@Indexes(
//        @Index(value = "topleft", fields = @Field("geohash"))
//)
public class Quad {
    final static Logger logger = Logger.getLogger(Quad.class);

    /** Кут діагоналі квадрата в напрямку якого рухаємо координату щоб знайти центр квадрата та правий нижній кут */
    public static final int QUAD_DIAGONAL_BEARING_45 = 45;
    private static final int QUAD_DIAGONAL_BEARING_90 = 90;
    private static final int QUAD_DIAGONAL_BEARING_0 = 0;

    @Id
    private long _id;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    private int quadSide;

    private Location topleft, bottomright;

    /**
     * Hashtable of the inferred topics in the quad.
     * String - topic, Integer - number of URLs having this topic. */
    private Hashtable<String, Integer> stats;

    /** Hash for the center of the quad. */
    private String geoHash;

    /** URLs inside this quad. */
    //TODO: transform this field to index pointer of urls?
    @Property("urls")
    List<Long> urls = new LinkedList<>();


    public Quad(){}

    public Quad(Location topleft, Location bottomright){
        this.topleft = topleft;
        this.bottomright = bottomright;

        Location center = getCenter();
        geoHash = GeoHash
                .geoHashStringWithCharacterPrecision(
                        center.getLatitude(),
                        center.getLongitude(),
                        GeolocationUtil.GEOHASH_PRECISION);
    }

    public Location getTopleft() {
        return topleft;
    }

    public Location getBottomright() {
        return bottomright;
    }

    public Location calcBottomLeft() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_90,
                quadSide
        );
    }

    public Location calcTopRight() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_0,
                quadSide
        );
    }

    /**
     * Quad constructor that creates quad based on its topleft corner and side length
     * @param topleft
     * @param quadSide : quad side length
     */
    public Quad(Location topleft, int quadSide){
        this.topleft = topleft;
        this.bottomright = GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_45,
                Math.sqrt(quadSide*quadSide + quadSide*quadSide)
        );
        this.quadSide = quadSide;
        Location center = getCenter();
        geoHash = GeoHash
                .geoHashStringWithCharacterPrecision(
                        center.getLatitude(),
                        center.getLongitude(),
                        GeolocationUtil.GEOHASH_PRECISION);
    }


    public Location getCenter(){
       return GeolocationUtil.getNewLocation(
               topleft.getLatitude(),
               topleft.getLongitude(),
               QUAD_DIAGONAL_BEARING_45,
               Math.sqrt(quadSide*quadSide+quadSide*quadSide)/2.0
       ); //корінь суми квадратів катетів поділений на 2 - центр гіпотенузи
    }

    public void setStats(Hashtable<String, Integer> stats) {
        this.stats = stats;
    }

    public void setUrls(List<Long> urls) {
        this.urls = urls;
    }

    public void addUrl(Long url){
        urls.add(url);
    }

    public String getGeoHash() {
        return geoHash;
    }

    public Hashtable<String, Integer> getStats() {
        return stats;
    }

    public int getQuadSide() {
        return quadSide;
    }
}
