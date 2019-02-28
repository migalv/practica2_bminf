/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.vsm;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.impl.RankingImpl;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migal
 */
public class DocBasedVSMEngine extends AbstractVSMEngine {

    Map<Integer, Double> acumuladores;

    
    public DocBasedVSMEngine(Index index) {
        super(index);
        this.acumuladores = new HashMap<>();
    }

    @Override
    public SearchRanking search(String query, int cutoff) throws IOException {
        String queryTerms[] = parse(query);
        int numDocs = index.numDocs();
        RankingImpl ranking = new RankingImpl(index, cutoff);
        
        // Heap de postings ordenados de menor a mayor por docID (tantos como terminos de la query)
        PriorityQueue<HeapPosting> postingsHeap = new PriorityQueue(queryTerms.length);
        
        long termFreq=0;
        
        // Recuperamos la listas de postings para cada termino de la query
        for(String queryTerm : queryTerms){
            termFreq= index.getDocFreq(queryTerm);
            Iterator<Posting> postingIterator = index.getPostings(queryTerm).iterator();

            //Solo añadimos si esta presente en el indice
            if(termFreq > 0){
                PostingsList termPostingList=index.getPostings(queryTerm);
                postingsHeap.add(new HeapPosting(postingIterator.next(),postingIterator,queryTerm));
            }
        }
        
        while(!postingsHeap.isEmpty()){
            HeapPosting hp = postingsHeap.poll();
            
            int docID = hp.getPosting().getDocID();
            long freq = hp.getPosting().getFreq();
            String queryTerm= hp.getQueryTerm();
            
            // Calculamos el tfidf
            double tfidf = tfidf(freq, index.getDocFreq(queryTerm), numDocs);
            
            // Si el docID ya está en los acumuladores le sumamos el score
            if(acumuladores.containsKey(docID)){
                acumuladores.replace(docID, acumuladores.get(docID)+tfidf);
            }else{ // Si no, lo creamos
                acumuladores.put(docID, tfidf);
            }
            
            //Añadimos el posting list siguiente al ultimo que hemos cogido
            if(hp.getIteratorPosting().hasNext()){
                postingsHeap.add(new HeapPosting(hp.getIteratorPosting().next(),hp.getIteratorPosting(),queryTerm));
            }
        }
        
        // Recorremos la lista de los acumuladores dividimos por el módulo
        // Y finalmente los añadimos a un ranking.
        acumuladores.forEach((docID, score) -> {
            // Si el score es mayor que 0 entonces lo añadimos al ranking
            if(score > 0) {
                try {
                    ranking.add(docID, score / index.getDocNorm(docID));
                } catch (IOException ex) {
                    Logger.getLogger(TermBasedVSMEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        return ranking;
        
    }
        
}
