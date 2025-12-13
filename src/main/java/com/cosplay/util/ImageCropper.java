package com.cosplay.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Image cropper utility for cropping images to specific aspect ratios
 */
public class ImageCropper {
    
    private static final double BANNER_ASPECT_RATIO = 820.0 / 190.0; // 4.32:1
    
    private Image originalImage;
    private Canvas canvas;
    private double cropX = 0;
    private double cropY = 0;
    private double cropWidth = 0;
    private double cropHeight = 0;
    private double scale = 1.0;
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private String resultPath;
    
    /**
     * Show the image cropping dialog
     * 
     * @param sourceImagePath Path to the source image file
     * @return Path to the cropped image, or null if cancelled
     */
    public static String showCropDialog(String sourceImagePath) {
        ImageCropper cropper = new ImageCropper();
        return cropper.showDialog(sourceImagePath);
    }
    
    private String showDialog(String sourceImagePath) {
        try {
            File sourceFile = new File(sourceImagePath);
            if (!sourceFile.exists()) {
                return null;
            }
            
            originalImage = new Image(sourceFile.toURI().toString());
            if (originalImage.isError()) {
                return null;
            }
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Crop Image for Event Banner");
            
            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: #2C2C2C;");
            
            // Header
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(12, 15, 12, 15));
            header.setStyle("-fx-background-color: #1E1E1E;");
            
            Label title = new Label("Crop Image for Banner");
            title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label instructions = new Label("Drag • Scroll to zoom");
            instructions.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
            
            header.getChildren().addAll(title, spacer, instructions);
            
            // Canvas container
            StackPane canvasContainer = new StackPane();
            canvasContainer.setStyle("-fx-background-color: #2C2C2C;");
            canvasContainer.setPadding(new Insets(15));
            
            // Calculate canvas size - reduced for smaller screens
            double maxWidth = 700;
            double maxHeight = 450;
            double canvasWidth, canvasHeight;
            
            double imgAspect = originalImage.getWidth() / originalImage.getHeight();
            if (imgAspect > maxWidth / maxHeight) {
                canvasWidth = Math.min(maxWidth, originalImage.getWidth());
                canvasHeight = canvasWidth / imgAspect;
            } else {
                canvasHeight = Math.min(maxHeight, originalImage.getHeight());
                canvasWidth = canvasHeight * imgAspect;
            }
            
            canvas = new Canvas(canvasWidth, canvasHeight);
            scale = canvasWidth / originalImage.getWidth();
            
            // Initialize crop rectangle (centered, maximum size)
            initializeCropArea();
            
            // Draw initial state
            drawCanvas();
            
            // Mouse event handlers for dragging
            canvas.setOnMousePressed(e -> {
                if (isInsideCropArea(e.getX(), e.getY())) {
                    isDragging = true;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    canvas.setCursor(Cursor.MOVE);
                }
            });
            
            canvas.setOnMouseDragged(e -> {
                if (isDragging) {
                    double deltaX = e.getX() - dragStartX;
                    double deltaY = e.getY() - dragStartY;
                    
                    // Move crop area
                    double newCropX = cropX + deltaX;
                    double newCropY = cropY + deltaY;
                    
                    // Constrain to canvas bounds
                    newCropX = Math.max(0, Math.min(newCropX, canvas.getWidth() - cropWidth));
                    newCropY = Math.max(0, Math.min(newCropY, canvas.getHeight() - cropHeight));
                    
                    cropX = newCropX;
                    cropY = newCropY;
                    
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    
                    drawCanvas();
                }
            });
            
            canvas.setOnMouseReleased(e -> {
                isDragging = false;
                canvas.setCursor(Cursor.DEFAULT);
            });
            
            canvas.setOnMouseMoved(e -> {
                if (isInsideCropArea(e.getX(), e.getY())) {
                    canvas.setCursor(Cursor.HAND);
                } else {
                    canvas.setCursor(Cursor.DEFAULT);
                }
            });
            
            // Scroll to zoom
            canvas.setOnScroll(e -> {
                double delta = e.getDeltaY() > 0 ? 1.1 : 0.9;
                double newWidth = cropWidth * delta;
                double newHeight = cropHeight * delta;
                
                // Don't zoom beyond image bounds
                if (newWidth <= canvas.getWidth() && newHeight <= canvas.getHeight() 
                    && newWidth >= 100 && newHeight >= 100) {
                    
                    // Zoom from center of crop area
                    double centerX = cropX + cropWidth / 2;
                    double centerY = cropY + cropHeight / 2;
                    
                    cropWidth = newWidth;
                    cropHeight = newHeight;
                    
                    cropX = centerX - cropWidth / 2;
                    cropY = centerY - cropHeight / 2;
                    
                    // Constrain to canvas bounds
                    cropX = Math.max(0, Math.min(cropX, canvas.getWidth() - cropWidth));
                    cropY = Math.max(0, Math.min(cropY, canvas.getHeight() - cropHeight));
                    
                    drawCanvas();
                }
            });
            
            canvasContainer.getChildren().add(canvas);
            
            // Buttons
            HBox buttonBox = new HBox(12);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(15));
            buttonBox.setStyle("-fx-background-color: #1E1E1E;");
            
