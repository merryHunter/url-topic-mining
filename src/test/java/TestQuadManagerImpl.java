import detection.Location;
import detection.Quad;
import detection.QuadManagerImpl;
import detection.TopLevelQuad;
import org.apache.spark.sql.execution.columnar.FLOAT;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

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

    @Test
    public void testTopLevelTraverse() {
        int quadSide = 2048;
        Location topLeft = new Location(2.834115, 13.796509);
        TopLevelQuad firstQuad = new TopLevelQuad(topLeft, quadSide);
        /**
         * using the static variable topLevelQuadCount below to keep track of how many quads have already been named
         */
        firstQuad.setId(QuadManagerImpl.topLevelQuadCount);
        QuadManagerImpl.tempMultivaluedQuadStorage.add(firstQuad.getGeoHash(), firstQuad);
//        QuadManagerImpl.recursivePartitionQuadIntoChildren(topLeft, quadSide, QuadManagerImpl.topLevelQuadCount); //рекурсивно обробляємо дітей
        /**
         * new quad was created along with all it's kids (parent ID which relies on this variable won't be used anymore)
         */
        QuadManagerImpl.topLevelQuadCount++;

        QuadManagerImpl.recursiveTraverseTopLevelQuads(firstQuad);

//        System.out.println(QuadManagerImpl.tempMultivaluedQuadStorage);
        int i=0;
        for(List<TopLevelQuad> list : QuadManagerImpl.tempMultivaluedQuadStorage.values()) {
            for(TopLevelQuad quad: list) {
                StringBuilder str = new StringBuilder();
                //54.15626787405963, -58.88163421912802 {quad id} <green>
                //39.8338819223521, -42.15296783993988 {quad id 2} <default>
                str.append(quad.getTopleft().getLatitude() + ", " + quad.getTopleft().getLongitude());
                str.append(" {quad id: " + quad.getId() + "} <"+ getColour(i)+"> \n");
                str.append(quad.calcTopRight().getLatitude() + ", " + quad.calcTopRight().getLongitude());
                str.append(" {quad id: " + quad.getId() + "} <"+ getColour(i)+"> \n");
                str.append(quad.getBottomright().getLatitude() + ", " + quad.getBottomright().getLongitude());
                str.append(" {quad id: " + quad.getId() + "} <"+ getColour(i)+"> \n");
                str.append(quad.calcBottomLeft().getLatitude() + ", " + quad.calcBottomLeft().getLongitude());
                str.append(" {quad id: " + quad.getId() + "} <"+ getColour(i)+"> \n");
                //TODO: выпилить нахуй
                if (quad.getId() < 25) {
                    System.out.println(str.toString());
                }
                i++;
            }
        }
    }

    private String getColour(int i) {
        switch (i%7) {
            case 0: {
                return "default";
            }
            case 1: {
                return "green";
            }
            case 2: {
                return "pink";
            }
            case 3: {
                return "blue";
            }
            case 4: {
                return "tan";
            }
            case 5: {
                return "gray";
            }
            case 6: {
                return "yellow";
            }
        }
        return "default";
    }

}
