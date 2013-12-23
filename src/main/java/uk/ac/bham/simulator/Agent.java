package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 *
 * @author 
 */
public class Agent 
{
    ArrayList<Bid> bids;
    IdentityProvider identityProvider;
    
    public Agent(IdentityProvider identityProvider)
    {
        bids = new ArrayList<Bid>();
        this.identityProvider = identityProvider;
    }
    
    public Bid createBid()
    {
        Bid bid = new RandomBid(this);
        bid.configIdentityResources();
        bid.setMaxIncrementPercentage(Utilities.generateRandomInteger(1, 30));
        bids.add(bid);  
        return bid;
    }
    
    public void removeBid(Bid bid)
    {
       bids.remove(bid); 
    }
    
    public void requestAuthentication()
    {
        
    }
    
    public void requestPayment(double price, Bid bid)
    {

    }
    
    public IdentityProvider getIdentityProvider()
    {
        return identityProvider;
    }
    
    @Override
    public String toString()
    {
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode();
    }    
}
