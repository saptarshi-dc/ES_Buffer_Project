package com.saptarshi.internshipprojectmdb.perfstats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class ChartGenerator {
    @Value("${savepath}")
    private String savePath;
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
            ChartUtils.saveChartAsJPEG(new File(savePath + title + ".jpeg"), chart, 500, 500);
        }catch (IOException e){
            e.printStackTrace();
        }
            //        ChartFrame frame = new ChartFrame("Line Chart", chart);
//        frame.pack();
//        frame.setVisible(true);
    }
}
