package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 * QUESTIONS FOR THIS CLASS which resources/utility characteristics are we going
 * to use in the simulation? How should the qualityFactor be used in this class?
 *
 */
public class AuctionAsk 
{
    // TODO check naming convention for arraylist, compare to FederatedCoordinator.auctionAskList attribute
    ArrayList<IdentityResource> identityResources;
    private Integer maxDecrementPercentage; 
    //final int AlterSubmissionPerDay = 8; //describes how many times an SP can change or resubmit already submitted asks     
    ServiceProvider serviceProvider;

    public AuctionAsk() 
    {
        identityResources = new ArrayList<IdentityResource>();
    }

    public AuctionAsk(ServiceProvider serviceProvider)
    {
        this();
        this.serviceProvider = serviceProvider;
    }
    
    public void configIdentityResources()
    {
        
    } 
    
    public ServiceProvider getServiceProvider() 
    {
        return serviceProvider;
    }

    public ArrayList<IdentityResource> getIdentityResources() 
    {
        return identityResources;
    }

    /* 
     The FederatedCoordinator invoke the getAdaptedPrice for each AuctionAsk
     */
    public double getAdaptedPrice() 
    {
        // TODO check implementation, this is a simple one
        double ret = 0;

        for (IdentityResource ir : getIdentityResources()) {
            ret += ir.getPrice() * ir.getPriority().getLevel();
        }
        return ret;
    }

    /**
     * @return the maxDecrementPercentage
     */
    public Integer getMaxDecrementPercentage() 
    {
        return maxDecrementPercentage;
    }

    /**
     * @param maxDecrementPercentage the maxDecrementPercentage to set
     */
    public void setMaxDecrementPercentage(Integer maxDecrementPercentage) 
    {
        this.maxDecrementPercentage = maxDecrementPercentage;
    }
    
    @Override
    public String toString()
    {
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode();
    }    

}
