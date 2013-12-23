package uk.ac.bham.simulator;
/**QUESTIONS FOR THIS CLASS
 * What other attributes to include when registering an SP to the federation except the ServiceProviderID,PublicKey ?
 * how do we handle asks ? how frequently are they going to be submitted , how many and for how long?
 * Should the ServiceProvider Class contain a Main method or it should be only consist by setters and getters for attributes ?
 */ 


/*import java.util.Scanner;
import java.rmi.server.UID;*/
import java.security.*;
import java.util.ArrayList;
/*import java.util.logging.Level;
import java.util.logging.Logger;*/

public class ServiceProvider 
{
    
    StringBuffer publicK, privateK;
    ArrayList<AuctionAsk> auctionAsks;
    
    public ServiceProvider()
    {
        auctionAsks = new ArrayList<AuctionAsk>();        
    }
    
    public AuctionAsk createAuctionAsk()
    {
        AuctionAsk auctionAsk = new RandomAsk(this);
        auctionAsk.configIdentityResources();
        auctionAsk.setMaxDecrementPercentage(Utilities.generateRandomInteger(1, 50));
        auctionAsks.add(auctionAsk);  
        return auctionAsk;
    }
    
    public void removeAuctionAsk(AuctionAsk auctionAsk)
    {
       auctionAsks.remove(auctionAsk); 
    }
    
    public void requestAuthentication()
    {
        
    }
    /**
     * Constructs the public and private pair of cryptographic RSA keys for each Service provider
     *
     */
    public void GenerateRSAKeys() throws NoSuchAlgorithmException 
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
        publicK = new StringBuffer();

        for (int i = 0; i < publicKey.length; ++i) 
        {
            publicK.append(Integer.toHexString(0x0100 + (publicKey[i] & 0x00FF)).substring(1));
        }
//        System.out.println(publicK);
//        System.out.println();

        byte[] privateKey = keyGen.genKeyPair().getPrivate().getEncoded();
        privateK = new StringBuffer();

        for (int i = 0; i < privateKey.length; ++i) 
        {
            privateK.append(Integer.toHexString(0x0100 + (privateKey[i] & 0x00FF)).substring(1));
        }

        //   System.out.println(privateK);
        //   System.out.println();
        //   System.out.println("----------------------NEW PAIR--------------------------------");
    }


    /*public static void main(String[] args) 
    {
        
        AuctionAsk ask=new AuctionAsk();
        ServiceProvider obj = new ServiceProvider();
        
        System.out.println("Please specify the number of Service providers (Integer Required) required for the simulation");
        Scanner num = new Scanner(System.in);
        int SpNum = (int) num.nextDouble();//this ensures that even if a user gives a decimal number the system will trancate it and use it without crashing 
        String[] SpS = new String[SpNum]; //Array that will hold the constructed SP entities
        String CreatedSP, resource = "";
        boolean register=false;
        
        System.out.println("Please specify how the asks will be generated:"+"\n"  + "1: Statistical Model"+ "\n" +"2: Random Model");
        Scanner model=new Scanner (System.in);
        int AskType=model.nextInt(); //contains the type of asks the user wants to create for the simulation, random ask or statistical asks
        
                 
        for (int i = 0; i < SpS.length; i++) {

            //generates the unique identifier for each SP
            UID ServiceProviderId = new UID();

            //calls the function to generate keys for SPs
            try {
                obj.GenerateRSAKeys();
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ServiceProvider.class.getName()).log(Level.SEVERE, null, ex);
            }

            //this holds all the attributes that should be send to the federation to register
            CreatedSP = "ServiceProviderID: " + ServiceProviderId.toString() + ",PublicKey: " + obj.publicK + ",PrivateKey: " + obj.privateK;

            System.out.println(CreatedSP);
          //  register=obj.RegisterSPtoFederation(CreatedSP); //registers SP to the federation.. Create a FUNCTION IN FEDERATION TO REGISTER SP, IF SUCESFULL SEND A TRUE VALUE BACK.. REGISTER sp TO A DB 
            
            if(register==true){ // if SP successfully registered to federation add the certain SP to the SP list
               SpS[i] = CreatedSP; //adds created SP to the SP list
            }
         
           if(AskType==1){
             StatisticalAsk statistical=new StatisticalAsk();
           }else if(AskType==2){
             RandomAsk ran=new RandomAsk();
             ran.CreateRandomAsk();
           }

            
        }
        
    }*/
    

    /**
     *
     * @param resourceList
     * @param identityProvider
     * @param bid
     */
    
    public void notifyAuctionWinner(ArrayList<IdentityResource> resourceList, 
            IdentityProvider identityProvider, Bid bid)
    {
        
    }
    
    public boolean allocateResources(Bid bid)
    {
        executeJobs(bid.getIdentityResources());
        return true;
    }
    
    private boolean executeJobs(ArrayList<IdentityResource> identityResources)
    {
        for (IdentityResource identityResource : identityResources)
        {
            //executing job by job...
            System.out.println("Executing:"+identityResource.getResourceType());      
        }
        
        return true;
    }
    
}
