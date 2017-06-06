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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.FileReader;

@SuppressWarnings("deprecation")
public class Client {

	public static void main(String[] args) throws Exception {

		String indexDir = "/Users/sam/Desktop/lia.meetlucene/MyIndex";
    	String dataDir = "/Users/sam/Desktop/lia.meetlucene/MyText";

    	long start = System.currentTimeMillis();
	    Indexer indexer = new Indexer(indexDir);
	    int numIndexed;
	    Dictionary allWords;
	    try {
	      numIndexed = indexer.index(dataDir, new TextFilesFilter());
	      IndexReader reader = indexer.getWriter().getReader();
	      int numDocuments = reader.numDocs();
	      allWords = new Dictionary(reader, numDocuments);
	      reader.close();
	    } finally {
	      indexer.close();
	    }
	    long end = System.currentTimeMillis();
	    System.out.println("Indexing and creating dictionary for " + numIndexed + 
	    	" files took " + (end - start) + " milliseconds");


	    
	    Vector[] vectorArray = allWords.getVectorArray();
	    for (int i = 0; i < vectorArray.length; i++)
	    	vectorArray[i].normalize();
	    System.out.println("Vector array length: " + vectorArray.length);


	    System.out.printf("Dan with Sam: %.5f\n", vectorArray[0].dotProduct(vectorArray[2]));
	    System.out.printf("Dan with MST: %.5f\n", vectorArray[0].dotProduct(vectorArray[1]));
	    System.out.printf("Sam with MST: %.5f\n", vectorArray[2].dotProduct(vectorArray[1]));
	    
	  }

	private static class TextFilesFilter implements FileFilter {
    	public boolean accept(File path) {
      	return path.getName().toLowerCase()        //6
             .endsWith(".txt");                  //6
    }
  }

}
