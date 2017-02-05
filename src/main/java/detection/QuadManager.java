/**
 * @author Ivan Chernukha on 05.02.17.
 */
package detection;

import org.apache.spark.sql.Dataset;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

public class QuadManager {

    /** All URLs. */
    private Dataset<String> URLs;

    /** Mapped geohash to all quads ids. (for the world?)*/
    // !???? must be not quads, but indexes to them!!!
    private MultivaluedHashMap<String, Integer> quadHashMap;

    /** All quads partitioned over the ...???. */
    // TODO: transform to another data structure? (db?)
    private Quad[] quads;

    public QuadManager(String urlFile){
        createQuadHashMap(quads);
        partitionUrls(urlFile);
    }

    private void partitionMapIntoQuads(){
        /** */
    }



    public MultivaluedHashMap<String, Integer> createQuadHashMap(Quad[] quads){
        /** Compute geohashes for quads and create hashmap. */

        return null;
    }

    private void partitionUrls(String urlFile){
        /** Read urls and partition them into squares.
         * @param urlFile: dataset file contatining paris of URL and location.
         * */
        /*
            For each u in urlFile;
                //get hash
                String urlhash = Geohash.geoHashStringWithCharacterPrecision(u.lat, u.lon, n);

                //get quads with similar hash
                List<Quad> qList = quadsAll.get(urlhash);
                int q_index = 0; // index of quad containing the url
                if (q.size() > 1){
                    q_index = selectQuadByUrlLocation(qList, u.lat, u.lon);
                }

                //get quad containing url
                Quad q = qList.get(q_index);
                q.addUrl(u.url);

                //!! this operation requires another representation for all quads!
                quads.update(q)
                //!!

         */

    }

    private int selectQuadByUrlLocation(List<Quad> q, Location l){
        /** Select which quad among given contains @param l */

        return -1;
    }

    public MultivaluedHashMap<String, Integer> getQuadHashMap(){
        return quadHashMap;
    }

}
