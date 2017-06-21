import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.Term;


import java.util.ArrayList;

public class Dictionary {

	private ArrayList<String> wordList;
	private Vector[] vectorArray;
	private int uniqueWords;
	private int numDocuments;

	public Dictionary(IndexReader reader, int numDocs) throws Exception {
		TermEnum myEnum = reader.terms();
		this.wordList = new ArrayList<String>();
		this.numDocuments = numDocs;

	    while (myEnum.next()) {
	      Term t = myEnum.term();
	      if (t.field() == "contents") {
	      	this.wordList.add(t.text());
	      }
	    }
	    myEnum.close();

	    this.uniqueWords = wordList.size();
	    this.vectorArray = new Vector[this.numDocuments];

	    for (int i = 0; i < numDocuments; i++)
	      vectorArray[i] = new Vector(uniqueWords);

	  	TermEnum myTerms = reader.terms();
	  	int currentDimension = 0;

	    while (myTerms.next()) {

	      // get current term
	      Term currentTerm = myTerms.term();
	      if (currentTerm.field() != "contents") continue;

	      // calculate idf(t)
	      double documentFreqTerm = (double) myTerms.docFreq();
	      double inverseDocFreq = 1 + Math.log(numDocuments / documentFreqTerm);

	      // for each document in the collection
	      TermDocs myDocs = reader.termDocs(currentTerm);
	      while (myDocs.next()) {
	      	int docNum = myDocs.doc();
	      	// calculate tf-idf(term, document)
	      	int termFreq = myDocs.freq();
	      	double tfidf = termFreq * inverseDocFreq;
	      	// set corresponding field in vector
	      	vectorArray[docNum].setValue(currentDimension, tfidf);
                
                // NORMALIZE VECTORS!!!
                vectorArray[docNum].normalize();
	      }
	      myDocs.close();

	      currentDimension++;
	    }
	    myTerms.close();

	}

	public ArrayList<String> getAllWords() {
		return this.wordList;
	}

	public Vector[] getVectorArray() {
		return this.vectorArray;
	}

	public Vector getSingleVector(int docNum) {
		if (docNum < 0 || docNum >= numDocuments)
			throw new IllegalArgumentException("Invalid dimension index");
		return vectorArray[docNum];
	}

	public String getEntry(int index) {
		if (index < 0 || index >= uniqueWords)
			throw new IllegalArgumentException("Invalid dimension index");

		return wordList.get(index);
	}

}