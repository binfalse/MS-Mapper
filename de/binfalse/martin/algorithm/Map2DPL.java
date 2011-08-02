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

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JProgressBar;

import de.binfalse.martin.ms.Mountain;
import de.binfalse.martin.ms.MountainList;
import de.binfalse.martin.tools.ColorModel;
import de.binfalse.martin.tools.Scaler;


/**
 * Go away little jedi.
 * 
 * @author Martin Scharm
 *
 */
public class Map2DPL
		extends Map2D
{
	private MountainList peakList;
	public Map2DPL (MountainList peakList)
	{
		this.peakList = peakList;
	}
	
	/* (non-Javadoc)
	 * @see de.binfalse.martin.algorithm.Map2D#createMap(int, int, de.binfalse.martin.algorithm.Scaler)
	 */
	@Override
	public BufferedImage createMap (int width, int height, double startTime,
			double endTime, double startMz, double endMz, Scaler s, ColorModel col, JProgressBar progressBar)
	{
		if (width < 200 || height < 200)
		{
			throw new IllegalArgumentException ("Map has to be at least 200 x 200 pixels");
		}
		if (startMz < 0 || endMz <= startMz)
		{
			throw new IllegalArgumentException ("endMz has to be bigger than startMz, both positive");
		}
		if (startTime < 0 || endTime <= startTime)
		{
			throw new IllegalArgumentException ("endTime has to be bigger than startTime, both positive");
		}
		
		double [][] matrix = new double [width][height];
		double max = 0;
		
		for (int i = 0; i < peakList.size (); i++)
		{
			Mountain m = peakList.get (i);
			if (m.meanRT < startTime || m.meanRT > endTime)
				continue;
			if (m.meanMZ < startMz || m.meanMZ > endMz)
				continue;
			
			int w = (int) (((double)width - 1.) * (m.meanRT - startTime) / (endTime - startTime));
			int h = matrix[w].length - 1 - (int) (((double)height - 1.) * (m.meanMZ - startMz) / (endMz - startMz));
			
			double obs = s.scale (m.area);
			
			if (matrix[w][h] > obs)
				continue;
			
			matrix[w][h] = obs;
			
			if (obs > max)
				max = obs;
		}
		
		col.setMax (max);
		
		
		BufferedImage bi = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics ();
		
		g.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint (RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		col.setBackground (g);
		g.fillRect (0, 0, width, height);
		
		for (int w = 0; w < matrix.length; w++)
			for (int h = 0; h < matrix[w].length; h++)
			{
				if (matrix[w][h] < 1)
					continue;
				col.setColor (g, matrix[w][h]);
				g.fillRect (w - 1, h - 1, 3, 3);
				//g.drawLine(w, h, w, h);
			}
		

		if (legend)
		{
			FontMetrics fm = g.getFontMetrics (font);
			bi = addLegend (bi, col.createLegend (font, fm, height, s), fm, startTime, endTime, startMz, endMz);
		}
		
		
		return bi;
	}
	
}
