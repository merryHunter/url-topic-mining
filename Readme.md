# URL topic detection 

Having a set of URL(format: [latitude, longitude, url1|url2...]) that mobile users have visited, you know for each URL the location of the user that asked that URL. We consider the texts of the pages that the URLs are pointing and analyze them to identify topics (a topic is a vector of words that appear together very often). Imagine that we would like to know the topics in different regions of the map. 

Input:     
- A set U of pairs [Location, URLs]
- An area A of the map determined by a top left and a bottom right point)
- A step S (e.g. 2km)

Output: 
- Create a grid by dividing the area A in squares of size SxS and identify and show the popular topics in each such square.
      
**Look at the [report.pdf][1]**

[1]: https://github.com/merryHunter/url-topic-mining/blob/master/report.pdf
