/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;

/**
 *
 * @author Francisco Ramirez
 */
public class FederatedCoordinator {
    ArrayList<ServiceProvider> serviceProviderList=null;
    ArrayList<IdentityProvider> identityProviderList=null;
    ArrayList<AuctionAsk> auctionAskList=null;
    ArrayList<Agent> agentList=null;
    ArrayList<Bid> bidList=null;
    
    private final String SERVICE_PROVIDER_LOCK="SERVICE PROVIDER LOCK";
    private final String IDENTITY_PROVIDER_LOCK="IDENTITY PROVIDER LOCK";
    private final String AUCTION_ASK_LOCK="AUCTION ASK LOCK";
    private final String AGENT_LOCK="AGENT LOCK";
    private final String BID_LOCK="BID LOCK";
    
    public FederatedCoordinator()
    {
        synchronized (SERVICE_PROVIDER_LOCK)
        {
            serviceProviderList=new ArrayList<ServiceProvider>();
        }
        
        synchronized (IDENTITY_PROVIDER_LOCK)
        {
            identityProviderList=new ArrayList<IdentityProvider>();
        }
        
        synchronized (AUCTION_ASK_LOCK)
        {
            auctionAskList=new ArrayList<AuctionAsk>();
        }
        
        synchronized (AGENT_LOCK)
        {
            agentList=new ArrayList<Agent>();
        }
        
        synchronized (BID_LOCK)
        {
            bidList=new ArrayList<Bid>();
        }
    }
    
    
    
    public ArrayList<AuctionAsk> getCurrentAsks()
    {
        ArrayList<AuctionAsk> asks=new ArrayList<AuctionAsk>();
        
        synchronized (AUCTION_ASK_LOCK)
        {
            asks.addAll(auctionAskList);
        }
        
        return asks;
    }
    
    public void compareWithCheapestAsk()
    {
        
    }
    
    /* The agent publish a bid to the FederatedCoordinator */
    public boolean publishBid(Bid bid)
    {
        boolean isPublished;
        
        Agent agent=bid.getAgent();
        boolean isValidAgentSession=this.validateAgentSession(agent);
        
        if(!isValidAgentSession)
        {
            agent.requestAuthentication();
        }
        
        isValidAgentSession=this.validateAgentSession(agent);
        if(isValidAgentSession)
        {
            if(!bidList.contains(bid))
                bidList.add(bid);
        }
        isPublished=bidList.contains(bid);
        
        return isPublished;
    }
    
    /* The ServiceProvider publish an auctionAsk to the FederatedCoordinator */
    public boolean publishAuctionAsk(AuctionAsk auctionAsk)
    {
        boolean isPublished;
        
        synchronized(AUCTION_ASK_LOCK)
        {
            if(!auctionAskList.contains(auctionAsk))
            {
                auctionAskList.add(auctionAsk);
            }
            isPublished=auctionAskList.contains(auctionAsk);
        }
        
        return isPublished;
    }
    
    
    public void payForServiceExecution(double price, Bid bid)
    {
        Agent agent=bid.getAgent();
        IdentityProvider identityProvider=agent.getIdentityProvider();
        
        boolean existsIdentityProvider;
        boolean existsAgent;
        
        synchronized (IDENTITY_PROVIDER_LOCK)
        {
            existsIdentityProvider=identityProviderList.contains(identityProvider);
        }
        synchronized (AGENT_LOCK)
        {
            existsAgent=agentList.contains(agent);
        }
        
        if(existsIdentityProvider && existsAgent)
        {
            identityProvider.notifyPayment(bid);
        }
    }
    
    public boolean validateAgentSession(Agent agent)
    {
        boolean isValid=true;
        
        return isValid;
    }
    
    public void addSession(Agent agent)
    {
        synchronized (AGENT_LOCK)
        {
            if(!agentList.contains(agent)) 
                agentList.add(agent);
        }
    }
    
    public boolean registerIdentityProvider(IdentityProvider identityProvider)
    {
        boolean isRegistered;
        
        synchronized (IDENTITY_PROVIDER_LOCK)
        {
            if(!identityProviderList.contains(identityProvider))
            {
                identityProviderList.add(identityProvider);
            }
            isRegistered=identityProviderList.contains(identityProvider);
        }
        return isRegistered;
    }
    
    public boolean registerServiceProvider(ServiceProvider serviceProvider)
    {
        boolean isRegistered;
        synchronized (SERVICE_PROVIDER_LOCK)
        {
            if(!serviceProviderList.contains(serviceProvider))
            {
                serviceProviderList.add(serviceProvider);
            }
            isRegistered=serviceProviderList.contains(serviceProvider);
        }
        
        return isRegistered;
    }
}
