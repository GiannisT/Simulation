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
    
    public void createBid(ArrayList<IdentityResource> resources)
    {
        Bid bid = new RandomBid(this);
        bid.configIdentityResource();
        bids.add(bid);        
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
        return null;
    }
}
