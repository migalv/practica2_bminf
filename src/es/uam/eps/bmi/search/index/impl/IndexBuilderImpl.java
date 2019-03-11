/**
 * 
 * Fichero IndexBuilderImpl.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 8/03/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.AbstractIndexBuilder;
import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Clase abstracta IndexBuilderImpl encargada de gestionar los diferentes indices 
 * implementados
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public abstract class IndexBuilderImpl  extends AbstractIndexBuilder{
    int numDocs = 0;
    Map<String, PostingsListImpl> dictionary = new HashMap<>();
    String indexPath;
    IndexImpl index;
    List<String> paths = new ArrayList<>();
    
    /**
     * Funcion comun en la que realiza el parseo de los documentos
     * y guarda los terminos en el mapa junto con sus respectivas listas de postings
     * actualizadas a cada momento
     * 
     * @param text el texto del documento
     * 
     * @param path la ruta del documento
     * 
     * @throws IOException 
     */
    @Override
    protected void indexText(String text, String path) throws IOException {        
        // Recuperamos todos los terminos del texto
        String terms[];
        
        // Contamos un nuevo documento
        numDocs++;
        // Añadimos su path
        paths.add(path);
        
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

    /**
     * Devuelve el indice requerido
     * 
     * @return el indice 
     * 
     * @throws IOException 
     */
    @Override
    protected abstract Index getCoreIndex() throws IOException;
    
    /**
     * Funcion abstracta encargada de crear el indice en la ruta proporcionada
     * 
     * @param collectionPath ruta donde se encuentran los documentos
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    @Override
    public abstract void build(String collectionPath, String indexPath) throws IOException;
    
    /**
     * Funcion abstracta encargada de guardar el diccionario en disco
     * 
     * @param dictionary Diccionario de terminos y lista de postings
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    abstract void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException;

    /**
     * Funcion común que se encarga de escribir los paths de los archivos a un 
     * archivo.
     * 
     * @throws IOException 
     */
    protected void writePaths() throws FileNotFoundException, IOException{
        
        try (FileOutputStream outputStream = new FileOutputStream(indexPath + File.separator + Config.PATHS_FILE)) {
            for(String path : paths){
                outputStream.write((path + " ").getBytes());
            }
            outputStream.close();
        }
    }
    
}
