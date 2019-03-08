/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author sergio
 */
public abstract class IndexImpl extends AbstractIndex{
    Map<String, PostingsListImpl> dictionary;
    List<String> docPaths;
    String indexPath;
    
    @Override
    public int numDocs() {
        return this.docPaths.size();
    }
    
    @Override
    public abstract Collection<String> getAllTerms() throws IOException;

    @Override
    public String getDocPath(int docID) throws IOException {
        return docPaths.get(docID);
    }
    
    public void addDocPath(String path) {
        docPaths.add(path);
    }
    
    public void docPath() {
        docPaths = new ArrayList<>();
    }
    
    public List<String> getPaths(){
        return this.docPaths;
    }
    
    abstract void loadIndex(String indexPath) throws FileNotFoundException, IOException;

    abstract void put(String key, long offset);

    
}
