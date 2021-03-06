/*
 * Copyright (C) 2014 frankouz.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 *
 * IT Consultore (ITC)
 * Guayaquil, Ecuador
 */
package uk.ac.bham.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static uk.ac.bham.simulator.FederatedCoordinator.Initialtime;

/**
 *
 * @author Francisco Ramirez
 */
public class Auction implements Runnable {

    ArrayList<AuctionAsk> auctionAskList;   
    ArrayList<Bid> bidList;
    Map<Bid, AuctionAsk> waitingMap = null;
    ArrayList<Bid> notifiedBidList = null;
    

    private final String WAITING_MAP_LOCK = "WAITING MAP LOCK";
    private final String BID_AUCTION_ASK_LOCK = "BID AUCTION ASK LOCK";
    private final String NOTIFIED_BID_LOCK = "NOTIFIED BID LOCK";
    
    private Float firstHighestPrice;
    private Float secondHighestPrice;
    private Float initialOfferPrice;
    

    boolean isRunning;

    public Auction(ArrayList<Bid> bidList, ArrayList<AuctionAsk> askList) {
        this.bidList = new ArrayList<Bid>();
        this.auctionAskList = new ArrayList<AuctionAsk>();
        this.waitingMap = new java.util.HashMap<Bid, AuctionAsk>();
        this.notifiedBidList = new ArrayList<Bid>();
        
        this.bidList.addAll(bidList);
        this.auctionAskList.addAll(askList);

        this.isRunning = false;
    }

    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;

            Thread t = new Thread(this, "Auction");
            t.start();
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setWinnerAskForBid(Bid nextBid, AuctionAsk winnerAsk) {
        synchronized (WAITING_MAP_LOCK) {
            if (!waitingMap.containsKey(nextBid)) {
                waitingMap.put(nextBid, winnerAsk);
            }
        }
    }
    
    public boolean existsBid(Bid bid)
    {
        boolean exists=false;
        
        for(Bid b: bidList)
        {
            if(b==bid) {
                exists=true;
                break;
            }
        }
        return exists;
    }

    public void payForServiceExecution(double price, Bid bid) {
        Agent agent = bid.getAgent();
        IdentityProvider identityProvider = agent.getIdentityProvider();

        boolean existsIdentityProvider = FederatedCoordinator.getInstance().existsIdentityProvider(identityProvider);
        boolean existsAgent = FederatedCoordinator.getInstance().existsAgent(agent);

        if (existsIdentityProvider && existsAgent) {
            synchronized (WAITING_MAP_LOCK) {
                AuctionAsk winnerAsk = waitingMap.get(bid);
                if (winnerAsk != null) {
                    ServiceProvider serviceProvider = winnerAsk.getServiceProvider();
                    identityProvider.notifyPayment(bid, serviceProvider);
                    float willingToPayPrice = bid.getPreferredPrice(); // TODO check if this values is ok to calculate the revenue
                    float askPrice = winnerAsk.calculateCurrentPrice(willingToPayPrice);
                    serviceProvider.addRevenue(Math.round(new Float(askPrice * 0.1)));
                    //FederatedCoordinator.getInstance().addCommission(Math.round(askPrice * FederatedCoordinator.getDefaultCommission() * 1.0f) * 1.0f);
                }
            }
        }
    }
    
    public ArrayList<AuctionAsk> getCurrentAsks() {
        ArrayList<AuctionAsk> askList = new ArrayList<AuctionAsk>();

        synchronized (BID_AUCTION_ASK_LOCK) {
            askList.addAll(auctionAskList);
        }

        return askList;
    }
    
