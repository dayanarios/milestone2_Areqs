/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

/**
 * Creates a document and accumulator pair.
 * Used to retrieve the top K documents in ranked query mode
 * @author dayanarios
 */
public class Doc_accum  implements Comparable<Doc_accum>{
    double accumulator; 
    Posting posting; 
    
    public Doc_accum(Posting p, double acc){
        this.accumulator = acc; 
        this.posting = p; 
    }

    public double getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(double accumulator) {
        this.accumulator = accumulator;
    }
    
    public void increaseAccumulator(double accumulator) {
        this.accumulator += accumulator;
    }

    public Posting getPosting() {
        return posting;
    }

    public void setPosting(Posting posting) {
        this.posting = posting;
    }

    @Override
    public int compareTo(Doc_accum o) {
        if (this.accumulator > o.accumulator) {
            return 1;
        }
        if (this.accumulator < o.accumulator) {
            return -1;
        } else {
            return 0;
        }
    }
    
    
    
}
