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

/**
 *
 * @author migal
 */
public class SerializedRAMIndex extends AbstractIndex implements Serializable{

    Map<String, PostingsListImpl> dictionary;
    List<String> docPaths;
    String indexPath;

    public SerializedRAMIndex(String indexPath) throws NoIndexException, IOException {
        if (indexPath.equals("") || (new File(indexPath).exists() == false)  || indexPath == null){
            throw new NoIndexException(indexPath);
        }
        this.loadIndex(indexPath);

    }
    
    public SerializedRAMIndex(){
        
    }

    @Override
    public int numDocs() {
        return this.docPaths.size();
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
        this.dictionary = dictionary;
        this.indexPath = indexPath;
           
        //Guardamos los paths
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.PATHS_FILE))){
            out.writeObject(this.docPaths); 
            out.close();
        }
        
        //Finalmente guardamos el diccionario ordenado
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.INDEX_FILE))){
            out.writeObject(this.dictionary); 
            out.close();
        }
        
    }

    public void addDocPath(String path) {
        docPaths.add(path);
    }

    private void loadIndex(String indexPath) throws FileNotFoundException, IOException {
        
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

    public void docPath() {
        docPaths = new ArrayList<>();
    }
}
