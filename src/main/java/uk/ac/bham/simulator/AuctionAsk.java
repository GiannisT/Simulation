package uk.ac.bham.simulator;
/**QUESTIONS FOR THIS CLASS
  * which resources/utility characteristics are we going to use in the simulation?
  * How should the qualityFactor be used in this class?
  * 
 */
//import uk.ac.bham.simulator.IdentityResource;

public class AuctionAsk {

   final int maxDecrementPercentage=20; //describes what is the maximum amount of money that a price set by SP can be increased
   Double qualityFactor;
   final int AlterSubmissionPerDay=8; //describes how many times an SP can change or resubmit already submitted asks 
   String [] TypeOfResources;
   
   public AuctionAsk()
   {
       
   }
   
   
    public void create(String [] ReqUtilityChar){ 
     IdentityResource Ir=new IdentityResource();
     //Ir.create(ReqUtilityChar); //forwards the aks to the identity Resource
    }
    
     /**
     * Create a statistical AsK
     */
    public void configIdentityResources(){
        
    }
    
    
    /* 
        The FederatedCoordinator invoke the getAdaptedPrice for each AuctionAsk
    */
    public void getAdaptedPrice()
    {
        
    }
    
}
