/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author migal
 */
public class RankingImpl implements SearchRanking{

    Index index;
    int cutoff;
    PriorityQueue<SearchRankingDoc> ranking;
    
    public RankingImpl(Index index, int cutoff){
        this.index = index;
        this.cutoff = cutoff;
        // Heap inverso, queremos la lista de mayor score a menor
        ranking = new PriorityQueue(cutoff,Collections.reverseOrder());
    }
        
    @Override
    public int size() {
        return ranking.size();
    }

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
    
    public void add(int docID, double score){
        SearchRankingDoc srd= ranking.peek();
        
        if(srd == null){
            ranking.add(new RankingImplDoc(docID, score, index));
        }else if(score > srd.getScore() && ranking.size() == this.cutoff){
            ranking.poll();
            ranking.add(new RankingImplDoc(docID, score, index));
        }else if(ranking.size() < this.cutoff ){
            ranking.add(new RankingImplDoc(docID, score, index));
        }
        
        
    }
        
    
}
