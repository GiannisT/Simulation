package uk.ac.bham.simulator;

public class RandomAsk extends AuctionAsk 
{
    //String[] ResourcesOffered;
    private static final int MINPRICE = 80;
    private static final int MAXPRICE = 100;
    private static final int MINPRIORITY = 1;
    private static final int MAXPRIORITY = 3;
    private static final int FIRSTRESOURCETYPE = 1;//Availability(1), Anonymity(2),         
    private static final int LASTRESOURCETYPE = 4; //Integrity(3), Performance(4);
    
    public RandomAsk(ServiceProvider serviceProvider)
    {
        super(serviceProvider);
    }
    
    @Override
    public void configIdentityResources()
    {        
        int resourceTypeId = FIRSTRESOURCETYPE;
        while (resourceTypeId <= LASTRESOURCETYPE)        
        {
            IdentityResource identityResource = new IdentityResource();
            identityResource.setPrice(Utilities.generateRandomInteger(MINPRICE, MAXPRICE));
            identityResource.setPriority(IdentityResource.Priority.Low);//.createByNumber(Utilities.generateRandomInteger(MINPRIORITY,MAXPRIORITY))
            identityResource.setResourceType(IdentityResource.ResourceType.createByNumber(resourceTypeId++));//Utilities.generateRandomInteger(FIRSTRESOURCE,LASTRESOURCE)
            getIdentityResources().add(identityResource);
        }       
    }
     
}