package edu.sc.seis.fissuresUtil.map.tools;
import java.awt.event.MouseEvent;
import com.bbn.openmap.proj.Proj;
import edu.sc.seis.fissuresUtil.map.OpenMap;



public class PanTool extends OpenMapTool{

	private OpenMap openMap;
	private String id = "pan";
	private int[] startXYCoords;
	private int[] endXYCoords;
	private int[] centerXYCoords;
	
	public PanTool(OpenMap om){
		openMap = om;
	}
	
	public String getID(){
		return id;
	}
	
	public void mousePressed(MouseEvent e){
		if (isActive()){
			super.mousePressed(e);
			startXYCoords = new int[]{e.getX(), e.getY()};
		}
	}
	
	public void mouseDragged(MouseEvent e){
		if (isActive()){
			translate(e);
			startXYCoords = endXYCoords;
		}
	}
	
	public void mouseReleased(MouseEvent e){
		if (isActive()){
			super.mouseReleased(e);
			translate(e);
		}
	}
	
	private void translate(MouseEvent e){
		Proj proj = (Proj)openMap.getMapBean().getProjection();
		endXYCoords = new int[]{e.getX(), e.getY()};
		int[] diff = new int[]{endXYCoords[0] - startXYCoords[0], endXYCoords[1] - startXYCoords[1]};
		centerXYCoords = new int[]{proj.getWidth()/2 - diff[0], proj.getHeight()/2 - diff[1]};
		openMap.getMapBean().setCenter(proj.inverse(centerXYCoords[0], centerXYCoords[1]));
	}
	
	
	/**
	 * Return a pretty name, suitable for the GUI.
	 */
	public String getPrettyName() {
		return "Pan";
	}
}
