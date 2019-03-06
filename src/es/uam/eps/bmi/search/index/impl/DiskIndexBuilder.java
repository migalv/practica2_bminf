/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.AbstractIndexBuilder;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author migal
 */
public class DiskIndexBuilder  extends AbstractIndexBuilder{
    
    int numDocs = 0;
    Map<String, PostingsListImpl> dictionary = new HashMap<>();
    String indexPath;
    DiskIndex index;
    
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
                PostingsListImpl pli = dictionary.get(term);
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
    public void build(String collectionPath, String indexPath) throws IOException {
        if (indexPath == null || indexPath.equals("")) {
            throw new NoIndexException("Ruta esta vacia");
        }
        this.indexPath = indexPath;
        clear(indexPath);
        numDocs = 0;
        
        index = new DiskIndex();
        index.docPath();

        
        // Abrimos el fichero que nos pasan como coleccion
        File f = new File(collectionPath);
        
        // Si es un directorio lo abrimos como directorio
        if (f.isDirectory()) 
            indexFolder(f);
        // Si es un Zip lo abrimos como zip
        else if (f.getName().endsWith(".zip"))
            indexZip(f);
        // Si es un archivo, lo abrimos y leemos las urls
        else indexURLs(f);
        
        index.saveDictionary(dictionary,indexPath);
        index.setNumDocs(numDocs);
        
        saveDocNorms(indexPath);
        
    }
}
