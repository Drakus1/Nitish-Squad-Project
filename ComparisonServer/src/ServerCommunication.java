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
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;



public class ServerCommunication {
    
    private int port;
    private ArrayList<Dictionary> myLibrary;
    private DefaultListModel<String> listModel;
    private ServerSocket ss;
    private Socket client;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientChoice;
    private ArrayList<Double> products;
    
    
    
    public ServerCommunication(int port, ArrayList<Dictionary> myLibrary, 
            DefaultListModel<String> listModel) throws IOException {
        
        this.port = port;
        this.myLibrary = myLibrary;
        this.listModel = listModel;
        
        // open server socket, connect to client socket
        this.ss = new ServerSocket(port); 
        this.client = ss.accept();
        
        // set up input and output streams
        this.outputStream = new ObjectOutputStream(client.getOutputStream());
        this.inputStream = new ObjectInputStream(client.getInputStream());   
    }
    
    public void communicate() throws Exception {
        try {
            // sends collection choices to client
            this.outputStream.writeObject(this.listModel);
            // gets client's collection choice
            this.clientChoice = (String) inputStream.readObject();

            // determines which dictionary to send to client
            int index = this.listModel.indexOf(this.clientChoice);
            Dictionary selectedDictionary = this.myLibrary.get(index);
            ArrayList<String> myWords = selectedDictionary.getAllWords();

            // sends client unique words in selected collection
            this.outputStream.writeObject(myWords);

            // gets client vector
            Vector queryVector = (Vector)this.inputStream.readObject();

            // performs calculations on query Vector;
            ArrayList<Double> products = calculations(queryVector, selectedDictionary);

            // sends results to client
            this.outputStream.writeObject(products);

            // close server
            closeServer();
            return;
            
        } catch (Exception e) {
            closeServer();
            return;
        }
    }
    
    public ArrayList<Double> getProducts() {
        return this.products;
    }
    
    private ArrayList<Double> calculations(Vector queryVector, Dictionary myWords) {
        
        ArrayList<Double> products = new ArrayList<Double>();
        // does dot product for all vectors in dictionary's vector array
        Vector[] serverVectors = myWords.getVectorArray();
        for (int i = 0; i < serverVectors.length; i++) {
            double product = serverVectors[i].dotProduct(queryVector);
            products.add(product);
        }
        return products;
    }
    
    public void closeServer() throws Exception{
        this.inputStream.close();
        this.outputStream.close();
        this.client.close();
        this.ss.close();
    }
}
