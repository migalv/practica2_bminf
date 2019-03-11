/**
 * 
 * Fichero DiskIndex.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 1/03/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase DiskIndex encargada del indice en disco combinado con la RAM
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class DiskIndex extends IndexImpl{
    public Map<String, Long> dictionaryTerms;

    /**
     * Constructor primario de DiskIndex
     * 
     * @param indexPath ruta donde se encuentra el indice
     * 
     * @throws NoIndexException
     * @throws IOException 
     */
    public DiskIndex(String indexPath) throws NoIndexException, IOException {
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException(indexPath);
        }
        
        this.dictionaryTerms = new HashMap<>();
        this.indexPath=indexPath;
        
        //Cargamos el indice para poder tratarlo 
        this.loadIndex(indexPath);

    }
    
    /**
     * Constructor Secundario de DiskIndex 
     */
    public DiskIndex(){
        this.dictionaryTerms = new HashMap<>();
    }
    
    /**
     * Devuelve la lista de terminos del indice
     * 
     * @return una coleccion con los terminos del indice
     * @throws IOException 
     */
    @Override
    public Collection<String> getAllTerms() throws IOException {
        return dictionaryTerms.keySet();
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
        //Inicializamos variables
        long offset = dictionaryTerms.get(term);//Leemos la posicion en el fichero donde se encuentran los postings
        PostingsListImpl postingsList = new PostingsListImpl();
        int numPostings = 0;
        int docID = 0;
        long freq = 0;
        // Tamaño en Bytes de un posting
        int postingSize = Integer.BYTES + Long.BYTES;
        int j;
        // Wrapper para trabajar con los bytes
        ByteBuffer wrapped;
        
        // Abrimos un archivo para leer el archivo de postings
        RandomAccessFile postingFile = new RandomAccessFile(indexPath + File.separator + Config.POSTINGS_FILE, "r");
        // Nos colocamos en el offset donde empiezan los postings del termino
        postingFile.seek(offset);
        // Tamaño en bytes
        int postingsBytes = 0;
        
        // Leemos el numero de postings
        numPostings = postingFile.readInt();
        // Calculamos el tamaño en bytes de un posting
        postingsBytes = Integer.BYTES * numPostings + Long.BYTES * numPostings;
        
        // Leemos la lista de postings del archivo y nos la traemos a RAM
        byte[] buffer = new byte[postingsBytes];
        postingFile.read(buffer, 0, postingsBytes);

        // Creamos a lista de postings apartir de los bytes leidos
        for(int i = 0; i < numPostings; i++){
            byte[] arr = new byte[Integer.BYTES];
            // Leemos el docID en bytes
            for(j = 0; j < Integer.BYTES; j++){
                arr[j] = buffer[(i*postingSize)+j];
            }
            // Transformamos los bytes en integer
            wrapped = ByteBuffer.wrap(arr);
            docID = wrapped.getInt();
            
            // Leemos la frecuencia en bytes
            arr = new byte[Long.BYTES];
            for(j = Integer.BYTES; j < Long.BYTES + Integer.BYTES; j++){
                arr[j - Integer.BYTES] = buffer[(i*postingSize)+j];
            }
            // Transformamos los bytes en long
            wrapped = ByteBuffer.wrap(arr);
            freq = wrapped.getLong();
            
            // Añadimos el posting a la lista de postings.
            postingsList.addNewPosting(new Posting(docID, freq));
        }
        
        return postingsList;
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
        for (Posting posting : getPostings(term)) {
            total += posting.getFreq();
        }

        return total;
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
        return getPostings(term).size();
    }
    
    /**
     * Carga el indice DiskIndex, el cual ha sido previamente guardado
     * 
     * @param indexPath la ruta donde se encuentra el indice
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    @Override
    public void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        //Inicializacion de variables
        int termSize = 0;
        byte[] buffer;
        String term = null;
        long offset = 0;
        this.indexPath=indexPath;
        
        //Cargamos los path de los documentos
        loadPaths(indexPath);
        
        // Abrimos el archivo del diccionario
        FileInputStream fDic = new FileInputStream(indexPath + File.separator + Config.DICTIONARY_FILE);
        try (DataInputStream inDic = new DataInputStream(fDic)) {
            
            // Leemos hasta el final del archivo
            // Este archivo contiene el termino junto con el offset donde se encuentra la lista
            // de postings en el fichero proporcionado por Config.POSTINGS_FILE
            while(inDic.available() > 0) {
                // Leemos la longitud del termino
                termSize = inDic.readInt();
                buffer = new byte[termSize];
                
                //Leemos el termino
                inDic.read(buffer, 0, termSize);
                term = new String(buffer);
                
                // Leemos el offset de la lista de postings
                offset = inDic.readLong();
                
                //Finalmente lo añadimos al mapa de terminos y offsets
                dictionaryTerms.put(term, offset);
            }
            inDic.close();
        }
        
        //Cargamos las norms
        loadNorms(indexPath);
    }

    /**
     * Añadimos al mapa el termino junto con la posicion en el fichero de sus postings
     * 
     * @param key el termino
     * @param offset el offset en el fichero Config.POSTINGS_FILE
     */
    @Override
    public void put(String key, long offset) {
        this.dictionaryTerms.put(key, offset);
    }

}
