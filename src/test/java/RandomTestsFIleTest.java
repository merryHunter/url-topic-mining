import org.junit.Test;
import org.junit.*;

import javax.ws.rs.core.MultivaluedHashMap;

import java.util.LinkedList;
import java.util.List;

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

    @Test
    public void onTestHashMap(){
        MultivaluedHashMap<String,Long> map = new MultivaluedHashMap();
        Long l = 1L;
        List<Long> listlist = new LinkedList<>();
        listlist.add(l);
        map.add("sdf", l);

    }

}