/**
 * UpdateCheckerJob.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.fissuresUtil.chooser;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import com.isti.util.updatechecker.LocationUpdate;
import com.isti.util.updatechecker.UpdateAction;
import com.isti.util.updatechecker.UpdateInformation;
import com.isti.util.updatechecker.XMLUpdateCheckerClient;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class UpdateCheckerJob extends AbstractJob {

    public UpdateCheckerJob(String displayName,
                            String programName,
                            String version,
                            String updateURL,
                            boolean gui) {
        this(displayName, programName, version, updateURL, gui, false);
    }

    /**
     * @param forceCheck
     *            overides the users "don't bother me until..." setting in the
     *            Java Preferences. Usually this should be false, but is useful
     *            for testing.
     */
    public UpdateCheckerJob(String displayName,
                            String programName,
                            String version,
                            String updateURL,
                            boolean gui,
                            boolean forceCheck) {
        super(displayName);
        this.programName = programName;
        this.updateURL = updateURL;
        this.isGui = gui;
        this.forceCheck = forceCheck;
        this.version = version;
        prefs = Preferences.userNodeForPackage(this.getClass());
        this.prefsName = programName + "_" + NEXT_CHECK_DATE;
    }

    public void runJob() {
        // only check if have not yet checked, or if forceCheck is true
        if(!forceCheck && checkedYet) {
            setFinished();
            return;
        }
        checkedYet = true;
        boolean checkNeeded = true;
        prefs = prefs.node("UpdateCheckerTask");
        MicroSecondDate now = ClockUtil.now();
        String nextCheckDate = prefs.get(prefsName, now.subtract(SIX_HOUR)
                .getFissuresTime().date_time);
        MicroSecondDate date = new MicroSecondDate(new Time(nextCheckDate, -1));
        if(date.after(now) && !forceCheck) {
            // don't check
            logger.debug("no updated wanted until " + date);
            setFinished(true);
            return;
        }
        setStatus("Connect to server");
        XMLUpdateCheckerClient updateChecker = new XMLUpdateCheckerClient(version,
                                                                          updateURL);
        setStatus("Check for update");
        if(updateChecker.isUpdateAvailable()) {
            UpdateInformation[] updates = updateChecker.getUpdates();
            logger.info("our version is " + version + ", update version is "
                    + updates[updates.length - 1].getVersion());
            UpdateAction[] actions = updates[updates.length - 1].getUpdateActions();
            LocationUpdate locationUpdate = (LocationUpdate)actions[0];
            try {
                if(isGui) {
                    handleUpdateGUI(locationUpdate);
                } else {
                    handleUpdateNonGUI(locationUpdate);
                }
            } catch(BackingStoreException e) {
                GlobalExceptionHandler.handle("trouble flushing preferences for updatechecker",
                                              e);
            }
        } else if(showNoUpdate) {
            JOptionPane.showMessageDialog(null,
                                          "No update is available",
                                          "Update Check",
                                          JOptionPane.INFORMATION_MESSAGE);
            logger.info("No update is available");
        }
        setFinished();
    }

    protected void handleUpdateGUI(LocationUpdate locationUpdate)
            throws BackingStoreException {
        Object[] options = new String[3];
        options[0] = "Go To Update Page";
        options[1] = "Remind in a fortnight";
        options[2] = "Remind in a month";
        int n = JOptionPane.showOptionDialog(null,
                                             "An updated version of "
                                                     + programName
                                                     + " is available!\nPlease go to\n"
                                                     + locationUpdate.getLocation()
                                                     + "\nto get the latest version.",
                                             "An updated version of "
                                                     + programName
                                                     + " is available!",
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null, // don't use a custom Icon
                                             options, // the titles of buttons
                                             options[0]); // default button
                                                            // title
        logger.debug("return val is " + n);
        TimeInterval nextInterval = SIX_HOUR;
        if(n == JOptionPane.YES_OPTION) {
            logger.debug("Opening browser");
            setStatus("Opening browser");
            locationUpdate.run();
        } else if(n == 1) {
            nextInterval = FORTNIGHT;
        } else if(n == 2) {
            nextInterval = MONTH;
        }
        MicroSecondDate nextCheck = ClockUtil.now().add(nextInterval);
        prefs.put(prefsName, nextCheck.getFissuresTime().date_time);
        logger.debug("no update check wanted for " + nextInterval
                + ", next at " + nextCheck);
        prefs.flush();
        logger.debug("done flushing prefs");
    }

    protected void handleUpdateNonGUI(LocationUpdate locationUpdate)
            throws BackingStoreException {
        System.err.println("*******************************************************");
        System.err.println();
        System.err.println("An updated version of " + programName
                + " is available!");
        System.err.println("Please go to " + locationUpdate.getLocation()
                + " to get the latest version.");
        System.err.println();
        System.err.println("*******************************************************");
        prefs.put(prefsName,
                  ClockUtil.now().add(SIX_HOUR).getFissuresTime().date_time);
        prefs.flush();
    }

    protected final TimeInterval SIX_HOUR = new TimeInterval(6, UnitImpl.HOUR);

    protected final TimeInterval FORTNIGHT = new TimeInterval(14, UnitImpl.DAY);

    protected final TimeInterval MONTH = new TimeInterval(30, UnitImpl.DAY);

    protected String prefsName;

    protected String version;

    protected String programName;

    protected boolean forceCheck;

    protected boolean showNoUpdate = false;

    protected boolean isGui;

    static boolean checkedYet = false;

    protected String updateURL;

    protected Preferences prefs;

    static final String NEXT_CHECK_DATE = "nextCheckDate";

    private static final Logger logger = Logger.getLogger(UpdateCheckerJob.class);
}
