package org.greenstone.backend.admin.quote;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.greenstone.backend.persistence.entity.Quote;
import org.greenstone.backend.persistence.entity.QuoteItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class QuotePdfService {

    private static final float PAGE_MARGIN = 48;
    private static final float CONTENT_WIDTH = PDRectangle.A4.getWidth() - PAGE_MARGIN * 2;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    public byte[] generate(Quote quote, List<QuoteItem> items) {
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            var regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            var bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            var writer = new PdfWriter(document, regular, bold);

            writer.heading("QUOTE", quote.getQuoteNumber() + " · Revision " + quote.getRevisionNumber());
            writer.twoColumn("Prepared for", quote.getCustomerName(), "Valid until", DATE.format(quote.getValidUntil()));
            writer.twoColumn("Property", value(quote.getPropertyAddress()), "Prepared", DATE.format(quote.getCreatedAt().toLocalDate()));
            writer.space(12);
            writer.title(quote.getTitle());
            writer.label("Scope of work");
            writer.paragraph(quote.getScope());
            writer.space(8);
            writer.label("Pricing");

            var included = items.stream().filter(item -> !item.isOptional()).toList();
            var optional = items.stream().filter(QuoteItem::isOptional).toList();
            writer.items(included);
            writer.total("Subtotal", quote.getSubtotal(), false);
            writer.total("GST (15%)", quote.getGstAmount(), false);
            writer.total("Total including GST", quote.getTotal(), true);

            if (!optional.isEmpty()) {
                writer.space(12);
                writer.label("Optional additions — not included in the quoted total");
                writer.items(optional);
                writer.total("Optional additions", quote.getOptionalTotal(), false);
            }

            writer.space(14);
            writer.label("Indicative timing");
            writer.paragraph(timing(quote));
            writer.space(8);
            writer.label("Terms");
            writer.paragraph(quote.getTerms());
            writer.space(16);
            writer.callout("To accept or decline this quote, use the secure link in your Greenstone Painting email.");
            writer.footer();
            writer.close();

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("The quote PDF could not be generated.", exception);
        }
    }

    private String timing(Quote quote) {
        if (quote.getEstimatedStartDate() == null && quote.getEstimatedEndDate() == null) {
            return "Project timing will be confirmed with the customer before work begins.";
        }
        if (quote.getEstimatedEndDate() == null) return "Estimated start: " + DATE.format(quote.getEstimatedStartDate());
        if (quote.getEstimatedStartDate() == null) return "Estimated completion: " + DATE.format(quote.getEstimatedEndDate());
        return "Estimated " + DATE.format(quote.getEstimatedStartDate()) + " to " + DATE.format(quote.getEstimatedEndDate());
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "Not specified" : value;
    }

    private static final class PdfWriter {
        private final PDDocument document;
        private final PDFont regular;
        private final PDFont bold;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private PdfWriter(PDDocument document, PDFont regular, PDFont bold) throws IOException {
            this.document = document;
            this.regular = regular;
            this.bold = bold;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) stream.close();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - PAGE_MARGIN;
        }

        private void heading(String heading, String reference) throws IOException {
            color(8, 38, 55);
            stream.addRect(0, page.getMediaBox().getHeight() - 122, page.getMediaBox().getWidth(), 122);
            stream.fill();
            text("GREENSTONE PAINTING", bold, 12, PAGE_MARGIN, page.getMediaBox().getHeight() - 45, 255, 255, 255);
            text(heading, bold, 28, PAGE_MARGIN, page.getMediaBox().getHeight() - 82, 255, 255, 255);
            text(reference, regular, 10, PAGE_MARGIN, page.getMediaBox().getHeight() - 103, 217, 228, 231);
            y = page.getMediaBox().getHeight() - 155;
        }

        private void twoColumn(String leftLabel, String leftValue, String rightLabel, String rightValue) throws IOException {
            ensure(42);
            labelAt(leftLabel, PAGE_MARGIN, y);
            labelAt(rightLabel, PAGE_MARGIN + CONTENT_WIDTH * 0.62f, y);
            text(leftValue, bold, 10, PAGE_MARGIN, y - 16, 16, 27, 42);
            text(rightValue, bold, 10, PAGE_MARGIN + CONTENT_WIDTH * 0.62f, y - 16, 16, 27, 42);
            y -= 40;
        }

        private void title(String value) throws IOException {
            ensure(45);
            for (var line : wrap(value, bold, 20, CONTENT_WIDTH)) {
                text(line, bold, 20, PAGE_MARGIN, y, 16, 27, 42);
                y -= 24;
            }
            y -= 6;
        }

        private void label(String value) throws IOException {
            ensure(22);
            text(value.toUpperCase(Locale.ROOT), bold, 9, PAGE_MARGIN, y, 176, 70, 42);
            y -= 18;
        }

        private void paragraph(String value) throws IOException {
            for (var paragraph : value.split("\\R", -1)) {
                var lines = paragraph.isBlank() ? List.of("") : wrap(paragraph, regular, 10, CONTENT_WIDTH);
                for (var line : lines) {
                    ensure(16);
                    text(line, regular, 10, PAGE_MARGIN, y, 54, 67, 76);
                    y -= 15;
                }
                y -= 3;
            }
        }

        private void items(List<QuoteItem> items) throws IOException {
            for (var item : items) {
                var descriptionLines = wrap(item.getDescription(), regular, 9, CONTENT_WIDTH - 190);
                var rowHeight = Math.max(28, descriptionLines.size() * 13 + 10);
                ensure(rowHeight);
                color(240, 235, 227);
                stream.addRect(PAGE_MARGIN, y - rowHeight + 7, CONTENT_WIDTH, rowHeight);
                stream.fill();
                var textY = y - 5;
                for (var line : descriptionLines) {
                    text(line, regular, 9, PAGE_MARGIN + 8, textY, 16, 27, 42);
                    textY -= 13;
                }
                var quantity = item.getQuantity().stripTrailingZeros().toPlainString() + " " + item.getUnit();
                text(quantity, regular, 9, PAGE_MARGIN + CONTENT_WIDTH - 180, y - 5, 84, 96, 104);
                text(money(item.getUnitPrice()), regular, 9, PAGE_MARGIN + CONTENT_WIDTH - 105, y - 5, 84, 96, 104);
                rightText(money(item.getLineTotal()), bold, 9, PAGE_MARGIN + CONTENT_WIDTH - 8, y - 5);
                y -= rowHeight + 3;
            }
        }

        private void total(String label, BigDecimal amount, boolean emphasis) throws IOException {
            ensure(emphasis ? 36 : 25);
            if (emphasis) {
                color(225, 166, 77);
                stream.addRect(PAGE_MARGIN + CONTENT_WIDTH - 245, y - 26, 245, 34);
                stream.fill();
            }
            text(label, emphasis ? bold : regular, emphasis ? 11 : 9,
                    PAGE_MARGIN + CONTENT_WIDTH - 235, y - 12, 16, 27, 42);
            rightText(money(amount), bold, emphasis ? 12 : 10, PAGE_MARGIN + CONTENT_WIDTH - 10, y - 12);
            y -= emphasis ? 39 : 25;
        }

        private void callout(String value) throws IOException {
            var lines = wrap(value, bold, 10, CONTENT_WIDTH - 24);
            var height = lines.size() * 15 + 24;
            ensure(height);
            color(210, 221, 224);
            stream.addRect(PAGE_MARGIN, y - height + 8, CONTENT_WIDTH, height);
            stream.fill();
            var lineY = y - 9;
            for (var line : lines) {
                text(line, bold, 10, PAGE_MARGIN + 12, lineY, 16, 27, 42);
                lineY -= 15;
            }
            y -= height + 4;
        }

        private void footer() throws IOException {
            text("Greenstone Painting Limited · 29 Lachlan Drive, Dinsdale, Hamilton · 021 083 83831",
                    regular, 8, PAGE_MARGIN, 25, 92, 104, 111);
        }

        private void ensure(float height) throws IOException {
            if (y - height < 48) {
                footer();
                newPage();
                text("GREENSTONE PAINTING · QUOTE CONTINUED", bold, 10, PAGE_MARGIN, y, 8, 38, 55);
                y -= 30;
            }
        }

        private void labelAt(String value, float x, float textY) throws IOException {
            text(value.toUpperCase(Locale.ROOT), bold, 8, x, textY, 92, 104, 111);
        }

        private void rightText(String value, PDFont font, float size, float rightX, float textY) throws IOException {
            var safe = safe(value);
            var width = font.getStringWidth(safe) / 1000 * size;
            text(safe, font, size, rightX - width, textY, 16, 27, 42);
        }

        private void text(String value, PDFont font, float size, float x, float textY, int r, int g, int b) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            color(r, g, b);
            stream.newLineAtOffset(x, textY);
            stream.showText(safe(value));
            stream.endText();
        }

        private void color(int red, int green, int blue) throws IOException {
            stream.setNonStrokingColor(red / 255f, green / 255f, blue / 255f);
        }

        private List<String> wrap(String value, PDFont font, float size, float maxWidth) throws IOException {
            var result = new ArrayList<String>();
            var current = new StringBuilder();
            for (var word : safe(value).split("\\s+")) {
                var candidate = current.isEmpty() ? word : current + " " + word;
                if (!current.isEmpty() && font.getStringWidth(candidate) / 1000 * size > maxWidth) {
                    result.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            if (!current.isEmpty()) result.add(current.toString());
            return result.isEmpty() ? List.of("") : result;
        }

        private void space(float amount) throws IOException {
            ensure(amount);
            y -= amount;
        }

        private void close() throws IOException {
            stream.close();
            stream = null;
        }

        private String money(BigDecimal amount) {
            return "$" + String.format(Locale.ENGLISH, "%,.2f", amount);
        }

        private String safe(String value) {
            if (value == null) return "";
            var normalized = Normalizer.normalize(value, Normalizer.Form.NFKD)
                    .replaceAll("\\p{M}+", "")
                    .replace('\u2013', '-').replace('\u2014', '-').replace('\u2019', '\'')
                    .replace('\u2022', '-');
            return normalized.replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
}
