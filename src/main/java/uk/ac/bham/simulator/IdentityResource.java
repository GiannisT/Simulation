package uk.ac.bham.simulator;

/**
 *
 * @author 
 */
public class IdentityResource 
{
    private Integer price;
    private Integer MaxPrice;
    private Long Duration;
    private ResourceType resourceType;
    private Priority priority;   
    
    
    public enum ResourceType 
    {
        Availability(1), 
        Anonymity(2), 
        Integrity(3), 
        Performance(4);
        
        private int id;
        
        ResourceType(int id) 
        {
            this.id = id;
        }

        public int getId() 
        { 
            return id;
        }
        
        public static ResourceType createByNumber(int id)
        {            
            ResourceType instance = null;
            for (ResourceType p : ResourceType.values())
            {
                if (p.getId()==id)
                {
                    instance = p;
                    break;
                }
            }
            return instance;
        }
    }
       
    public enum Priority 
    {         
        Low(0.9f), 
        Medium(1f),
        High(1.1f);
        private float level;        
        
        Priority(float level) 
        {
            this.level = level;
        }
        
        public static Priority createByNumber(float id)
        {            
            Priority instance = null;
            for (Priority p : Priority.values())
            {
                if (p.getLevel()==id)
                {
                    instance = p;
                    break;
                }
            }
            return instance;
        }
        
        public float getLevel() 
        { 
            return level;
        }
    }
    
    /**
     * @return the price
     */
    public Integer getPrice() 
    {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(Integer price) 
    {
        this.price = price;
    }    
    
     /**
     * @return the resourceType
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceType the resourceType to set
     */
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public void setDurationOfAuction (long time){
        this.Duration=time;
    }
    
    public long getDurationOfAuction(){
        return Duration;
    }
    
    public void setMaxPrice(int MaxPrice){
        this.MaxPrice=MaxPrice;
    }
    
    public int getMaxPrice(){
        return MaxPrice;
    }
    
  
}
