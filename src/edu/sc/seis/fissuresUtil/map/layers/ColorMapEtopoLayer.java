package edu.sc.seis.fissuresUtil.map.layers;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import com.bbn.openmap.layer.etopo.ETOPOJarLayer;

/**
 * @author oliverpa Created on Aug 17, 2004
 */
public class ColorMapEtopoLayer extends ETOPOJarLayer {

    public ColorMapEtopoLayer(String colorMapFilename)
            throws FileNotFoundException, IOException {
        super();
        readInColorTable(colorMapFilename);
    }

    protected Color getColor(short elevation, byte slopeVal) {
        // build first time
        if(slopeColors == null) {
            // allocate storage for elevation bands, 8 slope bands
            slopeColors = new Color[elevationLimit.length][8];
            // process each elevation band
            for(int i = 0; i < elevationLimit.length; i++) {
                // get base color (0 slope color)
                Color base = new Color(redValues[i],
                                       greenValues[i],
                                       blueValues[i],
                                       opaqueness);
                // call the "brighter" method on the base color for
                // positive slope
                for(int j = 4; j < 8; j++) {
                    // set
                    if(j == 4) slopeColors[i][j] = base;
                    else slopeColors[i][j] = slopeColors[i][j - 1].brighter();
                }
                // call the "darker" method on the base color for negative
                // slopes
                for(int k = 3; k >= 0; k--) {
                    // set
                    slopeColors[i][k] = slopeColors[i][k + 1].darker();
                }
            }
        }
        // get the elevation band index
        int elIdx = getElevIndex(elevation);
        // compute slope idx
        int slopeIdx = ((int)slopeVal + 127) >> 5;
        // return color
        return slopeColors[elIdx][slopeIdx];
    }
    
    /* returns the color lookup index based on elevation */
    protected int getElevIndex(short el) {
        for (int i=0;i<elevationLimit.length-1;i++)
            if (el<elevationLimit[i+1])
                return i;
        return elevationLimit.length-1;
    }

    private void readInColorTable(String filename) throws IOException,
            FileNotFoundException {
        //read lines of file into a list
        File cMapFile = new File(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cMapFile)));
        List colorRangeList = new ArrayList();
        String line = null;
        while((line = reader.readLine()) != null) {
            colorRangeList.add(line);
            System.out.println(line);
        }
        int numValues = colorRangeList.size() + 1;
        //initialize arrays
        elevationLimit = new int[numValues];
        redValues = new int[numValues];
        greenValues = new int[numValues];
        blueValues = new int[numValues];
        StringTokenizer tok = null;
        for(int i = 0; i <= colorRangeList.size(); i++) {
            if(i < colorRangeList.size()) {
                tok = new StringTokenizer((String)colorRangeList.get(i));
            }
            System.out.println(i + " " + tok);
            getValuesFromTokenizer(tok, i);
        }
    }

    private void getValuesFromTokenizer(StringTokenizer tok, int i) {
        elevationLimit[i] = Integer.parseInt(tok.nextToken());
        redValues[i] = Integer.parseInt(tok.nextToken());
        greenValues[i] = Integer.parseInt(tok.nextToken());
        blueValues[i] = Integer.parseInt(tok.nextToken());
    }

    private int[] elevationLimit;

    private int[] redValues, greenValues, blueValues;
}