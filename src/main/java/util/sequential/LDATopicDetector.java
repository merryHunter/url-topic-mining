/**
 * @author Ivan Chernukha on 06.02.17.
 */
package util.sequential;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import detection.ITopicDetector;
import org.apache.log4j.Logger;
import util.HtmlUtil;

public class LDATopicDetector implements ITopicDetector {
    private static final Logger logger = Logger.getLogger(LDATopicDetector.class);
    public static String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero",
            "IE","Th","th","a","abbastanza","abbia","abbiamo","abbiano","abbiate","accidenti","ad","adesso","affinche","agl","agli","ahime","ahimÃ¨","ai","al","alcuna","alcuni","alcuno","all","alla","alle","allo","allora","altri","altrimenti","altro","altrove","altrui","anche","ancora","anni","anno","ansa","anticipo","assai","attesa","attraverso","avanti","avemmo","avendo","avente","aver","avere","averlo","avesse","avessero","avessi","avessimo","aveste","avesti","avete","aveva","avevamo","avevano","avevate","avevi","avevo","avrai","avranno","avrebbe","avrebbero","avrei","avremmo","avremo","avreste","avresti","avrete","avrà","avrò","avuta","avute","avuti","avuto","basta","bene","benissimo","berlusconi","brava","bravo","c","casa","caso","cento","certa","certe","certi","certo","che","chi","chicchessia","chiunque","ci","ciascuna","ciascuno","cima","cio","cioe","cioÃ¨","circa","citta","cittÃ","ciÃ²","co","codesta","codesti","codesto","cogli","coi","col","colei","coll","coloro","colui","come","cominci","comunque","con","concernente","conciliarsi","conclusione","consiglio","contro","cortesia","cos","cosa","cosi","cosÃ¬","cui","d","da","dagl","dagli","dai","dal","dall","dalla","dalle","dallo","dappertutto","davanti","degl","degli","dei","del","dell","della","delle","dello","dentro","detto","deve","di","dice","dietro","dire","dirimpetto","diventa","diventare","diventato","dopo","dov","dove","dovra","dovrÃ","dovunque","due","dunque","durante","e","ebbe","ebbero","ebbi","ecc","ecco","ed","effettivamente","egli","ella","entrambi","eppure","era","erano","eravamo","eravate","eri","ero","esempio","esse","essendo","esser","essere","essi","ex","fa","faccia","facciamo","facciano","facciate","faccio","facemmo","facendo","facesse","facessero","facessi","facessimo","faceste","facesti","faceva","facevamo","facevano","facevate","facevi","facevo","fai","fanno","farai","faranno","fare","farebbe","farebbero","farei","faremmo","faremo","fareste","faresti","farete","farà","farò","fatto","favore","fece","fecero","feci","fin","finalmente","finche","fine","fino","forse","forza","fosse","fossero","fossi","fossimo","foste","fosti","fra","frattempo","fu","fui","fummo","fuori","furono","futuro","generale","gia","giacche","giorni","giorno","giÃ","gli","gliela","gliele","glieli","glielo","gliene","governo","grande","grazie","gruppo","ha","haha","hai","hanno","ho","i","ieri","il","improvviso","in","inc","infatti","inoltre","insieme","intanto","intorno","invece","io","l","la","lasciato","lato","lavoro","le","lei","li","lo","lontano","loro","lui","lungo","luogo","lÃ","ma","macche","magari","maggior","mai","male","malgrado","malissimo","mancanza","marche","me","medesimo","mediante","meglio","meno","mentre","mesi","mezzo","mi","mia","mie","miei","mila","miliardi","milioni","minimi","ministro","mio","modo","molti","moltissimo","molto","momento","mondo","mosto","nazionale","ne","negl","negli","nei","nel","nell","nella","nelle","nello","nemmeno","neppure","nessun","nessuna","nessuno","niente","no","noi","non","nondimeno","nonostante","nonsia","nostra","nostre","nostri","nostro","novanta","nove","nulla","nuovo","o","od","oggi","ogni","ognuna","ognuno","oltre","oppure","ora","ore","osi","ossia","ottanta","otto","paese","parecchi","parecchie","parecchio","parte","partendo","peccato","peggio","per","perche","perchÃ¨","perché","percio","perciÃ²","perfino","pero","persino","persone","perÃ²","piedi","pieno","piglia","piu","piuttosto","piÃ¹","più","po","pochissimo","poco","poi","poiche","possa","possedere","posteriore","posto","potrebbe","preferibilmente","presa","press","prima","primo","principalmente","probabilmente","proprio","puo","pure","purtroppo","puÃ²","qualche","qualcosa","qualcuna","qualcuno","quale","quali","qualunque","quando","quanta","quante","quanti","quanto","quantunque","quasi","quattro","quel","quella","quelle","quelli","quello","quest","questa","queste","questi","questo","qui","quindi","realmente","recente","recentemente","registrazione","relativo","riecco","salvo","sara","sarai","saranno","sarebbe","sarebbero","sarei","saremmo","saremo","sareste","saresti","sarete","sarÃ","sarà","sarò","scola","scopo","scorso","se","secondo","seguente","seguito","sei","sembra","sembrare","sembrato","sembri","sempre","senza","sette","si","sia","siamo","siano","siate","siete","sig","solito","solo","soltanto","sono","sopra","sotto","spesso","srl","sta","stai","stando","stanno","starai","staranno","starebbe","starebbero","starei","staremmo","staremo","stareste","staresti","starete","starà","starò","stata","state","stati","stato","stava","stavamo","stavano","stavate","stavi","stavo","stemmo","stessa","stesse","stessero","stessi","stessimo","stesso","steste","stesti","stette","stettero","stetti","stia","stiamo","stiano","stiate","sto","su","sua","subito","successivamente","successivo","sue","sugl","sugli","sui","sul","sull","sulla","sulle","sullo","suo","suoi","tale","tali","talvolta","tanto","te","tempo","ti","titolo","torino","tra","tranne","tre","trenta","troppo","trovato","tu","tua","tue","tuo","tuoi","tutta","tuttavia","tutte","tutti","tutto","uguali","ulteriore","ultimo","un","una","uno","uomo","va","vale","vari","varia","varie","vario","verso","vi","via","vicino","visto","vita","voi","volta","volte","vostra","vostre","vostri","vostro","Ã¨","è",
            "a","a's","able","about","above","according","accordingly","across","actually","after","afterwards","again","against","ain't","all","allow","allows","almost","alone","along","already","also","although","always","am","among","amongst","an","and","another","any","anybody","anyhow","anyone","anything","anyway","anyways","anywhere","apart","appear","appreciate","appropriate","are","aren't","around","as","aside","ask","asking","associated","at","available","away","awfully","b","be","became","because","become","becomes","becoming","been","before","beforehand","behind","being","believe","below","beside","besides","best","better","between","beyond","both","brief","but","by","c","c'mon","c's","came","can","can't","cannot","cant","cause","causes","certain","certainly","changes","clearly","co","com","come","comes","concerning","consequently","consider","considering","contain","containing","contains","corresponding","could","couldn't","course","currently","d","definitely","described","despite","did","didn't","different","do","does","doesn't","doing","don't","done","down","downwards","during","e","each","edu","eg","eight","either","else","elsewhere","enough","entirely","especially","et","etc","even","ever","every","everybody","everyone","everything","everywhere","ex","exactly","example","except","f","far","few","fifth","first","five","followed","following","follows","for","former","formerly","forth","four","from","further","furthermore","g","get","gets","getting","given","gives","go","goes","going","gone","got","gotten","greetings","h","had","hadn't","happens","hardly","has","hasn't","have","haven't","having","he","he's","hello","help","hence","her","here","here's","hereafter","hereby","herein","hereupon","hers","herself","hi","him","himself","his","hither","hopefully","how","howbeit","however","i","i'd","i'll","i'm","i've","ie","if","ignored","immediate","in","inasmuch","inc","indeed","indicate","indicated","indicates","inner","insofar","instead","into","inward","is","isn't","it","it'd","it'll","it's","its","itself","j","just","k","keep","keeps","kept","know","known","knows","l","last","lately","later","latter","latterly","least","less","lest","let","let's","like","liked","likely","little","look","looking","looks","ltd","m","mainly","many","may","maybe","me","mean","meanwhile","merely","might","more","moreover","most","mostly","much","must","my","myself","n","name","namely","nd","near","nearly","necessary","need","needs","neither","never","nevertheless","new","next","nine","no","nobody","non","none","noone","nor","normally","not","nothing","novel","now","nowhere","o","obviously","of","off","often","oh","ok","okay","old","on","once","one","ones","only","onto","or","other","others","otherwise","ought","our","ours","ourselves","out","outside","over","overall","own","p","particular","particularly","per","perhaps","placed","please","plus","possible","presumably","probably","provides","q","que","quite","qv","r","rather","rd","re","really","reasonably","regarding","regardless","regards","relatively","respectively","right","s","said","same","saw","say","saying","says","second","secondly","see","seeing","seem","seemed","seeming","seems","seen","self","selves","sensible","sent","serious","seriously","seven","several","shall","she","should","shouldn't","since","six","so","some","somebody","somehow","someone","something","sometime","sometimes","somewhat","somewhere","soon","sorry","specified","specify","specifying","still","sub","such","sup","sure","t","t's","take","taken","tell","tends","th","than","thank","thanks","thanx","that","that's","thats","the","their","theirs","them","themselves","then","thence","there","there's","thereafter","thereby","therefore","therein","theres","thereupon","these","they","they'd","they'll","they're","they've","think","third","this","thorough","thoroughly","those","though","three","through","throughout","thru","thus","to","together","too","took","toward","towards","tried","tries","truly","try","trying","twice","two","u","un","under","unfortunately","unless","unlikely","until","unto","up","upon","us","use","used","useful","uses","using","usually","uucp","v","value","various","very","via","viz","vs","w","want","wants","was","wasn't","way","we","we'd","we'll","we're","we've","welcome","well","went","were","weren't","what","what's","whatever","when","whence","whenever","where","where's","whereafter","whereas","whereby","wherein","whereupon","wherever","whether","which","while","whither","who","who's","whoever","whole","whom","whose","why","will","willing","wish","with","within","without","won't","wonder","would","wouldn't","x","y","yes","yet","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves","z","zero",
            "aspx", "browser", "denied", "access", "forbidden","test", "video","photo","png","php","json","unavailable", "login", "moltissime", "molto", "per", "giorno", "home","non","com","http", "json", "potere", "dovere", "gli", "il", "lo", "le", "la", "lui","loro","io","ore","chi","che","www", "org","access", "lunedi","martedi","mercoledi","giovani","venerdi", "sabato","domenica","orari", "download","permission","numero","orario","ricorda","server","internet","web","attiva","script","document","var","key","url","api", "piace","vengono","scritto","found","consumati", "preso","function","return","null","net","settings","nav","view","sito", "ultimate","special","gif", "primo","secondo","terzo","cut","tipo","alcun","pictures","original","close","edition","disc","blu","ray","annunci","error","idle","terms","price", "temp","min","max","javascript","link","cookie","cookies","tmp","log","src","dns"};
    public static Set<String> stopWordSet = new HashSet<String>(Arrays.asList(stopwords));

    /**
     * Minimum number of topics to detect.
     */
    private static final int MIN_NUM_TOPICS = 2;

    /**
     * Number of keywords which represent a topic.
     */
    private static final int NUM_WORDS = 5;

    /**
     * Coefficient for saturating those keyword that is common
     * for other topics.
     */
    private static final float COEF_IF_IN_COMMON = 1.1f;

    private static final Pattern keywordPattern = Pattern.compile("\\n(([a-z]*(')?[a-z]*)\\t)");

    private static final Pattern numberPattern = Pattern.compile("\\t(([0-9]*)\\n)");

    /**
     *  Compute topic statistics for all urls in the list.
     *  @param urls: Each element contains a url.
     *  @param page_type: fast processing depending on page titles,
     *              depending on page body or url location itself.
     */
    public Hashtable<String, Integer> getTopicStatsByUrls(
            List<String> urls, HtmlUtil.PAGE_TYPE page_type) throws Exception {
        logger.info("getTopicStatsByUrls started");
        // curl url's
        List<String> htmlList = null;
        if (page_type == HtmlUtil.PAGE_TYPE.URL_LOCATION){
            htmlList = HtmlUtil
                    .getHtmlPages(urls, HtmlUtil.PAGE_TYPE.URL_LOCATION);
        } else{
            htmlList = HtmlUtil
                    .getHtmlPages(urls, HtmlUtil.PAGE_TYPE.BODY);
        }
        for(int i = 0; i < htmlList.size(); i++){
            htmlList.set(i,removeStopWords(htmlList.get(i)));
        }

        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(
                Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        //TODO: move stoplist to ...?
        pipeList.add( new TokenSequenceRemoveStopwords(
                new File("stoplist-en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        instances.addThruPipe(new ArrayIterator(htmlList));

        int num_topics = getNumTopics(htmlList.size());
        logger.info("URLS length:" + Integer.toString(htmlList.size()));
        logger.info("Topicsnumber: " + Integer.toString(num_topics));

        ParallelTopicModel model = new ParallelTopicModel(num_topics, 1.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(1);
        model.setNumIterations(200);
        model.estimate();

        // compute stats
        Hashtable<String, Integer> topicsStats = new Hashtable<>();
        String topWords = model.displayTopWords(NUM_WORDS, true);
        Matcher m = keywordPattern.matcher(topWords);
        Matcher counts = numberPattern.matcher(topWords);
        try {
            int[] keywordCount = new int[num_topics*NUM_WORDS];
            int j = 0;
            while (m.find() && counts.find()) {
                //if the keyword is common for topics then saturate it's rating
                String topic = m.group(1).trim();
                int rating = Integer.parseInt(counts.group(1).trim());
                keywordCount[j++] = rating;
                if (topicsStats.containsKey(topic)){
                    int old_rating = topicsStats.get(topic);
                    //multiply maximum value by coefficient
                    int new_rating =(int) (Math.max(rating, old_rating) *
                                LDATopicDetector.COEF_IF_IN_COMMON );
                    topicsStats.put(topic, new_rating);
                }else {
                    topicsStats.put(topic, rating);
                }
            }
            //normalize keyword ratings
            IntSummaryStatistics summary = Arrays.stream(keywordCount).summaryStatistics();
            int r_min = summary.getMin();
            int diff  = summary.getMax() - r_min;

            for(String t: topicsStats.keySet()){
                int r = 10* (topicsStats.get(t) - r_min) / diff;
                if( r != 0) {
                    topicsStats.put(t, r );
                } else{
                    //TODO:maybe don't put at all?
//                    topicsStats.put(t, 1);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
//        logger.info(topicsStats);
        logger.info("getTopicStatsByUrls finished");
        /*HashMap<String, Integer> topicsStats = new HashMap<>();
        String topWords = model.displayTopWords(NUM_WORDS, true);
        Matcher m = Pattern.compile("\\n(([a-z]*)\\t)").matcher(topWords);
        Matcher counts = Pattern.compile("\\t(([0-9]*)\\n)").matcher(topWords);
        try {
            while (m.find() && counts.find()) {
                topicsStats.put(m.group(1).trim(),
                        Integer.parseInt(counts.group(1).trim()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }*/
        return topicsStats;
    }

    private static int getNumTopics(int length){
        int num_topics = 0;
        if ( length >= 3000 ){
            while(length >= 10) length /=10;
            num_topics = 15  + length; //TODO:check carefully how many topics to model!
        } else if ( length >= 100){
            while(length >= 10) length /=10;
            num_topics = length + MIN_NUM_TOPICS;
        }else if( length >= 10){
            while(length >= 10) length /=10;
            num_topics = length + MIN_NUM_TOPICS;
        } else{
            num_topics = MIN_NUM_TOPICS;
        }
        return num_topics;
    }


    public static String removeStopWords(String string) {
        String result = "";
        String[] words = string.split("[^a-zA-Z']+");
        for(String word : words) {
            if(word.isEmpty()) continue;
            if(isStopword(word)) continue; //remove stopwords
            result += (word+" ");
        }
        return result;
    }

    public static boolean isStopword(String word) {
        if (word.length() < 2)
            return true;
        if (word.charAt(0) >= '0' && word.charAt(0) <= '9')
            return true; //remove numbers, "25th", etc
        if (stopWordSet.contains(word))
            return true;
        else return false;
    }

}
