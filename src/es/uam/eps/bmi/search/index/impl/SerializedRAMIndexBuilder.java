/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 *
 * @author migal
 */
public class SerializedRAMIndexBuilder extends IndexBuilderImpl{

    @Override
    public void build(String collectionPath, String indexPath) throws IOException {
        if (indexPath == null || indexPath.equals("")) {
            throw new NoIndexException("Ruta esta vacia");
        }
        this.indexPath = indexPath;
        clear(indexPath);
        
        index = new SerializedRAMIndex();
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
        
        this.saveDictionary(dictionary,indexPath);
        index.loadIndex(indexPath);

        
        saveDocNorms(indexPath);
        
    }
    
    @Override
    public void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException {
        this.dictionary = dictionary;
        this.indexPath = indexPath;
           
        //Guardamos los paths
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.PATHS_FILE))){
            out.writeObject(index.getPaths()); 
            out.close();
        }
        
        //Finalmente guardamos el diccionario ordenado
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.INDEX_FILE))){
            out.writeObject(this.dictionary); 
            out.close();
        }
        
    }
    

}
