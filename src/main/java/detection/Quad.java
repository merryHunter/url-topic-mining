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

import java.io.Serializable;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents physical squares of the map grid. Allows access to containing
 * URLs, computed topics, squares inside this square. */
@Entity("quad")
@Indexes({
        @Index(fields = @Field(value = "geohash", type = IndexType.TEXT)),
        @Index(fields = @Field(value = "qId", type = IndexType.ASC))
})
public class Quad implements Serializable {
    final static Logger logger = Logger.getLogger(Quad.class);

    /** Кут діагоналі квадрата в напрямку якого рухаємо координату щоб знайти центр квадрата та правий нижній кут */
    protected static final int QUAD_DIAGONAL_BEARING_0 = 0;
    protected static final int QUAD_DIAGONAL_BEARING_45 = 45;
    protected static final int QUAD_DIAGONAL_BEARING_90 = 90;
    protected static final int QUAD_DIAGONAL_BEARING_135 = 135;
    protected static final int QUAD_DIAGONAL_BEARING_180 = 180;
    protected static final int QUAD_DIAGONAL_BEARING_270 = 270;
    public static final int QUAD_SIDE_MIN = 16;
    public static final int QUAD_SIDE_MAX = 16;
    public static final double QUAD_DIAGONAL = Math.sqrt(QUAD_SIDE_MIN * QUAD_SIDE_MIN);
    @Id
    protected ObjectId id;

    /** Quad id calculated by recursively dividing the map in 4 sub squares.*/
    protected long qId;

    /** Length of a side of the quad.*/
    protected int qSide;

    protected Location topleft, bottomright;

    /**
     * Hashtable of the inferred topics in the quad.
     * String - topic, Integer - number of URLs having this topic. */
    protected Hashtable<String, Integer> stats;

    /** Hash for the center of the quad. */
    protected String geoHash;

    /** URLs inside this quad. */
    protected List<String> urls = new LinkedList<>();

    public Quad(){}

    public Quad(long qId, int qSide, Location topleft, Location bottomright,
                Hashtable<String, Integer> stats, String geoHash, List<String> urls) {
        this.qId = qId;
        this.qSide = qSide;
        this.topleft = topleft;
        this.bottomright = bottomright;
        this.stats = stats;
        this.geoHash = geoHash;
        this.urls = urls;
    }

    /** TODO: THIS IS NOT USED MADATUFKA */
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

    /**
     * Quad constructor that creates quad based on its topleft corner and side length
     * @param topleft : Location of the top left vertex of a quad.
     * @param quadSide : quad side length
     */
    public Quad(Location topleft, int quadSide){
        this.topleft = topleft;
        this.bottomright = GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_135,
                Math.sqrt(quadSide*quadSide + quadSide*quadSide)
        );
        this.qSide = quadSide;
        Location center = getCenter();
        if (quadSide == QUAD_SIDE_MIN) { // geoHash only for the smallest quads
            geoHash = GeoHash
                    .geoHashStringWithCharacterPrecision(
                            center.getLatitude(),
                            center.getLongitude(),
                            GeolocationUtil.GEOHASH_PRECISION);
        }else {
            geoHash = null;
        }
    }

    public Location calcBottomLeft() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_180,
                qSide
        );
    }

    public Location calcTopRight() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_90,
                qSide
        );
    }

    public Location getCenter(){
       return GeolocationUtil.getNewLocation(
               topleft.getLatitude(),
               topleft.getLongitude(),
               QUAD_DIAGONAL_BEARING_135,
               Math.sqrt(qSide * qSide + qSide * qSide)/2.0
       ); //корінь суми квадратів катетів поділений на 2 - центр гіпотенузи
    }

    /**
     * Getter & setters
     */

    public Location getTopleft() {
        return topleft;
    }

    public Location getBottomright() {
        return bottomright;
    }

    public void setStats(Hashtable<String, Integer> stats) {
        this.stats = stats;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void addUrl(String url){
        urls.add(url);
    }

    public void addUrlsAll(String[] urlsList){
        Collections.addAll(urls, urlsList);
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

    public long getId() {
        return qId;
    }

    public void setId(long id_) {
        this.qId = id_;
    }

    @Override
    public String toString() {
        String out ="\n" + Long.toString(qId) + "\n" +
                "Topleft: " + topleft.toString() + "\n" +
                "Bottomright: " + bottomright.toString() + "\n" +
                "Topright: " + this.calcTopRight().toString() + "\n" +
                "Bottomleft: " + this.calcBottomLeft().toString() + "\n";// +
//                "Center: " + this.getCenter() + "\n";
        if(stats != null && stats.size() > 0){
            out += stats.toString() + "\n";
        }
        return out;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public long getqId() {
        return qId;
    }

    public void setqId(long qId) {
        this.qId = qId;
    }

    public void setqSide(int qSide) {
        this.qSide = qSide;
    }

    public void setTopleft(Location topleft) {
        this.topleft = topleft;
    }

    public void setBottomright(Location bottomright) {
        this.bottomright = bottomright;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }


}
