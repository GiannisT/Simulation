package uk.ac.bham.simulator;

import java.security.SecureRandom;



public class RandomAsk extends AuctionAsk {
     String[] ResourcesOffered;
    /**
     * Generates the resources that a SP can provide to the market based on randomness with probability of offering each resource 80%
     */
    public void CreateRandomAsk() {
        
        String [] TypeOfResources = new String[]{"Anonymity", "Confidentiality", "Integrity", "Availability", "DeliveryImportance", "Performance", "NetworkLatency", "High_Computation_Resources", "Robustness", "Scalability"};
        String available = "";
        ResourcesOffered = new String[10]; //the number should change according to the number of the type of resources offered
        SecureRandom rand = new SecureRandom();
        int temp = 0;

        for (int i = 0; i < TypeOfResources.length; i++) {

            temp = rand.nextInt(100);

            if (temp < 80) { //giving a 80% chance to the SP to provide the certain resource/utility characteristic 
                available = "Offered";
            } else {
                available = "NOT Offered";
            }

            ResourcesOffered[i] = available; //assigns the value that indicates if a resource is availble for the market.   
            }
         create(ResourcesOffered);
    }
    
}
