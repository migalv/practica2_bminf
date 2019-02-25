package es.uam.eps.bmi.search.vsm;

import es.uam.eps.bmi.search.AbstractEngine;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.impl.RankingImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migal
 */
public class TermBasedVSMEngine extends AbstractVSMEngine {

    Map<Integer, Double> acumuladores;
    
    public TermBasedVSMEngine(Index idx) {
        super(idx);
        this.acumuladores = new HashMap<>();
    }

    @Override
    public SearchRanking search(String query, int cutoff) throws IOException {
        String queryTerms[] = parse(query);
        int numDocs = index.numDocs();
        RankingImpl ranking = new RankingImpl(index, cutoff);
        
        for(String queryTerm : queryTerms){
            PostingsList postings = index.getPostings(queryTerm);
            for(Posting posting : postings){
                int docID = posting.getDocID();
                long freq = posting.getFreq();
                
                // Calculamos el tfidf
                double tfidf = tfidf(freq, index.getDocFreq(queryTerm), numDocs);
                
                // Si el docID ya está en los acumuladores le sumamos el score
                if(acumuladores.containsKey(docID)){
                    acumuladores.replace(docID, acumuladores.get(docID)+tfidf);
                }else{ // Si no, lo creamos
                    acumuladores.put(docID, tfidf);
                }
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
