/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.structure.impl;

import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author migal
 */
public class PostingsListImpl implements PostingsList, Serializable {
    
    // Cada tupla es un posting.
    private final List<Posting> postings;
    
    public PostingsListImpl(){
        postings = new ArrayList<>();
    }
    
    public void addNewPosting(Posting newPosting){
        postings.add(newPosting);
    }
    
    public void addPosting(int docID){
        if(!postings.isEmpty() && docID == postings.get(postings.size()-1).getDocID()){
            postings.get(postings.size()-1).add1();
        }else{
            postings.add(new Posting(docID, 1));
        }
    }

    @Override
    public int size() {
        return postings.size();
    }

    @Override
    public Iterator<Posting> iterator() {
        return postings.iterator();
    }
    
}
