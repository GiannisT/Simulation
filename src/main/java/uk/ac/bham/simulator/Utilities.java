package uk.ac.bham.simulator;

import java.util.Random;

/**
 *
 * @author 
 */
public class Utilities 
{
    
    //Adapted from http://www.javapractices.com
    
    public static int generateRandomInteger(int aStart, int aEnd)
    {
        Random aRandom = new Random();
        if (aStart > aEnd) 
        {
            // TODO restore to throw exception
            //throw new IllegalArgumentException("Start cannot exceed End.");
            int tmp=aEnd;
            aEnd=aStart;
            aStart=tmp;
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        return (int)(fraction + aStart);          
    }    
   
}
