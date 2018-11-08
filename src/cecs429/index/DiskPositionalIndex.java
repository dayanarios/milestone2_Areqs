/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bhavy
 */
public class DiskPositionalIndex implements Index {

    //String path = "C:\\Users\\bhavy\\Desktop\\SET\\TestFiles\\Index";
    File vocTable;
    File vocab;
    File postings;
    File docWeights;

    private boolean Found = false;
    
    public DiskPositionalIndex(String path){
        this.vocTable = new File(path + "\\Index\\VocabTable.bin");
        this.vocab = new File(path + "\\Index\\vocab.bin");
        this.postings = new File(path + "\\Index\\Postings.bin");
        this.docWeights = new File(path + "\\Index\\docWeights.bin");
    }

    @Override
    public List<Posting> getPositional_posting(String term) {
            List<Posting> res = new ArrayList<>();
            
        try {
            
            long postingsposition = binarysearch(term); //returns position of term in postings.bin
            InputStream p = new FileInputStream(postings);
            RandomAccessFile pos = new RandomAccessFile(postings, "r");
            List<Integer> docID = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            if (Found == true) {
                //System.out.println(pos.getFilePointer());
                pos.seek(postingsposition);
                //System.out.println(pos.getFilePointer());
                int docFreq = pos.readInt();
                for (int i = 0; i < docFreq; i++) {
                    docID.add(pos.readInt());
                    int termfreq = pos.readInt();
                    for (int j = 0; j < termfreq; j++) {
                        positions.add(pos.readInt());
                    }
                    if(i==0){
                    Posting p1=new Posting(docID.get(i), positions.get(0));
                    res.add(p1);
                    for(int x=1;x<positions.size();x++){
                       
                        positions.set(x, positions.get(x)+positions.get(x-1));
                        p1.addPosition(positions.get(x));
                        
                      
                    }
                    
                    positions.clear();
                    }
                    else{
                    docID.set(i, docID.get(i) + docID.get(i-1));
                    Posting p1=new Posting(docID.get(i),positions.get(0));
                    res.add(p1);
                    for(int x=1;x<positions.size();x++){
                       
                        positions.set(x, positions.get(x)+positions.get(x-1));
                        p1.addPosition(positions.get(x));
                        
                      
                    }
                    
                    positions.clear();
                    }
                    
                    
                }
                
            } else {
                System.out.println("Not Found");
                res=null;
            }
            p.close();
            pos.close();
            
    
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    @Override
    public List<Posting> getPosting_noPos(String term){
        List<Posting> results = new ArrayList<>();
        try {
            
            long postingsposition = binarysearch(term); //returns position of term in postings.bin
            InputStream p = new FileInputStream(postings);
            RandomAccessFile pos = new RandomAccessFile(postings, "r");
            List<Integer> docID = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            
            if (Found == true) {
                //System.out.println(pos.getFilePointer());
                pos.seek(postingsposition);
                //System.out.println(pos.getFilePointer());
                int docFreq = pos.readInt();
                for (int i = 0; i < docFreq; i++) {
                    docID.add(pos.readInt());
                    //System.out.println("file pointer before reading term freq: " + pos.getFilePointer());
                    int termfreq = pos.readInt();
                    //System.out.println("file pointer before skipping: " + pos.getFilePointer());
                    //System.out.println("term freq: " + termfreq);
                    //int bytesSkipped = pos.skipBytes(termfreq);
                    //ong b_skipped = (long) bytesSkipped; 
                    pos.seek(pos.getFilePointer() + termfreq*4);
                    
                    //System.out.println("file pointer after skipping: " + pos.getFilePointer());
                    /*
                    while(bytesSkipped != termfreq){ //skip over positions
                        if (bytesSkipped < 0){
                            System.out.println("Error in reading bytes for ranked posting"); 
                            break; 
                        } 
                        int bytes_missed = termfreq - bytesSkipped; 
                        bytesSkipped+= bytes_missed; 
                    }
                    */
                    if(i == 0){
                        results.add(new Posting(docID.get(i), (double)docFreq, (double)termfreq));
                    }
                    else {
                        docID.set(i, docID.get(i) + docID.get(i - 1));
                        results.add(new Posting(docID.get(i), (double) docFreq, (double) termfreq));
                    }
                   
                }
            } 
            else {
                System.out.println("Not Found");
                results=null;
            }
            p.close();
            pos.close();
   
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return results;
    }
    

    public double getL_d(int docId) throws FileNotFoundException, IOException{
        //System.out.println(docId); 
        FileInputStream i = new FileInputStream(docWeights);
        RandomAccessFile ram = new RandomAccessFile(docWeights, "r");
        long docid = (long) docId;
        //System.out.println(docid); 
        ram.seek((docid - 1) * 8);
        //System.out.println(ram.readDouble());
        double L_d = ram.readDouble();
        //System.out.println(L_d); 
        i.close();
        ram.close();
        
        return L_d; 
        
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    public long binarysearch(String term) throws FileNotFoundException, IOException {

        InputStream f = new FileInputStream(vocab);
        RandomAccessFile vt = new RandomAccessFile(vocTable, "r");
        RandomAccessFile v = new RandomAccessFile(vocab, "r");
        long result = 0;

        long size = vt.length() / 16;
        long i = 0, m;
        long j = size - 1;
        m = (i + j) / 2;
        String s1 = term;
        while (i < j) {

            m = (i + j) / 2;

            String s2 = "";
            //go to vocabtable position m
            // System.out.println(vt.getFilePointer());
            vt.seek(m * 16);
            //System.out.println(vt.getFilePointer());
            long current = vt.readLong(); //gives decimal value of hexadecimal value in vocabtable
            //System.out.println(vt.getFilePointer());
            vt.readLong();
            //System.out.println(vt.getFilePointer());
            long next = vt.readLong();
            //System.out.println(vt.getFilePointer());

            //length of the string
            long k = next - current;

            //vocab.bin at pos m
            //System.out.println(v.getFilePointer());
            v.seek(current);
            // System.out.println(v.getFilePointer());

            while (k > 0) {
                s2 = s2 + (char) (v.readUnsignedByte());
                //System.out.println(v.getFilePointer());
                //s2=s2+(char)(v.readByte());
                //System.out.print((char)(v.readByte()));
                k--;

            }

          //  System.out.println("String at pos m is " + s2);
            if (term.compareTo(s2) == 0) {

                System.out.println("Found");
                Found = true;
                vt.seek(m * 16);
                vt.readLong();
                result = vt.readLong();
                break;
            } else if (term.compareTo(s2) < 0) {
                //term<s2
                j = m;
            } else {
                if((j-i)==1){ Found=false;result=0;break;}
                i = m;

            }

        }
        return result;
    }
    
    
}
