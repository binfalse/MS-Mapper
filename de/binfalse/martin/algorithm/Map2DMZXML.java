/*
 * MS-Mapper - generate 2D intensityu maps of MAss-Spec data
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;

import org.systemsbiology.jrap.stax.MSXMLParser;

import de.binfalse.martin.tools.ColorModel;
import de.binfalse.martin.tools.Scaler;


/**
 * Create a map of a MZXML file.
 * 
 * @author Martin Scharm
 *
 */
public class Map2DMZXML
		extends Map2D
{
	/**
	 * The parser of sashimi.
	 */
	private MSXMLParser parser;
	
	/**
	 * A new instance.
	 * 
	 * @param parser parser of sashimi
	 */
	public Map2DMZXML (MSXMLParser parser)
	{
		this.parser = parser;
	}
	

	/**
	 * Parse the RT. Unfortunately I cannot trust sashimi here.. 
	 * 
	 * @param rt
	 *          retention time XML string
	 * @return double retention time in seconds
	 */
	private double parseRetentionTime (String rt)
	{
	Matcher m = Pattern.compile (
				"^[-P]+(([0-9.]+)Y)?(([0-9.]+)M)?(([0-9.]+)D)?"
						+ "(T(([0-9.]+)H)?(([0-9.]+)M)?(([0-9.]+)S)?)?$").matcher (rt);
		if (m.find ())
		{
			double dur = 0;
			if (m.group (13) != null)
				dur += Double.parseDouble (m.group (13)); // sec
			if (m.group (11) != null)
				dur += 60. * Double.parseDouble (m.group (11)); // min
			if (m.group (9) != null)
				dur += 3600. * Double.parseDouble (m.group (9)); // hour
			if (m.group (6) != null)
				dur += 86400. * Double.parseDouble (m.group (6)); // day
			if (m.group (4) != null)
				dur += 2592000. * Double.parseDouble (m.group (4)); // mon
			if (m.group (2) != null)
				dur += 920160000. * Double.parseDouble (m.group (2)); // year
			if (dur == 0)
				return -1;
			else
				return dur;
		}
		else
			return -1;
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
		
		int scanCount = parser.getScanCount ();
		if (progressBar != null)
		{
			progressBar.setMaximum (scanCount);
			progressBar.setValue (1);
		}
		for (int i = 0; i < scanCount; i++)
		{
			if (progressBar != null)
			{
				progressBar.setValue (i + 1);
				
			}
			double rt = parseRetentionTime (parser.nextHeader ().getRetentionTime ());
			if (rt < startTime || rt > endTime)
				continue;
			
			int w = (int) (((double)width - 1.) * (rt - startTime) / (endTime - startTime));
			double[][] mzi = parser.rap (i + 1).getMassIntensityList ();
			
			for (int p = 0; p < mzi[0].length; p++)
			{
				if (mzi[0][p] < startMz || mzi[0][p] > endMz)
					continue;
				
				double obs = s.scale (mzi[1][p]);
				int h = matrix[w].length - 1 - (int) (((double)height - 1.) * (mzi[0][p] - startMz) / (endMz - startMz));
				
				if (matrix[w][h] > obs)
					continue;
				
				matrix[w][h] = obs;
				
				if (obs > max)
					max = obs;
			}
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
				col.setColor (g, matrix[w][h]);
				g.drawLine(w, h, w, h);
			}
		
		
		
		if (legend)
		{
			FontMetrics fm = g.getFontMetrics (font);
			bi = addLegend (bi, col.createLegend (font, fm, height, s), fm, startTime, endTime, startMz, endMz);
		}
		
		
		return bi;
	}
	
}
