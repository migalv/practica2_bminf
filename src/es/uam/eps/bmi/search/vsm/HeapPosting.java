/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.vsm;

import es.uam.eps.bmi.search.index.structure.Posting;
import java.util.Iterator;

/**
 *
 * @author e340875
 */
public class HeapPosting implements Comparable{
    private final Posting pl;
    private final Iterator<Posting> ip;
    private final String queryTerm;
    
    public HeapPosting(Posting pl,Iterator<Posting> ip,String queryTerm){
        this.pl=pl;
        this.ip=ip;
        this.queryTerm=queryTerm;
    }
    
    public Posting getPosting(){
        return this.pl;
    }
    public Iterator<Posting> getIteratorPosting(){
        return this.ip;
    }
    public String getQueryTerm(){
        return this.queryTerm;
    }

    @Override
    public int compareTo(Object o) {
       return this.pl.compareTo((Posting)o);
    }
}
