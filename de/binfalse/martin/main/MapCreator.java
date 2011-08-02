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
package de.binfalse.martin.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.systemsbiology.jrap.stax.MSXMLParser;

import de.binfalse.martin.algorithm.Map2D;
import de.binfalse.martin.algorithm.Map2DMZXML;
import de.binfalse.martin.algorithm.Map2DPL;
import de.binfalse.martin.ms.MountainList;
import de.binfalse.martin.tools.ColorModel;
import de.binfalse.martin.tools.ColorModelGray;
import de.binfalse.martin.tools.ColorModelHeat;
import de.binfalse.martin.tools.ColorModelMotley;
import de.binfalse.martin.tools.Scaler;
import de.binfalse.martin.tools.ScalerLog;
import de.binfalse.martin.tools.ScalerNone;
import de.binfalse.martin.tools.ScalerSQRT;


/**
 * The CLI.
 * 
 * @author Martin Scharm
 *
 */
public class MapCreator
{
	
	/**
	 * Entry.
	 * 
	 * @param args arguments
	 * @throws IOException 
	 */
	public static void main (String[] args) throws IOException
	{
		boolean help = false, legend = false;
		Vector<String> err = new Vector<String> ();
		String infile = null, outfile = null;
		int width = 500, height = 500;
		double startTime = 0, endTime = 60*60, startMz = 0, endMz = 1000;
		Scaler scaler = null;
		ColorModel cm = null;
		Map2D mapper = null;
		BufferedImage bi;
		
		for (int i = 0; i < args.length; i++)
		{
			if (i < args.length - 1)
			{
				if (args[i].equals ("--stime"))
				{
					startTime = Double.parseDouble (args[++i]);
					continue;
				}
				if (args[i].equals ("--etime"))
				{
					endTime = Double.parseDouble (args[++i]);
					continue;
				}
				
				if (args[i].equals ("--smz"))
				{
					startMz = Double.parseDouble (args[++i]);
					continue;
				}
				if (args[i].equals ("--emz"))
				{
					endMz = Double.parseDouble (args[++i]);
					continue;
				}
				
				if (args[i].equals ("--in"))
				{
					infile = args[++i];
					continue;
				}
				if (args[i].equals ("--out"))
				{
					outfile = args[++i];
					continue;
				}
				if (args[i].equals ("--width"))
				{
					width = Integer.parseInt (args[++i]);
					continue;
				}
				if (args[i].equals ("--height"))
				{
					height = Integer.parseInt (args[++i]);
					continue;
				}
			}
			if (args[i].equals ("--help") || args[i].equals ("-h"))
			{
				help = true;
				continue;
			}
			if (args[i].equals ("--motley"))
			{
				cm = new ColorModelMotley ();
				continue;
			}
			if (args[i].equals ("--heat"))
			{
				cm = new ColorModelHeat ();
				continue;
			}
			if (args[i].equals ("--gray"))
			{
				cm = new ColorModelGray ();
				continue;
			}
			if (args[i].equals ("--log"))
			{
				scaler = new ScalerLog ();
				continue;
			}
			if (args[i].equals ("--sqrt"))
			{
				scaler = new ScalerSQRT ();
				continue;
			}
			if (args[i].equals ("--noscale"))
			{
				scaler = new ScalerNone ();
				continue;
			}
			if (args[i].equals ("--legend"))
			{
				legend = true;
				continue;
			}
			err.add ("Don't understand " + args[i]);
		}
		
		// checks
		
		if (infile == null)
		{
			err.add ("please specify an file to read from");
		}
		else if (!new File (infile).canRead ())
		{
			err.add ("can't read " + infile);
		}
		else
		{
			String ext = infile.substring (infile.lastIndexOf(".") + 1);
			if (ext.equals ("mzxml"))
				mapper = new Map2DMZXML (new MSXMLParser (infile, true));
			else if (ext.equals ("pl"))
				mapper = new Map2DPL (new MountainList (new File (infile)));
			else
				err.add ("Don't know file format");
		}
		
		if (outfile == null)
		{
			err.add ("please specify an file to write to");
		}
		else if (new File (outfile).exists ())
		{
			err.add (outfile + " exists, we stop here to prevent loss of data...");
		}
		

		if (width < 200 || height < 200)
		{
			err.add ("Map has to be at least 200 x 200 pixels");
		}
		if (startMz < 0 || endMz <= startMz)
		{
			err.add ("endMz has to be bigger than startMz, both positive");
		}
		if (startTime < 0 || endTime <= startTime)
		{
			err.add ("endTime has to be bigger than startTime, both positive");
		}
		
		
		if (err.size () > 0 || help)
		{
			if (err.size () > 0)
			{
				System.err.println ("Following errors occoured:");
				for (int i = 0; i < err.size (); i++)
					System.err.println ("\t-> " + err.elementAt (i));
				System.err.println ();
			}
			System.out.println ("Arguments:");
			System.out.println ("I/O:");
			System.out.println ("\t--in\t\tfile to read");
			System.out.println ("\t--out\t\there we'll store the pic, only png supported yet");
			System.out.println ("Threshs:");
			System.out.println ("\t--stime\t\tstart retention time in secs - defaults to 0");
			System.out.println ("\t--etime\t\tend retention time in secs - defaults to 3600");
			System.out.println ("\t--smz\t\tmin mz - defaults to 0");
			System.out.println ("\t--emz\t\tmax mz - defaults to 1000");
			System.out.println ("Map settings:");
			System.out.println ("\t--motley\tcolorize the map - default");
			System.out.println ("\t--gray\t\tgray image map");
			System.out.println ("\t--legend\tadd a legend");
			System.out.println ("Scale settings:");
			System.out.println ("\t--log\t\tscale intensities with nat. log - default");
			System.out.println ("\t--sqrt\t\tscale intensities by sqare root");
			System.out.println ("\t--noscale\tdon't scale intensities");
			System.out.println ("help:");
			System.out.println ("\t--help\t\tprint this help message");
			
			System.exit (1);
		}
		
		if (cm == null)
			cm = new ColorModelMotley ();
		if (scaler == null)
			scaler = new ScalerLog ();
		
		if (legend)
			mapper.addLegend (null, null, null);
		
		bi = mapper.createMap (width, height, startTime, endTime, startMz, endMz, scaler, cm, null);
		
		if (bi != null)
		{
			ImageIO.write(bi, "png", new File (outfile));
		}
		else
			System.err.println ("While creating the map something went wrong");
	}
	
}
