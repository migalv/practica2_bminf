/**
 * 
 * Fichero DiskIndexBuilder.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 1/03/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Clase DiskIndexBuilder encargada de crear el indice DiskIndex
 *
 * @author Miguel Alvarez Lesmes
 * @author Sergio Romero Tapiador
 * 
 */
public class DiskIndexBuilder extends IndexBuilderImpl {

    /**
     * Funcion encargada de crear el indice DiskIndex en la ruta proporcionada
     * 
     * @param collectionPath ruta donde se encuentran los documentos
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    @Override
    public void build(String collectionPath, String indexPath) throws IOException {
        if (indexPath == null || indexPath.equals("")) {
            throw new NoIndexException(indexPath);
        }
        this.indexPath = indexPath;
        clear(indexPath);
        
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
        
        //Guardamos el indice 
        this.saveDictionary(dictionary,indexPath);
        
        //Finalmente guardamos las norms de cada termino en disco
        saveDocNorms(indexPath);
        
    }
    
    public Index getCoreIndex() throws IOException {
        return new DiskIndex(indexPath);
    }
    
    /**
     * Guardamos el diccionario en disco
     * 
     * @param dictionary Diccionario de terminos y lista de postings
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    @Override
    public void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException {        
        //Inicializamos la ruta del indice
        this.indexPath = indexPath;
        ByteBuffer bb;
        
        //Guardamos los paths de los documentos
        try(ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(indexPath + File.separator + Config.PATHS_FILE))){
            out.writeObject(paths); 
            out.close();
        }
        
        //Guardamos en un fichero el diccionario dato a dato
        FileOutputStream fDic = new FileOutputStream(indexPath + File.separator + Config.DICTIONARY_FILE);
        FileOutputStream fPost = new FileOutputStream(indexPath + File.separator + Config.POSTINGS_FILE);
        try (DataOutputStream outDic = new DataOutputStream(fDic); DataOutputStream outPost = new DataOutputStream(fPost);) {
            for (Map.Entry<String, PostingsListImpl> entry : dictionary.entrySet()) {
                long postingBytes = 0;
                
                // Escribimos el tamaño del termino en el fichero del diccionario
                outDic.writeInt(entry.getKey().getBytes().length);
                
                // Escribimos el termino en el fichero del diccionario
                outDic.writeBytes(entry.getKey());
                
                // Escribimos el numero de postings en el fichero de postings
                outPost.writeInt(entry.getValue().size());
                
                //Incrementamos la posicion en el fichero que correspondera con el offset
                postingBytes += Integer.BYTES;
                
                //Por cada posting, escribimos 
                for(Posting postings : entry.getValue()){
                    // Escribimos el DocID en el fichero de postings
                    outPost.writeInt(postings.getDocID());
                    postingBytes += Integer.BYTES;
                    
                    // Escribimos la frecuencia en el fichero de postings 
                    outPost.writeLong(postings.getFreq());
                    postingBytes += Long.BYTES;
                }
                // Escribimos el offset en el fichero del diccionario
                long offset = outPost.size() - postingBytes;
                outDic.writeLong(offset);
                
                //Finalmente insertamos en el mapa de terminos el termino y el offset
                //this.index.put(entry.getKey(), offset);
            }
            outDic.close();
            outPost.close();
        }
        dictionary.clear();
    }


}
