
package edu.sc.seis.fissuresUtil.parmo3d;

/*
 *      @(#)Text2D.java 1.5 99/02/16 12:58:50
 *
 * Copyright (c) 1996-1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import javax.media.j3d.Appearance;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;


/**
 * This class creates a texture-mapped rectangle which displays the
 * text string sent in by the user, given the appearance parameters
 * also supplied by the user.  The size of the rectangle (and its
 * texture map) is determined by the font parameters passed in to
 * the constructor.
 * The resulting Shape3D object is a transparent (except for the text)
 * rectangle located at (0, 0, 0) and extending up the positive y-axis and
 * out the positive x-axis.
 */
public class Text2D extends Shape3D {

    // This table caches FontMetrics objects to avoid the huge cost
    // of re-retrieving metrics for a font we've already seen.
    private static Hashtable metricsTable = new Hashtable(); 
    float rectangleScaleFactor = 1f/256f;

    Point3f   position = new Point3f();
    Color3f   color = new Color3f();
    String    fontName = new String();
    int       fontSize, fontStyle;

    /**
     * Creates a Shape3D object which holds a
     * rectangle that is texture-mapped with an image that has
     * the specified text written with the specified font
     * parameters.
     *
     * @param text The string to be written into the texture map.
     * @param position The location of the text string.
     * @param color The color of the text string.
     * @param fontName The name of the Java font to be used for
     *  the text string.
     * @param fontSize The size of the Java font to be used.
     * @param fontStyle The style of the Java font to be used.
     */
    public Text2D(String text, Point3f position, Color3f color, String fontName,
		  int fontSize, int fontStyle) {

      this.color.set(color);
      this.fontName = fontName;
      this.fontSize = fontSize;
      this.fontStyle = fontStyle;
      this.position = position;

      updateText2D(text, position, color, fontName, fontSize, fontStyle);
    }

  /*
   * Changes text of this Text2D to 'text'. All other
   * parameters (color, fontName, fontSize, fontStyle
   * remain the same.
   * @param text The string to be set.
   */
      private void setString(String text){
    
          updateText2D(text, position, color, fontName, fontSize, fontStyle);
      }

    private void updateText2D(String text, Point3f position, Color3f color, String fontName,
		  int fontSize, int fontStyle) {
	BufferedImage bImage = setupImage(text, color, fontName,
					  fontSize, fontStyle);
	
	Texture2D t2d = setupTexture(bImage);
	
	QuadArray rect = setupGeometry(bImage.getWidth(),
				       bImage.getHeight(),
                                       position);
	
	Appearance appearance = setupAppearance(t2d);
	
	setGeometry(rect);
	setAppearance(appearance);
    }


    /**
     * Sets the scale factor used in converting the image width/height
     * to width/height values in 3D.
     *
     * @param newScaleFactor The new scale factor.
     */
    public void setRectangleScaleFactor(float newScaleFactor) {
	rectangleScaleFactor = newScaleFactor;
    }

    /**
     * Gets the current scale factor being used in converting the image
     * width/height to width/height values in 3D.
     *
     * @return The current scale factor.
     */
    public float getRectangleScaleFactor() {
	return rectangleScaleFactor;
    }
    
    /**
     * Create the ImageComponent and Texture object.
     */
    private Texture2D setupTexture(BufferedImage bImage) {

	ImageComponent imageComponent =
	    new ImageComponent2D(ImageComponent.FORMAT_RGBA, 
				 bImage);
	Texture2D t2d = new Texture2D(Texture2D.BASE_LEVEL,
				      Texture.RGBA,
				      bImage.getWidth(),
				      bImage.getHeight());
	t2d.setMinFilter(t2d.BASE_LEVEL_LINEAR);
	t2d.setMagFilter(t2d.BASE_LEVEL_LINEAR);
	t2d.setImage(0, imageComponent);
	t2d.setEnable(true);

	return t2d;
    }

