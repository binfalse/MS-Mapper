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

import java.io.IOException;



/**
 * Aehm, jah.
 * 
 * @author Martin Scharm
 * 
 */
public class Mountain
{
	public double	meanRT, meanMZ, area;
	
	/**
	 * Nothing to see here...
	 * 
	 * @param fileContent
	 * @throws IOException
	 */
	public Mountain (String fileContent) throws IOException
	{
		String[] elements = fileContent.split ("\\s+");
		if (elements.length != 10)
			throw new IOException ("Don't understand: " + fileContent);
		
		meanRT = Double.parseDouble (elements[0]);
		meanMZ = Double.parseDouble (elements[3]);
		area = Double.parseDouble (elements[4]);
	}
}
