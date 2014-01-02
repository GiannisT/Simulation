package org.jfree.chart.demo;

import uk.ac.bham.simulator.*;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


public class Graph extends ApplicationFrame {

    ArrayList<Double> SubmittedTime = new ArrayList<Double>();
    ArrayList<Double> FinishTime = new ArrayList<Double>();

    public Graph(){}
    
     /**
     * This function should be called to continously check if the Simulator program has finished hence to produce the
     * Chart using the complete dataset generated from the simulation
     */
    public void GenerateGraph(){
    
        Thread t = new Thread() {
            public void run() {

                if (!FederatedCoordinator.getInstance().isRunning()) {
                    final Graph demo = new Graph("Efficiency Graph");
                    demo.pack();
                    RefineryUtilities.centerFrameOnScreen(demo);
                    demo.setVisible(true);
                }
            }
            };
      t.start();
        }
    
    
    
    public Graph(final String title) {
        super(title);
        IntervalXYDataset dataset = createDataset();
        JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    public void setChartValues(Double submissionTime, Double FinishTime) {
        this.SubmittedTime.add(submissionTime);
        this.FinishTime.add(FinishTime);
    }

    
    private IntervalXYDataset createDataset() {
        final XYSeries series = new XYSeries("Bids");

        for (int i=0; i<SubmittedTime.size(); i++){
           series.add((Double) SubmittedTime.get(i)/60000, (Double) FinishTime.get(i)/60000); //add the values to the chart by converting the time in MS to minutes
        }
        
        final XYSeriesCollection dataset = new XYSeriesCollection(series);
        return dataset;
    }

    
    private JFreeChart createChart(IntervalXYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYBarChart(
                "Duration For Matching Bids with Asks",
                "X-axis: SubmissionTime (Min)",
                false,
                "Y-axis: FinishTime (Min)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        return chart;
    }
    
    
}
