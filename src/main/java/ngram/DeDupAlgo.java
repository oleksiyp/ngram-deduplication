package ngram;

import java.util.*;

/**
 * User: Oleksiy Pylypenko
 * Date: 12/14/11
 * Time: 5:24 PM
 */
public class DeDupAlgo {
    public static final DeDupAlgo INSTANCE = new DeDupAlgo();

    public List<String> documents = new ArrayList<String>();

    public HashMap<Integer, Set<Long>> hash = new HashMap<Integer, Set<Long>>();

    public static final int GRAMITY = 5;
    public static final int WINNOWITY = 20;

    public void index(int doc) {
        String[]words = parseDocument(documents.get(doc));
        int[] fingerprint = calculateFingerprint(words);

        for (int i = 0; i < fingerprint.length; i++) {
            Set<Long> set = hash.get(fingerprint[i]);
            if (set == null) {
                set = new HashSet<Long>();
                hash.put(fingerprint[i], set);
            }
            long mark = doc;
            mark <<= 32;
            mark |= i * WINNOWITY;
            set.add(mark);
        }
    }

    public double calcUniqueness(int doc, List<DocumentMatch> list, int n, double threshold) {
        String[]words = parseDocument(documents.get(doc));
        int[] fingerprint = calculateFingerprint(words);
        if (doc == 1449 || doc == 1513) {
            System.out.println(Arrays.toString(fingerprint));
        }

        int count = 0;
        Map<Integer, Integer> freq = new HashMap<Integer, Integer>();
        for (int i = 0; i < fingerprint.length; i++) {
            Set<Long> set = hash.get(fingerprint[i]);
            if (set == null || set.isEmpty()) {
                count++;
            } else {
                Set<Integer> docsPast = new HashSet<Integer>();
                for(Long id : set) {
                    int similarDoc = (int)(id >> 32);
                    if (!docsPast.add(similarDoc)) {
                        continue;
                    }
                    Integer freqNum = freq.get(similarDoc);
                    if (freqNum == null) {
                        freqNum = 1;
                    } else {
                        freqNum++;
                    }
                    freq.put(similarDoc, freqNum);
                }
            }
        }
        for (Map.Entry res : freq.entrySet()) {
            int docIdx = (Integer) res.getKey();
            double mesure = (Integer)res.getValue();
            mesure /= fingerprint.length;
            if (mesure > threshold) {
                list.add(new DocumentMatch(docIdx, mesure));
            }
        }

        Collections.sort(list);

        if (list.size() > n) {
            List<DocumentMatch> topN = new ArrayList<DocumentMatch>(list.subList(0, n));
            list.clear();
            list.addAll(topN);
        }


        return ((double)count) / fingerprint.length;
    }


    public String[] parseDocument(String text) {
        return text.split("[^a-zA-Z0-9]+");
    }

    private int[] calculateFingerprint(String[] words) {
        int []hashes = new int[words.length];
        int []fingerprint = new int[(words.length + WINNOWITY - 1) / WINNOWITY];
        List<String> wordsList = Arrays.asList(words);
        for (int i = 0; i < words.length; i++) {
            int end = i + GRAMITY;
            if (end >= words.length) {
                end = words.length - 1;
            }
            hashes[i] = wordsList.subList(i, end).hashCode();
        }
        int k = 0;
        for (int i = 0; i < words.length; i += WINNOWITY) {
            int end = i + WINNOWITY;
            if (end >= words.length) {
                end = words.length - 1;
            }
            int min = -1;
            boolean minSet = false;
            for (int j = i; j < end; j++) {
                if (!minSet || min > hashes[j]) {
                    min = hashes[j];
                }
            }
            fingerprint[k++] = min;
        }
        return fingerprint;
    }

    public synchronized int addDocument(String text) {
        documents.add(text);
        return documents.size() -1 ;
    }

}
