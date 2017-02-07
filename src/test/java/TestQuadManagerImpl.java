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
        quadManager.partitionMapIntoQuads(
                new Location(46.049945, 11.121257), new Location(0.0,0.0), 2);

    }

    @Test
    public void onTestQuad() {
        Quad q1 = new Quad(new Location(46.067911409631655, 11.121257000000002), 2);
        Quad q2 = new Quad(new Location(46.049942077931405, 11.147144039024374 ), 2);
        printQuad(q1);
        printQuad(q2);
    }

    public void printQuad(Quad q){
        System.out.println("======================" + q.get_id());
        System.out.println("Top right: " + Double.toString(q.calcTopRight().getLatitude()) + " ");
        System.out.print(q.calcTopRight().getLongitude());
        System.out.println();
        System.out.println("BottomLeft: " + Double.toString(q.calcBottomLeft().getLatitude()) + " ");
        System.out.print(Double.toString(q.calcBottomLeft().getLongitude()));
        System.out.println();
        System.out.println("BottomRight: " + Double.toString(q.getBottomright().getLatitude()) + " ");
        System.out.print(Double.toString(q.getBottomright().getLongitude()));
        System.out.println();
    }
}
