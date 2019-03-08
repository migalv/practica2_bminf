/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.AbstractIndexBuilder;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public abstract class IndexBuilderImpl  extends AbstractIndexBuilder{
    int numDocs = 0;
    Map<String, PostingsListImpl> dictionary = new HashMap<>();
    String indexPath;
    IndexImpl index;
    
    @Override
    protected void indexText(String text, String path) throws IOException {        
        // Recuperamos todos los terminos del texto
        String terms[];
        
        // Contamos un nuevo documento
        numDocs++;
        // Añadimos su path
        index.addDocPath(path);
        
        terms = text.toLowerCase().split("\\P{Alpha}+");
        
        // Por cada termino lo añadimos al diccionario y vamos contando cada vez que aparece
        for(String term : terms){
            // El termino ya existe en el diccionario
            if(dictionary.containsKey(term)){
                // Recuperamos su lista de postings
                PostingsListImpl pli = (PostingsListImpl) dictionary.get(term);
                // Añadimos el posting a la lista
                pli.addPosting(numDocs - 1);
            }else{ // Es la primera vez que aparece el termino en el diccionario
                // Creamos una lista de postings
                PostingsListImpl pli = new PostingsListImpl();
                // Le añadimos un posting para este documento
                pli.addPosting(numDocs - 1);
                // Añadimos el termino en el diccionario
                dictionary.put(term, pli);
            }
        }
    }

    @Override
    protected Index getCoreIndex() throws IOException {
        return index;
    }
    
    @Override
    public abstract void build(String collectionPath, String indexPath) throws IOException;
    abstract void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException;

}
