package com.pventabase.reportes.util;

import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import java.io.ByteArrayInputStream;

@Component
public class PrintServiceUtil {

    public void imprimirPdf(byte[] pdfBytes, String jobName) {
        try {
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrintService == null) {
                throw new RuntimeException("No se encontro ninguna impresora predeterminada");
            }

            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(new ByteArrayInputStream(pdfBytes), flavor, null);

            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Copies(1));
            attrs.add(OrientationRequested.LANDSCAPE);

            DocPrintJob job = defaultPrintService.createPrintJob();
            job.print(doc, attrs);
        } catch (PrintException e) {
            throw new RuntimeException("Error al imprimir: " + e.getMessage(), e);
        }
    }
}
