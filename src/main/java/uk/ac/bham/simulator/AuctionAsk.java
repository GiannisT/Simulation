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
    private Integer minimumProfit;
    private Integer preferredProfit;
    protected static final int DEFAULTCOST = 100;


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
    /*public double getAdaptedPrice() 
    {
        // TODO check implementation, this is a simple one
        double ret = 0;

        for (IdentityResource ir : getIdentityResources()) {
            ret += ir.getPrice() * ir.getPriority().getLevel();
        }
        return ret;
    }*/

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
        /*
        String resource="";
        for (IdentityResource ir:this.getIdentityResources())
        {
            resource+="|"+ir.getResourceType().name()+","+ir.getPriority().name()+","+ir.getPrice();
        }
        resource=resource.substring(1);        
                */
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode();//+" {"+resource+"}";
    }    

    /**
     * @return the minimumProfit
     */
    public Integer getMinimumProfit() 
    {
        return minimumProfit;
    }

    /**
     * @param minimumProfit the minimumProfit to set
     */
    public void setMinimumProfit(Integer minimumProfit) 
    {
        this.minimumProfit = minimumProfit;
    }

    /**
     * @return the preferredProfit
     */
    public Integer getPreferredProfit() 
    {
        return preferredProfit;
    }

    /**
     * @param preferredProfit the preferredProfit to set
     */
    public void setPreferredProfit(Integer preferredProfit) 
    {
        this.preferredProfit = preferredProfit;
    }
    
    public float calculateCurrentPrice(Float willingToPayPrice)
    {        
        float sellingPrice = -1;
        
        for (IdentityResource identityResource : identityResources)
        {
            float currentPrice = identityResource.getCost()*(1+getPreferredProfit()/100)* identityResource.getPriority().getLevel();        
            if(currentPrice > willingToPayPrice)
            {
                currentPrice = identityResource.getCost()*(1+getMinimumProfit()/100)* identityResource.getPriority().getLevel();
                if(currentPrice <= willingToPayPrice)
                {
                    int maximumProfit = getPreferredProfit();
                    do
                    {                   
                        currentPrice = identityResource.getCost()*(1+Utilities.generateRandomInteger(getMinimumProfit(), --maximumProfit)/100)* identityResource.getPriority().getLevel();
                    } while(currentPrice>willingToPayPrice); 
                    sellingPrice = currentPrice;
                }            
            }
            else
            {
                sellingPrice = currentPrice;
            }
        }        
        return sellingPrice;
    }
    
}
