package nhb.test.stringsearch;

import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.amygdalum.stringsearchalgorithms.patternsearch.chars.BPGlushkov;
import net.amygdalum.stringsearchalgorithms.search.chars.AhoCorasick;
import net.amygdalum.stringsearchalgorithms.search.chars.BNDM;
import net.amygdalum.stringsearchalgorithms.search.chars.BOM;
import net.amygdalum.stringsearchalgorithms.search.chars.Horspool;
import net.amygdalum.stringsearchalgorithms.search.chars.KnuthMorrisPratt;
import net.amygdalum.stringsearchalgorithms.search.chars.SetBackwardOracleMatching;
import net.amygdalum.stringsearchalgorithms.search.chars.ShiftAnd;
import net.amygdalum.stringsearchalgorithms.search.chars.ShiftOr;
import net.amygdalum.stringsearchalgorithms.search.chars.Sunday;
import net.amygdalum.stringsearchalgorithms.search.chars.WuManber;
import net.amygdalum.util.io.StringCharProvider;

public class TestPerf {

    private static final String SEARCH_STRING = "adipiscing";
    private static String SAMPLE_TEXT;
    static {
        try {
            SAMPLE_TEXT = Files.readString(new File("test.txt").toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        var testCases = initTestCases();
        var round = (int) 1e5;
        var numLoop = 10;
        loopTest(numLoop, round, testCases);
    }

    static void loopTest(int numLoop, int round, Map<String, Supplier<Boolean>> testCases) {
        for (int i = 0; i < numLoop; i++) {
            System.out.println("***** loop " + (i + 1) + "/" + numLoop + " *****");
            SAMPLE_TEXT += " -> " + i;
            test(round, testCases);
        }
        System.out.println("***** END *****");
    }

    static void test(int round, Map<String, Supplier<Boolean>> testCases) {
        var results = new HashMap<String, Double>();
        for (var entry : testCases.entrySet()) {
            if (!entry.getValue().get()) {
                System.out.println("invalid result on algorithm: " + entry.getKey());
                continue;
            }
            results.put(entry.getKey(), measureTime(round, entry.getValue()));
        }
        var list = new LinkedList<String>();
        list.addAll(results.keySet());
        list.sort((key1, key2) -> {
            var delta = results.get(key1) - results.get(key2);
            return delta == 0 ? 0 : delta > 0 ? 1 : -1;
        });
        var df = new DecimalFormat("###,###.##");
        for (String key : list) {
            double totalSecs = results.get(key);
            System.out.println("[" + key + "] -> " + df.format(round / totalSecs) + " ops/s");
        }
    }

    static void measureTimeThreaded(int round, Supplier<Boolean> runner) {
        new Thread(() -> measureTime(round, runner)).start();
    }

    static double measureTime(int round, Supplier<Boolean> runner) {
        long start = System.nanoTime();
        for (int i = 0; i < round; i++) {
            runner.get();
        }
        return Double.valueOf(System.nanoTime() - start) / 1e9;
    }

    private static Map<String, Supplier<Boolean>> initTestCases() {
        var testCases = new HashMap<String, Supplier<Boolean>>();
        testCases.put("horspool", TestPerf::horspool);
        testCases.put("bndm", TestPerf::bndm);
        testCases.put("knuthMorrisPratt", TestPerf::knuthMorrisPratt);
        testCases.put("split+hash", TestPerf::splitAndHashSet);
        testCases.put("shiftAnd", TestPerf::shiftAnd);
        testCases.put("shiftOr", TestPerf::shiftOr);
        testCases.put("sunday", TestPerf::sunday);
        // testCases.put("bom", TestPerf::bom);
        // testCases.put("wuManber", TestPerf::wuManber);
        // testCases.put("setBackwardOracleMatching",
        // TestPerf::setBackwardOracleMatching);
        // testCases.put("bpGlushkov", TestPerf::bpGlushkov);
        // testCases.put("ahoCorasick", TestPerf::ahoCorasick);
        return testCases;
    }

    static boolean splitAndHashSet() {
        String[] splitted = SAMPLE_TEXT.split("[\\s]+");
        Set<String> set = new HashSet<>(asList(splitted));
        return set.contains(SEARCH_STRING);
    }

    static boolean horspool() {
        var stringSearch = new Horspool(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean bom() {
        var stringSearch = new BOM(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean knuthMorrisPratt() {
        var stringSearch = new KnuthMorrisPratt(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean bndm() {
        var stringSearch = new BNDM(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean shiftAnd() {
        var stringSearch = new ShiftAnd(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean shiftOr() {
        var stringSearch = new ShiftOr(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean sunday() {
        var stringSearch = new Sunday(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean wuManber() {
        var stringSearch = new WuManber(asList(SEARCH_STRING));
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean setBackwardOracleMatching() {
        var stringSearch = new SetBackwardOracleMatching(asList(SEARCH_STRING));
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean bpGlushkov() {
        var stringSearch = new BPGlushkov(SEARCH_STRING);
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider);
        return finder.findNext() != null;
    }

    static boolean ahoCorasick() {
        var stringSearch = new AhoCorasick(asList(SEARCH_STRING));
        var charProvider = new StringCharProvider(SAMPLE_TEXT, 0);
        var finder = stringSearch.createFinder(charProvider, LONGEST_MATCH, NON_OVERLAP);
        return finder.findNext() != null;
    }

}
