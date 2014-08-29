/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.image;


import de.ebf.office.Office2PDF;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.springframework.util.StringUtils;
/**
 *
 * @author dominik
 */
public class ImageUtil {
    
    private static final Logger log = Logger.getLogger(ImageUtil.class);

    private static final int        PREVIEW_IMAGE_TYPE                  = BufferedImage.TYPE_INT_ARGB;
    private static final String     PREVIEW_IMAGE_INFORMAL_FORMAT_NAME  = "png";
    
    public static byte[] getPreviewImageByteArray(File file, ImageSize targetSize) throws Exception {
        BufferedImage image = createPreview(file, targetSize);
        return getByteArrayFromBufferedImage(image);
    }
    
    private static BufferedImage createPreview(File file, ImageSize targetSize) throws Exception {
        if (file != null && file.exists() && file.isFile()) {
            String contentType = getContentType(file);
            if (StringUtils.isEmpty(contentType)) {
                throw new Exception("Cannot determine content type of file " + file);
            }
            switch (contentType){
                case  "application/pdf":
                    return createPdfPreview(file, targetSize);
                case "image/jpeg":
                case "image/pjpeg":
                case "image/png":
                case "image/x-png":
                case "image/gif":
                case "image/bmp":
                case "image/x-ms-bmp":
                    return createImagePreview(file, targetSize);
                case "application/msword":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                case "application/vnd.ms-excel":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                case "application/vnd.ms-powerpoint":
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation":                   
                    File pdfFile = Office2PDF.getInstance().convert2PDF(file);
                    BufferedImage previewImage = createPdfPreview(pdfFile, targetSize);
                    pdfFile.delete();
                    return previewImage;
                default:
                   throw new Exception("Unsupported content type " + contentType);
            }
        }
        throw new IllegalArgumentException("file parameter needs to be a file");
    }

    private static String getContentType(File file) throws IOException {
        //does not work on OSX
        //return Files.probeContentType(file.toPath());
        return new Tika().detect(file);
    }

    private static BufferedImage createImagePreview(File file, ImageSize targetSize) throws Exception {
        BufferedImage originalImage = ImageIO.read(file);
        return resizeImage(originalImage, targetSize);
    }

    private static BufferedImage createPdfPreview(File file, ImageSize targetSize) throws Exception {
        //System.getProperties().put("sun.java2d.d3d", false);
        // open the file
        Document document = new Document();
        document.setFile(file.getAbsolutePath());

        // save page captures to file.
        float scale = 1.0f;
        float rotation = 0f;

        // Paint each pages content to an image and
        // write the image to file
        if (document.getNumberOfPages() > 0) {
            BufferedImage image = (BufferedImage) document.getPageImage(0, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX, rotation, scale);
            return resizeImage(image, targetSize);
        } else {
            throw new PDFException("Couldn't get first page of PDF file: " + file.getName());
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, ImageSize origSize) throws Exception {
        ImageSize scaledSize = getScaledImageSize(originalImage, origSize);
        int width = scaledSize.getWidth().intValue();
        int height = scaledSize.getHeight().intValue();
        int t = originalImage.getType();
        if (t > 0) {
            BufferedImage resizedImage = new BufferedImage(width, height, PREVIEW_IMAGE_TYPE);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();
            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            return resizedImage;
        }
        throw new Exception("Cannot resize image due to unsupported image type " + t);
    }

    private static ImageSize getScaledImageSize(BufferedImage image, ImageSize origSize) {
        Double width = (double) image.getWidth();
        Double height = (double) image.getHeight();

        Double scaleFactor = 1.0;
        if (width > origSize.getWidth()) {
            scaleFactor = width / origSize.getWidth();
        }
        if (height > origSize.getHeight()) {
            scaleFactor = Math.max(scaleFactor, height / origSize.getHeight());
        }
        width = width / scaleFactor;
        height = height / scaleFactor;
        return new ImageSize(width, height);
    }

    private static byte[] getByteArrayFromBufferedImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, PREVIEW_IMAGE_INFORMAL_FORMAT_NAME, bos);
        return bos.toByteArray();
    }
}
