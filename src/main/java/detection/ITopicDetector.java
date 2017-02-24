/**
 * @author Ivan Chernukha on 05.02.17.
 */

package detection;


import util.HtmlUtil;

import java.util.Hashtable;
import java.util.List;

public interface ITopicDetector {

    public Hashtable<String, Integer> getTopicStatsByUrls(
            List<String> urls, HtmlUtil.PAGE_TYPE page_type) throws Exception;

}
