/**
 * @author Ivan Chernukha on 06.02.17.
 */
package detection.distributed;

import detection.Location;
import detection.Quad;
import detection.ITopicDetector;

import java.util.List;

public class SparkTopicDetector implements ITopicDetector {

    @Override
    public List<String> getTopicsByQuad(Quad q) {
        return null;
    }

    @Override
    public List<String> getTopics(Location topleft, Location bottomright, int S) {
        return null;
    }
}
