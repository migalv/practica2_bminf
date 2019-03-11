/**
 * 
 * Fichero SerializedRAMIndex.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 26/02/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Clase SerializedRAMIndex encargada del indice en RAM serializado
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class SerializedRAMIndex extends IndexImpl implements Serializable{

    /**
     * Constructor primario de SerializedRAMIndex
     * 
     * @param indexPath ruta del indice
     * 
     * @throws NoIndexException
     * @throws IOException 
     */
    public SerializedRAMIndex(String indexPath) throws NoIndexException, IOException {
        
        docPaths = new ArrayList<>();
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException(indexPath);
        }
        this.loadIndex(indexPath);

    }
    
    /**
     * Constructor secundario del indice
     * @param indexPath
     * @param dictionary
     * 
     */
    public SerializedRAMIndex(String indexPath, Map<String, PostingsListImpl> dictionary, List<String> paths){
        docPaths = new ArrayList<>();
        this.dictionary = dictionary;
        this.docPaths = paths;
    }

    /**
     * Devuelve una lista de postings dado un termino
     * 
     * @param term el termino
     * 
     * @return la lista de postings vinculada al termino
     * 
     * @throws IOException 
     */
    @Override
    public PostingsList getPostings(String term) throws IOException {
        return dictionary.get(term);
    }
    
    /**
     * Devuelve el numero de documentos en el que se encuentra el termino term
     * 
     * @param term el termino
     * 
     * @return el numero de documentos presentes
     * 
     * @throws IOException 
     */
    @Override
    public long getDocFreq(String term) throws IOException {
        return dictionary.get(term).size();
    }
    
    /**
     * Dado un termino devolvemos su frecuencia en el indice
     * 
     * @param term el termino
     * 
     * @return la frecuencia total del termino
     * 
     * @throws IOException 
     */
    @Override
    public long getTotalFreq(String term) throws IOException {

        long total = 0;

        //Sumamos el acumulado de la frecuencia del termino
        //en cada documento en el que se encuentra
        for (Posting posting : dictionary.get(term)) {
            total += posting.getFreq();
        }

        return total;
    }
    
    /**
     * Devuelve la lista de terminos del indice
     * 
     * @return una coleccion con los terminos del indice
     * @throws IOException 
     */
    @Override
    public Collection<String> getAllTerms() throws IOException {
        return dictionary.keySet();
    }

    /**
     * Carga el indice SerializedRAMIndex, el cual ha sido previamente guardado
     * 
     * @param indexPath la ruta donde se encuentra el indice
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    @Override
    void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        
        loadPaths(indexPath);
        
        //Cargamos el diccionario de los terminos junto con sus respectivas listas de postings
        try(ObjectInputStream in= new ObjectInputStream(new FileInputStream(indexPath + File.separator + Config.INDEX_FILE))){
            this.dictionary = (Map<String, PostingsListImpl>) in.readObject();
            in.close();
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        //Cargamos los norms
        loadNorms(indexPath);
    }
    
    /**
     * Funcion sin uso para esta clase
     * 
     * @param key el termino
     * @param offset el offset en el fichero Config.POSTINGS_FILE
     */
    @Override
    void put(String key, long offset) {
    }

}