    public AuctionAsk getCheapestAsk(ArrayList<AuctionAsk> askList, float price, AuctionAsk but) {
        AuctionAsk cheapestAsk = null;
        int counter = 0;
        String steps = "";

        for (AuctionAsk aa : askList) {
            if (aa == but) {
                continue;
            }

            if (cheapestAsk == null) {
                if (aa.calculateCurrentPrice(price) == -1) {
                    continue;
                }
                cheapestAsk = aa;
            }

            float askPrice;
            
            try
            {
                askPrice = aa.calculateCurrentPrice(price);
                if (askPrice == -1) {
                    continue;
                }
            }
            catch (IllegalArgumentException ex)
            {
                System.out.println("Error calculating current price for askPrice="+aa+"("+price+")");
                aa.calculateCurrentPrice(price);
                throw ex;
            }

            counter++;
            try
            {
                float cheapestPrice = cheapestAsk.calculateCurrentPrice(price);
                steps += ", " + Math.round(askPrice) + "(" + aa.getMinimumProfit() + "," + aa.getPreferredProfit() + ")";
                if (askPrice <= cheapestPrice) {
                    cheapestAsk = aa;
                }
            } catch (IllegalArgumentException ex)
            {
                System.out.println("Error calculating current price for cheapestAsk="+cheapestAsk+"("+price+")");
                throw ex;
            }
        }
        if (FederatedCoordinator.isDebugging()) System.out.printf("Calculate CHEAPEST ASK from a list of " + askList.size() + " AuctionAsk and a price of " + Math.round(price) + " only " + counter + " Ask as available %n%s%n", steps);
        return cheapestAsk;
    }
    
    public ArrayList<AuctionAsk> getTwoCheapestAsk(ArrayList<AuctionAsk> askList, float price) {
        ArrayList<AuctionAsk> cheapestAskList = new ArrayList<AuctionAsk>();

        AuctionAsk ask0 = this.getCheapestAsk(askList, price, null);
        AuctionAsk ask1 = this.getCheapestAsk(askList, price, ask0);

        if (ask0 != null) {
            cheapestAskList.add(ask0);
        }
        if (ask1 != null) {
            cheapestAskList.add(ask1);
        }

        return cheapestAskList;
    }    

    public ArrayList<AuctionAsk> compareWithCheapestAsk(Bid bid, ArrayList<AuctionAsk> askList) {
        ArrayList<AuctionAsk> winnerAskList = new ArrayList<AuctionAsk>();
        //AuctionAsk winnerAsk = null;
        float price = bid.getPreferredPrice();
        ArrayList<AuctionAsk> cheapestAsk = getTwoCheapestAsk(askList, price);
        if (cheapestAsk != null && cheapestAsk.size() == 2) {
            float askPrice = cheapestAsk.get(0).calculateCurrentPrice(price);
            float bidPrice = bid.calculateCurrentOffer(askPrice);
            if (askPrice <= bidPrice) {
                winnerAskList.addAll(cheapestAsk);
            }
        }

        return winnerAskList;
    }

    public boolean isNotifiedBid(Bid bid) {
        boolean ret;
        synchronized (NOTIFIED_BID_LOCK) {
            ret = this.notifiedBidList.contains(bid);
        }
        return ret;
    }

    public void notifyBid(Bid bid) {
        boolean addit = false;
        synchronized (BID_AUCTION_ASK_LOCK) {
            if (this.bidList.contains(bid) && !this.notifiedBidList.contains(bid)) {
                // TODO is it required to remove from the bidList ??
                // put in the notified list
                addit = true;
            }
        }
        if (addit) {
            synchronized (NOTIFIED_BID_LOCK) {
                // put in the notified list
                this.notifiedBidList.add(bid);
            }
        }
    }

    
    public Bid getNextBid() {
        Bid nextBid = null;
        synchronized (BID_AUCTION_ASK_LOCK) {
            for (Bid b : this.bidList) {
                if (!this.isNotifiedBid(b)) {
                    nextBid = b;
                    break;
                }
            }
        }
        return nextBid;
    }    

