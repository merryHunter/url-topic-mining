/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.sequential;

import detection.ITopicDetector;
import detection.Location;
import detection.Quad;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Hashtable;
import java.util.List;


public class WEKATopicDetector {
    public static Hashtable<String, Integer> getTopicStatsByUrls(List<String> urls) throws Exception{
        String[] aurls = {"https://github.com/mimno/Mallet",
                "https://docs.mongodb.com/manual/reference/mongo-shell/"};
        StringToWordVector stringToWordVector = new StringToWordVector();
        StringToWordVector filter = new StringToWordVector();
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(aurls[0]);
        Instances dataRaw = dataSource.getDataSet();
        filter.setInputFormat(dataRaw);
        Instances dataFiltered = Filter.useFilter(dataRaw, filter);

        return null;
    }
}
