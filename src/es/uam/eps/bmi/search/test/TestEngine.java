package es.uam.eps.bmi.search.test;

import es.uam.eps.bmi.search.SearchEngine;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.impl.DiskIndex;
import es.uam.eps.bmi.search.index.impl.DiskIndexBuilder;
import es.uam.eps.bmi.search.index.impl.SerializedRAMIndex;
import es.uam.eps.bmi.search.index.impl.SerializedRAMIndexBuilder;
import es.uam.eps.bmi.search.index.lucene.LuceneForwardIndex;
import es.uam.eps.bmi.search.index.lucene.LuceneForwardIndexBuilder;
import es.uam.eps.bmi.search.index.lucene.LuceneIndex;
import es.uam.eps.bmi.search.index.lucene.LuceneBuilder;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.lucene.LuceneEngine;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import es.uam.eps.bmi.search.ui.TextResultDocRenderer;
import es.uam.eps.bmi.search.util.Timer;
import es.uam.eps.bmi.search.vsm.DocBasedVSMEngine;
import es.uam.eps.bmi.search.vsm.SlowVSMEngine;
import es.uam.eps.bmi.search.vsm.TermBasedVSMEngine;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author pablo
 */
public class TestEngine {
    public static void main (String a[]) throws IOException {
        
        ///////////////////////////////////
        // Índices: pruebas de correción //
        ///////////////////////////////////
        
        String collPath = "collections/urls.txt";
        String baseIndexPath = "index/urls";

        // Construcción
        new LuceneForwardIndexBuilder().build(collPath, baseIndexPath + "/lucene/forward");
        new LuceneBuilder().build(collPath, baseIndexPath + "/lucene");
        new SerializedRAMIndexBuilder().build(collPath, baseIndexPath + "/ram");
        //new DiskIndexBuilder().build(collPath, baseIndexPath + "/disk");
        
        // Excepción
        try {
            new SerializedRAMIndex("here");
        } catch (NoIndexException ex) {
            System.out.println("No index in " + ex.getFolder());
        }
        
        // Inspección
        System.out.println("-----------------------");
        System.out.println("Checking index correction on URL collection");
        testIndex(new LuceneForwardIndex(baseIndexPath + "/lucene/forward"), "information");
        testIndex(new LuceneIndex(baseIndexPath + "/lucene"), "information");
        testIndex(new SerializedRAMIndex(baseIndexPath + "/ram"), "information");
        //testIndex(new DiskIndex(baseIndexPath + "/disk"), "information");

        /////////////////////////////////////
        // Índices: pruebas de rendimiento //
        /////////////////////////////////////
        
        testIndexPerformance("1k", "collections/docs1k.zip", "index/1k");
        testIndexPerformance("10k", "collections/docs10k.zip", "index/10k");
        testIndexPerformance("100k", "collections/docs100k.zip", "index/100k");

        /////////////////////////////////////
        // Búsqueda: pruebas de corrección //
        /////////////////////////////////////

        System.out.println("-----------------------");
        System.out.println("Checking engine results on URL collection");
        String query = "information probability";
        Index luceneFwdIndex = new LuceneForwardIndex(baseIndexPath + "/lucene/forward");
        Index luceneIndex = new LuceneIndex(baseIndexPath + "/lucene");
        Index ramIndex = new SerializedRAMIndex(baseIndexPath + "/ram");
        //Index diskIndex = new DiskIndex(baseIndexPath + "/disk");
        
        testSearch(new LuceneEngine(baseIndexPath + "/lucene"), query, 5);
        testSearch(new SlowVSMEngine(luceneFwdIndex), query, 5);
        
        testSearch(new TermBasedVSMEngine(luceneIndex), query, 5);
        testSearch(new TermBasedVSMEngine(ramIndex), query, 5);
        //testSearch(new TermBasedVSMEngine(diskIndex), query, 5);

        testSearch(new DocBasedVSMEngine(luceneIndex), query, 5);
        testSearch(new DocBasedVSMEngine(ramIndex), query, 5);
        //testSearch(new DocBasedVSMEngine(diskIndex), query, 5);
       
        //////////////////////////////////////
        // Búsqueda: pruebas de rendimiento //
        //////////////////////////////////////

        testSearchPerformance("1k", "index/1k", "obama family tree", 5);
        testSearchPerformance("10k", "index/10k", "air tavel information", 5);
        testSearchPerformance("100k", "index/100k", "living in india", 5);
    }
    
