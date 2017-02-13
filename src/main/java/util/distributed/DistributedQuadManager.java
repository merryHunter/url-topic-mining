/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.distributed;

import detection.IQuadManager;
import detection.Location;
import detection.Quad;

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

//    @Override
    public List<String> getTopics(Location topleft, Location bottomright, int S) {
        return null;
    }

//    @Override
    public void computeTopicStatsByQuad(Quad q) {

    }
}
