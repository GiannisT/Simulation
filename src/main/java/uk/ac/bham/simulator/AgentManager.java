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
public class AgentManager implements Runnable {
    
    private static final AgentManager instance=new AgentManager();
    
    private boolean running;
    
    public static final String RUNNING_LOCK="RUNNING LOCK";
    
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
                Thread thread=new Thread(this, "Agent Manager");
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
     * Create new agents according to a random delay, each new agent create two bids
     * 
     */
    public synchronized void run()
    {
        // TODO for this fase, a single IdentityProvider will be used
        int counter=0;
        IdentityProvider ip=new IdentityProvider();
        FederatedCoordinator.getInstance().registerIdentityProvider(ip);
        
        while (isRunning())
        {
            counter++;
            // TODO at the moment, ten agents with two bids eachone will be created
            if(counter>10) break;
            
            long delay_new_agent=Utilities.generateRandomInteger(1, 10)*10+250;
            long delay_first_bid=Utilities.generateRandomInteger(1, 10)*10+250;
            long delay_next_bid=Utilities.generateRandomInteger(1, 10)*10+250;
            try {
                wait(150);
            } catch (InterruptedException ex) {
                Logger.getLogger(AgentManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // wait delay_new_agent miliseconds before create a new Agent
            try {
                wait(delay_new_agent);
            } catch (InterruptedException ex) {
                Logger.getLogger(AgentManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Agent agent=new Agent(ip);
            FederatedCoordinator.getInstance().addSession(agent);
            Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new agent {0} was created and added to federated coordinator {1}", new Object[] {agent, FederatedCoordinator.getInstance()});
            
            try {
                wait(delay_first_bid);
            } catch (InterruptedException ex) {
                Logger.getLogger(AgentManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            Bid first_bid=agent.createBid();
            FederatedCoordinator.getInstance().publishBid(first_bid);
            Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new bid {0} was published by the agent {1} to federated coordinator {2}", new Object[] {first_bid, agent, FederatedCoordinator.getInstance()});

            try {
                wait(delay_next_bid);
            } catch (InterruptedException ex) {
                Logger.getLogger(AgentManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            Bid next_bid=agent.createBid();
            FederatedCoordinator.getInstance().publishBid(next_bid);
            Logger.getLogger(AgentManager.class.getName()).log(Level.INFO, "a new bid {0} was published by the agent {1} to federated coordinator {2}", new Object[] {next_bid, agent, FederatedCoordinator.getInstance()});
        }
        stop();
    }
}
