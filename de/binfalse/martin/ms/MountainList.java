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
package de.binfalse.martin.ms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Nothing to see here. This is nothing you should be interested in..
 * Just take a look at cooler stuff.
 * 
 * @author Martin Scharm
 * 
 */
public class MountainList
{
	/**
	 * The mountains.
	 */
	private Vector<Mountain> mountains;
	
	/**
	 * A new instance.
	 * 
	 * @param file file to read from
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public MountainList (File file) throws NumberFormatException, IOException
	{
		mountains = new Vector<Mountain> ();
		boolean cont = true;
		BufferedReader br = new BufferedReader (new FileReader (file));
		while (br.ready ())
		{
			String line = br.readLine ();

			if (line.startsWith ("#"))
				continue;
			if (cont)
			{
				cont = false;
				continue;
			}
			mountains.add (new Mountain (line));
		}
		br.close ();
	}
	
	/**
	 * Get number of mountains.
	 * 
	 * @return number of mountains
	 */
	public int size ()
	{
		return mountains.size ();
	}
	
	/**
	 * Get a single mountain.
	 * 
	 * @param m the no.
	 * @return the mountain
	 */
	public Mountain get (int m)
	{
		return mountains.elementAt (m);
	}
	
}