    static void testIndex(Index index, String word) throws IOException {
        System.out.println("  " + index.getClass().getSimpleName());
        System.out.print("\tWord \"" + word + "\" occurs in " +index.getDocFreq(word) + " documents: ");
        for (Posting posting : index.getPostings(word))
            System.out.print(posting.getDocID() + " (" + posting.getFreq() + ") ");
        System.out.println();
    }
    
    static void testIndexPerformance(String collName, String collPath, String baseIndexPath) throws IOException {
        System.out.println("-----------------------");
        System.out.println("Testing index performance on " + collName + " document collection");

        Timer.reset("  Build time...");
        new LuceneForwardIndexBuilder().build(collPath, baseIndexPath + "/lucene/forward");
        Timer.time("\tLuceneForwardIndex:\t");
        new LuceneBuilder().build(collPath, baseIndexPath + "/lucene");
        Timer.time("\tLuceneIndex:\t");
        new SerializedRAMIndexBuilder().build(collPath, baseIndexPath + "/ram");
        Timer.time("\tRAMIndex:\t");
        //new DiskIndexBuilder().build(collPath, baseIndexPath + "/disk");
        Timer.time("\tDiskIndex:\t");        

        Timer.reset("  Load time...");
        new LuceneForwardIndex(baseIndexPath + "/lucene/forward");
        Timer.time("\tLuceneForwardIndex:\t");
        new LuceneIndex(baseIndexPath + "/lucene");
        Timer.time("\tLuceneIndex:\t");
        new SerializedRAMIndex(baseIndexPath + "/ram");
        Timer.time("\tRAMIndex:\t");
        //new DiskIndex(baseIndexPath + "/disk");
        Timer.time("\tDiskIndex:\t");        

        System.out.println("  Disk space...");
        System.out.println("\tLuceneForwardIndex:\t" + diskSpace(baseIndexPath + "/lucene/forward") + "K");
        System.out.println("\tLuceneIndex:\t" + diskSpace(baseIndexPath + "/lucene") + "K");
        System.out.println("\tRAMIndex:\t" + diskSpace(baseIndexPath + "/ram") + "K");
//        System.out.println("\tDiskIndex:\t" + diskSpace(baseIndexPath + "/disk") + "K");
    }
    
    static void testSearchPerformance(String collName, String baseIndexPath, String query, int cutoff) throws IOException {
        System.out.println("-----------------------");
        System.out.println("Testing engine performance on " + collName + " document collection");
        Index luceneFwdIndex = new LuceneForwardIndex(baseIndexPath + "/lucene/forward");
        Index luceneIndex = new LuceneIndex(baseIndexPath + "/lucene");
        Index ramIndex = new SerializedRAMIndex(baseIndexPath + "/ram");
        //Index diskIndex = new DiskIndex(baseIndexPath + "/disk");
        
        Timer.reset();
        testSearch(new LuceneEngine(baseIndexPath + "/lucene"), query, cutoff);
        Timer.time("  --> ");
        testSearch(new SlowVSMEngine(luceneFwdIndex), query, cutoff);
        Timer.time("  --> ");
        
        testSearch(new TermBasedVSMEngine(luceneIndex), query, cutoff);
        Timer.time("  --> ");
        testSearch(new TermBasedVSMEngine(ramIndex), query, cutoff);
        Timer.time("  --> ");
        //testSearch(new TermBasedVSMEngine(diskIndex), query, cutoff);
        Timer.time("  --> ");
        
        //testSearch(new DocBasedVSMEngine(diskIndex), query, cutoff);
        Timer.time("  --> ");
    }
    
    static void testSearch (SearchEngine engine, String query, int cutoff) throws IOException {
        SearchRanking ranking = engine.search(query, cutoff);
        System.out.println("  " + engine.getClass().getSimpleName() 
                + " + " + engine.getIndex().getClass().getSimpleName()
                + ": top " + cutoff + " for query \"" + query + "\"");
        for (SearchRankingDoc result : ranking)
            System.out.println("\t" + new TextResultDocRenderer(result));
    }

    static long diskSpace(String dir) {
        long space = 0;
        for (File file : new File(dir).listFiles())
            if (file.isFile()) space += file.length();
        return space / 1000;
    }
}
