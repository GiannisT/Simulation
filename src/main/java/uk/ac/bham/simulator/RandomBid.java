package uk.ac.bham.simulator;

import java.util.ArrayList;
import uk.ac.bham.simulator.IdentityResource.Priority;
import uk.ac.bham.simulator.IdentityResource.ResourceType;

/**
 *
 * @author 
 */
public class RandomBid extends Bid
{
    private static final int MINPRICE = 80;
    private static final int MAXPRICE = 100;
    private static final int MINPRIORITY = 1;
    private static final int MAXPRIORITY = 3;
    private static final int FIRSTRESOURCETYPE = 1;//Availability(1), Anonymity(2),         
    private static final int LASTRESOURCETYPE = 4; //Integrity(3), Performance(4);
        
    public RandomBid(Agent agent)
    {
        super(agent);
    }
    
    @Override
    public void configIdentityResources()
    {        
        int resourceTypeId = FIRSTRESOURCETYPE;
         
        this.setPreferredPrice(Utilities.generateRandomInteger(600, 700));
        while (resourceTypeId <= LASTRESOURCETYPE)        
        {
            IdentityResource identityResource = new IdentityResource();
            //identityResource.setPrice(Utilities.generateRandomInteger(MINPRICE, MAXPRICE));
            //identityResource.setCost(100);
            //identityResource.setMinimumProfit(Utilities.generateRandomInteger(40, 50));
            //identityResource.setPreferredProfit(Utilities.generateRandomInteger(51, 100));
            identityResource.setPriority(Priority.Low);// .createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY))
            identityResource.setResourceType(ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
            setTimeOfSubmission(System.currentTimeMillis()); //used for creating the points in the graph
            getIdentityResources().add(identityResource);
        }
    }
    
}
