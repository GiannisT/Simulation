/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 */
public class ServiceProviderManager implements Runnable {
        
    private static final ServiceProviderManager instance=new ServiceProviderManager();
    
    private boolean running;
    
    public static final String RUNNING_LOCK="RUNNING LOCK";
    
    private ServiceProviderManager()
    {
        synchronized (RUNNING_LOCK)
        {
            this.running=false;
        }
    }
    
    public static ServiceProviderManager getInstance()
    {
        return ServiceProviderManager.instance;
    }
    
    
    
    public void start()
    {
        synchronized (RUNNING_LOCK)
        {
            if(!this.running)
            {
                Thread thread=new Thread(this, "Service Provider Manager");
                this.running=true;
                thread.start();
            }
        }
    }
    
    public void stop()
    {
        synchronized (RUNNING_LOCK)
        {
            if(running)
                running=false;
        }
    }
    
    public boolean isRunning()
    {
        boolean ret;
        synchronized (RUNNING_LOCK)
        {
            ret=running;
        }
        return ret;
    }
    
    /**
     * Create new service provider according to a random delay, each new service provider create only one new auction ask
     * 
     */
    public synchronized void run()
    {
        int counter=0;
        IdentityProvider ip=new IdentityProvider();
        FederatedCoordinator.getInstance().registerIdentityProvider(ip);
        
        while (isRunning())
        {
            counter++;
            // TODO at the moment, fifty service provider with one auction ask eachone will be created
            if(counter>50) break;
            
            long delay_new_service_provider=Utilities.generateRandomInteger(1, 10)*10;
            long delay_first_bid=Utilities.generateRandomInteger(1, 10)*10;
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // wait delay_new_agent miliseconds before create a new Agent
            try {
                wait(delay_new_service_provider);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            ServiceProvider serviceProvider=new ServiceProvider();
            FederatedCoordinator.getInstance().registerServiceProvider(serviceProvider);
            Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.INFO, "a new {0} was created and added to {1}", new Object[] {serviceProvider, FederatedCoordinator.getInstance()});
            
            try {
                wait(delay_first_bid);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            AuctionAsk first_auction_ask=new RandomAsk(serviceProvider); //serviceProvider.createBid(null);
            FederatedCoordinator.getInstance().publishAuctionAsk(first_auction_ask);
            Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.INFO, "a new {0} was published by {1} to {2}", new Object[] {first_auction_ask, serviceProvider, FederatedCoordinator.getInstance()});

        }
        stop();
    }
}
