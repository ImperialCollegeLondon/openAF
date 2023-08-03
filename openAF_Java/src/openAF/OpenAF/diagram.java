package openAF.OpenAF;

/*
 *Copyright 2023 Imperial College London
 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:
 *
 *1. Redistributions of source code must retain the above copyright notice, this
 *list of conditions and the following disclaimer.
 *2. Redistributions in binary form must reproduce the above copyright notice, this 
 *list of conditions and the following disclaimer in the documentation and/or
 *other materials provided with the distribution.
 *
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 *CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 *MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 *CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 *NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 *ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 * @author Jonathan Lightley
 */

import com.google.common.primitives.Doubles;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

public class diagram extends JFrame {
    MainAF parent_ = null;
    public diagram(final double[][] a, MainAF parent_in) {
        super("autofocus diagram");
        parent_ = parent_in;
        final XYSeries series = new XYSeries("FWHM");
        final XYSeries series2 = new XYSeries("FWHM2");
        final XYSeries series3 = new XYSeries("Average Intensity");
        for (int i = 0; i <= a.length-2; i++){
            series.add(a[i][0], a[i][1]);
            series2.add(a[i][0], a[i][2]);
            series3.add(a[i][0], a[i][3]);
        }
        XYSeriesCollection data = new XYSeriesCollection();
        data.addSeries(series);
        data.addSeries(series2);
        
        XYSeriesCollection data2 = new XYSeriesCollection();
        data2.addSeries(series3);
        
        XYPlot plot = new XYPlot();
        plot.setDataset(0, data);
        plot.setDataset(1, data2);        
        
        plot.setRenderer(0, new XYLineAndShapeRenderer()); ;//use default fill paint for first series
        XYLineAndShapeRenderer linerenderer = new XYLineAndShapeRenderer();
        linerenderer.setSeriesFillPaint(0, Color.BLUE);
        
        plot.setRenderer(1, linerenderer);
        plot.setRangeAxis(0, new NumberAxis("FWHM of Power Spectrum"));
        plot.setRangeAxis(1, new NumberAxis("Average Intensity"));
        plot.setDomainAxis(new NumberAxis("Objective Position [um]"));
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        System.out.println(Collections.min(parent_.af_.reList));
        System.out.println(Collections.max(parent_.af_.reList));
        domain.setRange(Collections.min(parent_.af_.reList)-1, Collections.max(parent_.af_.reList)+1);
        
        //Map the data to the appropriate axis
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);   

        //generate the chart
        JFreeChart chart = new JFreeChart("autofocus calibration data", getFont(), plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        JPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    public static void main(final String[] args, MainAF aefa) {
        double[][] b = null;
        final diagram demo = new diagram(b, aefa);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

}