/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.demo.Graph;

/**
 *
 * @author Francisco Ramirez
 */
public class FederatedCoordinator implements Runnable {

    private static final Float DEFAULTCOMMISSION = 0.05f;

    static Double Initialtime;
    ArrayList<ServiceProvider> serviceProviderList = null;
    ArrayList<IdentityProvider> identityProviderList = null;
    ArrayList<Agent> agentList = null;
    ArrayList<AuctionAsk> auctionAskList = null;
    ArrayList<Bid> bidList = null;

    ArrayList<Auction> auctionList;
    boolean running = false;

    private static final FederatedCoordinator instance = new FederatedCoordinator();
    private Float commission;

    private final String IDENTITY_PROVIDER_LOCK = "IDENTITY PROVIDER LOCK";
    private final String AGENT_LOCK = "AGENT LOCK";
    private final String SERVICE_PROVIDER_LOCK = "SERVICE PROVIDER LOCK";
    private final String BID_AUCTION_ASK_LOCK = "BID AUCTION ASK LOCK";
    private final String RUNNING_LOCK = "RUNNING LOCK";

    private FederatedCoordinator() {
        serviceProviderList = new ArrayList<ServiceProvider>();
        identityProviderList = new ArrayList<IdentityProvider>();
        auctionAskList = new ArrayList<AuctionAsk>();
        agentList = new ArrayList<Agent>();
        bidList = new ArrayList<Bid>();
        commission = 0.0f;

        auctionList = new ArrayList<Auction>();
    }

    public void clear() {
        serviceProviderList.clear();
        identityProviderList.clear();
        auctionAskList.clear();
        agentList.clear();
        bidList.clear();
        auctionList.clear();
        commission = 0.0f;
    }

    public static FederatedCoordinator getInstance() {
        return FederatedCoordinator.instance;
    }

    public ArrayList<AuctionAsk> getCurrentAsks() {
        ArrayList<AuctionAsk> askList = new ArrayList<AuctionAsk>();

        synchronized (BID_AUCTION_ASK_LOCK) {
            askList.addAll(auctionAskList);
        }

        return askList;
    }

    

   
    /* The agent publish a bid to the FederatedCoordinator */
    public boolean publishBid(Bid bid) {
        boolean isPublished;

        Agent agent = bid.getAgent();
        boolean isValidAgentSession = this.validateAgentSession(agent);

        if (!isValidAgentSession) {
            agent.requestAuthentication();
        }

        isValidAgentSession = this.validateAgentSession(agent);
        if (isValidAgentSession) {
            if (!bidList.contains(bid)) {
                bidList.add(bid);
            }
        }
        isPublished = bidList.contains(bid);

        return isPublished;
    }

    /* The ServiceProvider publish an auctionAsk to the FederatedCoordinator */
    public boolean publishAuctionAsk(AuctionAsk auctionAsk) {
        boolean isPublished;

        synchronized (BID_AUCTION_ASK_LOCK) {
            if (!auctionAskList.contains(auctionAsk)) {
                auctionAskList.add(auctionAsk);
            }
            isPublished = auctionAskList.contains(auctionAsk);
        }

        return isPublished;
    }

    public boolean existsAgent(Agent agent) {
        boolean existsAgent;

        synchronized (AGENT_LOCK) {
            existsAgent = agentList.contains(agent);
        }
        return existsAgent;
    }

    public boolean existsIdentityProvider(IdentityProvider identityProvider) {
        boolean existsIdentityProvider;

        synchronized (IDENTITY_PROVIDER_LOCK) {
            existsIdentityProvider = identityProviderList.contains(identityProvider);
        }

        return existsIdentityProvider;
    }
    
    public void payForServiceExecution(double price, Bid bid)
    {
        for (Auction a: auctionList)
        {
            if(a.existsBid(bid))
            {
                a.payForServiceExecution(price, bid);
                break;
            }
        }
    }

    public boolean validateAgentSession(Agent agent) {
        boolean isValid = true;

        return isValid;
    }

    public void addSession(Agent agent) {
        synchronized (AGENT_LOCK) {
            if (!agentList.contains(agent)) {
                agentList.add(agent);
            }
        }
    }

    public boolean registerIdentityProvider(IdentityProvider identityProvider) {
        boolean isRegistered;

        synchronized (IDENTITY_PROVIDER_LOCK) {
            if (!identityProviderList.contains(identityProvider)) {
                identityProviderList.add(identityProvider);
            }
            isRegistered = identityProviderList.contains(identityProvider);
        }
        return isRegistered;
    }

    public boolean registerServiceProvider(ServiceProvider serviceProvider) {
        boolean isRegistered;
        synchronized (SERVICE_PROVIDER_LOCK) {
            if (!serviceProviderList.contains(serviceProvider)) {
                serviceProviderList.add(serviceProvider);
            }
            isRegistered = serviceProviderList.contains(serviceProvider);
        }

        return isRegistered;
    }

