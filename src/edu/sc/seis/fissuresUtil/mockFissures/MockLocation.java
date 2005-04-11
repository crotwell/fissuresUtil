package edu.sc.seis.fissuresUtil.mockFissures;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Quantity;

public class MockLocation {

    public static final Location SIMPLE = create();

    public static final Location BERLIN = create(52.31f,
                                                 13.24f,
                                                 Defaults.TEN_K,
                                                 Defaults.TEN_K);

    public static Location create() {
        return create(0f, 0f);
    }

    public static Location create(float lat, float lon) {
        return create(lat, lon, Defaults.ZERO_K, Defaults.ZERO_K);
    }

    public static Location create(float lat,
                                  float lon,
                                  Quantity depth,
                                  Quantity elev) {
        return new Location(lat, lon, elev, depth, LocationType.from_int(1));
    }

    public static Location[] create(int rows, int cols) {
        return create(rows, cols, -70, 70, -180, 180);
    }

    public static Location[] create(int rows,
                                    int cols,
                                    double minLat,
                                    double maxLat,
                                    double minLon,
                                    double maxLon) {
        Location[] locs = new Location[rows * cols];
        double lonStep = 0;
        if(cols > 1) {
            lonStep = (maxLon - minLon) / (cols - 1);
        }
        double latStep = 0;
        if(rows > 1) {
            latStep = (maxLat - minLat) / (rows - 1);
        }
        for(int i = 0; i < rows; i++) {
            double lat = minLat + latStep * i;
            int rowOffset = i * cols;
            double lon = minLon;
            for(int j = 0; j < cols; j++) {
                locs[rowOffset + j] = MockLocation.create((float)lat,
                                                          (float)lon);
                lon += lonStep;
            }
        }
        return locs;
    }

    public static Location[] createMultiple() {
        Location[] locs = new Location[3];
        locs[0] = SIMPLE;
        locs[1] = BERLIN;
        locs[2] = create(21.3f, 31.4f, Defaults.ZERO_K, Defaults.TEN_K);
        return locs;
    }
}