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
public class AgentManager extends TimerTask {
    
    private static final AgentManager instance=new AgentManager();
    
    private boolean running;
    
    public static final String RUNNING_LOCK="RUNNING LOCK";
    
    private int counter;
    private Timer timer;
    
    private AgentManager()
    {
        synchronized (RUNNING_LOCK)
        {
            this.running=false;
        }
    }
    
    public static AgentManager getInstance()
    {
        return AgentManager.instance;
    }
    
    
    
    public void start()
    {
        synchronized (RUNNING_LOCK)
        {
            if(!this.running)
            {
                // TODO at the moment, ten agents with two bids eachone will be created
                this.counter=10;

                long delayNewAgent=Utilities.generateRandomInteger(1, 10)*10+250;                

                this.timer=new Timer("Agent Manager", true);
                this.running=true;
                timer.schedule(this, delayNewAgent, delayNewAgent);
                //Thread thread=new Thread(this, "Agent Manager");
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
     * Create new agents according to a random delay, each new agent create two bids
     * 
     */
    public synchronized void run()
    {
        // TODO for this fase, a single IdentityProvider will be used
        IdentityProvider ip=new IdentityProvider();
        FederatedCoordinator.getInstance().registerIdentityProvider(ip);
        
        if (isRunning() && counter>0)
        {   
            Agent agent=new Agent(ip);
            //FederatedCoordinator.getInstance().addSession(agent);
            Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new {0} was created and added to {1}", new Object[] {agent, FederatedCoordinator.getInstance()});
        }
        counter--;
        if (counter<=0)
        {
            this.timer.cancel();
            stop();
        }
    }
    
    class RandomBidCreator extends TimerTask
    {
        Agent agent;
        Timer timer;
        int counter=0;

        public RandomBidCreator(Agent a, int c)
        {
            this.agent=a;
            this.timer=new Timer("Bid Creator", true);
            this.counter=c;
        }

        public void start()
        {
            long delayNewBid=Utilities.generateRandomInteger(1, 10)*10+250;
            this.timer.schedule(this, delayNewBid, delayNewBid); 
        }
        
        public void run()
        {
            if(counter>0)
            {
                Bid bid=agent.createBid();
                FederatedCoordinator.getInstance().publishBid(bid);
                Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new {0} was published by {1} to {2}", new Object[] {bid, agent, FederatedCoordinator.getInstance()});
            }
            counter--;
            if(counter<=0)
            {
                timer.cancel();
            }
        }
    }
}
