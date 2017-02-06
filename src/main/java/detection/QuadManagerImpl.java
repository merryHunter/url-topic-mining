package detection;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

/**
 * Created by Dmytro on 06/02/2017.
 */
public class QuadManagerImpl implements IQuadManager{

    /*param S is the side length of the square*/
    @Override
    public void partitionMapIntoQuads(Location topleft, Location bottomright, int S) {
        //while zoomLevel <= 11
        Quad newQuad = new Quad(topleft, 2);
        newQuad.set_id(10000000000L);
    }

    @Override
    public MultivaluedHashMap<String, Integer> createQuadHashMap() {
        return null;
    }

    @Override
    public Quad selectQuadByUrlLocation(List<Quad> q, Location urllocation) {
        return null;
    }

    @Override
    public void partitionUrls() {

    }
}
