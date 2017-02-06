import org.junit.Test;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author Dmytro on 06/02/2017.
 */
public class RandomTestsFIleTest {

    @Test
    public void test_method_1() {
        for (int i=0; i<=11; i++) {
            System.out.println(2*Math.pow(2,i) + " ");

        }
    }

}