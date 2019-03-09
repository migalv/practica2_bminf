/**
 * 
 * Fichero RankingImpl.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 26/02/2019  
 */
package es.uam.eps.bmi.search.ranking.impl;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Clase RankingImpl encargada de ordenar un indice a partir de uno mismo
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class RankingImpl implements SearchRanking{

    Index index;
    int cutoff;
    PriorityQueue<SearchRankingDoc> ranking;
    
    /**
     * Constructor de RankingImpl
     * 
     * @param index el indice
     * @param cutoff el limite de resultados que queremos mostrar
     */
    public RankingImpl(Index index, int cutoff){
        this.index = index;
        this.cutoff = cutoff;
        // Heap inverso, queremos la lista de mayor score a menor
        ranking = new PriorityQueue(cutoff,Collections.reverseOrder());
    }
        
    /**
     * Devuelve el numero de resultados con mejor ranking
     * 
     * @return el numero de resultados
     */
    @Override
    public int size() {
        return ranking.size();
    }

    /**
     * Iterador del ranking
     * 
     * @return un SearchRankingDoc con el que poder iterar
     */
    @Override
    public Iterator<SearchRankingDoc> iterator() {
        List<SearchRankingDoc> orderedRanking= new ArrayList<>(Collections.nCopies(ranking.size(), null)); 
        int initialHeapSize = ranking.size();
        
        // Recorremos la lista al reves para devolver el ranking ordenado
        for(int i = initialHeapSize - 1; i >= 0; i--){
            orderedRanking.set(i, ranking.poll());
        }
        
        return orderedRanking.iterator();
    }
    
    /**
     * Funcion que añade y ordena con el mejor resultado los diferentes documentos
     * que le van pasando para obtener finalmente el ranking con los mejores resultados
     * 
     * @param docID el identificador del documento que queremos añadir
     * 
     * @param score el resultado de dicho documento
     */
    public void add(int docID, double score){
        SearchRankingDoc srd= ranking.peek();
        
        //Si no hay nada en el heap lo añadimos
        if(srd == null){
            ranking.add(new RankingImplDoc(docID, score, index));
        //En caso de que el heap este lleno y el primer elemento tenga
        //una puntuacion menor, sera remplazado por el nuevo documento
        }else if(score > srd.getScore() && ranking.size() == this.cutoff){
            ranking.poll();
            ranking.add(new RankingImplDoc(docID, score, index));
        //Si aun no se ha llenado el heap, se añade
        }else if(ranking.size() < this.cutoff ){
            ranking.add(new RankingImplDoc(docID, score, index));
        }
        
        
    }
        
    
}
