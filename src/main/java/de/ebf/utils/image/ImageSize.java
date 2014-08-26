/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ebf.utils.image;

/**
 *
 * @author Dominik
 */
public class ImageSize {

   private Double width;
   private Double height;

   public ImageSize(Double width, Double height) {
      this.width = width;
      this.height = height;
   }

   public Double getWidth() {
      return width;
   }

   public void setWidth(Double width) {
      this.width = width;
   }

   public Double getHeight() {
      return height;
   }

   public void setHeight(Double height) {
      this.height = height;
   }
}
