/**
 * 
 * Fichero EfficientIndexBuilder.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 11/03/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.Config;
import es.uam.eps.bmi.search.index.Index;
import es.uam.eps.bmi.search.index.NoIndexException;
import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Clase que realiza la construccion de un indice de forma eficiente.
 * 
 * Se encarga de limitar el uso de RAM a no más de 1GB, creando indices parciales 
 * que posteriormente serán mergeados para la creacion de un indice final.
 * 
 * @author Miguel Álvarez Lesmes
 * @author Sergio Romero Tapiador
 */
public class EfficientIndexBuilder extends IndexBuilderImpl {

    // Diccionario ordenado por termino
    Map<String, PostingsListImpl> sortedDict = new TreeMap<>();
    // Numero de indices parciales creados
    int numParcialIndex = 0;
    private final String PARCIAL_INDEX_FILE = "parcial_index";
    // Stream de datos hacia el archivo que contiene el diccionario final
    DataOutputStream dictionaryStream;
    // Stream de datos hacia el archivo que contiene las listas de postings
    DataOutputStream postingsStream;
    
    /**
     * Funcion que se encarga de recuperar los terminos de un texto para crear 
     * el diccionario.
     * 
     * Esta version de la función limita el uso de ram a no más de 1GB creando 
     * asi indices parciales
     * 
     * @param text el texto del documento
     * @param path la ruta del documento
     * 
     * @throws IOException 
     */
    @Override
    protected void indexText(String text, String path) throws IOException {
        // Recuperamos todos los terminos del texto
        String terms[];
        
        // Contamos un nuevo documento
        numDocs++;
        
        // Cada mil documentos guardamos el diccionario como indice parcial
        // Y liberamos la RAM
        if(numDocs % 1000 == 0){
            // La funcion saveDictionary ya hace clear del dictionary
            saveDictionary(sortedDict, indexPath);
        }
        
        // Añadimos su path
        paths.add(path);
        
        terms = text.toLowerCase().split("\\P{Alpha}+");
        
        // Por cada termino lo añadimos al diccionario y vamos contando cada vez que aparece
        for(String term : terms){
            // El termino ya existe en el diccionario
            if(sortedDict.containsKey(term)){
                // Recuperamos su lista de postings
                PostingsListImpl pli = (PostingsListImpl) sortedDict.get(term);
                // Añadimos el posting a la lista
                pli.addPosting(numDocs - 1);
            }else{ // Es la primera vez que aparece el termino en el diccionario
                // Creamos una lista de postings
                PostingsListImpl pli = new PostingsListImpl();
                // Le añadimos un posting para este documento
                pli.addPosting(numDocs - 1);
                // Añadimos el termino en el diccionario
                sortedDict.put(term, pli);
            }
        }
    }
    @Override
    protected Index getCoreIndex() throws IOException {
        return new DiskIndex(indexPath);
    }

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
        
        FileOutputStream fDic = new FileOutputStream(indexPath + File.separator + Config.DICTIONARY_FILE);
        FileOutputStream fPost = new FileOutputStream(indexPath + File.separator + Config.POSTINGS_FILE);
        dictionaryStream = new DataOutputStream(fDic); 
        postingsStream = new DataOutputStream(fPost);
        
        // Hacemos un último saveDictionary para los últimos documentos
        saveDictionary(sortedDict, indexPath);
        
        kmerge();
        
        dictionaryStream.close();
        postingsStream.close();
        fDic.close();
        fPost.close();
        
        deleteParcialFiles();
        
        writePaths();
        
