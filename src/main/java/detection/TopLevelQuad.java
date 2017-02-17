package detection;

import ch.hsr.geohash.GeoHash;
import org.mongodb.morphia.annotations.Reference;
import util.GeolocationUtil;

/**
 * @author Dmytro on 16/02/2017.
 */
public class TopLevelQuad extends Quad {
    @Reference
    protected Quad leftNeighbor;
    @Reference
    protected Quad rightNeighbor;
    @Reference
    protected Quad bottomNeighbor;
    @Reference
    protected Quad upperNeighbor;

    //TODO: constructor
    public TopLevelQuad(Location topleft, int quadSide) {
        super(topleft, quadSide);
        Location center = getCenter();
            geoHash = GeoHash
                    .geoHashStringWithCharacterPrecision(
                            center.getLatitude(),
                            center.getLongitude(),
                            1); //geohash precision - the lowest
    }

    public Quad getLeftNeighbor() {
        return leftNeighbor;
    }

    public void setLeftNeighbor(Quad leftNeighbor) {
        this.leftNeighbor = leftNeighbor;
    }

    public Quad getRightNeighbor() {
        return rightNeighbor;
    }

    public void setRightNeighbor(Quad rightNeighbor) {
        this.rightNeighbor = rightNeighbor;
    }

    public Quad getBottomNeighbor() {
        return bottomNeighbor;
    }

    public void setBottomNeighbor(Quad bottomNeighbor) {
        this.bottomNeighbor = bottomNeighbor;
    }

    public Quad getUpperNeighbor() {
        return upperNeighbor;
    }

    public void setUpperNeighbor(Quad upperNeighbor) {
        this.upperNeighbor = upperNeighbor;
    }

    public Location calcUpperNeighborStartingCoord() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_0, //it's going up
                qSide
        );
    }

    public Location calcLeftNeighborStartingCoord() {
        return GeolocationUtil.getNewLocation(
                topleft.getLatitude(),
                topleft.getLongitude(),
                QUAD_DIAGONAL_BEARING_270,
                qSide
        );
    }
}
