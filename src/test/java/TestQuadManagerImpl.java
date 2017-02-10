import detection.Location;
import detection.Quad;
import detection.QuadManagerImpl;
import org.apache.spark.sql.execution.columnar.FLOAT;
import org.junit.Test;

/**
 * @author Ivan Chernukha on 07.02.17.
 */

public class TestQuadManagerImpl {

    @Test
    public void onTestMapPartition(){
        QuadManagerImpl quadManager =  new QuadManagerImpl();
//        quadManager.partitionMapIntoQuads(
//                new Location(46.049945, 11.121257), new Location(0.0,0.0), 2);
        quadManager.partitionMapIntoQuads(
                new Location(47.185257, 8.206737), new Location(0.0,0.0), 2);

        quadManager.partitionUrls();
    }

    @Test
    public void onTestQuad() {
        Quad q1 = new Quad(new Location(46.067911409631655, 11.121257000000002), 2);
        Quad q2 = new Quad(new Location(46.049942077931405, 11.147144039024374 ), 2);
        System.out.println(q1);
        System.out.println(q2);
    }

}
