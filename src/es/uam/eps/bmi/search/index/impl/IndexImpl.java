/**
 * 
 * Fichero IndexImpl.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 08/03/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.AbstractIndex;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Clase abstracta IndexImpl encargada del indice 
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public abstract class IndexImpl extends AbstractIndex{
    Map<String, PostingsListImpl> dictionary;
    List<String> docPaths;
    String indexPath;
    
    /**
     * Devuelve el numero de documentos presentes en el indice
     * 
     * @return el numero de documentos
     */
    @Override
    public int numDocs() {
        return this.docPaths.size();
    }
    
    /**
     * Funcion abstracta que devuelve la lista de terminos del indice
     * 
     * @return una coleccion con los terminos del indice
     * @throws IOException 
     */
    @Override
    public abstract Collection<String> getAllTerms() throws IOException;

    /**
     * Devuelve la ruta de un documento dado su ID
     * 
     * @param docID el identificador del documento
     * 
     * @return la ruta para llegar al documento
     * 
     * @throws IOException 
     */
    @Override
    public String getDocPath(int docID) throws IOException {
        return docPaths.get(docID);
    }
    
    /**
     * Añade el path de un documento a la lista de rutas de todos los documentos
     * 
     * @param path la ruta del documento
     */
    public void addDocPath(String path) {
        docPaths.add(path);
    }
    
    /**
     * Crea una lista en la que se guardaran las rutas de los documentos del indice
     */
    public void docPath() {
        docPaths = new ArrayList<>();
    }
    
    /**
     * Devuelve la lista de las rutas de los documentos en el indice
     * 
     * @return la lista de las rutas de los documentos
     */
    public List<String> getPaths(){
        return this.docPaths;
    }
    
    /**
     * Funcion abstracta que carga el indice, el cual ha sido previamente guardado
     * 
     * @param indexPath la ruta donde se encuentra el indice
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */    
    abstract void loadIndex(String indexPath) throws FileNotFoundException, IOException;

    /**
     * Funcion abstracta en la que añadimos al mapa el termino junto con la 
     * posicion en el fichero de sus postings
     * 
     * @param key el termino
     * @param offset el offset en el fichero Config.POSTINGS_FILE
     */    
    abstract void put(String key, long offset);

    
}
