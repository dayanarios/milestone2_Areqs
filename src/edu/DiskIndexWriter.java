/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Positional_inverted_index;
import cecs429.index.Posting;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.String;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author bhavy
 */
public class DiskIndexWriter {

    List<String> l1 = new ArrayList<>();
    List<Posting> positional = new ArrayList<>();
    List<Integer> vocabPositions = new ArrayList<>();
    List<Long> posting_Pos = new ArrayList<>();
    String path;

    public void WriteIndex(Index index, String s) throws FileNotFoundException, IOException {

        path = s;
        //String s1="abandon";
        vocabPositions = write_vocab(index, path); //vocab positions is list of positions of each term in vocab. l1 is list of strings in vocavulary in sorted order.
        posting_Pos = write_posting(index, path);  //posting_Pos contains position of each term in postings.bin
        write_vocabtable(vocabPositions, posting_Pos, path); //

        //System.out.println(l1.indexOf(s1)); //gives position in vocab file. List array starts at 0 so if the answer is 2, it is 3rd word in vocabulary.
        //System.out.println(vocabPositions.get(l1.indexOf(s1))); //gives position of that term in vocab.bin. if the word is at 480th position in vocabulary, look for 481st row in vocabTable.bin to get the starting position of word in vocab.bin and its position in postings.bin
        
    }
    public void write_doc(String p, List<Double> doclength) throws FileNotFoundException, IOException{
    
        path=p;
        File f=new File(path+"\\Index\\docWeights.bin");
        FileOutputStream out = new FileOutputStream(f);
        DataOutputStream d2 = new DataOutputStream(out);
        for(int x=0 ;x < doclength.size();x++){
           
            
            d2.writeDouble(doclength.get(x));
            d2.flush();
        //    System.out.println(doclength.get(x));
        }
        d2.close();
        out.close();
        
    
    }
    public void write_vocabtable(List<Integer> vocab, List<Long> postings, String path) throws FileNotFoundException, IOException {

        File f1 = new File(path + "\\Index\\VocabTable.bin");
        FileOutputStream out = new FileOutputStream(f1);
        DataOutputStream d1 = new DataOutputStream(out);
        for (int i = 0; i < vocab.size(); i++) {

            d1.writeLong(vocab.get(i));
            d1.writeLong(postings.get(i));
            d1.flush();
        }
        d1.close();
        out.close();

    }

    public List<Long> write_posting(Index index, String path) throws FileNotFoundException, IOException {

        File f1 = new File(path + "\\Index\\Postings.bin");
        FileOutputStream out = new FileOutputStream(f1);
        DataOutputStream d1 = new DataOutputStream(out);

        int j = 0;
        int docfrequency;
        List<Integer> docfreq = new ArrayList<>();
        List<List<Integer>> termpositions = new ArrayList<>();
        List<Long> postings_position = new ArrayList<>();
        List<Integer> termfreq=new ArrayList<>();
        int entrysize = 0;
        
        long sizeoffile = 0;
        //<docfreq,
        //          d1,termfrequency,
        //                          positions
        //          d2,termfrequency,
        //                          positions
        //size=4B for each value.
        while (j < l1.size()) {

            sizeoffile = out.getChannel().position();
            //System.out.println(sizeoffile); 
            postings_position.add(sizeoffile);
            positional = index.getPositional_posting(l1.get(j));
            // System.out.print(l1.get(j));
            for (Posting p : positional) {
                docfreq.add(p.getDocumentId());
                termpositions.add(p.getPositions());
                termfreq.add(p.getPositions().size());

            }

            docfrequency = docfreq.size();
            //  System.out.println("Doc freq " + docfrequency + "Number of positions " + termfreq + "Positions: " + termpositions);

            d1.writeInt(docfrequency);
            if (docfrequency == 1) {
                d1.writeInt(docfreq.get(0));
                d1.writeInt(termfreq.get(0));
                d1.writeInt(termpositions.get(0).get(0));
                for (int p = 1; p < termfreq.get(0) ; p++) {
                    d1.writeInt(termpositions.get(0).get(p) - termpositions.get(0).get(p-1));
                }
            } else {
                d1.writeInt(docfreq.get(0));
                d1.writeInt(termfreq.get(0));
                d1.writeInt(termpositions.get(0).get(0));
                for (int p = 1; p < termfreq.get(0); p++) {
                    d1.writeInt(termpositions.get(0).get(p) - termpositions.get(0).get(p - 1));
                }
                for (int k = 1; k < docfreq.size(); k++) {

                    d1.writeInt(docfreq.get(k) - docfreq.get(k - 1));

                    d1.writeInt(termfreq.get(k));
                    d1.writeInt(termpositions.get(k).get(0));
                    for (int p = 1; p < termfreq.get(k); p++) {
                        d1.writeInt(termpositions.get(k).get(p) - termpositions.get(k).get(p - 1));
                    }
                }
            }

            d1.flush();
            docfreq.clear(); 
            termpositions.clear();
            termfreq.clear();
            j++;
            

        }
        d1.close();
        out.close();
        return postings_position;
    }

    public List<Integer> write_vocab(Index index, String path) throws IOException {

        l1 = index.getVocabulary();
        String filename = "\\Index\\vocab.bin";
        FileWriter w1 = new FileWriter(path + filename);
        BufferedWriter b1 = new BufferedWriter(w1);
        int i = 0;
        int position = 0;
        int numberofdocs = 0;
        List<Integer> positionsoftermsinvocabbin = new ArrayList<>();
        while (i < l1.size()) {
            b1.write(l1.get(i));
            positionsoftermsinvocabbin.add(position);
            //    vocabtablemap.put(i, i)
            position = position + l1.get(i).length();

            i++;
        }

        //System.out.println(positionsoftermsinvocabbin);
        b1.close();
        w1.close();
        return positionsoftermsinvocabbin;
    }

}
