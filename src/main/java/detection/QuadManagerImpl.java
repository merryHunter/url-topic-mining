package detection;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import util.MongoUtil;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

/**
 * Created by Dmytro on 06/02/2017.
 */
public class QuadManagerImpl implements IQuadManager{

    /** */
    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    private Morphia morphia;

    Datastore datastore;

    public QuadManagerImpl(){
        mongoClient = MongoUtil.getOrCreateMongoClient();
        mongoDatabase = MongoUtil.getDatabase("urlsdb");
        morphia = new Morphia();
        morphia.map(Quad.class);
        datastore = morphia.createDatastore(mongoClient, "morphia_test");
        System.out.println("Quadmanager inittialized");
    }
    /**
     * @param S: is the side length of the square
     */
    @Override
    public void partitionMapIntoQuads(Location topleft, Location bottomright, int S) {
        //while zoomLevel <= 11
        int quadSide = 4096; //початковий розмір квадратіка
        //поки не кінець світу
        //створили the Daddy
        Quad newQuad = new Quad(topleft, 4096);
        newQuad.set_id(1L);
        datastore.save(newQuad);
        //4 рази рекурсивно зайшли в дітей
        recursivePartitionMapIntoQuads(topleft, 4096, 1); //вперше заходимо в дітей

        //в цій верхній функції спробувати пройтися по всіх сусідах, і для кожного сусіда заходити в дітей.
    }

    private void recursivePartitionMapIntoQuads(Location topleft, int fatherQuadSide, long fatherQuadId) {
        if (fatherQuadSide == 2)
            return;

        //creating subquad 0
        Quad newQuad0 = new Quad(topleft, fatherQuadSide/2);
        newQuad0.set_id(fatherQuadId*10L+0); //shift father ID by 1 digit (розряд)
        datastore.save(newQuad0);
        recursivePartitionMapIntoQuads(newQuad0.getTopleft(), newQuad0.getQuadSide(), newQuad0.get_id());

        //creating subquad 1
        Quad newQuad1 = new Quad(newQuad0.calcTopRight(), fatherQuadSide/2);
        newQuad1.set_id(fatherQuadId*10L+1);
        datastore.save(newQuad1);
        recursivePartitionMapIntoQuads(newQuad1.getTopleft(), newQuad1.getQuadSide(), newQuad1.get_id());

        //creating subquad 2
        Quad newQuad2 = new Quad(newQuad0.calcBottomLeft(), fatherQuadSide/2);
        newQuad2.set_id(fatherQuadId*10L+2);
        datastore.save(newQuad2);
        recursivePartitionMapIntoQuads(newQuad2.getTopleft(), newQuad2.getQuadSide(), newQuad2.get_id());

        //creating subquad 3
        Quad newQuad3 = new Quad(newQuad0.getBottomright(), fatherQuadSide/2);
        newQuad3.set_id(fatherQuadId*10L+3);
        datastore.save(newQuad3);
        recursivePartitionMapIntoQuads(newQuad3.getTopleft(), newQuad3.getQuadSide(), newQuad3.get_id());
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
