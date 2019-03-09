/**
 * 
 * Fichero RankingImplDoc.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 26/02/2019  
 */
package es.uam.eps.bmi.search.ranking.impl;

import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import java.io.IOException;

/**
 * Clase RankingImplDoc que implementa un indice con un posting concreto
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
class RankingImplDoc extends SearchRankingDoc{
    
    int docID;
    double score;
    Index index;
    
    /**
     * Constructor de RankingImplDoc
     * 
     * @param docID el identificador del documento
     * 
     * @param score el resultado del documento
     * 
     * @param index el indice donde se encuentra el documento
     */
    public RankingImplDoc(int docID, double score, Index index){
        this.docID = docID;
        this.score = score;
        this.index = index;
    }
    
    /**
     * Devuelve el resultado del documento
     * 
     * @return el resultado del documento
     */
    @Override
    public double getScore() {
        return this.score;
    }

    /**
     * Devuelve el identificador del documento
     * 
     * @return el identificador del documento
     */
    @Override
    public int getDocID() {
        return this.docID;
    }

    /**
     * Devuelve el path donde se encuentra el documento 
     * 
     * @return la ruta donde se encuentra el documento
     * 
     * @throws IOException 
     */
    @Override
    public String getPath() throws IOException {
        return index.getDocPath(docID);
    }
    
}
