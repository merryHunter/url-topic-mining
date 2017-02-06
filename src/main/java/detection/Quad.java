/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import ch.hsr.geohash.GeoHash;
import org.mongodb.morphia.annotations.*;
import util.GeolocationUtil;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents physical squares of the map grid. Allows access to containing
 * URLs, computed topics, squares inside this square. */
//
//TODO: provide interface for representing connections between quads.
//TODO: think about secondary indexes(commented out now)
@Entity("quad")
//@Indexes(
//        @Index(value = "topleft", fields = @Field("geohash"))
//)
public class Quad {
    /** Кут діагоналі квадрата в напрямку якого рухаємо координату щоб знайти центр квадрата та правий нижній кут */
    public static final int QUAD_DIAGONAL_BEARING_45 = 45;

    @Id
    private long _id;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    private Location topleft;
    private Location bottomright;
//    topright, bottomleft;

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

    public Location getBottomLeft() {
        //TODO for convenient generation?
        return null;
    }

    /**
     * Quad constructor that creates quad based on its topleft corner and side length
     * @param topleft
     * @param d : quad side length
     */
    public Quad(Location topleft, int d){
        this.topleft = topleft;
        this.bottomright = GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_45,
                d
        );
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
               Math.sqrt(4+4)/2.0
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
}
