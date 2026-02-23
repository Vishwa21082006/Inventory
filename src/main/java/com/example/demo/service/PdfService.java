package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public ByteArrayInputStream generateInvoice(Order order) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.DARK_GRAY);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // 2. Info Section
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            document.add(new Paragraph("Order ID: #" + order.getId(), infoFont));
            
            String customerName = "Guest";
            if(order.getCustomer() != null) {
                customerName = (order.getCustomer().getFullName() != null) ? order.getCustomer().getFullName() : order.getCustomer().getUsername();
            }
            document.add(new Paragraph("Customer: " + customerName, infoFont));
            document.add(new Paragraph("Date: " + order.getOrderDate().toLocalDate(), infoFont));
            document.add(new Paragraph("Status: " + order.getTrackingStatus(), infoFont));
            document.add(Chunk.NEWLINE);

            // 3. Table
            PdfPTable table = new PdfPTable(4); 
            table.setWidthPercentage(100);
            table.setWidths(new int[]{4, 1, 2, 2}); 

            addTableHeader(table, "Product");
            addTableHeader(table, "Qty");
            addTableHeader(table, "Price");
            addTableHeader(table, "Total");

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getProduct().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("$" + item.getPriceAtPurchase());
                double lineTotal = item.getPriceAtPurchase().doubleValue() * item.getQuantity();
                table.addCell("$" + String.format("%.2f", lineTotal));
            }
            document.add(table);

            // 4. Totals
            document.add(Chunk.NEWLINE);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Paragraph totalPara = new Paragraph("Grand Total: $" + String.format("%.2f", order.getTotalAmount()), totalFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);
            
            // 5. Footer (Updated with Email)
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            
            Paragraph footer1 = new Paragraph("Thank you for your business!", footerFont);
            footer1.setAlignment(Element.ALIGN_CENTER);
            document.add(footer1);

            // 🟢 NEW: Contact Email Line
            Paragraph footer2 = new Paragraph("For any queries contact: vishwavivekanathan2108@gmail.com", footerFont);
            footer2.setAlignment(Element.ALIGN_CENTER);
            document.add(footer2);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setPadding(5);
        header.setPhrase(new Phrase(headerTitle));
        table.addCell(header);
    }
}