/**
 * @author Ivan Chernukha on 05.02.17.
 */

package detection;


import java.util.Hashtable;
import java.util.List;

public interface ITopicDetector {

    Hashtable<String, Integer> getTopicStatsByQuad(Quad q);

    List<String> getTopics(Location topleft, Location bottomright, int S);

}
