/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.ArrayList;
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
    private static final Float DEFAULTCOMMISSION=0.05f;
    
    static Double Initialtime;
    ArrayList<ServiceProvider> serviceProviderList=null;
    ArrayList<IdentityProvider> identityProviderList=null;
    ArrayList<AuctionAsk> auctionAskList=null;
    ArrayList<Agent> agentList=null;
    ArrayList<Bid> bidList=null;
    ArrayList<Bid> notifiedBidList=null;
    Map<Bid, AuctionAsk> waitingMap=null;
    boolean running=false;
    
    private static final FederatedCoordinator instance=new FederatedCoordinator();
    private Float commission;
    
    
    private final String IDENTITY_PROVIDER_LOCK="IDENTITY PROVIDER LOCK";
    private final String AGENT_LOCK="AGENT LOCK";
    private final String SERVICE_PROVIDER_LOCK="SERVICE PROVIDER LOCK";
    private final String BID_AUCTION_ASK_LOCK="BID AUCTION ASK LOCK";
    private final String NOTIFIED_BID_LOCK="NOTIFIED BID LOCK";
    private final String RUNNING_LOCK="RUNNING LOCK";
    private final String WAITING_MAP_LOCK="WAITING MAP LOCK";
    
    
    private FederatedCoordinator()
    {
        serviceProviderList=new ArrayList<ServiceProvider>();
        identityProviderList=new ArrayList<IdentityProvider>();
        auctionAskList=new ArrayList<AuctionAsk>();
        agentList=new ArrayList<Agent>();
        bidList=new ArrayList<Bid>();
        notifiedBidList=new ArrayList<Bid>();
        waitingMap=new java.util.HashMap<Bid, AuctionAsk>();
        commission = 0.0f;
    }
    
    public void clear()
    {
        serviceProviderList.clear();
        identityProviderList.clear();
        auctionAskList.clear();
        agentList.clear();
        bidList.clear();
        notifiedBidList.clear();
        waitingMap.clear();
        commission= 0.0f;
    }

    
    public static FederatedCoordinator getInstance()
    {
        return FederatedCoordinator.instance;
    }
    
    
    
    public ArrayList<AuctionAsk> getCurrentAsks()
    {
        ArrayList<AuctionAsk> askList=new ArrayList<AuctionAsk>();
        
        synchronized (BID_AUCTION_ASK_LOCK)
        {
            askList.addAll(auctionAskList);
        }
        
        return askList;
    }
    
    public ArrayList<AuctionAsk> getTwoCheapestAsk(ArrayList<AuctionAsk> askList, float price)
    {
        ArrayList<AuctionAsk> cheapestAskList=new ArrayList<AuctionAsk>();
        
        AuctionAsk ask0=this.getCheapestAsk(askList, price, null);
        AuctionAsk ask1=this.getCheapestAsk(askList, price, ask0);
        
        if(ask0!=null) cheapestAskList.add(ask0);
        if(ask1!=null) cheapestAskList.add(ask1);
        
        return cheapestAskList;
    }
    
    public AuctionAsk getCheapestAsk(ArrayList<AuctionAsk> askList, float price, AuctionAsk but)
    {
        AuctionAsk cheapestAsk=null;
        int counter=0;
        String steps="";
        
        for (AuctionAsk aa: askList)
        {
            if(aa==but) continue;
            
            if(cheapestAsk==null) 
            {
                if(aa.calculateCurrentPrice(price)==-1) continue;
                cheapestAsk=aa;
            }

            float askPrice=aa.calculateCurrentPrice(price);
            if(askPrice==-1) continue;
            
            counter++;
            float cheapestPrice=cheapestAsk.calculateCurrentPrice(price);
            steps+=", "+Math.round(askPrice)+"("+aa.getMinimumProfit()+","+aa.getPreferredProfit()+")";
            if(askPrice <= cheapestPrice)
            {
                cheapestAsk=aa;
            }
        }
        System.out.printf("Calculate CHEAPEST ASK from a list of "+askList.size()+" AuctionAsk and a price of "+Math.round(price)+" only "+counter+" Ask as available %n%s%n",steps);
        return cheapestAsk;
    }
    
    public ArrayList<AuctionAsk> compareWithCheapestAsk(Bid bid, ArrayList<AuctionAsk> askList)
    {
        ArrayList<AuctionAsk> winnerAskList=new ArrayList<AuctionAsk>();
        AuctionAsk winnerAsk=null;
        float price=bid.getPreferredPrice();
        ArrayList<AuctionAsk> cheapestAsk=getTwoCheapestAsk(askList, price);
        if(cheapestAsk!=null && cheapestAsk.size()==2)
        {
            float askPrice=cheapestAsk.get(0).calculateCurrentPrice(price);
            float bidPrice=bid.calculateCurrentOffer(askPrice);
            if(askPrice <= bidPrice)
            {
                winnerAskList.addAll(cheapestAsk);
            }
        }
       
        Graph.setChartValues( (Math.floor((bid.getTimeOfSubmission()/6000)*100)/100), (double)(Math.floor(((System.currentTimeMillis()-Initialtime)/6000)*100)/100) ); //update the dataset that will be used to develop the graph
     
        return winnerAskList;
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
        
        synchronized(BID_AUCTION_ASK_LOCK)
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
                    float willingToPayPrice=bid.getPreferredPrice(); // TODO check if this values is ok to calculate the revenue
                    float askPrice=winnerAsk.calculateCurrentPrice(willingToPayPrice);
                    serviceProvider.addRevenue(Math.round(new Float(askPrice*0.1)));
                    this.addCommission(Math.round(askPrice*DEFAULTCOMMISSION)*1.0f);
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
        synchronized (BID_AUCTION_ASK_LOCK)
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
        synchronized (BID_AUCTION_ASK_LOCK)
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
    
    public void setWinnerAskForBid(Bid nextBid, AuctionAsk winnerAsk)
    {
        synchronized (WAITING_MAP_LOCK)
        {
            if(!waitingMap.containsKey(nextBid))
            {
                waitingMap.put(nextBid, winnerAsk);
            }
        }
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
                
                ArrayList<AuctionAsk> askList=this.getCurrentAsks();
                ArrayList<AuctionAsk> winnerAskList=this.compareWithCheapestAsk(nextBid, askList);
                AuctionAsk winnerAsk=null;
                if(winnerAskList.size()>0) winnerAsk=winnerAskList.get(0);
                this.notifyBid(nextBid);
                IdentityProvider ip=nextBid.getAgent().getIdentityProvider();
                
                this.setWinnerAskForBid(nextBid, winnerAsk);
                // TODO notify to IdentityProvider of Agent ??
                try
                {
                    if(winnerAsk!=null)
                    {
                        // winnerAsk.getAdaptedPrice()
                        float willingToPay=nextBid.getPreferredPrice(); // TODO check if this is the final value insted of the calculate from winnerAsk
                        float askPrice=winnerAsk.calculateCurrentPrice(willingToPay);
                        float bidPrice=nextBid.calculateCurrentOffer(askPrice);
                        
                        if(winnerAskList!=null && winnerAskList.size()==2)
                        {
                            askPrice=winnerAskList.get(1).calculateCurrentPrice(willingToPay); // second price
                            bidPrice=nextBid.calculateCurrentOffer(askPrice);
                        }
                        winnerAsk.getServiceProvider().notifyAuctionWinner(ip, nextBid, bidPrice, askPrice);
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
                break;
            }
        }
        
        this.printWinnerAuctionAsk();
        
        stop();
    }
    
    public void printWinnerAuctionAsk()
    {
        System.out.println();
        System.out.println("Number of bids: "+this.bidList.size());
        System.out.println("Number of service providers: "+this.serviceProviderList.size());
        System.out.println("Federated Commission ("+Math.round(DEFAULTCOMMISSION*100)+"%): "+Math.round(this.commission));
        
        
        synchronized (WAITING_MAP_LOCK)
        {
            System.out.println();
            for (int i=0; i<16+18*3+2*2; i++) System.out.print("/"); System.out.println();
            for (int i=0; i<(16+18*3+2*2)/16; i++) System.out.print("RANDOM AUCTION  "); System.out.println();
            for (int i=0; i<16+18*3+2*2; i++) System.out.print("\\"); System.out.println();
            
            int bidCounter=0;
            for (Map.Entry<Bid, AuctionAsk> entry: this.waitingMap.entrySet())
            {
                bidCounter++;
                String footer="";
                String header=""+bidCounter;
                while (header.length()<3) header="0"+header;
                footer="  END BID # "+header+" ";
                header="  BEGIN BID # "+header+"  ";
                while (header.length()<16+18*3+2*2)
                    if(header.length()%2==0) header+="*";
                    else header="*"+header;
                while (footer.length()<16+18*3+2*2)
                    if(footer.length()%2==0) footer+="*";
                    else footer="*"+footer;
                
                System.out.printf("%n%n%n");
                System.out.println(header);
                
                System.out.printf("%-15s %-17s   %-17s   %-17s%n", "", "Initial Bid", "Ask Winner", "Adapted Bid");
                for (int i=0; i<16+18*3+2*2; i++) System.out.print("-"); System.out.println();
                Bid bid=entry.getKey();
                Bid bidInitial=bid;
                Bid bidModified=null;
                if(bid.getOriginal()!=null)
                {
                    bidModified=bid;
                    bidInitial=bid.getOriginal();
                }
                AuctionAsk ask=entry.getValue();
                
                Map<Integer, IdentityResource[]> join=new HashMap<Integer, IdentityResource[]>();
                if(bidInitial!=null)
                {
                    for(IdentityResource ir:bidInitial.getIdentityResources())
                    {
                        int id=ir.getResourceType().getId();
                        if(!join.containsKey(id))
                        {
                            join.put(id, new IdentityResource[]{null,null,null});
                        }
                        join.get(id)[0]=ir;
                    }
                }
                
                if(ask!=null)
                {
                    for(IdentityResource ir:ask.getIdentityResources())
                    {
                        int id=ir.getResourceType().getId();
                        if(!join.containsKey(id))
                        {
                            join.put(id, new IdentityResource[]{null,null,null});
                        }
                        join.get(id)[1]=ir;
                    }
                }
                
                if(bidModified!=null)
                {
                    for(IdentityResource ir:bidModified.getIdentityResources())
                    {
                        int id=ir.getResourceType().getId();
                        if(!join.containsKey(id))
                        {
                            join.put(id, new IdentityResource[]{null,null,null});
                        }
                        join.get(id)[2]=ir;
                    }
                }
                
                float price=0;
                
                String[] bidTextInitial=new String[] {"--", "--", "--", "--", "--"};
                String[] bidTextModified=new String[] {"--", "--", "--", "--", "--"};
                if(bidInitial!=null)
                {
                    price=bidInitial.getPreferredPrice();
                    bidTextInitial[0]="Id="+bidInitial.hashCode();
                    //TODO check how to pass the price
                    bidTextInitial[1]="Price="+Math.round(bidInitial.getPreferredPrice()*100+0.5)/100.0;
                }
                if(bidModified!=null)
                {
                    price=bidInitial.getPreferredPrice();
                    bidTextModified[0]="Id="+bidModified.hashCode();
                    //TODO check how to pass the price
                    bidTextModified[1]="Price="+Math.round(bidModified.getPreferredPrice()*100+0.5)/100.0;
                }
                
                String[] askText=new String[] {"--", "--", "--", "--", "--"};
                if(ask!=null)
                {
                    int revenue=ask.getServiceProvider().getRevenue();
                    int icommission=Math.round(ask.calculateCurrentPrice(price)*DEFAULTCOMMISSION);
                    askText[0]="Id="+ask.hashCode();
                    //TODO check how to pass the price
                    askText[1]="Price="+Math.round(ask.calculateCurrentPrice(price));
                    askText[2]="Profit="+Math.round(ask.calculateCurrentPrice(price)-ask.getTotalCosts());
                    askText[3]="Fed.Commission="+Math.round(icommission);
                    //askText[2]="Revenue="+Math.round(revenue);
                }
                
                for(int i=0; i<4; i++)
                {
                    System.out.printf("%-15s %-17s   %-17s   %-17s %n", "", bidTextInitial[i], askText[i], bidTextModified[i]);
                }
                
                
                System.out.printf("%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", "Resource", "Priority", "", "Priority", "", "Priority", "");
                for (int i=0; i<16+6*9+2*2; i++) System.out.print("="); System.out.println();
                
                for (Map.Entry<Integer, IdentityResource[]> j:join.entrySet())
                {
                    IdentityResource.ResourceType rt=IdentityResource.ResourceType.createByNumber(j.getKey());
                    IdentityResource irBidInitial=j.getValue()[0];
                    IdentityResource irAsk=j.getValue()[1];
                    IdentityResource irBidModified=j.getValue()[2];
                    String pnameBidInitial="";
                    String pnameAsk="";
                    String pnameBidModified="";
                    String priceBidInitial="";
                    String priceAsk="";
                    String priceBidModified="";
                    
                    
                    if(irBidInitial!=null) 
                    {
                        pnameBidInitial=irBidInitial.getPriority().name();
                        if(irBidInitial.getCost()!=null)
                            priceBidInitial=""+irBidInitial.getCost();
                    }
                    if(irAsk!=null)
                    {
                        pnameAsk=irAsk.getPriority().name();
                        ////priceAsk=""+irAsk.getCost();
                    }
                    if(irBidModified!=null)
                    {
                        pnameBidModified=irBidModified.getPriority().name();
                        if(irBidModified.getCost()!=null)
                            priceBidModified=""+irBidModified.getCost();
                    }
                    
                    if(pnameBidModified=="") pnameBidModified="--";
                    if(pnameBidModified==pnameBidInitial) pnameBidModified="--";
                    System.out.printf("%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", rt.name(), pnameBidInitial, priceBidInitial, pnameAsk, priceAsk, pnameBidModified, priceBidModified);
                    //System.out.printf("%-30s %-30s %n", bid.toString(), ask.toString());
                }
                System.out.println(footer);
                System.out.println();
            }
        }
    }
    
    
    public static void main(String[] args)
    {
        Initialtime=(double) System.currentTimeMillis();
        FederatedCoordinator.getInstance().start();
        
        AgentManager.getInstance().setRandom();
        ServiceProviderManager.getInstance().setRandom();                
        AgentManager.getInstance().start();
        ServiceProviderManager.getInstance().start();
            //Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
              //public void run() {
        
        boolean working=true;
        while (working)
        {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(!FederatedCoordinator.getInstance().isRunning() && !AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning())
            {
                working=false;
            }
        }
              //}}));
        
        
        /*
        System.out.println();
        System.out.println("*** Ready for new task ***");
        FederatedCoordinator.getInstance().clear();
        AgentManager.clear();
        ServiceProviderManager.clear();
        
        FederatedCoordinator.getInstance().start();
        AgentManager.getInstance().setModelled();
        ServiceProviderManager.getInstance().setModelled();
        AgentManager.getInstance().start();
        ServiceProviderManager.getInstance().start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        Graph.GenerateGraph();
        */
    }
    

    
    @Override
    public String toString()
    {
        return ""+this.getClass().getSimpleName()+"@"+this.hashCode();
    }

    /**
     * @return the commission
     */
    public Float getCommission() 
    {
        return commission;
    }

    /**
     * @param commission the commission to set
     */
    public void addCommission(Float commission) 
    {
        this.commission += commission;
    }
    

}
