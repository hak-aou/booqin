package fr.uge.booqin.app.controller.rest;

import com.github.javafaker.Faker;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.UnitValue;
import fr.uge.booqin.app.dto.cart.BookLendTransaction;
import fr.uge.booqin.app.dto.cart.Order;
import fr.uge.booqin.app.service.TransactionService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/txs", "/android/txs"})
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/borrows")
    public List<Order> getBorrowTransactions(@AuthenticationPrincipal SecurityUser currentUser) {
        return transactionService.myBorrowsTransactions(currentUser.authenticatedUser());
    }

    @GetMapping("/loans")
    public List<BookLendTransaction> getLendTransactions(@AuthenticationPrincipal SecurityUser currentUser) {
        return transactionService.myLendTransactions(currentUser.authenticatedUser());
    }

    @PostMapping("{txId}/nextStep")
    public void nextStep(@PathVariable UUID txId,
                         @AuthenticationPrincipal SecurityUser currentUser) {
        transactionService.nextStep(currentUser.authenticatedUser(),txId);
    }

    @GetMapping("/{txId}/shipping-label")
    public ResponseEntity<byte[]> txShippingLabel(@PathVariable UUID txId) {
        // fake recipient details
        Faker faker = new Faker();
        var recipientName = faker.name().fullName();
        var recipientAddress = faker.address().fullAddress();
        var date = LocalDate.now().toString();
        var trackingCode = faker.code().isbn10();

        try(var baos = new ByteArrayOutputStream()) {
            // PDF in memory
            var writer = new PdfWriter(baos);
            var pdf = new PdfDocument(writer);
            var document = new Document(pdf);

            document.add(new Paragraph("BooqIn").setBold().setFontSize(20));
            var div = new com.itextpdf.layout.element.Div();
            var border = new com.itextpdf.layout.borders.DashedBorder(ColorConstants.BLACK, 1);
            div.setBorder(border);
            div.setPaddingLeft(8);
            div.setMarginTop(8);

            div.add(new Paragraph(recipientName));
            div.add(new Paragraph(recipientAddress));
            div.add(new Paragraph(date));

            var shipCodeParagraph = new Paragraph();
            shipCodeParagraph.add(new com.itextpdf.layout.element.Text("Ship. code ").setBold());
            shipCodeParagraph.add(new com.itextpdf.layout.element.Text(trackingCode));
            div.add(shipCodeParagraph);

            var txIdParagraph = new Paragraph();
            txIdParagraph.add(new com.itextpdf.layout.element.Text("Tx ").setBold());
            txIdParagraph.add(new com.itextpdf.layout.element.Text(txId.toString()));
            div.add(txIdParagraph);

            // QR code
            var qrCode = new BarcodeQRCode(txId.toString());
            var qrCodeImage = new Image(qrCode.createFormXObject(pdf));
            qrCodeImage.setWidth(UnitValue.createPercentValue(30));
            qrCodeImage.setHeight(UnitValue.createPercentValue(30));
            div.add(qrCodeImage);

            // Add the Div to the document
            document.add(div);

            document.close();
            pdf.close();
            writer.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "shipping-label-" + txId + ".pdf");
            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
