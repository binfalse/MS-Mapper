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


/**
 * Don't scale anything
 * 
 * @author Martin Scharm
 *
 */
public class ScalerNone extends Scaler
{

	/* (non-Javadoc)
	 * @see de.binfalse.martin.tools.Scaler#scale(double)
	 */
	@Override
	public double scale (double s)
	{
		return s;
	}

	/* (non-Javadoc)
	 * @see de.binfalse.martin.tools.Scaler#unscale(double)
	 */
	@Override
	public double unscale (double s)
	{
		return s;
	}
	
}
