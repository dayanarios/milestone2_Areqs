/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu;

import Strategy.StrategyFactory;
import Strategy.StrategyInterface;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Doc_accum;
import cecs429.index.Index;
import cecs429.index.Positional_inverted_index;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.EnglishTokenStream;
import cecs429.text.NewTokenProcessor;
import cecs429.text.TokenProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.swing.JOptionPane;

/**
 * Main driver class used in the GUI to run the search engine
 *
 * @author dayanarios
 */
public class DocumentIndexer {

    protected static DocumentCorpus corpus;
    private static Index index;
    protected static String query;
    protected static List<Posting> postings = new ArrayList<>();
    protected static Boolean clickList = false;  //prevents clicking the list when there is nothing to click
    protected static Boolean booleanMode = true; //indicates which mode the search engine should run in
    protected static Boolean rankedMode = false; 
    private int topK = 10; //search engine always returns the top K = 10 docs
    private static double N = 0; //corpus size
    private static String path; 
    protected static int rankedOption = 0; 
    
    
    /**
     * Indexes the corpus given by the path parameter, records the time it takes
     * to index.
     *
     * @param path supplied by user in
     * GUI.SearchDirectoriesButtonActionPerformed
     *
     */
    protected static void startIndexing(Path p) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        
        path = p.toString();
        
        corpus = DirectoryCorpus.loadTextDirectory(p.toAbsolutePath(), ".txt");// To run .txt files
        //corpus = DirectoryCorpus.loadJsonTextDirectory(p.toAbsolutePath(), ".json");
        
        long startTime = System.nanoTime();
        index = posindexCorpus(corpus);
        long endTime = System.nanoTime();
        
