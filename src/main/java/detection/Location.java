/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import java.io.Serializable;

/**
 * Geographical location.
 * */
@Entity("location")
public class Location implements Serializable {

    @Property("lat")
    private double latitude;

    @Property("lon")
    private double longitude;

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

    @Override
    public String toString() {
        return "Lat: " + Double.toString(latitude) +
                ", Lon: " + Double.toString(longitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
