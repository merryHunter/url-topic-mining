/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import ch.hsr.geohash.GeoHash;
import util.GeolocationUtil;

import java.util.Hashtable;
import java.util.List;

/**
 * Represents physical squares of the map grid. Allows access to containing
 * URLs, computed topics, squares inside this square. */
//
//TODO: provide interface for representing connections between quads.
//
public class Quad {

    private int _id;

    private Location topleft, bottomright;

    /**
     * Hashtable of the inferred topics in the quad.
     * String - topic, Integer - number of URLs having this topic. */
    private Hashtable<String, Integer> stats;

    /** Hash for the center of the quad. */
    private String geoHash; // ??? should we move it to Util and compute as we need it?

    /** URLs inside this quad. */
    //TODO: transform this field to index pointer of urls?
    List<String> urls;


    public Quad(Location topleft, Location bottomright){
        this.topleft = topleft;
        this.bottomright = bottomright;
        Location center = getCenter();
        //---??? ---
        geoHash = GeoHash
                .geoHashStringWithCharacterPrecision(
                        center.getLatitude(),
                        center.getLongitude(),
                        GeolocationUtil.GEOHASH_PRECISION);

    }

    //TODO: implement method
    public Location getCenter(){
        return null;
    }

    public void setStats(Hashtable<String, Integer> stats) {
        this.stats = stats;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public void addUrl(String url){
        urls.add(url);
    }

    public String getGeoHash() {
        return geoHash;
    }

}
