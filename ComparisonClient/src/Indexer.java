

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
public class Indexer {

/*
  public static void main(String[] args) throws Exception {

    String indexDir = "/Users/sam/Desktop/lia.meetlucene/MyIndex";
    String dataDir = "/Users/sam/Desktop/lia.meetlucene/MyText";

    long start = System.currentTimeMillis();
    Indexer indexer = new Indexer(indexDir);
    int numIndexed;
    try {
      numIndexed = indexer.index(dataDir, new TextFilesFilter());
    } finally {
      //indexer.close();
    }
    long end = System.currentTimeMillis();

    System.out.println("Indexing " + numIndexed + " files took "
      + (end - start) + " milliseconds");



    // My Code!!!

    IndexReader reader = indexer.writer.getReader();
    int dimensions = uniqueWords(reader);
    int numDocuments = reader.numDocs();

    Vector[] vectorArray = new Vector[numDocuments];
    for (int i = 0; i < numDocuments; i++)
      vectorArray[i] = new Vector(dimensions);

    // for each term in the dictionary
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
      for (int j = 0; myDocs.next(); j++) {
        // calculate tf-idf(term, document)
        int termFreq = myDocs.freq();
        double tfidf = termFreq * inverseDocFreq;
        // set corresponding field in vector
        vectorArray[j].setValue(currentDimension, tfidf);
      }
      myDocs.close();
      currentDimension++;
    }
    myTerms.close();
    
    indexer.close();
  }
*/
/*
  private static int uniqueWords(IndexReader reader) throws Exception {
    
    TermEnum myEnum = reader.terms();
    int uniques = 0;

    while (myEnum.next()) {
      Term t = myEnum.term();
      if (t.field() == "contents") {
        uniques++;
      }
    }
    myEnum.close();
    return uniques;
  }
  */

  private IndexWriter writer;

  public IndexWriter getWriter() {
    return this.writer;
  }
  
  public Indexer(String indexDir) throws IOException {
    Directory dir = FSDirectory.open(new File(indexDir));


    
    writer = new IndexWriter(dir,new StandardAnalyzer(Version.LUCENE_30),
          true, IndexWriter.MaxFieldLength.UNLIMITED);           
  }

  public void close() throws IOException {
    writer.close();                             
  }

  public int index(String dataDir, FileFilter filter)
    throws Exception {

    File[] files = new File(dataDir).listFiles();

    for (File f: files) {
      if (!f.isDirectory() &&
          !f.isHidden() &&
          f.exists() &&
          f.canRead() &&
          (filter == null || filter.accept(f))) {
        indexFile(f);
      }
    }

    return writer.numDocs();                     //5
  }

  private static class TextFilesFilter implements FileFilter {
    public boolean accept(File path) {
      return path.getName().toLowerCase()        //6
             .endsWith(".txt");                  //6
    }
  }

  protected Document getDocument(File f) throws Exception {
    Document doc = new Document();
    doc.add(new Field("contents", new FileReader(f), Field.TermVector.YES));      
    doc.add(new Field("filename", f.getName(),              
                Field.Store.YES, Field.Index.NOT_ANALYZED));
    doc.add(new Field("fullpath", f.getCanonicalPath(),     
                Field.Store.YES, Field.Index.NOT_ANALYZED));
    return doc;
  }

  private void indexFile(File f) throws Exception {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = getDocument(f);
    writer.addDocument(doc);                              //10
  }
}


