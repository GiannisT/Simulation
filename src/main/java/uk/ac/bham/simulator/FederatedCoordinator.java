/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.demo.Graph;

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
    Map<Bid, AuctionAsk> waitingMap=null;
    boolean running=false;
<<<<<<< HEAD
    private Integer commission;
=======
    Map<String, Integer> propertiesInteger=new HashMap<String, Integer>();
    Map<String, ArrayList<MonitorRecord>> monitorMap=new HashMap<String, ArrayList<MonitorRecord>>();
>>>>>>> branch 'master' of ssh://git.soravi.com:9022/home/itconsultore/giannis/simulator.git/
    
    private static final FederatedCoordinator instance=new FederatedCoordinator();
    
    
    
    private final String SERVICE_PROVIDER_LOCK="SERVICE PROVIDER LOCK";
    private final String IDENTITY_PROVIDER_LOCK="IDENTITY PROVIDER LOCK";
    private final String AUCTION_ASK_LOCK="AUCTION ASK LOCK";
    private final String AGENT_LOCK="AGENT LOCK";
    private final String BID_LOCK="BID LOCK";
    private final String NOTIFIED_BID_LOCK="NOTIFIED BID LOCK";
    private final String RUNNING_LOCK="RUNNING LOCK";
    private final String WAITING_MAP_LOCK="WAITING MAP LOCK";
    static Graph gr;
    
    private FederatedCoordinator()
    {
        serviceProviderList=new ArrayList<ServiceProvider>();
        identityProviderList=new ArrayList<IdentityProvider>();
        auctionAskList=new ArrayList<AuctionAsk>();
        agentList=new ArrayList<Agent>();
        bidList=new ArrayList<Bid>();
        notifiedBidList=new ArrayList<Bid>();
        waitingMap=new java.util.HashMap<Bid, AuctionAsk>();
        commission = 0;
    }
    

    
    public static FederatedCoordinator getInstance()
    {
        return FederatedCoordinator.instance;
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
        gr.setChartValues(bid.getTimeOfSubmission(), (double)System.currentTimeMillis()); //update the dataset that will be used to develop the graph
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
            synchronized (WAITING_MAP_LOCK)
            {
                AuctionAsk winnerAsk=waitingMap.get(bid);
                if(winnerAsk!=null)
                {
                    ServiceProvider serviceProvider=winnerAsk.getServiceProvider();
                    identityProvider.notifyPayment(bid, serviceProvider);
                }
            }
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
        synchronized (RUNNING_LOCK)
        {
            if(!running)
            {
                Thread thread=new Thread(this, "FederatedCoordiantor");
                this.running=true;
                thread.start();
            }
        }
    }
    
    public void stop()
    {
        synchronized (RUNNING_LOCK)
        {
            this.running=false;
        }
    }
    
    public boolean isRunning()
    {
        boolean ret;
        synchronized (RUNNING_LOCK)
        {
            ret=this.running;
        }
        return ret;
    }
    
    public boolean isNotifiedBid(Bid bid)
    {
        boolean ret;
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
        while (isRunning())
        {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            Bid nextBid=this.getNextBid();
            if(nextBid!=null)
            {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.INFO, "a {0} was detected by {1} to search and auction ask winner", new Object[] {nextBid, FederatedCoordinator.getInstance()});
                
                ArrayList<AuctionAsk> aList=this.getCurrentAsks();
                AuctionAsk winnerAsk=this.compareWithCheapestAsk(nextBid, aList);
                this.notifyBid(nextBid);
                IdentityProvider ip=nextBid.getAgent().getIdentityProvider();
                synchronized (WAITING_MAP_LOCK)
                {
                    if(!waitingMap.containsKey(nextBid))
                    {
                        waitingMap.put(nextBid, winnerAsk);
                    }
                }
                // TODO notify to IdentityProvider of Agent ??
                try
                {
                    if(winnerAsk!=null)
                    {
                        winnerAsk.getServiceProvider().notifyAuctionWinner(ip, nextBid, winnerAsk.getAdaptedPrice());
                    }
                }
                catch (Exception exception)
                {
                    System.out.println("debug why exception here..."+exception);
                }
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.INFO, "the {0} had a winner {1}", new Object[] {nextBid, winnerAsk});
            }
            
            if(!AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning() && nextBid==null)
            {
                stop();
            }
        }
        
        this.printWinnerAuctionAsk();
    }
    
    public void printWinnerAuctionAsk()
    {
        System.out.println();
        System.out.println("Number of bids: "+this.bidList.size());
        System.out.println("Number of service providers: "+this.serviceProviderList.size());
        
        
        System.out.printf("%n%n%-30s %-30s %n", "Bid", "Auction Ask Winner");
        synchronized (WAITING_MAP_LOCK)
        {
            for (Map.Entry<Bid, AuctionAsk> entry: this.waitingMap.entrySet())
            {
                Bid bid=entry.getKey();
                AuctionAsk ask=entry.getValue();
                
                Map<Integer, IdentityResource[]> join=new HashMap<Integer, IdentityResource[]>();
                for(IdentityResource ir:bid.getIdentityResources())
                {
                    int id=ir.getResourceType().getId();
                    if(!join.containsKey(id))
                    {
                        join.put(id, new IdentityResource[]{null,null});
                    }
                    join.get(id)[0]=ir;
                }
                
                if(ask!=null)
                {
                    for(IdentityResource ir:ask.getIdentityResources())
                    {
                        int id=ir.getResourceType().getId();
                        if(!join.containsKey(id))
                        {
                            join.put(id, new IdentityResource[]{null,null});
                        }
                        join.get(id)[1]=ir;
                    }
                }
                
                String bidText="";
                if(bid!=null)
                {
                    bidText=bid.hashCode()+"("+bid.getAdaptedPrice()+")";
                }
                
                String askText="";
                if(ask!=null)
                {
                    askText=ask.hashCode()+"("+ask.getAdaptedPrice()+")";
                }
                
                System.out.printf("%-30s %-30s%n", bidText, askText);
                
                for (Map.Entry<Integer, IdentityResource[]> j:join.entrySet())
                {
                    IdentityResource.ResourceType rt=IdentityResource.ResourceType.createByNumber(j.getKey());
                    IdentityResource irBid=j.getValue()[0];
                    IdentityResource irAsk=j.getValue()[1];
                    String pnameBid="";
                    String pnameAsk="";
                    Integer priceBid=0;
                    Integer priceAsk=0;
                    
                    
                    if(irBid!=null) 
                    {
                        pnameBid=irBid.getPriority().name();
                        priceBid=irBid.getPrice();
                    }
                    if(irAsk!=null)
                    {
                        pnameAsk=irAsk.getPriority().name();
                        priceAsk=irAsk.getPrice();
                    }
                    System.out.printf("%-15s %-8s %5s %-8s %5s %n", rt.name(), pnameBid, priceBid, pnameAsk, priceAsk);
                    //System.out.printf("%-30s %-30s %n", bid.toString(), ask.toString());
                }
                System.out.println();
            }
        }
    }
    
    public static void main(String[] args)
    {
        gr=new Graph();
        FederatedCoordinator.getInstance().start();
        gr.GenerateGraph();
        AgentManager.getInstance().start();
        ServiceProviderManager.getInstance().start();
    }
    
    public synchronized void setPropertyAsInteger(String key, Integer value)
    {
        propertiesInteger.put(key, value);
    }
    
    public synchronized Integer getPropertyAsInteger(String key)
    {
        Integer tmp=propertiesInteger.get(key);
        return tmp;
    }
    
    public synchronized void initCounter(String counter)
    {
        this.setPropertyAsInteger(counter, 0);
    }
    
    public synchronized int incrementCounter(String counter)
    {
        Integer otmp=this.getPropertyAsInteger(counter);
        int tmp=0;
        if(otmp!=null) tmp=otmp;
        tmp++;
        this.setPropertyAsInteger(counter, tmp);
        return tmp;
    }
    
    @Override
    public String toString()
    {
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode();
<<<<<<< HEAD
    }    

    /**
     * @return the commission
     */
    public Integer getCommission() {
        return commission;
    }

    /**
     * @param commission the commission to set
     */
    public void addCommission(Integer commission) {
        this.commission += commission;
=======
    }
    
    
    class MonitorRecord 
    {
        Calendar timestamp;
        double value;
        
        public MonitorRecord(long ts, double v)
        {
            timestamp=Calendar.getInstance();
            timestamp.setTimeInMillis(ts);
            value=v;
        }
        
        public Calendar getTimestamp()
        {
            return timestamp;
        }
        
        public double getValue()
        {
            return value;
        }
        
    }
    
    public synchronized void recordValue(String monitor, double value)
    {
        ArrayList<MonitorRecord> list=this.monitorMap.get(monitor);
        if(list==null)
        {
            this.monitorMap.put(monitor, new ArrayList<MonitorRecord>());
            list=this.monitorMap.get(monitor);
        }
        MonitorRecord mr=new MonitorRecord(Calendar.getInstance().getTimeInMillis(), value);
        list.add(mr);
>>>>>>> branch 'master' of ssh://git.soravi.com:9022/home/itconsultore/giannis/simulator.git/
    }
}
