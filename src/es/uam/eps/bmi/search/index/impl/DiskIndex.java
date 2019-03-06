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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author migal
 */
public class DiskIndex extends AbstractIndex implements Serializable{
  
    Map<String, PostingsListImpl> dictionary;
    int numDocs;
    List<String> docPaths;
    String indexPath;

    public DiskIndex(String indexPath) throws NoIndexException, IOException {
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException("Ruta esta vacia");
        }
        this.loadIndex(indexPath);

    }
    
    public DiskIndex(){
        
    }

    @Override
    public int numDocs() {
        return numDocs;
    }

    @Override
    public PostingsList getPostings(String term) throws IOException {
        return dictionary.get(term);
    }

    @Override
    public Collection<String> getAllTerms() throws IOException {
        return dictionary.keySet();
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
    public long getDocFreq(String term) throws IOException {
        return dictionary.get(term).size();
    }

    @Override
    public String getDocPath(int docID) throws IOException {
        return docPaths.get(docID);
    }

    public void saveDictionary(Map<String, PostingsListImpl> dictionary,String indexPath) throws IOException {
        this.dictionary = new TreeMap<>(dictionary);
        this.indexPath = indexPath;

        // Limpiamos la memoria RAM para no tener dos diccionarios a la vez
        dictionary.clear();
        dictionary = null;

        //Guardamos en un fichero el diccionario dato a dato
        //HAY QUE REVISAR QUÉ ESTRUCTURA QUEREMOS ESCRIBIR Y EN DONDE
        FileWriter fDic = new FileWriter(indexPath + File.separator + Config.DICTIONARY_FILE);
        FileWriter fPost = new FileWriter(indexPath + File.separator + Config.POSTINGS_FILE);
        try (BufferedWriter outDic = new BufferedWriter(fDic); BufferedWriter outPost = new BufferedWriter(fPost);) {
            for (Map.Entry<String, PostingsListImpl> entry : this.dictionary.entrySet()) {
                outDic.write(entry.getKey()+"\n");
                for(Posting postings : entry.getValue()){
                    outPost.write(postings.getDocID() + "\t" + postings.getFreq()+"|");
                }
                outPost.write("\n");
            }
            outDic.close();
            outPost.close();
        }
        
        this.dictionary.clear();

    }

    public void setNumDocs(int numDocs) {
        this.numDocs = numDocs;
    }

    public void addDocPath(String path) {
        docPaths.add(path);
    }

    private void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        String term;
        this.dictionary = new TreeMap<>();
        
        //Cargamos el indice termino a termino
        FileReader fDic = new FileReader(indexPath + File.separator + Config.DICTIONARY_FILE);
        try (BufferedReader in = new BufferedReader(fDic)) {
            while((term = in.readLine() )!= null){
                this.dictionary.put(term,null);
            }
            in.close();
        }
        
        
    }

    public void docPath() {
        docPaths = new ArrayList<>();
    }  
}
