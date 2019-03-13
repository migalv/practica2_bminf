/**
 * 
 * Fichero HeapPosting.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 23/02/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.structure.Posting;
import java.util.Iterator;

/**
 * Clase HeapPosting encargada de ordenar los resultados de los diferentes Postings
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class HeapPosting implements Comparable<HeapPosting>{
    private final Posting pl;
    private final Iterator<Posting> ip;
    private final String queryTerm;
    
    /**
     * Constructor de HeapPosting
     * 
     * @param pl posting que tiene un docID y frecuencia
     * 
     * @param ip iterador del posting para las comparaciones
     * 
     * @param queryTerm termino de la query
     */
    public HeapPosting(Posting pl,Iterator<Posting> ip,String queryTerm){
        this.pl=pl;
        this.ip=ip;
        this.queryTerm=queryTerm;
    }
    
    /**
     * Devuelve el posting
     * 
     * @return el posting
     */
    public Posting getPosting(){
        return this.pl;
    }
    
    /**
     * Devuelve el iterador del posting
     * 
     * @return el iterador
     */
    public Iterator<Posting> getIteratorPosting(){
        return this.ip;
    }
    
    /**
     * Devuelve el termino de la query
     * 
     * @return el termino de la query
     */
    public String getQueryTerm(){
        return this.queryTerm;
    }

    /**
     * Comparador de la clase HeapPosting
     * 
     * @param o HeapPosting con el que va a ser comparado
     * 
     * @return devuelve -1 si es menor, 0 si es igual o 1 si es mayor
     */
    @Override
    public int compareTo(HeapPosting o) {
       return this.pl.compareTo(o.pl);
    }

}
