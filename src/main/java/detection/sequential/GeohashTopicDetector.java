/**
 * @author Ivan Chernukha on 06.02.17.
 */
package detection.sequential;

import detection.ITopicDetector;
import detection.Location;
import detection.Quad;

import java.util.List;

public class GeohashTopicDetector implements ITopicDetector {
    @Override
    public List<String> getTopicsByQuad(Quad q) {
        return null;
    }

    @Override
    public List<String> getTopics(Location topleft, Location bottomright, int S) {
        return null;
    }
}
