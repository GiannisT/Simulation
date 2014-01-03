package org.jfree.chart.demo;

import uk.ac.bham.simulator.*;
import java.util.ArrayList;
import org.jfree.chart.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.*;


public class Graph {

    static ArrayList<Double> SubmittedTime= new ArrayList<Double>();
    static ArrayList<Double> FinishTime= new ArrayList<Double>();
    static int totalMatchedBids=0, TotalFast=0, TotalSlow=0, TotalAverage=0;
  

    
    public static void setChartValues(Double submission, Double Finish) {
        SubmittedTime.add(submission);
        FinishTime.add(Finish);
                         
    }
    
        public static void GenerateGraph() {
              totalMatchedBids=SubmittedTime.size();
                
         for (int i = 0; i < SubmittedTime.size(); i++) {
           if(FinishTime.get(i)-SubmittedTime.get(i)<=0.30){ //if the bid was matched within 20seconds of the bid submission we lable it as a fast bid.
               TotalFast++;
           }else if(FinishTime.get(i)-SubmittedTime.get(i)<=1.00){
               TotalAverage++;
           }else if(FinishTime.get(i)-SubmittedTime.get(i)>=1.1){
               TotalSlow++;
           }
         }  
            DefaultPieDataset pieDataset = new DefaultPieDataset();
            if(TotalFast!=0)
            pieDataset.setValue("Bids Matched <= 30 Sec from time of submission", (TotalFast/totalMatchedBids)*100);
            if(TotalAverage!=0)
            pieDataset.setValue("Bids Matched <=1.00 Sec from time of submission", (TotalAverage/totalMatchedBids)*100);
            if(TotalSlow!=0)
            pieDataset.setValue("Bids Matched <=1.1 Sec from time of submission", (TotalSlow/totalMatchedBids)*100);
            JFreeChart chart = ChartFactory.createPieChart3D ("Time Required to Match Bids", pieDataset, true,true,true);
            PiePlot3D p=(PiePlot3D)chart.getPlot();
            p.setForegroundAlpha(0.5f);
            ChartFrame frame1=new ChartFrame("Bid Efficiency",chart);
            frame1.setVisible(true);
            frame1.setSize(300,300);
        
    }
  
}
