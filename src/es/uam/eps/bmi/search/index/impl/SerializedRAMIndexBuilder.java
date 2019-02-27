/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.AbstractIndexBuilder;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author migal
 */
public class SerializedRAMIndexBuilder extends AbstractIndexBuilder{

    long numDocs = 0;
    Map<String, List<Posting>> diccionario = new HashMap<>();
    
    @Override
    protected void indexText(String text, String path) throws IOException {        
        // Recuperamos todos los terminos del texto
        String terms[];
        
        // Contamos un nuevo documento
        numDocs++;
        
        terms = text.toLowerCase().split("\\P{Alpha}+");
        
        // Por cada termino lo añadimos al diccionario y vamos contando cada vez que aparece
        for(String term : terms){
            if(diccionario.containsKey(term)){
                diccionario.replace(term, diccionario.get(term));
            }else{
                diccionario.put(term, new ArrayList().add(new Posting(numDocs - 1, 1)));
            }
        }
    }

    @Override
    protected Index getCoreIndex() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void build(String collectionPath, String indexPath) throws IOException {
        clear(indexPath);
        numDocs = 0;
        
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
        
        saveDocNorms(indexPath);
        
        return;
    }


}
