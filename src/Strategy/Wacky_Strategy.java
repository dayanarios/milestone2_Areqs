/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Strategy;

import cecs429.index.DiskPositionalIndex;
import java.io.IOException;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author bhavy
 */
public class Wacky_Strategy implements StrategyInterface{

    double wdt=0;
    double length=0;
    double doclength=0;
    
    @Override
    public double calculate_wqt(double N, double dft) {
        double numerator = N - dft; 
        double denominator = dft; 
        double rs = log((numerator/denominator));
        return max(0, rs);
    }

    @Override
    public void calculate_wdt(HashMap<String,Integer> tftd) {
    }

    @Override
    public double calculate_Ld(DiskPositionalIndex index, int docId) {
        try {
            double bytesize = index.getByteSize(docId); //need to read from docweights
            doclength=pow(bytesize,0.5);
            return doclength;
        } catch (IOException ex) {
            Logger.getLogger(Wacky_Strategy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1; 
    }
     @Override
    public double get_wdt(double t_fd, DiskPositionalIndex index, int docID) {
        try {
            double numerator = 1 + log(t_fd);
            double ave = index.getAve(docID); //need to read this from docweights
            double denominator = 1 + log(ave);
            return (numerator/denominator);
        } catch (IOException ex) {
            Logger.getLogger(Wacky_Strategy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1; 
    }
    
}
