/**
 * 
 * Fichero HeapTPL.java.
 * 
 * 
 * @version 1.0
 * 
 * Created on 11/03/2019  
 */
package es.uam.eps.bmi.search.index.impl;

import es.uam.eps.bmi.search.index.structure.Posting;
import es.uam.eps.bmi.search.index.structure.impl.PostingsListImpl;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.PriorityQueue;

/**
 * La clase HeapTPL (Heap Term-PostingsList) permite almacenar un termino, su 
 * lista de postings además del puntero al stream del cual proviene. 
 * 
 * Esta clase es comparable para que pueda ser utilizada en un heap.
 * Se ordena por termino en orden alfabético y en caso de empate por el indice
 * del archivo del que proviene
 * 
 * @author Miguel Álvarez Lesmes
 * @author Sergio Romero Tapiador
 */
public class HeapTPL implements Comparable<HeapTPL>{

    String term;
    PostingsListImpl postingsList;
    DataInputStream inputStream;
    int iFile;
    
    /**
     * Constructor de la clase
     * 
     * @param term 
     * @param postingsList
     * 
     */
    public HeapTPL(String term, PostingsListImpl postingsList, DataInputStream is, int iF){
        this.term = term;
        this.postingsList = postingsList;
        this.inputStream = is;
        this.iFile = iF;
    } 
    
    public HeapTPL(DataInputStream is, int iF){
        this.term = null;
        this.postingsList = new PostingsListImpl();
        this.inputStream = is;
        this.iFile = iF;
    }
    
    public HeapTPL(String term , PostingsListImpl postingsList, int iF){
        this.term = term;
        this.postingsList = postingsList;
        this.inputStream = null;
        this.iFile = iF;
    }
    
    public HeapTPL(){
        this.term = "";
        this.postingsList = new PostingsListImpl();
        this.inputStream = null;
        this.iFile = -1;
    }
    
    /**
     * Getter de la lista de postings
     * 
     * @return la lista de postings
     */
    public PostingsListImpl getPostingsList() {
        return this.postingsList;
    }
    
     /**
     * Getter para el termino
     * 
     * @return el termino
     */
    public String getTerm() {
        return this.term;
    }
    
    /**
     * Permite añadir nuevos postings a la lista de postings
     * 
     * @param newList Lista con los postings a añadir
     */
    public void appendToList(PostingsListImpl newList){
        for(Posting newPosting : newList){
            this.postingsList.getPostingsList().add(newPosting);
        }
    }
    
    public void closeStream() throws IOException{
        this.inputStream.close();
    }
    
    /**
     * Funcion que permite unir dos heaps si el termino es el mismo.
     * 
     * En caso de que el termino sea igual, las dos lista se unen, además la
     * función devolverá true para afirmar el merge.
     * En caso de que no sean iguales, entonces la función devuelve false
     * y no se hace el merge
     * Además si el Heap fue inicializado con un constructor vacio, merge se encarga
     * de inicializar su termino al de el nuevo heap y de añadir su lista de postings
     * 
     * @param newHeap El nuevo heap a mergear con this.
     * @return 
     */
    public boolean mergeTPL(HeapTPL newHeap){
        
        if(newHeap == null)
            return false;
        
        if(this.term == null){
            this.term = newHeap.term;
        }
        
        if(this.term.equals(newHeap.term)){
            this.appendToList(newHeap.postingsList);
            return true;
        }
        
        return false;
    }
    
    /**
     * Funcion que lee del tream del HeapTPL y carga el nuevo valor del termino
     * y su lista correspondiente.
     * 
     * Si el heapTPL donde fue llamada esta función no tiene un stream asignado
     * devolverá null.
     * Si al stream asignado al HeapTPL no le quedan datos que leer entonces 
     * devolverá null.
     * 
     * @return Si todo fue correcto retorna this, si no retorna null.
     */
    public HeapTPL readNextTPL() throws IOException{
        
        // Longitud en bytes del termino
        int termLength = 0;
        // Numero de postings en la lista de postings
        int numPostings = 0;
        // Buffer para leer el termino del archivo
        byte[] buffer;
        
        // Si es HeapTPL sin inputStream no se puede leer el siguiente
        if(this.inputStream == null){
            return null;
        }
        if(this.inputStream.available() < Integer.BYTES){
            return null;
        }
        
        this.postingsList = new PostingsListImpl();
        
        // Tamaño en bytes del termino
        termLength = inputStream.readInt();
        
        // Leemos el termino
        buffer = new byte[termLength];
        inputStream.read(buffer, 0, termLength);
        term = new String(buffer);
        
        // Recuperamos el tamaño de la lista de postings del termino
        numPostings = inputStream.readInt();
        
        // Leemos la lista de postings
        for(int i = 0; i < numPostings; i++){
            // Leemos el docID del posting
            int docID = inputStream.readInt();
            // Leemos la frecuencia del termino
            long freq = inputStream.readLong();
            postingsList.addNewPosting(new Posting(docID, freq));
        }
        
        return this;
    }
    
    @Override
    public int compareTo(HeapTPL o) {
        
        int diff = this.term.compareTo(o.term);
        
        // Si son iguales comparalos por indice de archivo
        if(diff == 0){
            return o.iFile - this.iFile;
        }
        
        return diff;
    }

    public HeapTPL cloneHeap() {
        return new HeapTPL(this.term, this.postingsList, this.inputStream, this.iFile);
    }
}