    /**
     * Creates a BufferedImage of the correct dimensions for the
     * given font attributes.  Draw the given text into the image in
     * the given color.  The background of the image is transparent
     * (alpha = 0).
     */
    private BufferedImage setupImage(String text, Color3f color,
				     String fontName,
				     int fontSize, int fontStyle) {
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Font font = new java.awt.Font(fontName, fontStyle, fontSize);

	FontMetrics metrics;
	if ((metrics = (FontMetrics)metricsTable.get(font)) == null) {
	    metrics = toolkit.getFontMetrics(font);
	    metricsTable.put(font, metrics);
	}
	int width = metrics.stringWidth(text);
	int descent = metrics.getMaxDescent();
	int ascent = metrics.getMaxAscent();
	int leading = metrics.getLeading();
	int height = descent + ascent;

	// Need to make width/height powers of 2 because of Java3d texture
	// size restrictions
	int pow = 1;
	for (int i = 1; i < 32; ++i) {
	    pow *= 2;
	    if (width <= pow)
		break;
	}
	width = Math.max (width, pow);
	pow = 1;
	for (int i = 1; i < 32; ++i) {
	    pow *= 2;
	    if (height <= pow)
		break;
	}
	height = Math.max (height, pow);

	// For now, jdk 1.2 only handles ARGB format, not the RGBA we want
	BufferedImage bImage = new BufferedImage(width, height,
						 BufferedImage.TYPE_INT_ARGB);
	Graphics offscreenGraphics = bImage.createGraphics();

	// First, erase the background to the text panel - set alpha to 0
	Color myFill = new Color(0f, 0f, 0f, 0f);
	offscreenGraphics.setColor(myFill);
	offscreenGraphics.fillRect(0, 0, width, height);

	// Next, set desired text properties (font, color) and draw String
	offscreenGraphics.setFont(font);
	Color myTextColor = new Color(color.x, color.y, color.z, 1f);
	offscreenGraphics.setColor(myTextColor);
	offscreenGraphics.drawString(text, 0, height - descent);

	return bImage;
    }

    /**
     * Creates a rectangle of the given width and height and sets up
     * texture coordinates to map the text image onto the whole surface
     * of the rectangle (the rectangle is the same size as the text image)
     */
    private QuadArray setupGeometry(int width, int height, Point3f position) {
        float[] loc = {0f,0f,0f};
        position.get(loc);
	float xPosition = loc[0];
	float yPosition = loc[1];
	float zPosition = loc[2];
	float rectWidth = (float)width * rectangleScaleFactor;
	float rectHeight = (float)height * rectangleScaleFactor;
	float[] verts1 = {
	    xPosition + rectWidth, yPosition, zPosition,
	    xPosition + rectWidth, yPosition + rectHeight, zPosition,
	    xPosition, yPosition + rectHeight, zPosition,
	    xPosition, yPosition, zPosition
	};
	float[] texCoords = {
	    0f, -1f,
	    0f, 0f,
	    (-1f), 0f,
	    (-1f), -1f
	};
	
	QuadArray rect = new QuadArray(4, QuadArray.COORDINATES |
				       QuadArray.TEXTURE_COORDINATE_2);
	rect.setCoordinates(0, verts1);
	rect.setTextureCoordinates(0, texCoords);
	
	return rect;
    }

    /**
     * Creates Appearance for this Shape3D.  This sets transparency
     * for the object (we want the text to be "floating" in space,
     * so only the text itself should be non-transparent.  Also, the
     * appearance disables lighting for the object; the text will
     * simply be colored, not lit.
     */
    private Appearance setupAppearance(Texture2D t2d) {
	TransparencyAttributes transp = new TransparencyAttributes();
	transp.setTransparencyMode(TransparencyAttributes.BLENDED);
	transp.setTransparency(0f);
	Appearance appearance = new Appearance();
	appearance.setTransparencyAttributes(transp);
	appearance.setTexture(t2d);

	Material m = new Material();
	m.setLightingEnable(false);
	appearance.setMaterial(m);
	
	return appearance;
    } 	
	
    
}





