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
    
    public double getAdaptedPrice()
    {
        // TODO check implementation, this is a simple one
        double ret=0;
        
        for (IdentityResource ir: getIdentityResources())
        {
            ret+=ir.getPrice()*ir.getPriority().getLevel();
        }
        return ret;
    }
    
    @Override
    public String toString()
    {
        String resource="";
        for (IdentityResource ir:this.getIdentityResources())
        {
            resource+="|"+ir.getResourceType().name()+","+ir.getPriority().name()+","+ir.getPrice();
        }
        resource=resource.substring(1);
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode()+" {"+resource+"}";
    }
    
    
    //  ------------------------------------Remove if Graph not needed---------------------------------
    private Double SubmissionTime;
    
    public void setTimeOfSubmission(double time){
        this.SubmissionTime=time;
    }
    
    public double getTimeOfSubmission(){
        return SubmissionTime;
    }
       
}
