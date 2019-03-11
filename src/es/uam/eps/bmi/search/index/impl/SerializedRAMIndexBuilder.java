/**
 * 
 * Fichero SerializedRAMIndexBuilder.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 26/02/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Clase SerializedRAMIndexBuilder encargada de crear el indice en RAM serializado
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class SerializedRAMIndexBuilder extends IndexBuilderImpl{
    
    /**
     * Funcion encargada de crear el indice SerializedRAMIndex en la ruta proporcionada
     * 
     * @param collectionPath ruta donde se encuentran los documentos
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    @Override
    public void build(String collectionPath, String indexPath) throws IOException {
        if (indexPath == null || indexPath.equals("")) {
            throw new NoIndexException(indexPath);
        }
        this.indexPath = indexPath;
        clear(indexPath);
        
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
        
        //Guardamos el indice 
        this.saveDictionary(dictionary,indexPath);
        
        writePaths();

        //Finalmente guardamos las norms de cada termino en disco
        saveDocNorms(indexPath);
        
    }
    
    /**
     * Funcion para crear y recuperar un nuevo index.
     * 
     * @return Index El nuevo indice.
     * 
     * @throws IOException 
     */
    @Override
    public Index getCoreIndex() throws IOException {
        return new SerializedRAMIndex(indexPath, dictionary, paths);
    }
    
    /**
     * Guardamos el diccionario en disco
     * 
     * @param dictionary Diccionario de terminos y lista de postings
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    @Override
    public void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException {
        this.dictionary = dictionary;
        this.indexPath = indexPath;
        
        //Finalmente guardamos el diccionario ordenado
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.INDEX_FILE))){
            out.writeObject(this.dictionary); 
            out.close();
        }
        
    }
    

}
