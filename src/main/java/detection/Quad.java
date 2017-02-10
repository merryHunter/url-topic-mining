/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import ch.hsr.geohash.GeoHash;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;
import util.GeolocationUtil;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents physical squares of the map grid. Allows access to containing
 * URLs, computed topics, squares inside this square. */
//
@Entity("quad")
@Indexes({
        @Index(fields = @Field(value = "geohash", type = IndexType.TEXT)),
        @Index(fields = @Field(value = "qId", type = IndexType.ASC))
})
public class Quad {
    final static Logger logger = Logger.getLogger(Quad.class);

    /** Кут діагоналі квадрата в напрямку якого рухаємо координату щоб знайти центр квадрата та правий нижній кут */
    public static final int QUAD_DIAGONAL_BEARING_45 = 45;
    private static final int QUAD_DIAGONAL_BEARING_90 = 90;
    private static final int QUAD_DIAGONAL_BEARING_0 = 0;

    @Id
    private ObjectId id;

    private long qId;

    public long getId() {
        return qId;
    }

    public void setId(long id_) {
        this.qId = id_;
    }

    private int qSide;

    private Location topleft, bottomright;

    /**
     * Hashtable of the inferred topics in the quad.
     * String - topic, Integer - number of URLs having this topic. */
    private Hashtable<String, Integer> stats;

    /** Hash for the center of the quad. */
    private String geoHash;

    /** URLs inside this quad. */
    //TODO: transform this field to index pointer of urls?
    //TODO: ensure ObjectId is the best. Perhaps String will fit better.
    private List<ObjectId> urls = new LinkedList<>();



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
                qSide
        );
    }

    public Location calcTopRight() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_0,
                qSide
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
                135,
                Math.sqrt(quadSide*quadSide + quadSide*quadSide)
        );
        this.qSide = quadSide;
        Location center = getCenter();
        if (quadSide == 16) { // геохеш тільки для найменших квадратів
            geoHash = GeoHash
                    .geoHashStringWithCharacterPrecision(
                            center.getLatitude(),
                            center.getLongitude(),
                            GeolocationUtil.GEOHASH_PRECISION);
        }else {
            geoHash = null;
        }
    }


    public Location getCenter(){
       return GeolocationUtil.getNewLocation(
               topleft.getLatitude(),
               topleft.getLongitude(),
               QUAD_DIAGONAL_BEARING_45,
               Math.sqrt(qSide * qSide + qSide * qSide)/2.0
       ); //корінь суми квадратів катетів поділений на 2 - центр гіпотенузи
    }

    public void setStats(Hashtable<String, Integer> stats) {
        this.stats = stats;
    }

    public void setUrls(List<ObjectId> urls) {
        this.urls = urls;
    }

    public List<ObjectId> getUrls() {
        return urls;
    }

    public void addUrl(ObjectId url){
        urls.add(url);
    }

    public String getGeoHash() {
        return geoHash;
    }

    public Hashtable<String, Integer> getStats() {
        return stats;
    }

    public int getqSide() {
        return qSide;
    }

    @Override
    public String toString() {
        return "Topleft: " + topleft.toString() + "\n" +
                "Bottomright: " + bottomright.toString() + "\n" +
                "Topright: " + this.calcTopRight().toString() + "\n" +
                "Bottomleft: " + this.calcBottomLeft().toString();
    }
}