    public void start() {
        synchronized (RUNNING_LOCK) {
            if (!running) {
                Thread thread = new Thread(this, "FederatedCoordiantor");
                this.running = true;
                thread.start();
            }
        }
    }

    public void stop() {
        synchronized (RUNNING_LOCK) {
            this.running = false;
        }
    }

    public boolean isRunning() {
        boolean ret;
        synchronized (RUNNING_LOCK) {
            ret = this.running;
        }
        return ret;
    }
    
 /*   public ArrayList<AuctionAsk> getCurrentAsks()
    {
        ArrayList<AuctionAsk> list=new ArrayList<AuctionAsk>();
        synchronized (BID_AUCTION_ASK_LOCK)
        {
            list.addAll(this.auctionAskList);
            this.auctionAskList.clear(); // TODO test this
        }
        return list;
    }*/
    
    public ArrayList<Bid> pullCurrentBids()
    {
        ArrayList<Bid> list=new ArrayList<Bid>();
        synchronized (BID_AUCTION_ASK_LOCK)
        {
            list.addAll(this.bidList);
            this.bidList.clear(); // TODO test this
        }
        return list;
    }
    
    public void addAuction(Auction auction)
    {
        synchronized(BID_AUCTION_ASK_LOCK)
        {
            auctionList.add(auction);
        }
    }


    /**
     *
     * Implements Runnable to do tasks of FederatedCoordinator
     *
     */
    public synchronized void run() {
        while (isRunning()) {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            boolean existsBidNoAuction = this.existsBidNoAuction();
            
            //TODO prepare for auction
            
            if(Utilities.generateRandomInteger(1, 10)%2==0)
            {
                if(this.bidList.size()==1 && AgentManager.getInstance().getTotalBid()>=1) continue;
                
                if((this.bidList.size()>2 && this.auctionAskList.size()>5) || (this.bidList.size()>0 && !AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning()))
                {
                    System.out.println("*********** BEGIN A NEW AUCTION ***********");
                    ArrayList<AuctionAsk> aList=this.getCurrentAsks();
                    ArrayList<Bid> bList=this.pullCurrentBids();
                    
                    Auction auction=new Auction(bList, aList);
                    this.addAuction(auction);
                    auction.start();
                }
            }
            

            if (!AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning() && !existsBidNoAuction) {
                if (!existsAuctionRunning()) {
                    break;
                }
            }
        }

        this.printAuctionList();

        stop();
        
        System.out.println("+++++ END +++++");
        System.out.println("+++++ "+this.auctionList.size()+" AUCTIONS +++++");
    }
    
    public boolean existsBidNoAuction()
    {
        boolean exists=false;
        synchronized (BID_AUCTION_ASK_LOCK)
        {
            for (Bid b:bidList)
            {
                exists=false;
                for (Auction a: auctionList)
                {
                    exists=(exists || a.existsBid(b));
                }
                if(!exists) return true;
            }
            if(bidList.isEmpty()) return false;
        }
        
        return false;
    }

    public boolean existsAuctionRunning() {
        boolean exists = false;
        for (Auction a : auctionList) {
            exists = (exists || a.isRunning());
        }
        return exists;
    }

    public void printAuctionList() {
        int c=0;
        for (Auction a : auctionList) {
            c++;
            System.out.println();
            System.out.println();
            System.out.println("AUCTION # 0"+c);
            a.printWinnerAuctionAsk(c);
        }
        System.out.println();
        System.out.println("Federated Commission (" + Math.round(FederatedCoordinator.getDefaultCommission() * 100) + "%): " + Math.round(FederatedCoordinator.getInstance().getCommission()));

    }

    public static void main(String[] args) {
        Initialtime = (double) System.currentTimeMillis();
        FederatedCoordinator.getInstance().start();

        AgentManager.getInstance().setRandom();
        ServiceProviderManager.getInstance().setRandom();
        AgentManager.getInstance().start();
        ServiceProviderManager.getInstance().start();
            //Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        //public void run() {

        boolean working = true;
        while (working) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FederatedCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!FederatedCoordinator.getInstance().isRunning() && !AgentManager.getInstance().isRunning() && !ServiceProviderManager.getInstance().isRunning()) {
                working = false;
            }
        }
              //}}));

        
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
         
         /*
         Graph.GenerateGraph();
         */
    }

    @Override
    public String toString() {
        return "" + this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    public static Float getDefaultCommission() {
        return DEFAULTCOMMISSION;
    }

    /**
     * @return the commission
     */
    public Float getCommission() {
        return commission;
    }

    /**
     * @param commission the commission to set
     */
    public void addCommission(Float commission) {
        this.commission += commission;
    }

}
