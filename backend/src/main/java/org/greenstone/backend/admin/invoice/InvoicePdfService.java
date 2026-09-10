package org.greenstone.backend.admin.invoice;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.greenstone.backend.persistence.entity.Invoice;
import org.springframework.stereotype.Service;
import java.io.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoicePdfService {
    public byte[] generate(Invoice invoice) {
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            var page = new PDPage(PDRectangle.A4); document.addPage(page);
            var regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            var bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            try (var stream = new PDPageContentStream(document, page)) {
                stream.setNonStrokingColor(8/255f,38/255f,55/255f); stream.addRect(0,720,595,122); stream.fill();
                text(stream,bold,13,48,800,"GREENSTONE PAINTING",255,255,255);
                text(stream,bold,30,48,755,"INVOICE",255,255,255);
                text(stream,regular,11,48,735,invoice.getInvoiceNumber(),217,228,231);
                var job=invoice.getJob(); var y=680f;
                text(stream,bold,10,48,y,"BILL TO",176,70,42); text(stream,bold,14,48,y-24,job.getCustomerName(),16,27,42);
                text(stream,regular,10,48,y-42,value(job.getCustomerEmail()),84,96,104); text(stream,regular,10,48,y-58,value(job.getPropertyAddress()),84,96,104);
                text(stream,bold,10,360,y,"DUE DATE",176,70,42); text(stream,bold,12,360,y-24,invoice.getDueDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy",Locale.ENGLISH)),16,27,42);
                y=560; text(stream,bold,12,48,y,job.getTitle(),16,27,42); text(stream,regular,10,48,y-20,"Accepted quote " + job.getQuote().getQuoteNumber(),84,96,104);
                y=490; total(stream,regular,bold,y,"Subtotal",invoice.getSubtotal()); total(stream,regular,bold,y-34,"GST (15%)",invoice.getGstAmount());
                stream.setNonStrokingColor(225/255f,166/255f,77/255f); stream.addRect(310,y-110,237,42); stream.fill(); total(stream,bold,bold,y-82,"Total",invoice.getTotal());
                total(stream,regular,bold,y-136,"Paid",invoice.getAmountPaid()); total(stream,bold,bold,y-174,"Balance due",invoice.getTotal().subtract(invoice.getAmountPaid()));
                text(stream,regular,9,48,40,"Greenstone Painting Limited · 29 Lachlan Drive, Dinsdale, Hamilton · 021 083 83831",92,104,111);
            }
            document.save(output); return output.toByteArray();
        } catch(IOException e){throw new IllegalStateException("The invoice PDF could not be generated.",e);}
    }
    private void total(PDPageContentStream s, PDFont l, PDFont v, float y, String label, BigDecimal value)throws IOException{text(s,l,11,330,y,label,16,27,42); text(s,v,12,455,y,money(value),16,27,42);}
    private void text(PDPageContentStream s, PDFont f,float z,float x,float y,String value,int r,int g,int b)throws IOException{s.beginText();s.setFont(f,z);s.setNonStrokingColor(r/255f,g/255f,b/255f);s.newLineAtOffset(x,y);s.showText(safe(value));s.endText();}
    private String money(BigDecimal v){return "$"+v.setScale(2).toPlainString();} private String value(String v){return v==null||v.isBlank()?"Not supplied":v;}
    private String safe(String v){var n=Normalizer.normalize(value(v),Normalizer.Form.NFKD).replaceAll("\\p{M}","");return n.replaceAll("[^\\x20-\\x7E]","?");}
}
