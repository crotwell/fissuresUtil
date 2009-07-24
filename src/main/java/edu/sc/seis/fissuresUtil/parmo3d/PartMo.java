package edu.sc.seis.fissuresUtil.parmo3d;

/*
 * PartMo Annimation adapted from several Java3D tutorial examples plus whatever
 * else I picked up ... tjo 05/2000
 */
/*
 * Getting Started with the Java 3D API written in Java 3D
 * 
 * This program demonstrates: 1. writing a visual object class In this program,
 * Axis class defines a visual object This particular class does not extend
 * another class. See other the text for a discussion and a differnt approach.
 * 2. Using LineArray to draw 3D lines. Three LineArray geometries are contained
 * in an instance of Axis. There are better ways of doing this. This is a simple
 * example.
 */
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import edu.sc.seis.fissuresUtil.exceptionHandler.GUIReporter;

public class PartMo extends JPanel {

    // ///////////////////////////////////////////////
    //
    // create graph branch group
    //
    public class Axis {

        private BranchGroup axisBG;

        private BranchGroup textBG;

        private BranchGroup surfaceBG;

        // //////////////////////////////////////////
        //
        // create axis subgraph
        //
        public Axis() {
            axisBG = new BranchGroup();
            textBG = new BranchGroup();
            surfaceBG = new BranchGroup();
            // create line for X axis ==> MAPPED TO seismic Z direction
            LineArray axisXLines = new LineArray(2, LineArray.COORDINATES);
            axisBG.addChild(new Shape3D(axisXLines));
            axisXLines.setCoordinate(0, new Point3f(-1.0f, 0.0f, 0.0f));
            axisXLines.setCoordinate(1, new Point3f(1.0f, 0.0f, 0.0f));
            // create line for Y axis ==> MAPPED TO seismic E direction
            LineArray axisYLines = new LineArray(2, LineArray.COORDINATES);
            axisBG.addChild(new Shape3D(axisYLines));
            axisYLines.setCoordinate(0, new Point3f(0.0f, -1.0f, 0.0f));
            axisYLines.setCoordinate(1, new Point3f(0.0f, 1.0f, 0.0f));
            // create line for Z axis ==> MAPPED to seismic N direction
            LineArray axisZLines = new LineArray(2, LineArray.COORDINATES);
            axisBG.addChild(new Shape3D(axisZLines));
            axisZLines.setCoordinate(0, new Point3f(0.0f, 0.0f, -1.0f));
            axisZLines.setCoordinate(1, new Point3f(0.0f, 0.0f, 1.0f));
            /* Work on the surface */
            Appearance SurfaceLook = new Appearance();
            TransparencyAttributes SurfaceTransp = new TransparencyAttributes();
            SurfaceTransp.setTransparency(0.5f);
            SurfaceTransp.setTransparencyMode(TransparencyAttributes.BLENDED);
            ColoringAttributes SurfaceColor = new ColoringAttributes();
            SurfaceColor.setColor(0.0f, 0.8f, 0.0f);
            SurfaceColor.setShadeModel(ColoringAttributes.SHADE_GOURAUD);
            SurfaceLook.setTransparencyAttributes(SurfaceTransp);
            SurfaceLook.setColoringAttributes(SurfaceColor);
            surfaceBG.addChild(new Box(0.05f, 0.75f, 0.75f, SurfaceLook));
        } // end of axis constructor

        public BranchGroup getAxisBG() {
            return axisBG;
        }

        public BranchGroup getTextBG() {
            return textBG;
        }

        public BranchGroup getSurfaceBG() {
            return surfaceBG;
        }
    } // end of class Axis

    public class Traces {

        private BranchGroup traceBG;

        private BranchGroup singAlongBG;

        int numberOfPoints = 82;

        PointArray zPointArray = new PointArray(numberOfPoints,
                                                GeometryArray.COORDINATES);

        PointArray ePointArray = new PointArray(numberOfPoints,
                                                GeometryArray.COORDINATES);

        PointArray nPointArray = new PointArray(numberOfPoints,
                                                GeometryArray.COORDINATES);

