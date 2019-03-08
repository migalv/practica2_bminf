/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author migal
 */
public class DiskIndex extends IndexImpl{
    public Map<String, Long> dictionaryTerms;


    public DiskIndex(String indexPath) throws NoIndexException, IOException {
        this.dictionaryTerms = new HashMap<>();
        this.indexPath=indexPath;
        
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException(indexPath);
        }
        this.loadIndex(indexPath);

    }
    
    public DiskIndex(){
        this.dictionaryTerms = new HashMap<>();
    }
    
    @Override
    public Collection<String> getAllTerms() throws IOException {
        return dictionaryTerms.keySet();
    }


    @Override
    public PostingsList getPostings(String term) throws IOException {
        long offset = dictionaryTerms.get(term);
        int numPostings = 0;
        int docID = 0;
        long freq = 0;
        PostingsListImpl postingsList = new PostingsListImpl();

        try ( //Guardamos en un fichero el diccionario dato a dato
            FileInputStream fPost = new FileInputStream(indexPath + File.separator + Config.POSTINGS_FILE)) {
            fPost.skip(offset);
            try (DataInputStream inPost = new DataInputStream(fPost)) {
                numPostings = inPost.readInt();
                for(int i = 0; i < numPostings; i++){
                    docID = inPost.readInt();
                    freq = inPost.readLong();
                    postingsList.addNewPosting(new Posting(docID, freq));
                }
            }
        }
        
        return postingsList;
    }

    @Override
    public long getTotalFreq(String term) throws IOException {

        long total = 0;

        for (Posting posting : getPostings(term)) {
            total += posting.getFreq();
        }

        return total;
    }
    
    @Override
    public long getDocFreq(String term) throws IOException {
        return getPostings(term).size();
    }
    
    
    @Override
    public void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        
        int termSize = 0;
        byte[] buffer;
        String term = null;
        long offset = 0;
        
        this.indexPath=indexPath;
        
        //Cargamos los paths
        try(ObjectInputStream in= new ObjectInputStream(new FileInputStream(indexPath + File.separator + Config.PATHS_FILE))){
            this.docPaths =  (List<String>) in.readObject();
            in.close();
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        // Abrimos el archivo del dictionario
        FileInputStream fDic = new FileInputStream(indexPath + File.separator + Config.DICTIONARY_FILE);
        try (DataInputStream inDic = new DataInputStream(fDic)) {
            // Leemos hasta el final del archivo
            while(inDic.available() > 0) {
                // Leemos la longitud del termino
                termSize = inDic.readInt();
                buffer = new byte[termSize];
                //Leemos el termino
                inDic.read(buffer, 0, termSize);
                term = new String(buffer);
                // Leemos el offset de la lista de postings
                offset = inDic.readLong();
                
                dictionaryTerms.put(term, offset);
            }
            inDic.close();
        }
        //Cargamos los norms
        loadNorms(indexPath);
    }

    public void put(String key, long offset) {
        this.dictionaryTerms.put(key, offset);
    }

}
