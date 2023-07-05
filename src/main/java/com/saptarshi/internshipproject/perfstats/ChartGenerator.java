package com.saptarshi.internshipproject.perfstats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.*;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class ChartGenerator {
    @Value("${savepath}")
    private String savePath;
    @Value("${buffer.type}")
    private String bufferType;

    public void generateLineChart(Map<Integer,Long> data,String title,String xTitle,String yTitle){
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYSeries series=new XYSeries(title);
        for(Map.Entry<Integer,Long>entry:data.entrySet()){
            series.add(entry.getKey(), entry.getValue());
        }
        dataset.addSeries(series.getKey(), series.toArray());

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xTitle,
                yTitle,
                dataset
        );
        try {
            ChartUtils.saveChartAsJPEG(new File(savePath +bufferType+"/"+ title + ".jpeg"), chart, 500, 500);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void generateLineChartWithBackground(Map<Integer,Long> data, String title, String xTitle, String yTitle,List<Long> intervals){
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYSeries series=new XYSeries(title);
        for(Map.Entry<Integer,Long>entry:data.entrySet()){
            series.add(entry.getKey(), entry.getValue());
        }
        dataset.addSeries(series.getKey(), series.toArray());

        JFreeChart chart = ChartFactory.createXYLineChart(
                bufferType.toUpperCase()+title,
                xTitle,
                yTitle,
                dataset
        );
        XYPlot plot=chart.getXYPlot();
        for(int i=1;i<intervals.size();i++)
        {
            if(i%2==1) {
                IntervalMarker marker=new IntervalMarker(intervals.get(i-1),intervals.get(i));
                marker.setPaint(new Color(0,200,255,250));
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }
            else{
                IntervalMarker marker=new IntervalMarker(intervals.get(i-1),intervals.get(i));
                marker.setPaint(new Color(0,200,255,50));
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }
        }
        try {
            ChartUtils.saveChartAsJPEG(new File(savePath + bufferType+title + ".jpeg"), chart, 1500, 500);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void generateLineChartWithSolid(Map<Integer,Long> data, String title, String xTitle, String yTitle,List<Long> intervals){
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYAreaRenderer renderer = new XYAreaRenderer();
        for(int i=0;i<intervals.size()-1;i++)
        {
            long nreq=intervals.get(i+1)-intervals.get(i);
            if(nreq<=0)
                break;
            XYSeries series=new XYSeries("Producer iteration "+(i+1));
            for(Map.Entry<Integer,Long>entry:data.entrySet()){
                if(entry.getKey()>=intervals.get(i)&&entry.getKey()<intervals.get(i+1))
                    series.add(entry.getKey(), entry.getValue());
            }
            if(i%2==0)
                renderer.setSeriesPaint(i,new Color(135, 226, 250, 255));
            else
                renderer.setSeriesPaint(i,new Color(142, 255, 142));
            dataset.addSeries(series.getKey()+", Rate="+(12*nreq*1000)+" docs/min", series.toArray());
        }
        JFreeChart chart = ChartFactory.createXYLineChart(
                bufferType.toUpperCase()+"/"+title,
                xTitle,
                yTitle,
                dataset
        );
        XYPlot plot=chart.getXYPlot();
        plot.setRenderer(renderer);

        try {
            ChartUtils.saveChartAsJPEG(new File(savePath + bufferType+"/"+title + ".jpeg"), chart, 1500, 500);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void generateLineChartWithVertical(Map<Integer,Long> data, String title, String xTitle, String yTitle,List<Long> intervals){
        XYSeriesCollection dataset = new XYSeriesCollection();

//        DefaultIntervalXYDataset dataset = new DefaultIntervalXYDataset();
//        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(){
//            @Override
//            public Shape getItemShape(int row, int column) {
//                // Use a vertical line shape
//                return new Line2D.Double(-1, 0, 1, 0);
//            }
//        };

        XYBarRenderer renderer=new XYBarRenderer();
//        {
//            @Override
//            protected double getBarWidth(double base, int series, int item) {
//                return 5.0;
//            }
//        };
//        lineRenderer.setSeriesLinesVisible(0, true); // Hide lines
//        lineRenderer.setSeriesShapesVisible(0, true); // Show data points as shapes
//        lineRenderer.setSeriesShape(0, new Line2D.Double(-1, 0, 1, 0)); // Set the shape as a vertical line
        for(int i=0;i<intervals.size()-1;i++)
        {
            long nreq=intervals.get(i+1)-intervals.get(i);
            if(nreq<=0)
                break;
            double gap=5/(1.0*nreq);
            XYSeries series=new XYSeries("Producer iteration "+(i+1));
            for(Map.Entry<Integer,Long>entry:data.entrySet()){
                if(i!=intervals.size()-2)
                {
                    if(entry.getKey()>=intervals.get(i)&&entry.getKey()<intervals.get(i+1)) {
//                    series.add(entry.getKey(), entry.getValue());
//                    double x=5*i+(entry.getKey()-intervals.get(i))*gap;
                        series.add(5*i+(entry.getKey()-intervals.get(i)+1)*gap, entry.getValue());
//                    series.add(x-1,x,x+1, entry.getValue()/2, 0, entry.getValue());
//                    System.out.println("key="+entry.getKey());
//                    dataset.addValue((double)entry.getValue(),""+(i+1),""+5*i+(entry.getKey()-intervals.get(i))*gap);
                    }
                }
            }
//            if(i%2==0)
//                renderer.setSeriesPaint(i,new Color(135, 226, 250, 255));
//            else
//                renderer.setSeriesPaint(i,new Color(142, 255, 142));
            if(i%2==0)
                renderer.setSeriesPaint(i,new Color(9, 72, 215, 255));
            else
                renderer.setSeriesPaint(i,new Color(222, 5, 22));
            dataset.addSeries(series);
        }
        IntervalXYDataset intervaldataset=new XYBarDataset(dataset,0.01);
        JFreeChart chart = ChartFactory.createXYBarChart(
                bufferType.toUpperCase()+"/"+title,
                xTitle,
                false,
                yTitle,
                intervaldataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        plot.setRenderer(renderer);
//        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis(); // Get the range axis
//        rangeAxis.setTickUnit(new NumberTickUnit(1000));


        try {
            ChartUtils.saveChartAsJPEG(new File(savePath + bufferType+"/"+title + ".jpeg"), chart, 1500, 500);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
