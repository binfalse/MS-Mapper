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
package de.binfalse.martin.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;



/**
 * Colorizes the map.
 * 
 * @author Martin Scharm
 *
 */
public class ColorModelMotley
		extends ColorModel
{

	/* (non-Javadoc)
	 * @see de.binfalse.martin.algorithm.ColorModel#getColor(double)
	 */
	@Override
	public void setColor (Graphics g, double v)
	{
		g.setColor (Color.getHSBColor ((float) ((200. / 360.) + (220. / 360.) * v / max), 1, (float) (v / max)));//new Color ((int) (255. * v / max), (int) (255. * v / max), (int) (255. * v / max)));
	}

	/* (non-Javadoc)
	 * @see de.binfalse.martin.tools.ColorModel#setBackground(java.awt.Graphics)
	 */
	@Override
	public void setBackground (Graphics g)
	{
		g.setColor (Color.BLACK);
	}
	
	/* (non-Javadoc)
	 * @see de.binfalse.martin.tools.ColorModel#createLegend(java.awt.Font, java.awt.FontMetrics, int, de.binfalse.martin.tools.Scaler)
	 */
	public BufferedImage createLegend (Font font, FontMetrics fm, int height, Scaler s)
	{
		double tmpmax = Math.ceil (s.unscale (max));
		double fthird = Math.ceil (s.unscale (max / 3.));
		double sthird = Math.ceil (s.unscale (2. * max / 3.));
		
		String maxtext = getLabel (tmpmax), fthirdtext = getLabel (fthird), sthirdtext = getLabel (sthird), mintext = "0";
		
		int width = fm.stringWidth (maxtext);
		if (width < fm.stringWidth (fthirdtext))
			width = fm.stringWidth (fthirdtext);
		if (width < fm.stringWidth (sthirdtext))
			width = fm.stringWidth (sthirdtext);
		width += 10;
		
		BufferedImage bi = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics ();
		
		g.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint (RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		for (int row = 0; row < height; row++)
		{
			setColor (g, max * ((double) (row)) / ((double) (height)));
			g.drawLine (0, height - row - 1, width - 1, height - row - 1);
		}
		
		g.setFont (font);
		g.setColor (Color.WHITE);
		g.drawString (mintext, width / 2 - fm.stringWidth (mintext) / 2, height - 4);
		g.setColor (Color.WHITE);
		g.drawString (fthirdtext, (width - fm.stringWidth (fthirdtext)) / 2, 2 * height / 3);
		g.setColor (Color.BLACK);
		g.drawString (sthirdtext, (width - fm.stringWidth (sthirdtext)) / 2, height / 3);
		g.setColor (Color.BLACK);
		g.drawString (maxtext, (width - fm.stringWidth (maxtext)) / 2, 3 + fm.getHeight ());
		
		return bi;
	}
	
}
