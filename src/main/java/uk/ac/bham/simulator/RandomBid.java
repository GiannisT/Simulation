package uk.ac.bham.simulator;

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
    private static final int FIRSTRESOURCE = 1;
    private static final int LASTRESOURCE = 4;
        
    public RandomBid(Agent agent)
    {
        super(agent);
    }
    
    @Override
    public void configIdentityResource()
    {
        //here there will a loop
        IdentityResource identityResource = new IdentityResource();
        identityResource.setPrice(Utilities.generateRandomInteger(MINPRICE, MAXPRICE));
        identityResource.setPriority(Priority.createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY)));
        identityResource.setResourceType(ResourceType.createByNumber(Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)));
        getIdentityResources().add(identityResource);
    }
    
}
