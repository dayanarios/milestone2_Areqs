/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Strategy;

/**
 *
 * @author bhavy
 */
public class StrategyFactory {
    public StrategyInterface execute(int selection){
    
        if(selection==0){
            return new DefaultStrategy();
        }
        else if(selection==1){
            return new TF_IDF_Strategy();
        }
        else if(selection==2){
            return new OKAPI_Strategy();
        }
        else if(selection==3){
            return new Wacky_Strategy();
        }
        else{}
        return null;
    }
}
