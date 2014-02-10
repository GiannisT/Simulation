/*
 * Copyright (C) 2014 frankouz.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 *
 * IT Consultore (ITC)
 * Guayaquil, Ecuador
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import uk.ac.bham.simulator.IdentityResource.ResourceType;

/**
 *
 * @author Francisco Ramirez
 */
public class HistoricalPrice {
    private Map<ResourceType, ArrayList<Float>> historical;
    private static final HistoricalPrice instance=new HistoricalPrice();
    
    private HistoricalPrice()
    {
        historical=new HashMap<ResourceType, ArrayList<Float>>();
    }
    
    public static HistoricalPrice getInstance()
    {
        return instance;
    }
    
    public void addPrice(ResourceType ir, Float price)
    {
        historical.get(ir).add(price);
    }
    
    public Float getPrice(ResourceType ir)
    {
        Float avgPrice=0f;
        int count=0;
        
        for (Float f: historical.get(ir))
        {
            avgPrice+=f;
            count++;
        }
        avgPrice=avgPrice/count;
        
        return avgPrice;
    }
}