            Button btnCrop = new Button("Crop & Save");
            btnCrop.setStyle("-fx-background-color: #F7A84C; -fx-text-fill: white; -fx-font-size: 13px; " +
                           "-fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
            btnCrop.setOnMouseEntered(e -> btnCrop.setStyle("-fx-background-color: #E89530; -fx-text-fill: white; " +
                           "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
            btnCrop.setOnMouseExited(e -> btnCrop.setStyle("-fx-background-color: #F7A84C; -fx-text-fill: white; " +
                           "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
            btnCrop.setOnAction(e -> {
                try {
                    resultPath = saveCroppedImage(sourceImagePath);
                    dialog.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    resultPath = null;
                }
            });
            
            Button btnCancel = new Button("Cancel");
            btnCancel.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 13px; " +
                             "-fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
            btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #666; -fx-text-fill: white; " +
                             "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
            btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: #555; -fx-text-fill: white; " +
                             "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
            btnCancel.setOnAction(e -> {
                resultPath = null;
                dialog.close();
            });
            
            Button btnReset = new Button("Reset");
            btnReset.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 13px; " +
                            "-fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
            btnReset.setOnMouseEntered(e -> btnReset.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
            btnReset.setOnMouseExited(e -> btnReset.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
            btnReset.setOnAction(e -> {
                initializeCropArea();
                drawCanvas();
            });
            
            buttonBox.getChildren().addAll(btnCrop, btnReset, btnCancel);
            
            root.getChildren().addAll(header, canvasContainer, buttonBox);
            
            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();
            
            return resultPath;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void initializeCropArea() {
        // Calculate crop area maintaining banner aspect ratio
        double canvasAspect = canvas.getWidth() / canvas.getHeight();
        
        if (BANNER_ASPECT_RATIO > canvasAspect) {
            // Width constrained
            cropWidth = canvas.getWidth() * 0.9;
            cropHeight = cropWidth / BANNER_ASPECT_RATIO;
        } else {
            // Height constrained
            cropHeight = canvas.getHeight() * 0.9;
            cropWidth = cropHeight * BANNER_ASPECT_RATIO;
        }
        
        // Center the crop area
        cropX = (canvas.getWidth() - cropWidth) / 2;
        cropY = (canvas.getHeight() - cropHeight) / 2;
    }
    
    private void drawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw image
        gc.drawImage(originalImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw dimmed overlay outside crop area
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        
        // Top
        gc.fillRect(0, 0, canvas.getWidth(), cropY);
        // Bottom
        gc.fillRect(0, cropY + cropHeight, canvas.getWidth(), canvas.getHeight() - cropY - cropHeight);
        // Left
        gc.fillRect(0, cropY, cropX, cropHeight);
        // Right
        gc.fillRect(cropX + cropWidth, cropY, canvas.getWidth() - cropX - cropWidth, cropHeight);
        
        // Draw crop area border
        gc.setStroke(Color.web("#F7A84C"));
        gc.setLineWidth(3);
        gc.strokeRect(cropX, cropY, cropWidth, cropHeight);
        
        // Draw corner handles
        double handleSize = 12;
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.web("#F7A84C"));
        gc.setLineWidth(2);
        
        // Top-left
        gc.fillRect(cropX - handleSize/2, cropY - handleSize/2, handleSize, handleSize);
        gc.strokeRect(cropX - handleSize/2, cropY - handleSize/2, handleSize, handleSize);
        
        // Top-right
        gc.fillRect(cropX + cropWidth - handleSize/2, cropY - handleSize/2, handleSize, handleSize);
        gc.strokeRect(cropX + cropWidth - handleSize/2, cropY - handleSize/2, handleSize, handleSize);
        
        // Bottom-left
        gc.fillRect(cropX - handleSize/2, cropY + cropHeight - handleSize/2, handleSize, handleSize);
        gc.strokeRect(cropX - handleSize/2, cropY + cropHeight - handleSize/2, handleSize, handleSize);
        
        // Bottom-right
        gc.fillRect(cropX + cropWidth - handleSize/2, cropY + cropHeight - handleSize/2, handleSize, handleSize);
        gc.strokeRect(cropX + cropWidth - handleSize/2, cropY + cropHeight - handleSize/2, handleSize, handleSize);
        
        // Draw grid lines (rule of thirds)
        gc.setStroke(Color.rgb(255, 255, 255, 0.3));
        gc.setLineWidth(1);
        
        // Vertical lines
        gc.strokeLine(cropX + cropWidth / 3, cropY, cropX + cropWidth / 3, cropY + cropHeight);
        gc.strokeLine(cropX + 2 * cropWidth / 3, cropY, cropX + 2 * cropWidth / 3, cropY + cropHeight);
        
        // Horizontal lines
        gc.strokeLine(cropX, cropY + cropHeight / 3, cropX + cropWidth, cropY + cropHeight / 3);
        gc.strokeLine(cropX, cropY + 2 * cropHeight / 3, cropX + cropWidth, cropY + 2 * cropHeight / 3);
    }
    
    private boolean isInsideCropArea(double x, double y) {
        return x >= cropX && x <= cropX + cropWidth && y >= cropY && y <= cropY + cropHeight;
    }
    
    private String saveCroppedImage(String originalPath) throws IOException {
        // Convert crop coordinates back to original image scale
        double scaleToOriginal = originalImage.getWidth() / canvas.getWidth();
        
        int srcX = (int) (cropX * scaleToOriginal);
        int srcY = (int) (cropY * scaleToOriginal);
        int srcWidth = (int) (cropWidth * scaleToOriginal);
        int srcHeight = (int) (cropHeight * scaleToOriginal);
        
        // Ensure dimensions are within image bounds
        srcX = Math.max(0, Math.min(srcX, (int) originalImage.getWidth() - 1));
        srcY = Math.max(0, Math.min(srcY, (int) originalImage.getHeight() - 1));
        srcWidth = Math.min(srcWidth, (int) originalImage.getWidth() - srcX);
        srcHeight = Math.min(srcHeight, (int) originalImage.getHeight() - srcY);
        
        // Create cropped image
        WritableImage croppedImage = new WritableImage(
            originalImage.getPixelReader(),
            srcX, srcY, srcWidth, srcHeight
        );
        
        // Convert to BufferedImage with proper color model for JPEG
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(croppedImage, null);
        
        // Generate output file name in the same directory as the original
        File originalFile = new File(originalPath);
        String fileName = originalFile.getName();
        
        // Handle filenames that might not have an extension
        String baseName;
        String extension;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = fileName.substring(0, lastDot);
            extension = fileName.substring(lastDot);
        } else {
            baseName = fileName;
            extension = ".png"; // Default to PNG if no extension
        }
        
        // Generate unique filename if cropped version already exists
        File outputFile = new File(originalFile.getParent(), baseName + "_cropped" + extension);
        int counter = 1;
        while (outputFile.exists()) {
            outputFile = new File(originalFile.getParent(), baseName + "_cropped" + counter + extension);
            counter++;
        }
        
        // Determine format
        String formatName = extension.substring(1).toLowerCase();
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
        } else if (formatName.isEmpty()) {
            formatName = "png";
        }
        
        // For JPEG, ensure we have RGB color model (JPEG doesn't support alpha channel)
        if (formatName.equals("jpeg") && bufferedImage.getColorModel().hasAlpha()) {
            BufferedImage rgbImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );
            java.awt.Graphics2D g = rgbImage.createGraphics();
            g.setColor(java.awt.Color.WHITE); // Set background to white
            g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            g.drawImage(bufferedImage, 0, 0, null);
            g.dispose();
            bufferedImage = rgbImage;
        }
        
        try {
            // Write the image file
            boolean written = ImageIO.write(bufferedImage, formatName, outputFile);
            
            if (!written) {
                throw new IOException("ImageIO.write returned false - format may not be supported: " + formatName);
            }
            
            // Verify the file was actually written
            if (!outputFile.exists()) {
                // Try alternative approach - write to ByteArrayOutputStream first, then to file
                System.err.println("File not found immediately after write, attempting alternative method...");
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                ImageIO.write(bufferedImage, formatName, baos);
                
                java.nio.file.Files.write(outputFile.toPath(), baos.toByteArray());
            }
            
            System.out.println("Cropped image saved to: " + outputFile.getAbsolutePath());
            System.out.println("File exists: " + outputFile.exists());
            System.out.println("File size: " + outputFile.length() + " bytes");
            
        } catch (IOException e) {
            System.err.println("Error writing cropped image: " + e.getMessage());
            throw e;
        }
        
        return outputFile.getAbsolutePath();
    }
}
