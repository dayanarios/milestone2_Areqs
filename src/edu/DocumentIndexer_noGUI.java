/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Positional_inverted_index;
import cecs429.index.Posting;
import cecs429.text.EnglishTokenStream;
import cecs429.text.NewTokenProcessor;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to run the search engine through the output window, no GUI Follows the
 * documentation in DocumentIndexder.java.
 *
 * @author bhavy
 */


public class DocumentIndexer_noGUI {

    private static String path="C:\\Users\\bhavy\\Desktop\\SET\\TestFiles";
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {

        //DocumentCorpus corpus = DirectoryCorpus.loadJsonTextDirectory(Paths.get("").toAbsolutePath(), ".json"); //to run json files
        
        //String path= "/Users/dayanarios/MobyDick10Chapters"; 
        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(path).toAbsolutePath(), ".txt");// To run .txt files
        Positional_inverted_index index = posindexCorpus(corpus);
        
        DiskIndexWriter d1= new DiskIndexWriter();
        d1.WriteIndex(index,path);
        DiskPositionalIndex dp1 = new DiskPositionalIndex(path);
        List<Posting> result=new ArrayList<>();
        result=dp1.getPositional_posting("pretend");
        
        if(result==null){
        }
        else{
        for(Posting p:result){
            System.out.println("Document: " + p.getDocumentId() + "\t Positions:  " + p.getPositions());
        }}
        //index.print();
        /*
        List<Posting> result = new ArrayList<>();
        List<QueryComponent> l = new ArrayList<>();

        boolean cont = true;

        Scanner scan = new Scanner(System.in);
        String query;

        while (cont) {
            System.out.println("\nEnter your query: ");
            query = scan.nextLine();
            query = query.toLowerCase();

            if (query.equals("quit")) {
                cont = false;
                break;
            }
            BooleanQueryParser b = new BooleanQueryParser();
            QueryComponent c = b.parseQuery(query);
            result = c.getPostings(index);
            DiskIndexWriter(result,path);
             System.out.println(result);
            
            if (result.isEmpty()) {
                System.out.println("No results");
            } else {
                for (Posting p : result) {
                    //   System.out.println(result);
                    System.out.println("Doctument: " + p.getDocumentId() + "\t Positions:  " + p.getPositions());;
                }
            }

    
        }*/

    }

    private static Positional_inverted_index posindexCorpus(DocumentCorpus corpus) throws ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException, IOException {

        NewTokenProcessor processor = new NewTokenProcessor();
        Iterable<Document> docs = corpus.getDocuments(); //call registerFileDocumentFactory first?
        HashMap<String, Integer> tftd = new HashMap<>();
        List<Double> Doc_length = new ArrayList<>();
        Positional_inverted_index index = new Positional_inverted_index();

        // Iterate through the documents, and:
        for (Document d : docs) {
            // Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
            Reader reader = d.getContent();
            EnglishTokenStream stream = new EnglishTokenStream(reader); //can access tokens through this stream

            // Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
            //		and adding them to the HashSet vocabulary.
            Iterable<String> tokens = stream.getTokens();
            int i = 0;

            for (String token : tokens) {

                //adding token in index
                List<String> word = new ArrayList<String>();
                word = processor.processToken(token);

                if (word.size() > 0) {

                    index.addTerm(word.get(0), i, d.getId());

                }
                i = i + 1;

                //we check if token already exists in hashmap or not. 
                //if it exists, increase its freq by 1 else make a new entry.
                if (tftd.containsKey(token)) {
                    int count = tftd.get(token);
                    tftd.replace(token, count + 1);
                } else {
                    tftd.put(token, 1);
                }

            }
            double length = 0;
            double wdt = 0;
            //calculate w(d,t)= 1 + ln(tft)
            for (Map.Entry<String, Integer> entry : tftd.entrySet()) {

                wdt = 1 + log(entry.getValue());
                length = length + pow(wdt, 2);
            }

            Doc_length.add(pow(length, 0.5));
        }

        DiskIndexWriter d = new DiskIndexWriter();
        d.write_doc(path, Doc_length);
        /*System.out.println("LD: " );
        for(int x=0 ;x<Doc_length.size();x++){
            System.out.println(Doc_length.get(x));
        }*/
        return index;
    }

}
