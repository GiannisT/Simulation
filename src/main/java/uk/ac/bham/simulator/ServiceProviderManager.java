/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.bham.simulator;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Francisco Ramirez
 */
public class ServiceProviderManager extends TimerTask {
        
    private static ServiceProviderManager instance=new ServiceProviderManager();
    
    private boolean running;
    
    public static final String RUNNING_LOCK="RUNNING LOCK";
    
    private int counter;
    private Timer timer;
    
    private boolean isRandom;
    
    private ServiceProviderManager()
    {
        synchronized (RUNNING_LOCK)
        {
            this.running=false;
        }
    }

    public static void clear()
    {
        ServiceProviderManager.instance=new ServiceProviderManager();
    }
    
    public static ServiceProviderManager getInstance()
    {
        return ServiceProviderManager.instance;
    }
    
    public void setRandom()
    {
        this.isRandom=true;
    }
    
    public void setModelled()
    {
        this.isRandom=false;
    }
    
    public boolean isRandom()
    {
        return this.isRandom;
    }
    
    
    
    public void start()
    {
        synchronized (RUNNING_LOCK)
        {
            if(!this.running)
            {
                this.counter=50;
                
                long delayNewServiceProvider=Utilities.generateRandomInteger(1, 10)*10;

                this.timer=new Timer("Service Provider Manager", true);
                this.running=true;
                timer.schedule(this, delayNewServiceProvider, delayNewServiceProvider);
                //Thread thread=new Thread(this, "Service Provider Manager");
                //thread.start();
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
        //IdentityProvider ip=new IdentityProvider();
        //FederatedCoordinator.getInstance().registerIdentityProvider(ip);
        
        if (isRunning() && counter >0)
        {
            ServiceProvider serviceProvider=new ServiceProvider();
            FederatedCoordinator.getInstance().registerServiceProvider(serviceProvider);
            Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.INFO, "a new {0} was created and added to {1}", new Object[] {serviceProvider, FederatedCoordinator.getInstance()});

            AskCreator creator=new AskCreator(serviceProvider, 2, isRandom);
            creator.start();
        }
        counter--;
        if(counter<=0)
        {
            this.timer.cancel();
            stop();
        }
    }
    
    class AskCreator extends TimerTask
    {
        ServiceProvider serviceProvider;
        Timer timer;
        int counter=0;
        
        boolean isRandom;
        
        public AskCreator(ServiceProvider sp, int c, boolean isRandom)
        {
            this.serviceProvider=sp;
            this.timer=new Timer("AuctionAsk Creator", true);
            this.counter=c;
            this.isRandom=isRandom;
        }
        
        public void start()
        {
            long delayNewAsk=Utilities.generateRandomInteger(1, 10)*10;
            this.timer.schedule(this, delayNewAsk, delayNewAsk);            
        }
        
        public void run()
        {
            if(counter>0)
            {
                AuctionAsk ask;
                if(isRandom)
                {
                    ask=new RandomAsk(serviceProvider); //serviceProvider.createBid(null);
                }
                else
                {
                    ask=new ModelledAsk(serviceProvider);
                }
                ask.configIdentityResources();
                
                FederatedCoordinator.getInstance().publishAuctionAsk(ask);
                Logger.getLogger(ServiceProviderManager.class.getName()).log(Level.INFO, "a new {0} was published by {1} to {2}", new Object[] {ask, serviceProvider, FederatedCoordinator.getInstance()});
            }
            counter--;
            if(counter<=0)
            {
                timer.cancel();
            }
        }
    }
}
