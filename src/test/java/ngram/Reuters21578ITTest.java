package ngram;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Oleksiy Pylypenko
 * Date: 12/14/11
 * Time: 2:23 PM
 */
public class Reuters21578ITTest {
    private static final Logger log = Logger.getLogger(Reuters21578ITTest.class);
    public static File CORPUS_PATH;

    public Reuters21578ITTest() {
        try {
            out = new PrintWriter(new File("result.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void initCorpus()
    {
        String corpusPath = System.getProperty("reuters21578.corpus", "~/data/reuters21578");

        corpusPath = corpusPath.replace("~", System.getProperty("user.home"));
        CORPUS_PATH = new File(corpusPath).getAbsoluteFile();
        if (!new File(CORPUS_PATH, "reut2-000.xml").exists()) {
            System.err.println("Please convert your sgm to xml with command " +
                    "'ls *.sgm | sed s/.sgm// | xargs -IF bash -c \"sgml2xml F.sgm > F.xml\"' " +
                    "and specify system property 'reuters21578.corpus' to set reuters21578 corpus path");
            System.exit(1);
        }
    }

    @Test
    public void testCount() throws Exception {
        final long []charsCount = new long[1];
        final long []docsCount = new long[1];
        readCorpus(new DocumentIterator() {
            public void process(String documentBody) {
                docsCount[0]++;
                charsCount[0] += documentBody.length();
            }
        });
        log.info("Corpus is " + docsCount[0] + " documents and " + charsCount[0] + " chars");
    }

    @Test
    public void testParse() throws Exception {
        final DeDupAlgo algo = new DeDupAlgo();

        readCorpus(new DocumentIterator() {
            public void process(String documentBody) {
                if (documentBody.length() > 800) {
                    parseText(documentBody, algo);
                }
            }
        });
    }

    PrintWriter out;
    private void parseText(String documentBody, DeDupAlgo algo) {
        int doc = algo.addDocument(documentBody);
        List<DocumentMatch> list = new ArrayList<DocumentMatch>();
        double uniq = algo.calcUniqueness(doc, list, 10, 0.33);
        algo.index(doc);
        out.println(String.format("doc=%d unique=%.2f%% similars=%s [[[%s]]]", doc, uniq * 100, list, documentBody));
    }

    private void readCorpus(DocumentIterator iterator) throws Exception {
        DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docBuildFactory.newDocumentBuilder();
        XPathFactory xPathFactory=XPathFactory.newInstance();
        for (int i = 0; i <= 21; i++) {
            File file = new File(CORPUS_PATH, String.format("reut2-%03d.xml", i));
            InputStream in = new SkipControlCodesInputStream(new FileInputStream(file));
            try {
                Document doc = builder.parse(in);

                XPath xPath=xPathFactory.newXPath();
                NodeList texts=(NodeList)(xPath.evaluate("/LEWIS/REUTERS/TEXT/BODY/text()", doc, XPathConstants.NODESET));
                for (int j = 0; j < texts.getLength(); j++) {
                    Text textNode = (Text) texts.item(j);
                    iterator.process(textNode.getWholeText());
                }
            } finally {
                in.close();
            }
        }
    }

    private static class SkipControlCodesInputStream extends FilterInputStream {
        public SkipControlCodesInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = super.read(b, off, len);
            for (int i = 0; i < read; i++) {
                if (b[off + i] < 0x20) {
                    b[off + i] = 0x20; // replace with space
                }
            }
            return read;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value < 0x20) {
                value = 0x20; // replace with space
            }
            return value;
        }
    }

    private interface DocumentIterator {
        void process(String documentBody);
    }
}
