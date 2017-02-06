/**
 * @author Ivan Chernukha on 05.02.17.
 */

package detection;


import java.util.List;

public interface ITopicDetector {

    List<String> getTopicsByQuad(Quad q);

    List<String> getTopics(Location topleft, Location bottomright, int S);

}
