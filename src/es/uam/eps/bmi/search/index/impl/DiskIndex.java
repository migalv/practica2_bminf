/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.AbstractIndex;
import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.PostingsList;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author migal
 */
public class DiskIndex extends AbstractIndex implements Serializable{
  
    Map<String, Long> dictionary;
    int numDocs;
    List<String> docPaths;
    String indexPath;

    public DiskIndex(String indexPath) throws NoIndexException, IOException {
        this.dictionary = new HashMap<>();
        this.indexPath = indexPath;
        
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException(indexPath);
        }
        this.loadIndex(indexPath);

    }
    
    public DiskIndex(){
        this.dictionary = new HashMap<>();
    }

    @Override
    public int numDocs() {
        return numDocs;
    }

    @Override
    public PostingsList getPostings(String term) throws IOException {
        long offset = dictionary.get(term);
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
    public Collection<String> getAllTerms() throws IOException {
        return dictionary.keySet();
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
    public String getDocPath(int docID) throws IOException {
        return docPaths.get(docID);
    }

    public void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException {        
        this.indexPath = indexPath;
        
        //Guardamos en un fichero el diccionario dato a dato
        FileOutputStream fDic = new FileOutputStream(indexPath + File.separator + Config.DICTIONARY_FILE);
        FileOutputStream fPost = new FileOutputStream(indexPath + File.separator + Config.POSTINGS_FILE);
        try (DataOutputStream outDic = new DataOutputStream(fDic); DataOutputStream outPost = new DataOutputStream(fPost);) {
            for (Map.Entry<String, PostingsListImpl> entry : dictionary.entrySet()) {
                long postingBytes = 0;
                // Escribimos el tamaño del termino
                outDic.writeInt(entry.getKey().getBytes().length);
                // Escribimos el termino
                outDic.writeBytes(entry.getKey());
                // Escribimos el numero de postings
                outPost.writeInt(entry.getValue().size());
                postingBytes += Integer.BYTES;
                for(Posting postings : entry.getValue()){
                    // Escribimos el DocID
                    outPost.writeInt(postings.getDocID());
                    postingBytes += Integer.BYTES;
                    // Escribimos la frecuencia 
                    outPost.writeLong(postings.getFreq());
                    postingBytes += Long.BYTES;
                }
                // Escribimos el offset
                long offset = outPost.size() - postingBytes;
                outDic.writeLong(offset);
                this.dictionary.put(entry.getKey(), offset);
            }
            outDic.close();
            outPost.close();
        }
        dictionary.clear();
    }

    public void setNumDocs(int numDocs) {
        this.numDocs = numDocs;
    }

    public void addDocPath(String path) {
        docPaths.add(path);
    }

    public void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        
        int termSize = 0;
        byte[] buffer;
        String term = null;
        long offset = 0;
        
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
                
                dictionary.put(term, offset);
            }
            inDic.close();
        }
    }

    public void docPath() {
        docPaths = new ArrayList<>();
    }  
}
