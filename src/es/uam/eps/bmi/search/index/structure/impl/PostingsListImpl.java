/**
 * 
 * Fichero PostingsListImpl.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 23/02/2019  
 */
package es.uam.eps.bmi.search.index.structure.impl;

import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Clase PostingsListImpl en el que contiene una lista de Postings
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class PostingsListImpl implements PostingsList, Serializable {
    
    // Cada tupla es un posting.
    private final List<Posting> postings;
    
    /**
     * Constructor de PostingsListImpl
     */
    public PostingsListImpl(){
        postings = new ArrayList<>();
    }
    
    /**
     * Funcion en la que se añade un nuevo posting a la lista actual
     * 
     * @param newPosting el nuevo posting a añadir
     */
    public void addNewPosting(Posting newPosting){
        postings.add(newPosting);
    }
    
    /**
     * Funcion en la que se añade un nuevo documento a la lista de postings,
     * comprobando si el documento ya pertenece o no
     * 
     * @param docID el identificador del documento
     */
    public void addPosting(int docID){
        //Si el documento ya se encuentra en la lista, añadimos la frecuencia en 
        //una unidad
        if(!postings.isEmpty() && docID == postings.get(postings.size()-1).getDocID()){
            postings.get(postings.size()-1).add1();
        }else{//Si no, se crea un nuevo posting con el identificador del documento
            postings.add(new Posting(docID, 1));
        }
    }

    /**
     * Devuelve el numero de postings 
     * 
     * @return el numero de postings que forma la lista
     */
    @Override
    public int size() {
        return postings.size();
    }

    /**
     * Iterador de Posting
     *
     * @return el iterador
     */
    @Override
    public Iterator<Posting> iterator() {
        return postings.iterator();
    }
    
}
