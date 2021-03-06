package edu.sc.seis.fissuresUtil.gmt;

import java.io.IOException;

/**
 * @author oliverpa Created on Jan 21, 2005
 */
public class MapCropper {

    public MapCropper(int width, int height, int leftOffset, int bottomOffset,
            int top, int right, int bottom, int left) {
        this.width = width;
        this.height = height;
        this.leftOffset = leftOffset;
        this.bottomOffset = bottomOffset;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public int[][] translatePoints(int[][] rawPoints) {
        int[][] translatedPoints = new int[rawPoints.length][2];
        for(int i = 0; i < rawPoints.length; i++) {
            int x = rawPoints[i][0];
            int y = rawPoints[i][1];
            x = x + leftOffset - left;
            y = height - y - bottomOffset - top;
            translatedPoints[i][0] = x;
            translatedPoints[i][1] = y;
        }
        return translatedPoints;
    }

    public void crop(String filename) throws InterruptedException, IOException {
        String command = "mogrify -crop " + getNewWidth() + "x" + getNewHeight() + "+"
                + left + "+" + top + " " + filename;
        GenericCommandExecute.execute(command);
    }

    public static void main(String[] args) {
        try {
            String projection = "Kf166/10i", region = "-14/346/-90/90";
            double[][] stationCoords = { {-180, 90},
                                        {-135, 67.5},
                                        {-90, 45},
                                        {-45, 22.5},
                                        {0, 0}};
            double[][] eventCoords = { {45, -22.5},
                                      {90, -45},
                                      {135, -67.5},
                                      {180, -90}};
            PSXYExecute.addPoints("world.ps",
                                  projection,
                                  region,
                                  "t0.4",
                                  "0/0/255",
                                  "5/255",
                                  stationCoords);
            PSXYExecute.addPoints("world.ps",
                                  projection,
                                  region,
                                  "c0.7",
                                  null,
                                  "12/255/0/0",
                                  eventCoords);
            int[][] rawStationPoints = MapProjectExecute.forward(projection,
                                                                 region,
                                                                 stationCoords);
            int[][] rawEventPoints = MapProjectExecute.forward(projection,
                                                               region,
                                                               eventCoords);
            ConvertExecute.convert("world.ps",
                                   "world.png",
                                   "-antialias -rotate 90");
            MapCropper cropper = new MapCropper(842,
                                                595,
                                                72,
                                                72,
                                                150,
                                                22,
                                                45,
                                                60);
            cropper.crop("world.png");
            int[][] stationPoints = cropper.translatePoints(rawStationPoints);
            int[][] eventPoints = cropper.translatePoints(rawEventPoints);
            System.out.println("calculated station points: ");
            for(int i = 0; i < stationPoints.length; i++) {
                System.out.println(stationPoints[i][0] + " "
                        + stationPoints[i][1]);
            }
            System.out.println("calculated event points: ");
            for(int i = 0; i < eventPoints.length; i++) {
                System.out.println(eventPoints[i][0] + " " + eventPoints[i][1]);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public int getBottom() {
        return bottom;
    }

    public int getBottomOffset() {
        return bottomOffset;
    }

    public int getHeight() {
        return height;
    }

    public int getLeft() {
        return left;
    }

    public int getLeftOffset() {
        return leftOffset;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }
    
    public int getNewWidth(){
        return width - right - left;
    }
    
    public int getNewHeight(){
        return height - top - bottom;
    }

    private int width, height, leftOffset, bottomOffset, top, right, bottom,
            left;
}