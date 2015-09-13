/*
 * $RCSfile: CBlkInfo.java,v $
 * $Revision: 1.1 $
 * $Date: 2005-02-11 05:02:01 $
 * $State: Exp $
 *
 * Class:                   CBlkInfo
 *
 * Description:             Object containing code-block informations.
 *
 *
 *
 * COPYRIGHT:
 *
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 *
 * Copyright (c) 1999/2000 JJ2000 Partners.
 *
 *
 *
 */

package jj2000.j2k.codestream.reader;

import java.util.*;

/**
 * This class contains location of code-blocks' piece of codewords
 * (there is one piece per layer) and some other information.
 *
 * */
public class CBlkInfo{

    /** Upper-left x-coordinate of the code-block (relative to the
        tile) */
    public int ulx;

    /** Upper-left y-coordinate of the code-block (relative to the
        tile) */
    public int uly;

    /** Width of the code-block */
    public int w;

    /** Height of the code-block */
    public int h;

    /** The number of most significant bits which are skipped for this
     * code-block (= Mb-1-bitDepth). See VM text */
    public int msbSkipped;

    /** Length of each piece of code-block's codewords */
    public int[] len;

    /** Offset of each piece of code-block's codewords in the file */
    public int[] off;

    /** The number of truncation point for each layer */
    public int[] ntp;

    /** The cumulative number of truncation points */
    public int ctp;

    /** The length of each segment (used with regular termination or
     * in selective arithmetic bypass coding mode) */
    public int[][] segLen;

    /** Index of the packet where each layer has been found */
    public int[] pktIdx;

    /**
     * Constructs a new instance with specified number of layers and
     * code-block coordinates. The number corresponds to the maximum
     * piece of codeword for one code-block.
     *
     * @param ulx The uper-left x-coordinate
     *
     * @param uly The uper-left y-coordinate
     *
     * @param w Width of the code-block
     *
     * @param h Height of the code-block
     *
     * @param nl The number of layers
     *
     */
    public CBlkInfo(int ulx,int uly,int w,int h,int nl){
        this.ulx = ulx;
        this.uly = uly;
        this.w = w;
        this.h = h;
        off = new int[nl];
        len = new int[nl];
        ntp = new int[nl];
        segLen = new int[nl][];
	pktIdx = new int[nl];
	for(int i=nl-1;i>=0;i--)
	    pktIdx[i] = -1;
    }

    /**
     * Adds the number of new truncation for specified layer.
     *
     * @param l layer index
     *
     * @param newtp Number of new truncation points
     *
     */
    public void addNTP(int l,int newtp){
        ntp[l] = newtp;
        ctp = 0;
        for(int lIdx=0; lIdx<=l; lIdx++){
            ctp += ntp[lIdx];
        }
    }

    /**
     * Object information in a string.
     *
     * @return Object information
     *
     */
    public String toString(){
        String string = "(ulx,uly,w,h)= "+ulx+","+uly+","+w+","+h;
        string += ", "+msbSkipped+" MSB bit(s) skipped\n";
        if( len!=null )
            for(int i=0; i<len.length; i++){
                string += "\tl:"+i+", start:"+off[i]+
                    ", len:"+len[i]+", ntp:"+ntp[i]+", pktIdx="+
		    pktIdx[i];
                if(segLen!=null && segLen[i]!=null){
                    string += " { ";
                    for(int j=0; j<segLen[i].length; j++)
                        string += segLen[i][j]+" ";
                    string += "}";
                }
                string += "\n";
            }
        string += "\tctp="+ctp;
        return string;
    }
}
