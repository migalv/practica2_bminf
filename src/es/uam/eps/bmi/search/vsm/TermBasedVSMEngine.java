/**
 * 
 * Fichero TermBasedVSMEngine.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 24/02/2019  
 */
package es.uam.eps.bmi.search.vsm;

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
 * Clase TermBasedVSMEngine encargada de realizar las busquedas 
 * a partir del metodo orientado a los terminos
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class TermBasedVSMEngine extends AbstractVSMEngine {

    Map<Integer, Double> acumuladores;
    
    /**
     * Constructor de TermBasedVSMEngine
     * 
     * @param idx indice con el que realizaremos las distintas busquedas
     */
    public TermBasedVSMEngine(Index idx) {
        super(idx);
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
        //Inicializamos variables necesarias para la busqueda
        String queryTerms[] = parse(query);
        int numDocs = index.numDocs();
        RankingImpl ranking = new RankingImpl(index, cutoff);
        RankingImpl finalRanking = new RankingImpl(index, cutoff);

        // Recuperamos la listas de postings para cada termino de la query
        for(String queryTerm : queryTerms){
            //Obtenemos los postings de un termino
            PostingsList postings = index.getPostings(queryTerm);
            
            //Por cada posting del termino
            for(Posting posting : postings){
                //Obtenemos su docID y frecuencia de cada posting
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