        public Traces(float backAzimuth, float angleOfIncidence) {
            traceBG = new BranchGroup();
            singAlongBG = new BranchGroup();
            zPointArray.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
            ePointArray.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
            nPointArray.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
            Appearance zApp = new Appearance();
            Appearance eApp = new Appearance();
            Appearance nApp = new Appearance();
            ColoringAttributes z_coloring = new ColoringAttributes();
            ColoringAttributes e_coloring = new ColoringAttributes();
            ColoringAttributes n_coloring = new ColoringAttributes();
            z_coloring.setColor(1.0f, 1.0f, 1.0f);
            e_coloring.setColor(1.0f, 1.0f, 1.0f);
            n_coloring.setColor(1.0f, 1.0f, 1.0f);
            zApp.setColoringAttributes(z_coloring);
            eApp.setColoringAttributes(e_coloring);
            nApp.setColoringAttributes(n_coloring);
            PointAttributes points_points = new PointAttributes(5.0f, true);
            zApp.setPointAttributes(points_points);
            eApp.setPointAttributes(points_points);
            nApp.setPointAttributes(points_points);
            Shape3D ztr = new Shape3D(zPointArray);
            ztr.setAppearance(zApp);
            traceBG.addChild(ztr);
            Shape3D etr = new Shape3D(ePointArray);
            etr.setAppearance(eApp);
            traceBG.addChild(etr);
            Shape3D ntr = new Shape3D(nPointArray);
            ntr.setAppearance(nApp);
            traceBG.addChild(ntr);
            Appearance zSingAlong = new Appearance();
            Appearance eSingAlong = new Appearance();
            Appearance nSingAlong = new Appearance();
            ColoringAttributes z_sing = new ColoringAttributes();
            ColoringAttributes e_sing = new ColoringAttributes();
            ColoringAttributes n_sing = new ColoringAttributes();
            z_sing.setColor(1.0f, 0.0f, 0.0f);
            e_sing.setColor(0.0f, 1.0f, 0.0f);
            n_sing.setColor(0.0f, 0.0f, 1.0f);
            zSingAlong.setColoringAttributes(z_sing);
            eSingAlong.setColoringAttributes(e_sing);
            nSingAlong.setColoringAttributes(n_sing);
            PointAttributes sing_points = new PointAttributes(8.0f, true);
            zSingAlong.setPointAttributes(sing_points);
            eSingAlong.setPointAttributes(sing_points);
            nSingAlong.setPointAttributes(sing_points);
            Shape3D zSing = new Shape3D(zPointArray);
            zSing.setAppearance(zSingAlong);
            singAlongBG.addChild(zSing);
            Shape3D eSing = new Shape3D(ePointArray);
            eSing.setAppearance(eSingAlong);
            singAlongBG.addChild(eSing);
            Shape3D nSing = new Shape3D(nPointArray);
            nSing.setAppearance(nSingAlong);
            singAlongBG.addChild(nSing);
            Shaker3D shaker = new Shaker3D(Shaker3D.SINE,
                                           numberOfPoints,
                                           backAzimuth,
                                           angleOfIncidence);
            shaker.getSeisPointArrays(zPointArray, ePointArray, nPointArray);
        }

        public BranchGroup getTraceBG() {
            return traceBG;
        }

        public void getSingAlongPoints(Point3f zSingArray[],
                                       Point3f eSingArray[],
                                       Point3f nSingArray[]) {
            int vertex = zPointArray.getVertexCount();
            int i = 0;
            while(i < vertex) {
                Point3f pointZ = new Point3f();
                Point3f pointE = new Point3f();
                Point3f pointN = new Point3f();
                zPointArray.getCoordinate(i, pointZ);
                zSingArray[i] = pointZ;
                ePointArray.getCoordinate(i, pointE);
                eSingArray[i] = pointE;
                nPointArray.getCoordinate(i, pointN);
                nSingArray[i] = pointN;
                i++;
            }
        }

        public int getNumberOfPoints() {
            return numberOfPoints;
        }
    }

    public class MotionVector {

        private BranchGroup motionBG;

        LineArray GroundMotion = new LineArray(82, LineArray.COORDINATES
                | LineArray.COLOR_3);

