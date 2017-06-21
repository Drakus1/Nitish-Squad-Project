/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sam
 */


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import java.util.Collections;


import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.Term;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.FileReader;
import java.util.HashSet;
import javax.swing.DefaultListModel;

public class ClientCommunication {
    
    public final int sigDigits = 4;
    
    private static Conversions myConversions;
    
    private double topThreshold;
    private Vector queryVector;
    
    private String server;
    private int port;
    private String dataDir;
    private String indexDir;
    
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private DefaultListModel<String> listModel;
    private ArrayList<Double> products;
    
    static {
        System.load("/Users/sam/NetBeansProjects/myGMP/dist/myGMP.dylib");
    }
    
    private native void initialize(int numBits, int seed);
    private native String encrypt(int plaintext);
    private native int decrypt(String ciphertext);
    private native void clear();
    
    public ClientCommunication(String server, int port) throws Exception {
        
        this.server = server;
        this.port = port;
        
        this.socket = new Socket(server, port);
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        
        this.listModel = (DefaultListModel<String>) inputStream.readObject();
        myConversions = new Conversions(sigDigits);
        return;
    }
    
    public DefaultListModel<String> getListModel() {
        return this.listModel;
    }
    
    public ArrayList<Double> getProducts() {
        return this.products;
    }
    
    public void communicate(String selection, String indexDir, 
            String dataDir) throws Exception{
        
        this.indexDir = indexDir;
        this.dataDir = dataDir;
        
        // tells server which collection was selected
        this.outputStream.writeObject(selection);
        
        // receives unique words of selected collection
        ArrayList<String> uniqueWords = (ArrayList<String>) this.inputStream.readObject();
        
        // creates and normalizes vector from unique word dictionary
        Vector myVector = LuceneIndex(uniqueWords);
        myVector.normalize();
        
        // initizlies myGMP library
        int seed = 123456;
        int numBits = 512;
        initialize(numBits, seed);
        
        // encrypts value
//        String ciphertext = encrypt(123);
//        
//        // gets decrypted value;
//        int message = decrypt(ciphertext);
//        System.out.println("Message: " + message);
//        
//        // clears myGMP variables
//        clear();
        
        String[] sVector = encryptVector(myVector);
        
        
        this.outputStream.writeObject(sVector);
        
        ArrayList<String> encryptedProducts = (ArrayList<String>) this.inputStream.readObject();
        ArrayList<Double> decryptedProducts = new ArrayList<Double>();
        
        for (int i = 0; i < encryptedProducts.size(); i++) {
            String cipherProduct = encryptedProducts.get(i);
            int decryptedDot = decrypt(cipherProduct);
            double scaledDot = myConversions.scaledDotProduct(decryptedDot);
            decryptedProducts.add(scaledDot);
            
        }
        
        Collections.sort(decryptedProducts);
        
        // ORIGINAL:::
        
        // sends query vector to server
        this.outputStream.writeObject(myVector);
        
        // accepts results from server
        this.products = (ArrayList<Double>)this.inputStream.readObject();
        
        closeClient();
        return;
    }
    
    private void toWholeNumbers(int digits, Vector myVector) {
        for(int i = 0; i < myVector.getDimension(); i++)
            myVector.setValue(i, myVector.getValue(i) * Math.pow(10, digits));   
    }
    
    private Vector LuceneIndex(ArrayList<String> uniqueWords) throws Exception{
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        Vector queryVector = new Vector(uniqueWords.size());
        try {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
            IndexReader reader = indexer.getWriter().getReader();
            int numDocuments = reader.numDocs();
            
            
            // create vector;
            TermFreqVector freqVector = reader.getTermFreqVector(0, "contents");
            int[] termFreqs = freqVector.getTermFrequencies();
            int position = 0;
            int frequency = 0;
            
            for (String term : uniqueWords) {
                int index = freqVector.indexOf(term);
                if (index == -1)
                    queryVector.setValue(position, 0);
                else {
                    frequency = termFreqs[index];
                    queryVector.setValue(position, frequency);
                }
                position++;
                
            }
            reader.close();
            
        } finally {
            indexer.close();
        }
        return queryVector;
    }
        
    private String[] encryptVector(Vector myVector) {
        
        int[] iVector = myConversions.wholeNumberVector(myVector);
        String[] sVector = new String[myVector.getDimension()];
        for (int i = 0; i < myVector.getDimension(); i++) {
            sVector[i] = encrypt(iVector[i]);
        }
        clear();
        return sVector;
    }
    
    private void closeClient() throws Exception{
        inputStream.close();
        outputStream.close();
        socket.close();
    }
    
    public double getTopThreshold() {
        return this.topThreshold;
    }
    
    private static class TextFilesFilter implements FileFilter {
    	public boolean accept(File path) {
      	return path.getName().toLowerCase()        
             .endsWith(".txt");                  
        }
    }
}
