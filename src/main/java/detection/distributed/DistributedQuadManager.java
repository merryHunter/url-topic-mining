/**
 * @author Ivan Chernukha on 06.02.17.
 */
package detection.distributed;

import detection.IQuadManager;
import detection.Location;
import detection.Quad;
import org.bson.types.ObjectId;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

public class DistributedQuadManager  implements IQuadManager{

    @Override
    public void partitionMapIntoQuads(Location topleft, Location bottomright, int S) {

    }

    @Override
    public Quad selectQuadByUrlLocation(List<Quad> q, Location l) {
        return null;
    }

    @Override
    public void partitionUrls() {

    }
}
