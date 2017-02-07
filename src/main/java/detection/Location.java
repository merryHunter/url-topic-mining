/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import org.mongodb.morphia.annotations.Entity;

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

    @Override
    public String toString() {
        return "Lat: " + Double.toString(latitude) +
                ", Lon: " + Double.toString(longitude);
    }
}
