/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Geographical location.
 * */
@Entity("location")
public class Location {
    private double latitude, longitude;

    public Location(){}

    public Location(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