        long elapsedTime = (endTime - startTime);
        double seconds = (double) elapsedTime / 1000000000.0;
        GUI.ResultsLabel.setText("Total Indexing Time: " + new DecimalFormat("##.##").format(seconds) + " seconds");

    }

    
    protected static void startSearchEngine() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
       
        DiskIndexWriter diskWriter= new DiskIndexWriter();
        //diskWriter.WriteIndex(index,path);
        DiskPositionalIndex Disk_posIndex = new DiskPositionalIndex(path);
        
        
        if (booleanMode){
            BooleanQueryMode(diskWriter, Disk_posIndex); 
        }
        else{
            
            RankedQueryMode(diskWriter, Disk_posIndex); 
        }
            
        
    }

    
    /**
     * Runs the search engine in Boolean Query Mode.
     * Checks for the existence of special queries.
     * Ensures the list is cleared after every new search query.
     * Displays any relevant result in the GUI's result label.
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static void BooleanQueryMode(DiskIndexWriter diskWriter, DiskPositionalIndex disk_posIndex) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        if (!specialQueries(query)) {
            //newCorpus = false; 
            BooleanQueryParser bParser = new BooleanQueryParser();
            QueryComponent qComponent = bParser.parseQuery(query);
            postings = qComponent.getPostings(disk_posIndex);

            if (postings.isEmpty()) 
            {
                GUI.JListModel.clear();
                GUI.ResultsLabel.setText("");
                clickList = false;

                String notFound = "Your search '" + query + "' is not found in any documents";
                GUI.ResultsLabel.setText(notFound);

            } else {
                clickList = true;

                //clears list and repopulates it 
                String docInfo;

                GUI.JListModel.clear();
                GUI.ResultsLabel.setText("");

                for (Posting p : postings) {
                    docInfo = corpus.getDocument(p.getDocumentId()).getTitle();
                    GUI.JListModel.addElement(docInfo);

                }
                GUI.ResultsLabel.setText("Total Documents Found: " + postings.size());
            }

        }

        //GUI.SearchBarTextField.setText("Enter a new search or 'q' to exit");
        GUI.SearchBarTextField.selectAll();

    }
    
    /**
     * Processes a query without any Boolean operators and returns the top 
     * K=10 documents satisfying the query. 
     */
    private static void RankedQueryMode(DiskIndexWriter diskWriter, DiskPositionalIndex disk_posIndex) throws IOException{

        if (query.equals("q")) {

            System.exit(0);
        }
        
        StrategyFactory sf = new StrategyFactory();
        StrategyInterface strategy = sf.execute(rankedOption);
        
        //maps postings to accumulator value
        HashMap<Integer, Doc_accum> postingMap = new HashMap<Integer, Doc_accum>();

        PriorityQueue<Doc_accum> queue = new PriorityQueue<>(Collections.reverseOrder());

        List<Posting> postings = new ArrayList<>();
        List<Doc_accum> results = new ArrayList<>();

        String[] query_array = query.split("\\s+");

        for (String term : query_array) {

            postings = disk_posIndex.getPosting_noPos(term);
            for (Posting p : postings) { //for each document in the postings list
                //System.out.println("docID: " + p.getDocumentId());
                double t_fd = p.getT_fd(); 
                //System.out.println("t_fd " + t_fd); 
                double d_ft = p.getD_ft(); 
                //System.out.println("d_ft " + d_ft); 
                double w_qt = strategy.calculate_wqt(N, d_ft);
                //System.out.println("w_qt " + w_qt); 
                double accum = 0;
                double w_dt = strategy.get_wdt(t_fd, disk_posIndex, p.getDocumentId());
                //System.out.println("w_dt " + w_dt); 

                //pairs (Ranked_posting, accumulator factor)
                if (postingMap.containsKey(p.getDocumentId())) {
                    accum = postingMap.get(p.getDocumentId()).getAccumulator();
                    accum += (w_qt * w_dt);
                    postingMap.replace(p.getDocumentId(), new Doc_accum(p, accum)); //replaces old accum value

                } else {
                    accum += (w_qt * w_dt);
                    postingMap.put(p.getDocumentId(), new Doc_accum(p, accum));
                }
            }

        }

        for (Integer p : postingMap.keySet()) {
            Doc_accum doc_temp = postingMap.get(p);
            double accum = doc_temp.getAccumulator(); //gets accum associated with doc

            if (accum > 0) {
                //search for each p's Ld factor in docWeights.bin
                double l_d = disk_posIndex.getL_d(p); 
                //System.out.println("accum before division for docId: "+ p + " is: " + accum);
                //System.out.println("Ld: " + l_d); 
                accum /= l_d;
                doc_temp.setAccumulator(accum);
                //System.out.println("final accum for docId: "+ p + " is: " + accum + "\n");
            }

            queue.add(postingMap.get(p));

        }

        //returns top K=10 results 
        int topK = 10;

        while ((results.size() < topK + 1) && queue.size() > 0) {

            results.add(queue.poll());  //gets the posting acc pair and returns only posting

        }

        display_RankedResults(results); 
        
    }
    
    public static void display_RankedResults(List<Doc_accum> results){
        if (results.isEmpty()) 
        {
            GUI.JListModel.clear();
            GUI.ResultsLabel.setText("");
            clickList = false;

            String notFound = "Your search '" + query + "' is not found in any documents";
            GUI.ResultsLabel.setText(notFound);

        } else {
            clickList = false;

            //clears list and repopulates it 
            String docInfo;

            GUI.JListModel.clear();
            GUI.ResultsLabel.setText("");

            for (Doc_accum p : results) {
                docInfo = corpus.getDocument(p.getPosting().getDocumentId()).getTitle();
                docInfo += " " + p.getAccumulator(); 
                GUI.JListModel.addElement(docInfo);

            }
            GUI.ResultsLabel.setText("Total Documents Found: " + results.size());
        }

        

        //GUI.SearchBarTextField.setText("Enter a new search or 'q' to exit");
        GUI.SearchBarTextField.selectAll ();
    }
    
    
    
    /**
     * Creates a positional inverted index Tokenizes all the terms found in each
     * document in the corpus
     *
     * @param corpus corpus to be indexed
     * @return a positional inverted index over the corpus
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Positional_inverted_index posindexCorpus(DocumentCorpus corpus) throws ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException, IOException {

        NewTokenProcessor processor = new NewTokenProcessor();
        Iterable<Document> docs = corpus.getDocuments(); //call registerFileDocumentFactory first?
        
        StrategyFactory sf = new StrategyFactory();
        StrategyInterface s1 = sf.execute(rankedOption);
        
        List<Double> Doc_length = new ArrayList<>();
        Positional_inverted_index index = new Positional_inverted_index();
        double token_count=0;
        
        // Iterate through the documents, and:
        for (Document d : docs) {
            File f = new File(path + "\\" + d.getTitle());
            double Filesize = f.length(); 
            
            double doc_tokens=0;
            double doc_length=0;
            HashMap<String, Integer> tftd = new HashMap<>();
            // Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
            Reader reader = d.getContent();
            EnglishTokenStream stream = new EnglishTokenStream(reader); //can access tokens through this stream
            N++; 
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
                if (tftd.containsKey(word.get(0))) {
                    doc_tokens++;
                    int count = tftd.get(word.get(0));
                    tftd.replace(word.get(0), count + 1);
                } else {
                    doc_tokens++;
                    token_count++; //gives total number of tokens.
                    tftd.put(word.get(0), 1);
                }

            }
            //double length = 0;
            //double wdt = 0;
            //calculate w(d,t)= 1 + ln(tft)
            double total_tftd=0;
            for (Map.Entry<String, Integer> entry : tftd.entrySet()) {

                //wdt = 1 + log(entry.getValue());

                //length = length + pow(wdt, 2);
                total_tftd=total_tftd + entry.getValue();
            }

            //Doc_length.add(pow(length, 0.5));
            //System.out.println("docId: " + d.getId() + " docTitle: "+ d.getTitle() + " Ld: " + pow(length, 0.5)); 

            double avg_tftd = total_tftd / (double) tftd.size();
            s1.calculate_wdt(tftd);
            Doc_length.add(s1.calculate_Ld(new DiskPositionalIndex(),0));
            Doc_length.add(avg_tftd);
            Doc_length.add(doc_tokens);
            Doc_length.add(Filesize);
        }
        Doc_length.add(token_count);
        System.out.println("Total docs: "+ N);
        System.out.println("Total tokens:" + token_count);
        DiskIndexWriter d = new DiskIndexWriter();
        d.write_doc(path, Doc_length);
        /*System.out.println("LD: " );
        for(int x=0 ;x<Doc_length.size();x++){
            System.out.println(Doc_length.get(x));
        }*/
        return index;
    }

    /**
     * Checks for the existence of special queries Checks if the user quit the
     * program Checks if the user wants to stem a word Checks if the user wants
     * to return the first 1000 terms in the vocab
     *
     * @param query inputted by the user
     * @return true if a special query was performed false otherwise
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Boolean specialQueries(String query) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        clickList = false;
        //newCorpus = true;

        if (query.equals("q")) {

            System.exit(0);
            return true;
        }

        String[] subqueries = query.split("\\s+"); //split around white space

        if (subqueries[0].equals("stem")) //first term in subqueries tells computer what to do 
        {
            GUI.JListModel.clear();
            GUI.ResultsLabel.setText("");
            TokenProcessor processor = new NewTokenProcessor();
            if (subqueries.length > 1) //user meant to stem the token not to search stem
            {
                List<String> stems = processor.processToken(subqueries[1]);

                //clears list and repopulates it 
                String stem = "Stem of query '" + subqueries[1] + "' is '" + stems.get(0) + "'";

                GUI.ResultsLabel.setText(stem);

                return true;
            }

        } else if (subqueries[0].equals("vocab")) {
            List<String> vocabList = index.getVocabulary();
            GUI.JListModel.clear();
            GUI.ResultsLabel.setText("");

            int vocabCount = 0;
            for (String v : vocabList) {
                if (vocabCount < 1000) {
                    vocabCount++;
                    GUI.JListModel.addElement(v);
                }
            }
            GUI.ResultsLabel.setText("Total size of vocabulary: " + vocabCount);
            return true;
        }

        return false;
    }

    /**
     * Checks if the user wants to index a new corpus
     *
     * @return true if the user wants to index a new corpus false otherwise
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected static Boolean newCorpus() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        String[] subqueries = query.split("\\s+"); //split around white space
        if (subqueries[0].equals("index") && subqueries.length > 1) {
            JOptionPane.showOptionDialog(GUI.indexingCorpusMessage, "Indexing corpus please wait", "Indexing Corpus", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.INFORMATION_MESSAGE, null, null, null);
            GUI.JListModel.clear();
            GUI.ResultsLabel.setText("Indexing");
            startIndexing(Paths.get(subqueries[1]));
            GUI.SearchBarTextField.setText("Enter a new search or 'q' to exit");

            return true;
        }
        return false;
    }
}
