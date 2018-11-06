/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static jdk.nashorn.internal.objects.NativeArray.map;

/**
 *
 * @author bhavy
 */
public class Posting {

    private int mDocumentId;
    private List<Integer> h; //h = positions
    private double t_fd;
    private double d_ft;


    public Posting(int docID, int pos) {

        mDocumentId = docID;
        h = new ArrayList<>();
        h.add(pos);
        

    }
    
    public Posting(int docID, double d_ft, double t_fd) {
        this.d_ft = d_ft;
        this.mDocumentId = docID;
        this.t_fd = t_fd;
        h = null; 
    }

    public List<Integer> getPositions() {
        return h;
    }

    public int getDocumentId() {

        return mDocumentId;
    }
    
    public double getT_fd() {
        return t_fd;
    }

    public double getD_ft() {
        return d_ft;
    }

    public void addPosition(int position) {

        h.add(position);

    }
}
