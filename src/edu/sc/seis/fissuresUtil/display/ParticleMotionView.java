package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.FilteredDataSetSeismogram;

/**
 * ParticleMotionView.java
 * 
 * 
 * Created: Tue Jun 11 15:14:17 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */

public class ParticleMotionView extends JComponent {

    public ParticleMotionView(ParticleMotionDisplay particleMotionDisplay) {
        this.pmd = particleMotionDisplay;
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resize();
            }

            public void componentShown(ComponentEvent e) {
                resize();
            }
        });
    }

    public synchronized void resize() {
        setSize(super.getSize());
        repaint();
    }

    public void add(NamedFilter filter) {
        Iterator it = parMos.iterator();
        while (it.hasNext()) {
            ParticleMotion cur = (ParticleMotion) it.next();
            cur.add(filter, getColor(cur.getTimeConfig(), filter));
        }
        repaint();
    }

    public void remove(NamedFilter filter) {
        Iterator it = parMos.iterator();
        while (it.hasNext()) {
            ((ParticleMotion) it.next()).remove(filter);
        }
        repaint();
    }

    public void setOriginal(boolean visible) {
        Iterator it = parMos.iterator();
        while (it.hasNext()) {
            ((ParticleMotion) it.next()).setVisible(visible);
        }
    }

    public synchronized void paintComponent(Graphics g) {
        if (displayKey == null) return;
        Graphics2D graphics2D = (Graphics2D) g;
        //first draw the azimuth if one of the display is horizontal plane
        for (int counter = 0; counter < parMos.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion) parMos.get(counter);
            if (!displayKey.equals(particleMotion.key)) continue;
            if (particleMotion.isHorizontalPlane()) {
                drawAzimuth(particleMotion, graphics2D);
                break;
            }
        }
        for (int counter = 0; counter < parMos.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion) parMos.get(counter);
            if (!displayKey.equals(particleMotion.key)) continue;
            particleMotion.draw(g, getSize());
        }
    }

    public void drawAzimuth(ParticleMotion particleMotion, Graphics2D graphics2D) {
        if (!particleMotion.isHorizontalPlane()) return;
        Shape sector = getSectorShape();
        graphics2D.setPaint(Color.LIGHT_GRAY);
        graphics2D.fill(sector);
        graphics2D.draw(sector);
        drawAzimuths(graphics2D);
        graphics2D.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
    }

    public void drawTitles(LocalSeismogramImpl hseis, LocalSeismogramImpl vseis) {
        pmd.setHorizontalTitle(hseis.getName());
        pmd.setVerticalTitle(vseis.getName());
    }

    public void drawAzimuths(Graphics2D g2D) {
        Insets insets = getInsets();
        double fmin = super.getSize().getWidth() - insets.left - insets.right;
        double fmax = super.getSize().getHeight() - insets.top - insets.bottom;
        int originx = (int) (fmin / 2);
        int originy = (int) (fmax / 2);
        int newx = originx;
        int newy = originy;
        Iterator it = azimuths.keySet().iterator();
        g2D.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
        while (it.hasNext()) {
            Double key = (Double) it.next();
            Color parMoCo = (Color) azimuths.get(key);
            if (parMoCo != null) {
                g2D.setColor(new Color(parMoCo.getRed(), parMoCo.getGreen(),
                        parMoCo.getBlue(), 96));
                GeneralPath generalPath = new GeneralPath();
                double degrees = key.doubleValue();
                int x = (int) (fmin * Math.cos(Math.toRadians(degrees)));
                int y = (int) (fmax * Math.sin(Math.toRadians(degrees)));
                generalPath.moveTo(newx + x, newy - y);
                generalPath.lineTo(newx - x, newy + y);
                g2D.draw(generalPath);
            }
        }
    }

    public synchronized Shape getSectorShape() {
        Insets insets = getInsets();
        double fmin = super.getSize().getWidth() - insets.left - insets.right;
        double fmax = super.getSize().getHeight() - insets.top - insets.bottom;
        int originx = (int) (fmin / 2);
        int originy = (int) (fmax / 2);
        int newx = originx;
        int newy = originy;
        GeneralPath generalPath = new GeneralPath();
        int size = sectors.size();
        for (int counter = 0; counter < size; counter++) {
            Point2D.Double point = (Point2D.Double) sectors.get(counter);
            double degreeone = point.getX();
            double degreetwo = point.getY();
            int xone = (int) (fmin * Math.cos(Math.toRadians(degreeone)));
            int yone = (int) (fmax * Math.sin(Math.toRadians(degreeone)));
            generalPath.moveTo(newx + xone, newy - yone);
            generalPath.lineTo(newx - xone, newy + yone);
            int xtwo = (int) (fmin * Math.cos(Math.toRadians(degreetwo)));
            int ytwo = (int) (fmax * Math.sin(Math.toRadians(degreetwo)));
            generalPath.lineTo(newx - xtwo, newy + ytwo);
            generalPath.lineTo(newx + xtwo, newy - ytwo);
            generalPath.lineTo(newx + xone, newy - yone);
        }
        return (Shape) generalPath;
    }

    public synchronized void add(DataSetSeismogram hseis,
            DataSetSeismogram vseis, TimeConfig tc, Color color, String key,
            boolean horizPlane) {
        ParticleMotion newParMo = new ParticleMotion(hseis, vseis, tc, color,
                key, horizPlane);
        parMos.add(newParMo);
        Iterator it = SeismogramDisplay.activeFilters.iterator();
        while (it.hasNext()) {
            NamedFilter cur = (NamedFilter) it.next();
            color = getColor(tc, cur);
            newParMo.add(cur, color);
        }
        if (statusTable != null) statusTable.add(newParMo);
        pmd.resize();
    }

    private Color getColor(TimeConfig tc, NamedFilter filter) {
        Map filterToColor = (Map) timeConfigToFilterColor.get(tc);
        if (filterToColor == null) {
            filterToColor = new HashMap();
            timeConfigToFilterColor.put(tc, filterToColor);
        }
        if (!filterToColor.containsKey(filter))
                filterToColor.put(filter,
                        SeismogramDisplay.COLORS[filterCount++
                                % SeismogramDisplay.COLORS.length]);
        return (Color) filterToColor.get(filter);
    }

    private Map timeConfigToFilterColor = new HashMap();

    public void addSector(double degreeone, double degreetwo) {
        sectors.add(new java.awt.geom.Point2D.Double(degreeone, degreetwo));
    }

    public void addAzimuthLine(double degrees, Color color) {
        azimuths.put(new Double(degrees), color);
    }

    /**
     * must be square
     * 
     * @param d
     *            a <code>Dimension</code> value
     */
    public void setSize(Dimension d) {
        if (d.width < d.height) {
            super.setSize(new Dimension(d.width, d.width));
        } else {
            super.setSize(new Dimension(d.height, d.height));
        }
    }

    public void setDisplayKey(String key) {
        displayKey = key;
        pmd.setActiveAmpConfig((AmpConfig) keysToAmpConfigs.get(displayKey));
    }

    public JTable getStatusTable() {
        if (statusTable == null)
                statusTable = new ParticleMotionStatusTable(parMos);
        return statusTable;
    }

    private String displayKey;

    private Map keysToAmpConfigs = new HashMap();

    LinkedList parMos = new LinkedList();

    Map azimuths = new HashMap();

    List sectors = new LinkedList();

    UnitRangeImpl horizRange = DisplayUtils.ONE_RANGE;

    UnitRangeImpl vertRange = DisplayUtils.ONE_RANGE;

    private ParticleMotionDisplay pmd;

    private ParticleMotionStatusTable statusTable;

    private static int filterCount = SeismogramDisplay.COLORS.length / 2;

    private static Logger logger = Logger.getLogger(ParticleMotionView.class);

    private int i = 0;

    private class ParticleMotion implements TimeListener, AmpListener,
            SeismogramContainerListener {
        public ParticleMotion(DataSetSeismogram hSeis, DataSetSeismogram vSeis,
                TimeConfig tc, Color color, String key, boolean horizPlane) {

            if (hSeis instanceof FilteredDataSetSeismogram) {
                name = hSeis.getName();
            } else {
                name = hSeis.getRequestFilter().channel_id.station_code;
            }
            this.color = color;
            hSeis = (DataSetSeismogram) hSeis.clone();
            hSeis.setName(hSeis.getName() + i++);
            vSeis = (DataSetSeismogram) vSeis.clone();
            vSeis.setName(vSeis.getName() + i++);
            horiz = new SeismogramContainer(this, hSeis);
            vert = new SeismogramContainer(this, vSeis);
            this.tc = tc;
            this.key = key;
            setUpConfigs();
            this.horizPlane = horizPlane;
            if (horizPlane) {
                pmd.displayBackAzimuth(hSeis.getDataSet(),
                        hSeis.getRequestFilter().channel_id, color);
            }
            //forces a time event through the amp config to make everything
            // look
            //decent
            tc.shaleTime(.1, 1);
            tc.shaleTime(-.1, 1);
        }

        private void setUpConfigs() {
            DataSetSeismogram[] seis = { horiz.getDataSetSeismogram(),
                    vert.getDataSetSeismogram() };
            AmpConfig ac = (AmpConfig) keysToAmpConfigs.get(key);
            if (ac == null) {
                ac = new RMeanAmpConfig();
                keysToAmpConfigs.put(key, ac);
            }
            if (!tc.contains(seis[0])) {
                tc.add(seis);
            }
            tc.addListener(ac);
            tc.addListener(this);
            ac.add(seis);
            ac.addListener(this);
        }

        private void tearDownConfigs() {
            DataSetSeismogram[] seis = { horiz.getDataSetSeismogram(),
                    vert.getDataSetSeismogram() };
            AmpConfig ac = (AmpConfig) keysToAmpConfigs.get(key);
            tc.removeListener(this);
            if (ac != null) {
                ac.remove(seis);
                ac.removeListener(this);
            }
        }

        public void draw(Graphics g, Dimension size) {
            if (visible) {
                Graphics2D g2D = (Graphics2D) g;
                if (horiz.getIterator(tr).numPointsLeft() <= 0
                        || vert.getIterator(tr).numPointsLeft() <= 0) { return; }
                g2D.setColor(color);
                g2D.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                GeneralPath generalPath = new GeneralPath(
                        GeneralPath.WIND_EVEN_ODD);
                boolean prevPointBad = true;//first point needs to be a move to
                SeismogramIterator hIt = horiz.getIterator(tr);
                UnitImpl horizUnit = hIt.getUnit();
                UnitRangeImpl horizRange = ae.getAmp(
                        horiz.getDataSetSeismogram()).convertTo(horizUnit);
                double hMin = horizRange.getMinValue();
                double hMax = horizRange.getMaxValue();
                SeismogramIterator vIt = vert.getIterator(tr);
                UnitImpl vertUnit = vIt.getUnit();
                UnitRangeImpl vertRange = ae.getAmp(vert.getDataSetSeismogram()).convertTo(
                        vertUnit);
                double vMin = vertRange.getMinValue();
                double vMax = vertRange.getMaxValue();
                while (hIt.hasNext() && vIt.hasNext()) {
                    double hVal = getVal(hIt, hMin, hMax, size.height);
                    double vVal = getVal(vIt, vMin, vMax, size.height);
                    if (hVal == Integer.MAX_VALUE || vVal == Integer.MAX_VALUE) {
                        prevPointBad = true;
                    } else {
                        vVal *= -1;
                        vVal += size.height;
                        if (prevPointBad) {
                            generalPath.moveTo((int) hVal, (int) vVal);
                            prevPointBad = false;
                        } else {
                            generalPath.lineTo((int) hVal, (int) vVal);
                        }
                    }
                }
                g2D.draw(generalPath);
            }
            Iterator it = filterToParMo.keySet().iterator();
            while (it.hasNext()) {
                ((ParticleMotion) filterToParMo.get(it.next())).draw(g, size);
            }
        }

        private double getVal(SeismogramIterator it, double minAmp,
                double maxAmp, int size) {
            double itVal = ((QuantityImpl) it.next()).getValue();
            if (Double.isNaN(itVal)) {//Gap in trace
                itVal = Integer.MAX_VALUE;
            } else {
                itVal = Math.round(SimplePlotUtil.linearInterp(minAmp, 0,
                        maxAmp, size, itVal));
            }
            return itVal;
        }

        public void updateData() {
            repaint();
        }

        public void updateAmp(AmpEvent event) {
            this.ae = event;
            repaint();
        }

        public void updateTime(TimeEvent timeEvent) {
            this.tr = timeEvent.getTime();
            repaint();
        }

        public boolean isHorizontalPlane() {
            return this.horizPlane;
        }

        public void add(NamedFilter filter, Color color) {
            if (!filterToParMo.containsKey(filter)) {
                filterToParMo.put(filter, new ParticleMotion(
                        FilteredDataSetSeismogram.getFiltered(
                                horiz.getDataSetSeismogram(), filter),
                        FilteredDataSetSeismogram.getFiltered(
                                vert.getDataSetSeismogram(), filter), tc,
                        color, key, horizPlane));
                if (statusTable != null)
                        statusTable.setComponent(this, generateLabel());
            }
        }

        private JComponent generateLabel() {
            JPanel labelPanel = new JPanel(new GridLayout(
                    filterToParMo.keySet().size() + 1, 1));
            labelPanel.add(ParticleMotionView.generateLabel(this));
            Iterator it = filterToParMo.keySet().iterator();
            while (it.hasNext()) {
                labelPanel.add(ParticleMotionView.generateLabel((ParticleMotion) filterToParMo.get(it.next())));
            }
            return labelPanel;
        }

        public void remove(NamedFilter filter) {
            if (filterToParMo.containsKey(filter)) {
                ((ParticleMotion) filterToParMo.get(filter)).tearDownConfigs();
                filterToParMo.remove(filter);
                if (statusTable != null)
                        statusTable.setComponent(this, generateLabel());
            }
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
            if (visible) {
                setUpConfigs();
            } else {
                tearDownConfigs();
            }
        }

        public Color getColor() {
            return color;
        }

        public TimeConfig getTimeConfig() {
            return tc;
        }

        Map filterToParMo = new HashMap();

        public String toString() {
            return name;
        }

        private String name;

        private SeismogramContainer horiz, vert;

        public String key = new String();

        private boolean visible = true;

        private MicroSecondTimeRange tr;

        private Color color;

        private boolean horizPlane = false;

        private AmpEvent ae;

        private TimeConfig tc;
    }

    private static JLabel generateLabel(ParticleMotion parMo) {
        JLabel label = new JLabel(parMo.toString(), JLabel.CENTER);
        label.setForeground(parMo.getColor());
        return label;
    }

    private class ParticleMotionStatusTable extends JTable {

        public ParticleMotionStatusTable(List parMos) {
            setModel(new ViewTableModel());
            setDefaultRenderer(String.class, new TableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus,
                        int row, int column) {
                    return ((CellStruct) cellStructs.get(row)).comp;
                }
            });
            setColumnWidths();
            Iterator it = parMos.iterator();
            while (it.hasNext())
                add((ParticleMotion) it.next());
        }

        public void setComponent(ParticleMotion parMo, JComponent comp) {
            synchronized (cellStructs) {
                TimeConfig tc = parMo.getTimeConfig();
                Iterator it = cellStructs.iterator();
                while (it.hasNext()) {
                    CellStruct cur = (CellStruct) it.next();
                    if (cur.tc.equals(tc)) {
                        cur.comp = comp;
                        rowHeight = comp.getPreferredSize().height;
                        revalidate();
                        return;
                    }
                }
            }
        }

        public void add(ParticleMotion parMo) {
            synchronized (cellStructs) {
                TimeConfig tc = parMo.getTimeConfig();
                Iterator it = cellStructs.iterator();
                while (it.hasNext()) {
                    CellStruct cur = (CellStruct) it.next();
                    if (cur.tc.equals(tc)) {
                        setOrientation(parMo.key, cur);
                        return;
                    }
                }
                CellStruct cell = new CellStruct(tc);
                cellStructs.add(cell);
                cell.name = parMo.toString();
                cell.comp = new JLabel(cell.name, JLabel.CENTER);
                cell.comp.setForeground(parMo.getColor());
                setOrientation(parMo.key, cell);
            }
        }

        private void setOrientation(String key, CellStruct cell) {
            if (key.equals(DisplayUtils.NORTHEAST))
                cell.orientations[0] = Boolean.TRUE;
            else if (key.equals(DisplayUtils.UPNORTH))
                cell.orientations[1] = Boolean.TRUE;
            else
                cell.orientations[2] = Boolean.TRUE;
        }

        private void setColumnWidths() {
            int columnCount = getColumnCount();
            for (int counter = 0; counter < columnCount; counter++) {
                String columnName = getColumnName(counter);
                int width = 50;
                if (columnName.equals("Trace"))
                    width = 100;
                else
                    width = 20;
                getColumnModel().getColumn(counter).setPreferredWidth(width);
                getColumnModel().getColumn(counter).setMinWidth(width);
            }
        }

        private class ViewTableModel extends AbstractTableModel {
            public int getColumnCount() {
                return keys.length + 1;
            }

            public int getRowCount() {
                return cellStructs.size();
            }

            public String getColumnName(int columnIndex) {
                if (columnIndex > 0)
                    return keys[columnIndex - 1];
                else
                    return "Trace";
            }

            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return String.class;
                else
                    return Boolean.class;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                synchronized (cellStructs) {
                    if (cellStructs.size() > rowIndex)
                            if (columnIndex == 0)
                                return ((CellStruct) cellStructs.get(rowIndex)).name;
                            else
                                return ((CellStruct) cellStructs.get(rowIndex)).orientations[columnIndex - 1];
                }
                return null;
            }

            private String[] keys = { DisplayUtils.NORTHEAST,
                    DisplayUtils.UPNORTH, DisplayUtils.UPEAST };

        }

        private class CellStruct {
            public CellStruct(TimeConfig tc) {
                this.tc = tc;
            }

            public TimeConfig tc;

            public Boolean[] orientations = { Boolean.FALSE, Boolean.FALSE,
                    Boolean.FALSE };

            public JComponent comp;

            public String name;
        }

        private List cellStructs = new ArrayList();
    }
}// ParticleMotionView