        public MotionVector(float backAzimuth, float angleOfIncidence) {
            motionBG = new BranchGroup();
            int jj = 0;
            while(jj < 82) {
                GroundMotion.setCoordinate(jj, new Point3f(0.0f, 0.0f, 0.0f));
                GroundMotion.setColor(jj, new Color3f(0.0f, 0.0f, 0.0f));
                jj++;
            }
            LineAttributes la = new LineAttributes();
            float three = 3;
            la.setLineWidth(three);
            la.setLinePattern(LineAttributes.PATTERN_SOLID);
            Appearance app = new Appearance();
            app.setLineAttributes(la);
            Shape3D gm = new Shape3D(GroundMotion);
            gm.setAppearance(app);
            motionBG.addChild(gm);
            Shaker3D shaker = new Shaker3D(Shaker3D.SINE,
                                           backAzimuth,
                                           angleOfIncidence);
            shaker.getWaveLineArray(GroundMotion);
            // System.out.println("After callt o shaker#D");
            jj = 0;
            while(jj < 82) {
                Point3f point = new Point3f();
                GroundMotion.getCoordinate(jj, point);
                GroundMotion.setColor(jj, new Color3f(0.6f, 0.9f, 0.9f));
                float t[] = new float[3];
                point.get(t);
                if(t[0] < 0.0) {
                    GroundMotion.setColor(jj, new Color3f(0.6f, 0.9f, 0.9f));
                }
                // //System.out.println("MV: Point " + jj + " " + point);
                jj++;
            }
            // System.out.println("After the while loop after shaker3D");
        }

        public BranchGroup getMotionBG() {
            return motionBG;
        }

        public void getMotionPoints(Point3f pointsGM[]) {
            // Point3f pointsGM[] = new Point3f[41];
            int i = 0;
            int count = 0;
            while(i < 41) {
                Point3f point = new Point3f();
                GroundMotion.getCoordinate(count, point);
                // //System.out.println("Index: " + count + " " + i + " " +
                // point);
                pointsGM[i] = point;
                i++;
                count = i * 2;
            }
        }
    }

