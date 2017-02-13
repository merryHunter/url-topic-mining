/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.distributed;

import detection.Location;
import detection.Quad;
import detection.ITopicDetector;

import java.util.Hashtable;
import java.util.List;

public class SparkTopicDetector implements ITopicDetector {

    @Override
    public Hashtable<String, Integer> getTopicStatsByQuad(Quad q) {
        return null;
    }

    @Override
    public List<String> getTopics(Location topleft, Location bottomright, int S) {
        return null;
    }
}
