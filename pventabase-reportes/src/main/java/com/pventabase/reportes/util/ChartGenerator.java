package com.pventabase.reportes.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class ChartGenerator {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;

    public byte[] generarBarra(String titulo, String ejeX, String ejeY,
                                List<String> categorias, List<Double> valores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < categorias.size() && i < valores.size(); i++) {
            dataset.addValue(valores.get(i), "Serie", categorias.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titulo, ejeX, ejeY, dataset, PlotOrientation.VERTICAL, false, true, false);

        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(41, 128, 185));
        renderer.setDrawBarOutline(false);

        return chartToImage(chart);
    }

    public byte[] generarPie(String titulo, List<String> categorias, List<Double> valores) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < categorias.size() && i < valores.size(); i++) {
            dataset.setValue(categorias.get(i), valores.get(i));
        }

        JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));

        return chartToImage(chart);
    }

    public byte[] generarLinea(String titulo, String ejeX, String ejeY,
                                List<String> categorias, List<Double> valores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < categorias.size() && i < valores.size(); i++) {
            dataset.addValue(valores.get(i), "Serie", categorias.get(i));
        }

        JFreeChart chart = ChartFactory.createLineChart(
                titulo, ejeX, ejeY, dataset, PlotOrientation.VERTICAL, false, true, false);

        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(39, 174, 96));

        return chartToImage(chart);
    }

    private byte[] chartToImage(JFreeChart chart) {
        try {
            BufferedImage image = chart.createBufferedImage(WIDTH, HEIGHT);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el grafico", e);
        }
    }
}