        //Finalmente guardamos las norms de cada termino en disco
        saveDocNorms(indexPath);
    }

    /**
     * Guardamos el diccionario parcial en disco
     * 
     * @param dictionary Diccionario ordenado por terminos y valor: lista de postings
     * 
     * @param indexPath ruta del indice
     * 
     * @throws IOException 
     */
    @Override
    public void saveDictionary(Map<String, PostingsListImpl> dictionary, String indexPath) throws IOException {
        //Inicializamos la ruta del indice
        this.indexPath = indexPath;
        numParcialIndex++;
        
        //Guardamos los paths de los documentos
        writePaths();
        
        //Guardamos en un fichero el indice parcial
        FileOutputStream fParcialIndex = new FileOutputStream(indexPath + File.separator + this.PARCIAL_INDEX_FILE + numParcialIndex + ".dat");
        try (DataOutputStream outParcialIndex = new DataOutputStream(fParcialIndex)) {
            for (Map.Entry<String, PostingsListImpl> entry : dictionary.entrySet()) {
                
                // Escribimos el tamaño del termino en el fichero de indice parcial
                outParcialIndex.writeInt(entry.getKey().getBytes().length);
                
                // Escribimos el termino en el fichero de indice parcial
                outParcialIndex.writeBytes(entry.getKey());
                
                // Escribimos el numero de postings en el fichero de de indice parcial
                outParcialIndex.writeInt(entry.getValue().size());
                
                //Por cada posting, escribimos 
                for(Posting postings : entry.getValue()){
                    // Escribimos el DocID en el fichero de indice parcial
                    outParcialIndex.writeInt(postings.getDocID());
                    
                    // Escribimos la frecuencia en el fichero de indice parcial
                    outParcialIndex.writeLong(postings.getFreq());
                }
            }
            outParcialIndex.close();
            fParcialIndex.close();
        }
        dictionary.clear();
    }

    /**
     * Función kMerge que se encarga de mergear los indices parciales en un
     * único indice final.
     * 
     * @throws FileNotFoundException, IOException
     */
    private void kmerge() throws FileNotFoundException, IOException{
        
        PriorityQueue<HeapTPL> kHeap = new PriorityQueue<>(numParcialIndex);
        
        // Abrimos los archivos de los indices parciales 
        // añadimos los k primeros pares (Termino/Lista de postings) al kHeap
        for(int i = 1; i < numParcialIndex+1; i++){
            // Abrimos archivo i-esimo y añadimos su puntero a la lista de punteros
            FileInputStream fInputStream = new FileInputStream(indexPath + File.separator + this.PARCIAL_INDEX_FILE + i + ".dat");
            // Leemos el primer par (Termino/Lista de postings)
            HeapTPL heapTPL = new HeapTPL(new DataInputStream(fInputStream), i);
            // Si hay más TPL los añadimos al k-Heap
            if(heapTPL.readNextTPL() != null)
                kHeap.add(heapTPL);
        }

        // Mientras el kHeap no esté vacio
        do{
            // Sacamos el primer HTPL
            HeapTPL toFileHeap = kHeap.poll();
            HeapTPL auxHeap = toFileHeap.cloneHeap();
            if(auxHeap.readNextTPL() != null)
                kHeap.add(auxHeap);
            else // Como ya no hay más datos que leer cerramos el stream
                auxHeap.closeStream();
            // Mientras el termino del HeapTPL sea el mismo que el de la cima del kHeap 
            // lo sacamos del kHeap y lo mergeamos
            if(!kHeap.isEmpty()){
                while(toFileHeap.getTerm().equals(kHeap.peek().getTerm())){
                    HeapTPL outHTPL = kHeap.poll();
                    toFileHeap.mergeTPL(outHTPL);
                    // Como hemos sacado del Heap leemos el siguiente TPL del HTPL que hemos sacado
                    if(outHTPL.readNextTPL() != null)
                        kHeap.add(outHTPL);
                    else // Como ya no hay mas datos que leer cerramos el stream
                        auxHeap.closeStream();
                }
            }
            // Finalmente escribimos el HTPL en el indice final
            writeHeapTPL(toFileHeap);
        } while(!kHeap.isEmpty());
    }
    
    private void writeHeapTPL(HeapTPL htpl) throws IOException{
        long postingBytes = 0;
                
        // Escribimos el tamaño del termino en el fichero del diccionario
        dictionaryStream.writeInt(htpl.getTerm().getBytes().length);

        // Escribimos el termino en el fichero del diccionario
        dictionaryStream.writeBytes(htpl.getTerm());

        // Escribimos el numero de postings en el fichero de postings
        postingsStream.writeInt(htpl.getPostingsList().size());

        //Incrementamos la posicion en el fichero que correspondera con el offset
        postingBytes += Integer.BYTES;

        //Por cada posting, escribimos 
        for(Posting posting : htpl.getPostingsList()){
            // Escribimos el DocID en el fichero de postings
            postingsStream.writeInt(posting.getDocID());
            postingBytes += Integer.BYTES;

            // Escribimos la frecuencia en el fichero de postings 
            postingsStream.writeLong(posting.getFreq());
            postingBytes += Long.BYTES;
        }
        // Escribimos el offset en el fichero del diccionario
        long offset = postingsStream.size() - postingBytes;
        dictionaryStream.writeLong(offset);
    }

    private void deleteParcialFiles() {
        
        for(int i = 1; i < numParcialIndex+1; i++){
            File deleteFile = new File(indexPath + File.separator + this.PARCIAL_INDEX_FILE + i + ".dat");
            if(deleteFile.delete())
                i = i;
        }
    }
}
