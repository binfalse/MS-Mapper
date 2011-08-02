/*
 * MS-Mapper - generate 2D intensity maps of Mass-Spec data
 * Copyright (C) 2011 Martin Scharm
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.binfalse.martin.algorithm;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.JProgressBar;

import de.binfalse.martin.tools.ColorModel;
import de.binfalse.martin.tools.Scaler;


/**
 * The Class Map2D.
 *
 * @author Martin Scharm
 */
public abstract class Map2D
{
	protected Font font;
	protected Color bg;
	protected Color fg;
	protected boolean legend;
	
	/**
	 * Creates the map.
	 *
	 * @param width the width
	 * @param height the height
	 * @param startTime the start time  in sec- inclusive
	 * @param endTime the end time in sec - inclusive
	 * @param startMz the start mz - inclusive
	 * @param endMz the end mz - inclusive
	 * @param s the scaler
	 * @param col the col
	 * @return the buffered image
	 */
	public abstract BufferedImage createMap (int width, int height, double startTime, double endTime, double startMz, double endMz, Scaler s, ColorModel col, JProgressBar progressBar);
	
	/**
	 * Should we add a legend.
	 * 
	 * @param font the font to use for text in legend
	 * @param bg the background to use for the legends
	 */
	public void addLegend (Font font, Color bg, Color fg)
	{
		legend = true;
		if (font == null)
			font = new Font ("SansSerif", Font.PLAIN, 12);
		this.font = font;
		this.bg = bg;
		if (this.bg == null)
			this.bg = Color.WHITE;
		this.fg = fg;
		if (this.fg == null)
			this.fg = Color.BLACK;
	}
	
	/**
	 * Add a legend for rt and m/z.
	 * 
	 * @param picture the map, generated yet
	 * @param colorLegend the picture of the legend
	 * @param fm the font metrics of your font, so we can estimate our string recs
	 * @param startTime the start rt in sec
	 * @param endTime the end rt in sec
	 * @param startMz the min mz
	 * @param endMz the max mz
	 * @return
	 */
	protected BufferedImage addLegend (BufferedImage picture, BufferedImage colorLegend, FontMetrics fm, double startTime, double endTime, double startMz, double endMz)
	{
		String mz = "m/z", smz = Math.floor (startMz) + "", emz = Math.ceil (endMz) + "";
		
		// want min:sec
		int srti = (int) Math.floor (startTime / 60);
		int srtfrac = (int) ((startTime / 60. - Math.floor (startTime / 60)) * 60.);
		int erti = (int) Math.floor (endTime / 60);
		int ertfrac = (int) ((endTime / 60. - Math.floor (endTime / 60)) * 60.);
		
		String rt = "RT", srt = srti + ":" + (srtfrac < 10 ? "0" : "") + srtfrac, ert = erti + ":" + (ertfrac < 10 ? "0" : "") + ertfrac;
		
		int width = 5 + fm.getHeight () + 5 + picture.getWidth () + 5 + colorLegend.getWidth () + 5;
		int height = 5 + picture.getHeight () + 5 + fm.getHeight () + 5;
		

		BufferedImage bi = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics ();
		
		// rendering hints?
		g.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint (RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setFont (font);
		g.setColor (bg);
		g.fillRect (0, 0, width, height);
		
		g.setColor (fg);
		
		g.drawImage (picture, new RescaleOp (1, 0, null), 5 + fm.getHeight () + 5, 5);

		g.drawImage (colorLegend, new RescaleOp (1, 0, null), 5 + fm.getHeight () + 5 + picture.getWidth () + 5, 5);
		
		// bottom legend
		g.drawString (srt, 5 + fm.getHeight () + 5,  5 + picture.getHeight () + 5 + fm.getHeight ());
		g.drawString (rt, 5 + fm.getHeight () + 5 + picture.getWidth () / 2 - fm.stringWidth (rt) / 2,  5 + picture.getHeight () + 5 + fm.getHeight ());
		g.drawString (ert, 5 + fm.getHeight () + 5 + picture.getWidth () - fm.stringWidth (ert),  5 + picture.getHeight () + 5 + fm.getHeight ());

		// left legend
		g.rotate(-Math.PI / 2);
		
		g.drawString (smz, -(5 + picture.getHeight ()),  5 + fm.getHeight ());
		g.drawString (emz, -(5 + fm.stringWidth (emz)),  5 + fm.getHeight ());
		g.setFont (font.deriveFont (Font.ITALIC));
		g.drawString (mz, -(5 + picture.getHeight () / 2 + fm.stringWidth (mz) / 2),  5 + fm.getHeight ());
		g.setFont (font);
		
		g.rotate(Math.PI / 2);
		
		return bi;
	}
}
