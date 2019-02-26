/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.vsm;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.index.structure.PostingsListIterator;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.impl.RankingImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author migal
 */
public class DocBasedVSMEngine extends AbstractVSMEngine {

    public DocBasedVSMEngine(Index index) {
        super(index);
    }

    @Override
    public SearchRanking search(String query, int cutoff) throws IOException {
        String queryTerms[] = parse(query);
        int numDocs = index.numDocs();
        RankingImpl ranking = new RankingImpl(index, cutoff);
        List<PostingsList> postingsLists;
        List<Integer> docIDs;
        
        // Lista de las listas de postings de cada termino de la query
        postingsLists = new ArrayList<>();
       
        //Guardaremos el ID de todos los docs
        docIDs= new ArrayList<>();
        
        // Heap de postings ordenados de menor a mayor por docID (tantos como terminos de la query)
        PriorityQueue<Posting> postingsHeap = new PriorityQueue(queryTerms.length);
        
        long termFreq=0;
        
        // Recuperamos la listas de postings para cada termino de la query
        for(String queryTerm : queryTerms){
            termFreq= index.getDocFreq(queryTerm);
            PostingsList termPostingList=index.getPostings(queryTerm);
            //PostingsListIterator postingIterator = (PostingsListIterator)index.getPostings(queryTerm).iterator();
            
            //Añadimos todos los ID de los docs en una lista por cada term
            for(Posting docIDPosting : termPostingList){
                int docID= docIDPosting.getDocID();
                if(!docIDs.contains(docID)){
                    docIDs.add(docID);
                }
            }
            
            //Solo añadimos si esta presente en el indice
            if(termFreq > 0){
                postingsLists.add(termPostingList);
            }
        }
        
        while(postingsLists.isEmpty() == false){
            
        }
        
        
        
        return ranking;
        
    }
    
}
