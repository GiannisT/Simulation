package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 *
 * @author Francisco Ramirez
 */
public class IdentityProvider 
{
    
    protected FederatedCoordinator federatedCoordinator;
    
    ArrayList<Agent> agentList=null;
    private final String AGENT_LOCK="AGENT";
    
    public IdentityProvider()
    {
        synchronized (AGENT_LOCK)
        {
            agentList=new ArrayList<Agent>();
        }
    }
    
    public void setFederatedCoordinator(FederatedCoordinator federatedCoordinator)
    {
        this.federatedCoordinator=federatedCoordinator;
    }
    
    public void requestPayment(Float price, Bid bid)
    {
        Agent agent=bid.getAgent();
        agent.requestPayment(price, bid);
    }
    
    public boolean notifyPayment(Bid bid, ServiceProvider serviceProvider)
    {
        boolean allocation = serviceProvider.allocateResources(bid);                
        return allocation;
    }
    
    /* TODO: Random authentication, 80% true / 20% false
    */
    public boolean authenticate(String credentials)
    {
        boolean isAuthenticated=true;
        
        return isAuthenticated;
    }
    
    public boolean addAgent(Agent agent)
    {
        boolean isAdded;
        
        synchronized (AGENT_LOCK)
        {
            if(!agentList.contains(agent))
            {
                agentList.add(agent);
            }
            isAdded=agentList.contains(agent);
        }
        return isAdded;
    }
    
    
}
