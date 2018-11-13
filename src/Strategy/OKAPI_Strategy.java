/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Strategy;

import cecs429.index.DiskPositionalIndex;
import java.io.IOException;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.max;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bhavy
 */
public class OKAPI_Strategy implements StrategyInterface{

    double wdt=0;
    double length=0;
    double doclength=0;
    
    @Override
    public double calculate_wqt(double N, double dft) {
        double numerator = N - dft + 0.5; 
        double denominator = dft + 0.5; 
        double rs = log((numerator/denominator));
        return max(0.1, rs);
    }

    @Override
    public void calculate_wdt(HashMap<String,Integer> tftd) {
       }

    @Override
    public double calculate_Ld(DiskPositionalIndex index, int docId) {
        doclength=1;
        return doclength;
    }

     @Override
    public double get_wdt(double t_fd, DiskPositionalIndex index, int docID) {
        try {
            double numerator = 2.2 * t_fd;
            double docLength_d = index.getDocLength_D(docID); //need to read these two values from docweights
            double docLength_a = index.getDocLength_A();
            double denominator = 1.2 * (0.25 + (0.75 * (docLength_d/docLength_a)) + t_fd);
            return (numerator/denominator);
        } catch (IOException ex) {
            Logger.getLogger(OKAPI_Strategy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1; 
    }
}