    // ///////////////////////////////////////////////
    //
    // create scene graph branch group
    //
    public BranchGroup createSceneGraph(float baz, float ang) {
        BranchGroup objRoot = new BranchGroup();
        BranchGroup Axes = new Axis().getAxisBG();
        BranchGroup Surface = new Axis().getSurfaceBG();
        BranchGroup Motion = new MotionVector(baz, ang).getMotionBG();
        BranchGroup Trace = new Traces(baz, ang).getTraceBG();
        Transform3D rotateX = new Transform3D();
        Transform3D rotateY = new Transform3D();
        Transform3D rotateZ = new Transform3D();
        double zRotation = 90.0d;
        double xRotation = -135.0d;
        double yRotation = -45.0d;
        rotateX.rotX(Math.toRadians(xRotation));
        rotateZ.rotZ(Math.toRadians(zRotation));
        rotateY.rotY(Math.toRadians(yRotation));
        Transform3D rotate = new Transform3D();
        Transform3D shift = new Transform3D();
        Transform3D scale = new Transform3D();
        Transform3D PlaceCoordSystem = new Transform3D();
        rotate.mul(rotateZ);
        rotate.mul(rotateY);
        rotate.mul(rotateX);
        scale.setScale(0.5d);
        shift.setTranslation(new Vector3f(0.0f, 0.4f, 0.00f));
        PlaceCoordSystem.mul(shift);
        PlaceCoordSystem.mul(rotate);
        PlaceCoordSystem.mul(scale);
        Transform3D SurfShift = new Transform3D();
        SurfShift.setTranslation(new Vector3f(0.0f, 0.36f, 0.00f));
        Transform3D PlaceSurface = new Transform3D();
        PlaceSurface.mul(SurfShift);
        PlaceSurface.mul(rotate);
        PlaceSurface.mul(scale);
        /*
         * Text is placed in the global coordinate sytem at locations
         * transformed by the PlaceCoordSystem object. This keeps them facing
         * the viewer, but in the proper location.
         */
        Point3f textpt = new Point3f(1.0f, 0.0f, 0.0f);
        PlaceCoordSystem.transform(textpt);
        Text2D up = new Text2D("Up",
                               textpt,
                               new Color3f(1.0f, 0f, 0.0f),
                               "Helvetica",
                               16,
                               Font.BOLD);
        objRoot.addChild(up);
        textpt = new Point3f(-1.2f, 0.0f, 0.0f);
        PlaceCoordSystem.transform(textpt);
        Text2D down = new Text2D("Down",
                                 textpt,
                                 new Color3f(1.0f, 0f, 0.0f),
                                 "Helvetica",
                                 16,
                                 Font.BOLD);
        objRoot.addChild(down);
        textpt = new Point3f(0.0f, 1.2f, 0.0f);
        PlaceCoordSystem.transform(textpt);
        Text2D east = new Text2D("East",
                                 textpt,
                                 new Color3f(0f, 1.0f, 0.2f),
                                 "Helvetica",
                                 16,
                                 Font.BOLD);
        objRoot.addChild(east);
        textpt = new Point3f(0.0f, -1.2f, 0.2f);
        PlaceCoordSystem.transform(textpt);
        Text2D west = new Text2D("West",
                                 textpt,
                                 new Color3f(0f, 1.0f, 0.0f),
                                 "Helvetica",
                                 16,
                                 Font.BOLD);
        objRoot.addChild(west);
        textpt = new Point3f(0.0f, 0.0f, 1.1f);
        PlaceCoordSystem.transform(textpt);
        Text2D north = new Text2D("North",
                                  textpt,
                                  new Color3f(0f, 0f, 1.0f),
                                  "Helvetica",
                                  16,
                                  Font.BOLD);
        objRoot.addChild(north);
        textpt = new Point3f(0.0f, 0.0f, -1.2f);
        PlaceCoordSystem.transform(textpt);
        Text2D south = new Text2D("South",
                                  textpt,
                                  new Color3f(0f, 0f, 1.0f),
                                  "Helvetica",
                                  16,
                                  Font.BOLD);
        objRoot.addChild(south);
        textpt = new Point3f(-1.2f, -1.4f, -1.0f);
        Text2D caption = new Text2D("Press and Hold any key to start animation",
                                    textpt,
                                    new Color3f(0f, 0f, 0.0f),
                                    "TimesRoman",
                                    24,
                                    Font.BOLD);
        objRoot.addChild(caption);
        /*
         * Set up the animation of the traces
         */
        Alpha alpha = new Alpha(-1, 4000);
        TransformGroup ZsingAlong = new TransformGroup();
        Transform3D ZaxisOfPos = new Transform3D();
        ZaxisOfPos.set(new Vector3f(1.0f, 0.0f, 0.0f));
        ZsingAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        TransformGroup NsingAlong = new TransformGroup();
        Transform3D NaxisOfPos = new Transform3D();
        NaxisOfPos.set(new Vector3f(1.0f, 0.0f, 0.0f));
        NsingAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        TransformGroup EsingAlong = new TransformGroup();
        Transform3D EaxisOfPos = new Transform3D();
        EaxisOfPos.set(new Vector3f(1.0f, 0.0f, 0.0f));
        EsingAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        int nknots = new Traces(baz, ang).getNumberOfPoints();
        Point3f zSing[] = new Point3f[nknots];
        Point3f eSing[] = new Point3f[nknots];
        Point3f nSing[] = new Point3f[nknots];
        float knots[] = new float[nknots];
        float zknots[] = new float[nknots];
        float knotInc = 1.0f / (nknots - 1);
        int i = 0;
        while(i < nknots) {
            knots[i] = i * knotInc;
            zknots[i] = i * knotInc;
            i++;
        }
        new Traces(baz, ang).getSingAlongPoints(zSing, nSing, eSing);
        // System.out.println("before tjo interpolator");
        tjoInterpolator nSinger = new tjoInterpolator(alpha,
                                                      NsingAlong,
                                                      NaxisOfPos,
                                                      knots,
                                                      nSing);
        // System.out.println("after tjo interpolator");
        nSinger.setSchedulingBounds(new BoundingSphere());
        // System.out.println("after set scheduling bounds");
        // System.out.println("before tjo interpolator");
        tjoInterpolator eSinger = new tjoInterpolator(alpha,
                                                      EsingAlong,
                                                      EaxisOfPos,
                                                      knots,
                                                      eSing);
        // System.out.println("after tjo interpolator");
        eSinger.setSchedulingBounds(new BoundingSphere());
        // System.out.println("after set scheduling bounds");
        // System.out.println("before tjo interpolator");
        tjoInterpolator zSinger = new tjoInterpolator(alpha,
                                                      ZsingAlong,
                                                      ZaxisOfPos,
                                                      knots,
                                                      zSing);
        // System.out.println("after tjo interpolator");
        zSinger.setSchedulingBounds(new BoundingSphere());
        // System.out.println("after set scheduling bounds");
        int GMknots = 41;
        Point3f GMpoints[] = new Point3f[41];
        Point3f GMzOnly[] = new Point3f[41];
        Point3f GMeOnly[] = new Point3f[41];
        Point3f GMnOnly[] = new Point3f[41];
        new MotionVector(baz, ang).getMotionPoints(GMpoints);
        float gknots[] = new float[GMknots];
        float gnotInc = 1.0f / (GMknots - 1);
        for(i = 0; i < GMknots; i++) {
            gknots[i] = i * gnotInc;
            float t[] = new float[3];
            GMpoints[i].get(t);
            GMzOnly[i] = new Point3f(t[0], 0.0f, 0.0f);
            GMeOnly[i] = new Point3f(0.0f, t[1], 0.0f);
            GMnOnly[i] = new Point3f(0.0f, 0.0f, t[2]);
            PlaceCoordSystem.transform(GMpoints[i]);
            PlaceCoordSystem.transform(GMzOnly[i]);
            PlaceCoordSystem.transform(GMeOnly[i]);
            PlaceCoordSystem.transform(GMnOnly[i]);
        }
        TransformGroup GMsingAlong = new TransformGroup();
        GMsingAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        TransformGroup GMzOnly_singAlong = new TransformGroup();
        GMzOnly_singAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        TransformGroup GMnOnly_singAlong = new TransformGroup();
        GMnOnly_singAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        TransformGroup GMeOnly_singAlong = new TransformGroup();
        GMeOnly_singAlong.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D GMaxisOfPos = new Transform3D();
        GMaxisOfPos.set(new Vector3f(0.0f, 1.0f, 0.0f));
        tjoInterpolator gmSinger = new tjoInterpolator(alpha,
                                                       GMsingAlong,
                                                       GMaxisOfPos,
                                                       gknots,
                                                       GMpoints);
        gmSinger.setSchedulingBounds(new BoundingSphere());
        tjoInterpolator gmZonlySinger = new tjoInterpolator(alpha,
                                                            GMzOnly_singAlong,
                                                            GMaxisOfPos,
                                                            gknots,
                                                            GMzOnly);
        gmZonlySinger.setSchedulingBounds(new BoundingSphere());
        tjoInterpolator gmNonlySinger = new tjoInterpolator(alpha,
                                                            GMnOnly_singAlong,
                                                            GMaxisOfPos,
                                                            gknots,
                                                            GMnOnly);
        gmNonlySinger.setSchedulingBounds(new BoundingSphere());
        tjoInterpolator gmEonlySinger = new tjoInterpolator(alpha,
                                                            GMeOnly_singAlong,
                                                            GMaxisOfPos,
                                                            gknots,
                                                            GMeOnly);
        gmEonlySinger.setSchedulingBounds(new BoundingSphere());
        TransformGroup objPlace3D = new TransformGroup(PlaceCoordSystem);
        TransformGroup objPlaceSurf = new TransformGroup(PlaceSurface);
        Background background = new Background();
        background.setColor(0.6f, 0.6f, 0.6f);
        background.setApplicationBounds(new BoundingSphere());
        objRoot.addChild(background);
        objPlace3D.addChild(Axes);
        objPlace3D.addChild(Motion);
        objRoot.addChild(objPlace3D);
        objPlaceSurf.addChild(Surface);
        objRoot.addChild(objPlaceSurf);
        /*
         * objPlaceAxesText.addChild(AxesText);
         * objRoot.addChild(objPlaceAxesText);
         */
        objRoot.addChild(Trace);
        objRoot.addChild(ZsingAlong);
        objRoot.addChild(zSinger);
        Appearance zSphere = new Appearance();
        ColoringAttributes zCA = new ColoringAttributes();
        zCA.setColor(1.0f, 0.0f, 0.0f);
        zSphere.setColoringAttributes(zCA);
        ZsingAlong.addChild(new Sphere(0.02f, zSphere));
        objRoot.addChild(NsingAlong);
        objRoot.addChild(nSinger);
        Appearance nSphere = new Appearance();
        ColoringAttributes nCA = new ColoringAttributes();
        nCA.setColor(0.0f, 0.0f, 1.0f);
        nSphere.setColoringAttributes(nCA);
        NsingAlong.addChild(new Sphere(0.02f, nSphere));
        objRoot.addChild(EsingAlong);
        objRoot.addChild(eSinger);
        Appearance eSphere = new Appearance();
        ColoringAttributes eCA = new ColoringAttributes();
        eCA.setColor(0.0f, 1.0f, 0.0f);
        eSphere.setColoringAttributes(eCA);
        EsingAlong.addChild(new Sphere(0.02f, eSphere));
        objRoot.addChild(GMzOnly_singAlong);
        objRoot.addChild(gmZonlySinger);
        Appearance gmZonlySphere = new Appearance();
        ColoringAttributes gmZonlyCA = new ColoringAttributes();
        gmZonlyCA.setColor(1.0f, 0.0f, 0.0f);
        gmZonlySphere.setColoringAttributes(gmZonlyCA);
        GMzOnly_singAlong.addChild(new Sphere(0.015f, gmZonlySphere));
        objRoot.addChild(GMnOnly_singAlong);
        objRoot.addChild(gmNonlySinger);
        Appearance gmNonlySphere = new Appearance();
        ColoringAttributes gmNonlyCA = new ColoringAttributes();
        gmNonlyCA.setColor(0.0f, 0.0f, 1.0f);
        gmNonlySphere.setColoringAttributes(gmNonlyCA);
        GMnOnly_singAlong.addChild(new Sphere(0.015f, gmNonlySphere));
        objRoot.addChild(GMeOnly_singAlong);
        objRoot.addChild(gmEonlySinger);
        Appearance gmEonlySphere = new Appearance();
        ColoringAttributes gmEonlyCA = new ColoringAttributes();
        gmEonlyCA.setColor(0.0f, 1.0f, 0.0f);
        gmEonlySphere.setColoringAttributes(gmEonlyCA);
        GMeOnly_singAlong.addChild(new Sphere(0.015f, gmEonlySphere));
        objRoot.addChild(GMsingAlong);
        objRoot.addChild(gmSinger);
        Appearance gmSphere = new Appearance();
        ColoringAttributes gmCA = new ColoringAttributes();
        gmCA.setColor(0.4f, 0.2f, 0.2f);
        gmSphere.setColoringAttributes(gmCA);
        GMsingAlong.addChild(new Sphere(0.02f, gmSphere));
        // Let Java 3D perform optimizations on this scene graph.
        objRoot.compile();
        return objRoot;
    } // end of CreateSceneGraph method

    // Create a simple scene and attach it to the virtual universe
    public PartMo(float baz, float ang) {
        setLayout(new BorderLayout());
        try {
            // Create Canvas3D
            GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
            Canvas3D canvas3D = new Canvas3D(config);
            add("Center", canvas3D);
            BranchGroup scene = createSceneGraph(baz, ang);
            SimpleUniverse simpleU = new SimpleUniverse(canvas3D);
            simpleU.getViewingPlatform().setNominalViewingTransform();
            simpleU.addBranchGraph(scene);
        } catch(Throwable t) {
            GUIReporter.swapGreetingAndHandle(t,
                                              "Oooops!  GEE was unable to create the particle motion animation.  There must be a problem in the configuration of the animation on this system.  "
                                                      + "Don't panic ... this is our fault, not yours. If you use the Save "
                                                      + "button at the bottom of this screen to make a file of the details of this problem and email it to: geebugs@seis.sc.edu and we will try to "
                                                      + "help fix the configuration problem leading to this little hiccup. You should be able to continue your current "
                                                      + "GEE session without trouble.  Only particle motion animations are affected by this problem. "
                                                      + "Thanks for your help in improving GEE!");
        }
    }
}