    public synchronized void run() {
        Bid oneWinnerBid=null;
        AuctionAsk oneWinnerAsk=null;
        
        Float lastPrice=null;
        ArrayList<Float> priceList=new ArrayList<Float>();
        
        while (isRunning()) {
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(Auction.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Bid nextBid = this.getNextBid();
            if (nextBid!=null)
            {
                for (IdentityResource ir: nextBid.getIdentityResources())
                {
                    // TODO why get cost is null
                    HistoricalPrice.getInstance().addPrice(ir.getResourceType(), ir.getPriority().getLevel());
                }
            }
            if (nextBid != null) {
                if (FederatedCoordinator.isDebugging()) Logger.getLogger(Auction.class.getName()).log(Level.INFO, "a {0} was detected by {1} to search and auction winner ask", new Object[]{nextBid, FederatedCoordinator.getInstance()});

                ArrayList<AuctionAsk> askList = this.getCurrentAsks();
                ArrayList<AuctionAsk> winnerAskList = this.compareWithCheapestAsk(nextBid, askList);
                AuctionAsk winnerAsk = null;
                if (winnerAskList.size() > 0) {
                    winnerAsk = winnerAskList.get(0);
                }
                if(oneWinnerAsk==null) 
                {
                    oneWinnerAsk=winnerAsk;
                } else {
                    
                }
                if(oneWinnerBid==null) {
                    try
                    {
                        oneWinnerBid=nextBid;
                        float willingToPay = oneWinnerBid.getPreferredPrice();
                        float askPrice = oneWinnerAsk.calculateCurrentPrice(willingToPay);
                        if(this.initialOfferPrice==null)
                            this.initialOfferPrice=askPrice;
                        float bidPrice = oneWinnerBid.calculateCurrentOffer(askPrice);
                        
                        lastPrice=bidPrice;
                        priceList.add(bidPrice);
                    } catch (Exception exception)
                    {
                        System.out.println("debug why exception here..." + exception);
                        exception.printStackTrace();
                    }
                } else {
                    try
                    {
                        float willingToPay = nextBid.getPreferredPrice();
                        float askPrice = oneWinnerAsk.calculateCurrentPrice(willingToPay);
                        float bidPrice = nextBid.calculateCurrentOffer(askPrice);
                        
                        if (bidPrice>lastPrice)
                        {
                            oneWinnerBid=nextBid;
                            lastPrice=bidPrice;
                        }
                        priceList.add(bidPrice);
                    } catch (Exception exception)
                    {
                        System.out.println("debug why exception here..." + exception);
                        exception.printStackTrace();
                    }
                }

                this.notifyBid(nextBid); // to move nextBid
            }
            
            if(nextBid==null) stop();
        }
        
        // ONE BID AND WINNER ASK FOR EACH AUCTION
        if(oneWinnerAsk!=null && oneWinnerBid!=null)
        {
            for(int i=0; i<priceList.size()-1; i++)
            {
                for(int j=i+1; j<priceList.size(); j++)
                {
                    float pi=priceList.get(i);
                    float pj=priceList.get(j);
                    
                    if(pi<pj)
                    {
                        priceList.set(i, pj);
                        priceList.set(j, pi);
                    }
                }
            }
            this.firstHighestPrice=priceList.get(0);
            this.secondHighestPrice=priceList.get(0);
            float secondLowPrice=priceList.get(0);
            if(priceList.size()>1) {
                secondLowPrice=priceList.get(1);
                this.secondHighestPrice=priceList.get(1);
            }
            
            IdentityProvider ip = oneWinnerBid.getAgent().getIdentityProvider();

            this.setWinnerAskForBid(oneWinnerBid, oneWinnerAsk);
            // TODO notify to IdentityProvider of Agent ??
            try {
                // winnerAsk.getAdaptedPrice()
                //float willingToPay = oneWinnerBid.getPreferredPrice(); // TODO check if this is the final value insted of the calculate from winnerAsk
                float askPrice;
                float bidPrice;

                askPrice = oneWinnerAsk.calculateCurrentPrice(secondLowPrice); // second price
                bidPrice = oneWinnerBid.calculateCurrentOffer(askPrice);

                oneWinnerAsk.getServiceProvider().notifyAuctionWinner(ip, oneWinnerBid, bidPrice, askPrice);
            } catch (Exception exception) {
                System.out.println("debug why exception here..." + exception);
                exception.printStackTrace();
            }
            if (FederatedCoordinator.isDebugging()) Logger.getLogger(Auction.class.getName()).log(Level.INFO, "the {0} had a winner {1}", new Object[]{oneWinnerBid, oneWinnerAsk});
        }
    }

    public void printWinnerAuctionAsk(int n) {
        String s="";
        
        System.out.println(s);
        System.out.println(s+"Number of bids: " + this.bidList.size());
        System.out.println(s+"Number of auction asks: " + this.auctionAskList.size());
        //System.out.println(s+"Federated Commission (" + Math.round(FederatedCoordinator.getDefaultCommission() * 100) + "%): " + Math.round(FederatedCoordinator.getInstance().getCommission()));

        synchronized (WAITING_MAP_LOCK) {
            System.out.printf(s+"%n"+s);
            for (int i = 0; i < 16 + 18 * 3 + 2 * 2; i++) {
                System.out.print("/");
            }
            System.out.printf("%n"+s);
            for (int i = 0; i < (16 + 18 * 3 + 2 * 2) / 16; i++) {
                if(AgentManager.getInstance().isRandom())
                    System.out.print("RANDOM AUCTION  ");
                else
                    System.out.print("MODELLED AUCTION  ");
            }
            System.out.printf("%n"+s);
            for (int i = 0; i < 16 + 18 * 3 + 2 * 2; i++) {
                System.out.print("\\");
            }
            System.out.printf("%n");

            int bidCounter = 0;
            for (Map.Entry<Bid, AuctionAsk> entry : this.waitingMap.entrySet()) {
                bidCounter++;
                String footer = "";
                String header = "" + bidCounter;
                while (header.length() < 3) {
                    header = "0" + header;
                }
                footer = "  END AUCTION # " + header + " ";
                header = "  BEGIN AUCTION # " + header + "  ";
                while (header.length() < 16 + 18 * 3 + 2 * 2) {
                    if (header.length() % 2 == 0) {
                        header += "*";
                    } else {
                        header = "*" + header;
                    }
                }
                while (footer.length() < 16 + 18 * 3 + 2 * 2) {
                    if (footer.length() % 2 == 0) {
                        footer += "*";
                    } else {
                        footer = "*" + footer;
                    }
                }

                System.out.printf("%s%n%s%n",s,s);
                System.out.println(s+header);

                System.out.printf(s+"%-15s %-17s   %-17s   %-17s%n", "", "Current State", "Matched Ask", "Adapted State");
                System.out.print(s);
                for (int i = 0; i < 16 + 18 * 3 + 2 * 2; i++) {
                    System.out.print("-");
                }
                System.out.println();
                Bid bid = entry.getKey();
                Bid bidInitial = bid;
                Bid bidModified = null;
                if (bid.getOriginal() != null) {
                    bidModified = bid;
                    bidInitial = bid.getOriginal();
                }
                AuctionAsk ask = entry.getValue();

                Map<Integer, IdentityResource[]> join = new HashMap<Integer, IdentityResource[]>();
                if (bidInitial != null) {
                    for (IdentityResource ir : bidInitial.getIdentityResources()) {
                        int id = ir.getResourceType().getId();
                        if (!join.containsKey(id)) {
                            join.put(id, new IdentityResource[]{null, null, null});
                        }
                        join.get(id)[0] = ir;
                    }
                }

                if (ask != null) {
                    for (IdentityResource ir : ask.getIdentityResources()) {
                        int id = ir.getResourceType().getId();
                        if (!join.containsKey(id)) {
                            join.put(id, new IdentityResource[]{null, null, null});
                        }
                        join.get(id)[1] = ir;
                    }
                }

                if (bidModified != null) {
                    for (IdentityResource ir : bidModified.getIdentityResources()) {
                        int id = ir.getResourceType().getId();
                        if (!join.containsKey(id)) {
                            join.put(id, new IdentityResource[]{null, null, null});
                        }
                        join.get(id)[2] = ir;
                    }
                }

                float price = 0;

                String[] bidTextInitial = new String[]{"--", "--", "--", "--", "--"};
                String[] bidTextModified = new String[]{"--", "--", "--", "--", "--"};
                if (bidInitial != null) {
                    price = bidInitial.getPreferredPrice();
                    bidTextInitial[0] = "Id=" + bidInitial.hashCode();
                    //TODO check how to pass the price
                    bidTextInitial[1] = "F-H Price="+ Math.round(this.firstHighestPrice*100+0.5)/100.0;
                    bidTextInitial[2] = "S-H Price=" + Math.round(this.secondHighestPrice*100+0.5)/100.0;
                    // no required /////bidTextInitial[3] = "Price=" + Math.round(bidInitial.getPreferredPrice() * 100 + 0.5) / 100.0;
                }

                String[] askText = new String[]{"--", "--", "--", "--", "--"};
                int icommission=0;
                if (ask != null) {
                    price=this.secondHighestPrice;
                    if (bidModified!=null) price=bidModified.getPreferredPrice();
                    int revenue = ask.getServiceProvider().getRevenue();
                    icommission = Math.round(price * FederatedCoordinator.getDefaultCommission());
                    askText[0] = "Id=" + ask.hashCode();
                    //TODO check how to pass the price
                    askText[1] = "O-P Price=" + Math.round(this.initialOfferPrice*100+0.5)/100.0;
                    double profit=price - ask.getTotalCosts();
                    askText[2] = "Profit=" + Math.round(profit)+" ("+Math.round(profit/price*100)+"%)";
                    //askText[2]="Revenue="+Math.round(revenue);
                }

                if (bidModified != null) {
                    price = bidModified.getPreferredPrice();
                    bidTextModified[0] = "Id=" + bidModified.hashCode();
                    //TODO check how to pass the price
                    bidTextModified[1] = "T-A Price=" + Math.round(bidModified.getPreferredPrice() * 100 + 0.5) / 100.0;
                    bidTextModified[2] = "Fed.Commission=" + Math.round(icommission);
                    
                    FederatedCoordinator.getInstance().addCommission(icommission*1.0f);
                }

                for (int i = 0; i < 4; i++) {
                    System.out.printf(s+"%-15s %-17s   %-17s   %-17s %n", "", bidTextInitial[i], askText[i], bidTextModified[i]);
                }

                System.out.printf(s+"%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", "Features", "Priority", "", "Priority", "", "Priority", "");
                System.out.print(s);
                for (int i = 0; i < 16 + 6 * 9 + 2 * 2; i++) {
                    System.out.print("=");
                }
                System.out.println();

                for (Map.Entry<Integer, IdentityResource[]> j : join.entrySet()) {
                    IdentityResource.ResourceType rt = IdentityResource.ResourceType.createByNumber(j.getKey());
                    IdentityResource irBidInitial = j.getValue()[0];
                    IdentityResource irAsk = j.getValue()[1];
                    IdentityResource irBidModified = j.getValue()[2];
                    String pnameBidInitial = "";
                    String pnameAsk = "";
                    String pnameBidModified = "";
                    String priceBidInitial = "";
                    String priceAsk = "";
                    String priceBidModified = "";

                    if (irBidInitial != null) {
                        pnameBidInitial = irBidInitial.getPriority().name();
                        if (irBidInitial.getCost() != null) {
                            priceBidInitial = "" + irBidInitial.getCost();
                        }
                    }
                    if (irAsk != null) {
                        pnameAsk = irAsk.getPriority().name();
                        ////priceAsk=""+irAsk.getCost();
                    }
                    if (irBidModified != null) {
                        pnameBidModified = irBidModified.getPriority().name();
                        if (irBidModified.getCost() != null) {
                            priceBidModified = "" + irBidModified.getCost();
                        }
                    }

                    if (pnameBidModified == "") {
                        pnameBidModified = "--";
                    }
                    if (pnameAsk == pnameBidInitial) {
                        pnameAsk = "--";
                    }
                    if (pnameBidModified == pnameBidInitial) {
                        pnameBidModified = "--";
                    }
                    System.out.printf(s+"%-15s %-8s %8s   %-8s %8s   %-8s %8s%n", rt.name(), pnameBidInitial, priceBidInitial, pnameAsk, priceAsk, pnameBidModified, priceBidModified);
                    //System.out.printf("%-30s %-30s %n", bid.toString(), ask.toString());
                }
                System.out.println(s+footer);
        System.out.println(s);
        System.out.println(s+"F-H Price=First-Highest Price");
        System.out.println(s+"S-H Price=Second-Highest Price");
        System.out.println(s+"O-P Price=Opening Price");
        System.out.println(s+"T-A Price=Total Price After Adaptation");
            }
        }
    }
}
