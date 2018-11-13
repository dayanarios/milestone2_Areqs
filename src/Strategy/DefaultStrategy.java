/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Strategy;

import cecs429.index.DiskPositionalIndex;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bhavy
 */
public class DefaultStrategy implements StrategyInterface{

    double wdt;
    double length;
    double doclength;
    
    @Override
    public double calculate_wqt(double N, double dft) {
        return log(1 + (N / dft));
    }

    @Override
    public void calculate_wdt(HashMap<String,Integer> tftd) {
            wdt=0;
            length=0;
            doclength=0;
            for (Map.Entry<String, Integer> entry : tftd.entrySet()) {
                //Math.log gives natural log whereas math.log10 gives log base 10
                wdt = 1 + Math.log(entry.getValue());

                //    System.out.println("wdt " + wdt);
                length += pow(wdt, 2);
                //   weight.add(length);
                //  System.out.println(length);
            }
    }

    
    @Override
    public double calculate_Ld(DiskPositionalIndex index, int docId) {
        doclength=pow(length,0.5);
        return doclength;
    }
    
    

    @Override
    public double get_wdt(double t_fd, DiskPositionalIndex index, int docID) {
        double w_dt = 1 + log(t_fd);
        return w_dt;
        
    }
    
    
}
