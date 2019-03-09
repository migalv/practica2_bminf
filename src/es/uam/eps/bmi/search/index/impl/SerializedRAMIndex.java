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
        
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException(indexPath);
        }
        this.loadIndex(indexPath);

    }
    
    /**
     * Constructor secundario del indice
     */
    public SerializedRAMIndex(){
    }

    /**
     * 
     * @param term
     * @return
     * @throws IOException 
     */
    @Override
    public PostingsList getPostings(String term) throws IOException {
        return dictionary.get(term);
    }
    
    @Override
    public long getDocFreq(String term) throws IOException {
        return dictionary.get(term).size();
    }
    
    @Override
    public long getTotalFreq(String term) throws IOException {

        long total = 0;

        for (Posting posting : dictionary.get(term)) {
            total += posting.getFreq();
        }

        return total;
    }
    
    @Override
    public Collection<String> getAllTerms() throws IOException {
        return dictionary.keySet();
    }

    @Override
    void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        
        //Cargamos los paths
        try(ObjectInputStream in= new ObjectInputStream(new FileInputStream(indexPath + File.separator + Config.PATHS_FILE))){
            this.docPaths =  (List<String>) in.readObject();
            in.close();
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        //Cargamos el diccionario
        try(ObjectInputStream in= new ObjectInputStream(new FileInputStream(indexPath + File.separator + Config.INDEX_FILE))){
            this.dictionary = (Map<String, PostingsListImpl>) in.readObject();
            in.close();
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
        //Cargamos los norms
        loadNorms(indexPath);
    }

    @Override
    void put(String key, long offset) {
    }

}
