/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.ranking.impl;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import java.io.IOException;

/**
 * Document of a ranking
 * @author migal
 */
class RankingImplDoc extends SearchRankingDoc{
    
    // The document ID
    int docID;
    // Score of the document
    double score;
    // Index used to get the path of the document
    Index index;
    
    public RankingImplDoc(int docID, double score, Index index){
        this.docID = docID;
        this.score = score;
        this.index = index;
    }
    
    @Override
    public double getScore() {
        return this.score;
    }

    @Override
    public int getDocID() {
        return this.docID;
    }

    @Override
    public String getPath() throws IOException {
        return index.getDocPath(docID);
    }
    
}
