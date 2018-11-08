/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * positional merge between the terms to the left and right of the NEAR operator,
 * selecting documents where the second term appears at most k positions
 * away from thefirst term. 
 * @author dayanarios
 */
public class NearLiteral implements QueryComponent {
    PhraseLiteral nliteral; 
    int k; 
    List<QueryComponent> tokens = new ArrayList<>(); 
    Boolean phraseLit = false; 
    
    public NearLiteral(String terms ){

        int startIndex = 1; 
        
        String regex = "NEAR/."; 
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); //. represents single character  
        Matcher m = p.matcher(terms);
       if(m.find())
       {
           int near_startIndex = m.start();
           int near_endIndex = m.end(); 
           
           String near_keyword = terms.substring(near_startIndex, near_endIndex);
           k = Integer.parseInt(near_keyword.substring(near_keyword.length()-1)); 
           terms = terms.replace(near_keyword+ " ", ""); 
           
        
           //classifies the query's tokens as specific QueryComponents 
            while (startIndex < terms.length()){

                if (terms.charAt(startIndex) == '"') {
                    //String subStr = terms.substring(startIndex);
                    int posOfQuote = terms.indexOf('"', startIndex + 1);
                    PhraseLiteral temp;
                    if (posOfQuote > 0) {
                        temp = new PhraseLiteral(terms.substring(startIndex, 1 + posOfQuote), 1);
                        startIndex = posOfQuote + 2;
                    } else {
                        temp = new PhraseLiteral(terms.substring(startIndex, terms.length()), 1);
                        startIndex = terms.length();
                    }

                    phraseLit = true; 
                    tokens.add(temp); 
                }/*
                else if (terms.charAt(startIndex) == '[') {
                    String subStr = terms.substring(startIndex);
                    int posOfQuote = subStr.indexOf(']', 0);
                    tokens.add(new NearLiteral(terms.substring(2,posOfQuote)));
                    startIndex = posOfQuote +1; 
                }*/
                else{
                    //String subStr = terms.substring(startIndex);
                    int posOfQuote = terms.indexOf(' ', startIndex);
                    TermLiteral temp; 
                    if(posOfQuote > 0){
                        temp = new TermLiteral(terms.substring(startIndex, startIndex + posOfQuote - 1));
                        startIndex = posOfQuote +1; 
                    } else{
                        temp = new TermLiteral(terms.substring(startIndex, terms.length()));
                        startIndex = terms.length(); 
                    }
                    
                    tokens.add(temp); 
                    //tokens.add(new TermLiteral(terms.substring(startIndex,startIndex + posOfQuote)));
                    
                }
            }
            
            nliteral = new PhraseLiteral(tokens, k); //new PhraseLiteral(tokens.get(0) + " " + tokens.get(1), k);
       }
       else{
           System.out.println("unable to search near query");
       }
       
       
        
        
            
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> results = new ArrayList<>();
        
        /*
        List<Posting> temp = new ArrayList<>(); 
        
        if(phraseLit && tokens.get(0) instanceof PhraseLiteral){
            
        } else if (phraseLit && tokens.get(0) instanceof PhraseLiteral){
            
            
        }else{
            
        }
    */
        
        
        results = nliteral.getPostings(index); 
        return results; 
        
    }

    @Override
    public Boolean Component() {
        return true;
    }

    @Override
    public List<Posting> getPosting_noPositions(Index index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
