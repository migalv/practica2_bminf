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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author migal
 */
public class SerializedRAMIndex extends AbstractIndex implements Serializable{

    Map<String, PostingsListImpl> dictionary;
    int numDocs;
    List<String> docPaths;
    String indexPath;

    public SerializedRAMIndex(String indexPath) throws NoIndexException, IOException {
        if (indexPath == null || indexPath.equals("")) {
            throw new NoIndexException("Ruta esta vacia");
        }
        this.loadIndex(indexPath);

    }
    
    public SerializedRAMIndex(){
        
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

              
        //Finalmente guardamos el diccionario ordenado
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.INDEX_FILE))){
            out.writeObject(this.dictionary); 
            out.close();
        }

    }

    public void setNumDocs(int numDocs) {
        this.numDocs = numDocs;
    }

    public void addDocPath(String path) {
        docPaths.add(path);
    }

    private void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        
        try(ObjectInputStream in= new ObjectInputStream(new FileInputStream(indexPath + File.separator + Config.INDEX_FILE))){
            this.dictionary = (Map<String, PostingsListImpl>) in.readObject();
            in.close();
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void docPath() {
        docPaths = new ArrayList<>();
    }
}
