/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
/**
 *
 * @author migal
 */
public class DiskIndexBuilder extends IndexBuilderImpl {

    @Override
    public void build(String collectionPath, String indexPath) throws IOException {
        if (indexPath == null || indexPath.equals("")) {
            throw new NoIndexException(indexPath);
        }
        this.indexPath = indexPath;
        clear(indexPath);
        
        index = new DiskIndex();
        index.docPath();

        
        // Abrimos el fichero que nos pasan como coleccion
        File f = new File(collectionPath);
        
        // Si es un directorio lo abrimos como directorio
        if (f.isDirectory()) 
            indexFolder(f);
        // Si es un Zip lo abrimos como zip
        else if (f.getName().endsWith(".zip"))
            indexZip(f);
        // Si es un archivo, lo abrimos y leemos las urls
        else indexURLs(f);
        
        this.saveDictionary(dictionary,indexPath);
        index.loadIndex(indexPath);
        
        saveDocNorms(indexPath);
        
    }
    
    @Override
    public void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException {        
        this.indexPath = indexPath;
        
        //Guardamos los paths
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.PATHS_FILE))){
            out.writeObject(index.getPaths()); 
            out.close();
        }
        
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
                this.index.put(entry.getKey(), offset);
            }
            outDic.close();
            outPost.close();
        }
        dictionary.clear();
    }


}
