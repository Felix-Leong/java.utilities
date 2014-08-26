/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ebf.utils.image;

/**
 *
 * @author dominik
 */
public interface ProgressCallback {
    
    public void onProgress(Long total, Long progress);
    
}
