/**
 * 
 * Fichero DocBasedVSMEngine.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 28/02/2019  
 */
package es.uam.eps.bmi.search.vsm;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.impl.RankingImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase DocBasedVSMEngine encargada de realizar las busquedas 
 * a partir del metodo orientado a los documentos
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class DocBasedVSMEngine extends AbstractVSMEngine {

    Map<Integer, Double> acumuladores;

    /**
     * Constructor de DocBasedVSMEngine
     * 
     * @param index indice con el que realizaremos las distintas busquedas
     */
    public DocBasedVSMEngine(Index index) {
        super(index);
        this.acumuladores = new HashMap<>();
    }

    /**
     * Funcion encargada de buscar los mejores resultados de los documentos
     * a partir de una query enviada
     * 
     * @param query la consulta 
     * 
     * @param cutoff el limite de resultados que queremos mostrar
     * 
     * @return los mejores "cutoff" resultados del indice dada la query
     * 
     * @throws IOException 
     */
    @Override
    public SearchRanking search(String query, int cutoff) throws IOException {
        String queryTerms[] = parse(query);
        int numDocs = index.numDocs();
        RankingImpl ranking = new RankingImpl(index, cutoff);
        // Usamos un HashMap para relacionar un queryTerm con su lista de postings
        // Así tenemos guardada la lista de postings para no tener que leerla de
        // disco a cada vez.
        Map<String, PostingsList> termPostingsList = new HashMap<>();
        
        if(queryTerms.length < 0){
            return ranking;
        }
        
        // Heap de postings ordenados de menor a mayor por docID (tantos como terminos de la query)
        PriorityQueue<HeapPosting> postingsHeap = new PriorityQueue(queryTerms.length);
        
        long termFreq=0;
        
        // Recuperamos la listas de postings para cada termino de la query
        for(String queryTerm : queryTerms){
            termFreq= index.getDocFreq(queryTerm);
            //Solo añadimos si esta presente en el indice
            if(termFreq > 0){
                termPostingsList.put(queryTerm, index.getPostings(queryTerm));
                Iterator<Posting> postingIterator = termPostingsList.get(queryTerm).iterator();
                postingsHeap.add(new HeapPosting(postingIterator.next(),postingIterator,queryTerm));
            }
        }
        
        //Mientras existan aun postings de los terminos
        while(!postingsHeap.isEmpty()){
            //Sacamos el primer elemento de la cola
            HeapPosting hp = postingsHeap.poll();
            
            //Obtenemos su docID y frecuencia
            int docID = hp.getPosting().getDocID();
            long freq = hp.getPosting().getFreq();
            
            //Obtenemos el termino de la query
            String queryTerm= hp.getQueryTerm();
            
            // Calculamos el tfidf
            double tfidf = tfidf(freq, termPostingsList.get(queryTerm).size(), numDocs);
            
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
