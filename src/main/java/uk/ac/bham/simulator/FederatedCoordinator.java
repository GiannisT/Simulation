/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 */
public class FederatedCoordinator implements Runnable {
    ArrayList<ServiceProvider> serviceProviderList=null;
    ArrayList<IdentityProvider> identityProviderList=null;
    ArrayList<AuctionAsk> auctionAskList=null;
    ArrayList<Agent> agentList=null;
    ArrayList<Bid> bidList=null;
    ArrayList<Bid> notifiedBidList=null;
    boolean running=false;
    
    
    
    private final String SERVICE_PROVIDER_LOCK="SERVICE PROVIDER LOCK";
    private final String IDENTITY_PROVIDER_LOCK="IDENTITY PROVIDER LOCK";
    private final String AUCTION_ASK_LOCK="AUCTION ASK LOCK";
    private final String AGENT_LOCK="AGENT LOCK";
    private final String BID_LOCK="BID LOCK";
    private final String NOTIFIED_BID_LOCK="NOTIFIED BID";
    
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
        
        synchronized (NOTIFIED_BID_LOCK)
        {
            notifiedBidList=new ArrayList<Bid>();
        }
    }
    
    
    
    public ArrayList<AuctionAsk> getCurrentAsks()
    {
        ArrayList<AuctionAsk> askList=new ArrayList<AuctionAsk>();
        
        synchronized (AUCTION_ASK_LOCK)
        {
            askList.addAll(auctionAskList);
        }
        
        return askList;
    }
    
    public AuctionAsk getCheapestAsk(ArrayList<AuctionAsk> askList)
    {
        AuctionAsk cheapestAsk=null;
        
        for (AuctionAsk aa: askList)
        {
            if(cheapestAsk==null) cheapestAsk=aa;
            else 
            {
                if(aa.getAdaptedPrice()<cheapestAsk.getAdaptedPrice())
                {
                    cheapestAsk=aa;
                }
            }
        }
        return cheapestAsk;
    }
    
    public AuctionAsk compareWithCheapestAsk(Bid bid, ArrayList<AuctionAsk> askList)
    {
        AuctionAsk winnerAsk=null;
        AuctionAsk cheapestAsk=getCheapestAsk(askList);
        if(cheapestAsk!=null)
        {
            if(cheapestAsk.getAdaptedPrice() <= bid.getAdaptedPrice())
            {
                winnerAsk=cheapestAsk;
            }
        }
        return winnerAsk;
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
    
    public void start()
    {
        Thread thread=new Thread(this, "FederatedCoordiantor");
        this.running=true;
        thread.start();
    }
    
    public void stop()
    {
        this.running=false;
    }
    
    public boolean isNotifiedBid(Bid bid)
    {
        boolean ret=false;
        synchronized (NOTIFIED_BID_LOCK)
        {
            ret=this.notifiedBidList.contains(bid);
        }
        return ret;
    }
    
    public void notifyBid(Bid bid)
    {
        boolean addit=false;
        synchronized (BID_LOCK)
        {
            if(this.bidList.contains(bid) && !this.notifiedBidList.contains(bid))
            {
                // TODO is it required to remove from the bidList ??
                // put in the notified list
                addit=true;
            }
        }
        if(addit)
        {
            synchronized (NOTIFIED_BID_LOCK)
            {
                // put in the notified list
                this.notifiedBidList.add(bid);
            }
        }
    }
    
    public Bid getNextBid()
    {
        Bid nextBid=null;
        synchronized (BID_LOCK)
        {
            for(Bid b:this.bidList)
            {
                if(!this.isNotifiedBid(b))
                {
                    nextBid=b;
                    break;
                }
            }
        }
        return nextBid;
    }
    
    /**
     * 
     * Implements Runnable to do tasks of FederatedCoordinator
     * 
     */
    
    public synchronized void run()
    {
        while (running)
        {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            Bid nextBid=this.getNextBid();
            if(nextBid!=null)
            {
                ArrayList<AuctionAsk> aList=this.getCurrentAsks();
                AuctionAsk winnerAsk=this.compareWithCheapestAsk(nextBid, aList);
                this.notifyBid(nextBid);
                IdentityProvider ip=nextBid.getAgent().getIdentityProvider();
                // TODO which one notify to IdentityProvider of the AuctionAsk or Bid ??
                winnerAsk.getServiceProvider().notifyAuctionWinner(nextBid.getIdentityResources(), ip, nextBid);
            }
        }
    }
}
