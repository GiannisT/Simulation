package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 *
 * @author 
 */
public class Bid 
{
    private Integer maxIncrementPercentage;
    private ArrayList<IdentityResource> identityResources;  
    private Agent agent;
    private float preferredPrice;
    private Bid original;

    public Bid getOriginal() {
        return original;
    }

    public void setOriginal(Bid original) {
        this.original = original;
    }

    public float getPreferredPrice() {
        return preferredPrice;
    }

    public void setPreferredPrice(float preferredPrice) {
        this.preferredPrice = preferredPrice;
    }
    
    public Bid()
    {
        identityResources = new  ArrayList<IdentityResource>();
    }
    
    public Bid(Agent agent)
    {
        this();
        this.agent = agent;
    }
    
    public void configIdentityResources()
    {
        
    }
    
    /**
     * @return the maxIncrementPercentage
     */
    public Integer getMaxIncrementPercentage() {
        return maxIncrementPercentage;
    }

    /**
     * @param maxIncrementPercentage the maxIncrementPercentage to set
     */
    public void setMaxIncrementPercentage(Integer maxIncrementPercentage) {
        this.maxIncrementPercentage = maxIncrementPercentage;
    }

    /**
     * @return the identityResources
     */
    public ArrayList<IdentityResource> getIdentityResources() {
        return identityResources;
    }

    /**
     * @param identityResources the identityResources to set
     */
    public void setIdentityResources(ArrayList<IdentityResource> identityResources) {
        this.identityResources = identityResources;
    }
         
    public Agent getAgent()
    {
        return this.agent;
    }
    
    /*public double getAdaptedPrice()
    {
        // TODO check implementation, this is a simple one
        double ret=0;
        
        for (IdentityResource ir: getIdentityResources())
        {
            //ret+=ir.getPrice()*ir.getPriority().getLevel();
            ret+=ir.calculateCurrentPrice(this.getPreferredPrice());
        }
        return ret;
    }*/
    
    @Override
    public String toString()
    {
        String resource="";
        for (IdentityResource ir:this.getIdentityResources())
        {
            resource+="|"+ir.getResourceType().name()+","+ir.getPriority().name()+","+getPreferredPrice()/getIdentityResources().size();
        }
        resource=resource.substring(1);
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode()+" {"+resource+"}";
    }
    
    
    //  ------------------------------------Remove if Graph not needed---------------------------------
    private Double SubmissionTime;
    
    public void setTimeOfSubmission(double time){
        this.SubmissionTime=time-FederatedCoordinator.getInstance().Initialtime; //by applying this we get the exact time a bid has submitted since the begining of running this software
    }
    
    public double getTimeOfSubmission(){
        return SubmissionTime;
    }
    
    @Override
    public Bid clone()
    {
        Bid clone=new Bid();
        
        clone.setMaxIncrementPercentage(this.getMaxIncrementPercentage());
        clone.setPreferredPrice(this.getPreferredPrice());
        clone.setTimeOfSubmission(this.getTimeOfSubmission());
        
        ArrayList<IdentityResource> irList=new ArrayList<IdentityResource>();
        for(IdentityResource ir: this.getIdentityResources())
        {
            IdentityResource nir=new IdentityResource();
            nir.setCost(ir.getCost());
            nir.setDurationOfAuction(ir.getDurationOfAuction());
            //nir.setMaxPrice(ir.getMaxPrice());
            //nir.setMinimumProfit(ir.getMinimumProfit());
            //nir.setPreferredProfit(ir.getPreferredProfit());
            //nir.setPrice(ir.getPrice());
            nir.setPriority(IdentityResource.Priority.createByNumber(ir.getPriority().getLevel()));
            nir.setResourceType(ir.getResourceType());
            
            irList.add(nir);
        }
        clone.setIdentityResources(irList);
        
        return clone;
    }
    
    public void modifiedBy(IdentityResource.ResourceType resourceType, IdentityResource.Priority priority)
    {
        this.original=this.clone();
        for(IdentityResource ir:this.getIdentityResources())
        {
            if (ir.getResourceType().equals(resourceType))
            {
                Float currentPrice=calculateCurrentOffer(this.getPreferredPrice());
                IdentityResource.Priority oldPriority=ir.getPriority();
                ir.setPriority(priority);
                Float newPrice=calculateCurrentOffer(this.getPreferredPrice());
                Float delta=(newPrice-currentPrice);
                this.setPreferredPrice(this.getPreferredPrice()+delta);
                
                System.out.println(this.original+"was modified\n"+
                        " resource="+resourceType.name()+" from "+oldPriority.name()+ " to "+priority.name()+
                        " old price="+currentPrice+", new price="+newPrice+
                        "\n"+this);
            }
        }
    }
    
    
    public float calculateCurrentOffer(Float requiredPrice)
    {        
        float offeringPrice = -1;
        if(getPreferredPrice() < requiredPrice)
        {
            float currentPrice = getPreferredPrice()*(1+getMaxIncrementPercentage()/100);
            if(currentPrice >= requiredPrice)
            {
                int currentIncrementPercentage = 0;
                do
                {                   
                    currentIncrementPercentage++;
                    currentPrice = getPreferredPrice()*(1+currentIncrementPercentage/100);
                } while(currentIncrementPercentage<getMaxIncrementPercentage() || currentPrice>requiredPrice); 
                offeringPrice = currentPrice;
            }            
        }
        else
        {
            offeringPrice = preferredPrice;
        }               
        return offeringPrice;
    }
}
