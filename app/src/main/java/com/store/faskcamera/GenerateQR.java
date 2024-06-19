package com.store.faskcamera;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GenerateQR extends AppCompatActivity {
    private ImageView qrCodeImageView;
    private Button generateButton, print;
    private EditText linkEditText;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_generate_qr);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        generateButton = findViewById(R.id.generateButton);
        linkEditText = findViewById(R.id.linkEditText);
        print = findViewById(R.id.Print);
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateString = now.format(formatter);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = linkEditText.getText().toString();

                //   if (!link.startsWith("http://") && !link.startsWith("https://")) {
                //       link = "http://" + link;
                //   }

                //String fullLink =  "http://"+ dateString;


                if (link.isEmpty()) {
                    Toast.makeText(GenerateQR.this, "Thiếu dữ liệu", Toast.LENGTH_SHORT).show();
                } else {
                  //  String fullLink = link;  // Use link directly if not empty
                  //  String encryptedLink = encryptLink(fullLink);
                    String encryptedLink = link;

                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    try {
                        com.google.zxing.common.BitMatrix bitMatrix = barcodeEncoder.encode(encryptedLink, BarcodeFormat.QR_CODE, 400, 400);

                        qrCodeBitmap = barcodeEncoder.createBitmap(bitMatrix);
                        qrCodeImageView.setImageBitmap(qrCodeBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qrCodeBitmap != null) {
                    if (ContextCompat.checkSelfPermission(GenerateQR.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(GenerateQR.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                    } else {
                        generateQRCodePDF();
                    }
                }
            }
        });
    }

    private String encryptLink(String link) {
        char[] inputChars = link.toCharArray();
        char[] keyChars = "AAAWEWEWWEERTYUI".toCharArray();
        char[] outputChars = new char[inputChars.length];

        for (int i = 0; i < inputChars.length; i++) {
            outputChars[i] = (char) (inputChars[i] ^ keyChars[i % keyChars.length]);
        }

        return new String(outputChars);
    }

    private void generateQRCodePDF() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GenerateQR.this);
        builder.setTitle("Enter PDF Document Name");

        final EditText input = new EditText(GenerateQR.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String documentName = input.getText().toString().trim();
                if (!documentName.isEmpty()) {
                    createAndPrintPDF(documentName);
                } else {
                    Toast.makeText(GenerateQR.this, "Document name cannot be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createAndPrintPDF(String documentName) {
        PdfDocument pdfDocument = new PdfDocument();

        try {
            // Define the page size (A4 size)
            int pageWidth = 595;  // 595 points = 8.27 inches
            int pageHeight = 842; // 842 points = 11.69 inches

            int qrCodeWidth = qrCodeBitmap.getWidth();
            int qrCodeHeight = qrCodeBitmap.getHeight();
            int left = (pageWidth - qrCodeWidth) / 2;
            int top = (pageHeight - qrCodeHeight) / 2;

            // Create a PdfDocument.PageInfo with A4 size
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

            // Start a new page
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            // Get the Canvas for drawing
            Canvas canvas = page.getCanvas();

            // Draw the QR code in the center of the page
            canvas.drawBitmap(qrCodeBitmap, left, top, null);

            // Finish the page
            pdfDocument.finishPage(page);

            // Define the path where you want to save the PDF file
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + documentName + ".pdf";

            // Write the PDF to a file
            pdfDocument.writeTo(new FileOutputStream(filePath));

            // Close the document
            pdfDocument.close();

            // Notify the user that the PDF has been generated and offer to print it
            Toast.makeText(GenerateQR.this, "PDF Saved successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
