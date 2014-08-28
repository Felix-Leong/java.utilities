/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.office;

import java.io.File;

/**
 *
 * @author Dominik
 */
public interface Office2PDFInterface{
    
    File convert2PDF(File wordFile) throws Exception;
}
