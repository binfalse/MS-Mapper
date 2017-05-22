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
package de.binfalse.martin.x;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.systemsbiology.jrap.stax.MSXMLParser;

import de.binfalse.martin.algorithm.Map2D;
import de.binfalse.martin.algorithm.Map2DMZXML;
import de.binfalse.martin.tools.ColorModel;
import de.binfalse.martin.tools.ColorModelGray;
import de.binfalse.martin.tools.ColorModelHeat;
import de.binfalse.martin.tools.ColorModelMotley;
import de.binfalse.martin.tools.Scaler;
import de.binfalse.martin.tools.ScalerLog;
import de.binfalse.martin.tools.ScalerNone;
import de.binfalse.martin.tools.ScalerSQRT;


/**
 * The graphical user interface for this tool.
 * 
 * @author Martin Scharm
 *
 */
public class MapCreatorGui extends javax.swing.JFrame
{
	private static final long	serialVersionUID	= 1L;
	private javax.swing.JButton jButtonInput;
  private javax.swing.JButton jButtonOutput;
  private javax.swing.JButton jButtonRun;
  private javax.swing.JCheckBox jCheckBoxLegend;
  private javax.swing.JComboBox jComboBoxColor;
  private javax.swing.JComboBox jComboBoxScale;
  private javax.swing.JLabel jLabelScale;
  private javax.swing.JLabel jLabelColor;
  private javax.swing.JLabel jLabelEndTime;
  private javax.swing.JLabel jLabelInput;
  private javax.swing.JLabel jLabelMaxMz;
  private javax.swing.JLabel jLabelMinMz;
  private javax.swing.JLabel jLabelMapWidth;
  private javax.swing.JLabel jLabelMapHeight;
  private javax.swing.JLabel jLabelOutput;
  private javax.swing.JLabel jLabelStartTime;
  private javax.swing.JSeparator jSeparatorBtm;
  private javax.swing.JTextField jTextFieldEndTime;
  private javax.swing.JTextField jTextFieldInput;
  private javax.swing.JTextField jTextFieldMaxMz;
  private javax.swing.JTextField jTextFieldMinMz;
  private javax.swing.JTextField jTextFieldMapWidth;
  private javax.swing.JTextField jTextFieldMapHeight;
  private javax.swing.JTextField jTextFieldOutput;
  private javax.swing.JTextField jTextFieldStartTime;
  private javax.swing.JProgressBar progress;
  private Link jLabelLink;
	
  
  /**
   * Create a new instance.
   */
  public MapCreatorGui ()
  {
  	init ();
  }
  
  /**
   * Check the input and run.
   */
  private void run ()
  {
  	// test values and create objects
  	
		Vector<String> err = new Vector<String> ();
		
		String outfile = jTextFieldOutput.getText ();
		if (outfile.length () < 1)
		{
			err.add ("no file to write to");
		}
		else if (new File (outfile).exists ())
		{
			int n = javax.swing.JOptionPane.showConfirmDialog(this,
			    "File exists, are you sure you want to overwrite?",
			    "File exists", javax.swing.JOptionPane.YES_NO_OPTION);
			if (n != javax.swing.JOptionPane.YES_OPTION)
				err.add ("Died because " + outfile + " exists");
		}
		
		String infile = jTextFieldInput.getText ();
		Map2D mapper = null;
		try
		{
			if (infile.length () < 1)
			{
				err.add ("no file to read");
			}
			else if (!new File (infile).canRead ())
			{
				err.add ("can't read " + infile);
			}
			else
			{
				try
				{
					mapper = new Map2DMZXML (new MSXMLParser (infile, true));
				}
				catch (NoClassDefFoundError e)
				{
					 System.err.println( e );
					e.printStackTrace();
					javax.swing.JOptionPane.showMessageDialog(this, "Could not find Parser, please check your CLASSPATH " + e.getMessage (),
					    "NoClassDefFoundError", javax.swing.JOptionPane.ERROR_MESSAGE);
					err.add ("Could not find Parser, please check your CLASSPATH");
				}
				if (jCheckBoxLegend.isSelected ())
					mapper.addLegend (null, null, null);
			}
		}
		catch (IOException e)
		{
			err.add ("Failed to read: " + infile + " (" + e.getMessage () + ")");
		}
		Scaler scaler = null;
		ColorModel cm = null;
		
		if (((String) jComboBoxColor.getSelectedItem ()).equals ("gray"))
			cm = new ColorModelGray ();
		if (((String) jComboBoxColor.getSelectedItem ()).equals ("heat"))
			cm = new ColorModelHeat ();
		else
			cm = new ColorModelMotley ();
		
		if (((String) jComboBoxScale.getSelectedItem ()).equals ("none"))
			scaler = new ScalerNone ();
		else if (((String) jComboBoxScale.getSelectedItem ()).equals ("square root"))
			scaler = new ScalerSQRT ();
		else
			scaler = new ScalerLog ();
		
		int width = 0;
		int height = 0;
		double startTime = 0, endTime = 0, startMz = 0, endMz = 0;
		try
		{
			width = Integer.parseInt (jTextFieldMapWidth.getText ());
		}
		catch (NumberFormatException e)
		{
			err.add ("Width has to be an Integer");
		}
		try
		{
			height = Integer.parseInt (jTextFieldMapHeight.getText ());
		}
		catch (NumberFormatException e)
		{
			err.add ("Height has to be an Integer");
		}
		try
		{
			startTime = Double.parseDouble (jTextFieldStartTime.getText ());
		}
		catch (NumberFormatException e)
		{
			err.add ("Start time has to be of type Double");
		}
		try
		{
			endTime = Double.parseDouble (jTextFieldEndTime.getText ());
		}
		catch (NumberFormatException e)
		{
			err.add ("End time has to be of type Double");
		}
		try
		{
			startMz = Double.parseDouble (jTextFieldMinMz.getText ());
		}
		catch (NumberFormatException e)
		{
			err.add ("Min m/z has to be of type Double");
		}
		try
		{
			endMz = Double.parseDouble (jTextFieldMaxMz.getText ());
		}
		catch (NumberFormatException e)
		{
			err.add ("Max m/z has to be of type Double");
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
		
		
		if (err.size () > 0)
		{
			String errors = "Following errors occoured:\n\n";
			System.err.println ("Following errors occoured:");
			for (int i = 0; i < err.size (); i++)
			{
				System.err.println ("\t-> " + err.elementAt (i));
				errors += err.elementAt (i) + "\n";
			}
			System.err.println ();

			javax.swing.JOptionPane.showMessageDialog(this, errors,
			    "Error processing", javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		run (mapper, width, height, startTime, endTime, startMz, endMz, scaler, cm, outfile);
  }
  
  /**
   * Run the stuff in a thread.
   * 
   * @param mapper the mapper
   * @param width image width
   * @param height image height
   * @param startTime start rt
   * @param endTime end rt
   * @param startMz min m/z
   * @param endMz max m/z
   * @param scaler the scale instance
   * @param cm the color model instance
   * @param outfile the file to save to
   */
  private void run (final Map2D mapper, final int width, final int height,
  		final double startTime, final double endTime, final double startMz,
  		final double endMz, final Scaler scaler, final ColorModel cm,
  		final String outfile)
  {
		jButtonRun.setEnabled (false);
		SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void> ()
		{
			public BufferedImage doInBackground ()
			{
				return mapper.createMap (width, height, startTime, endTime, startMz, endMz, scaler, cm, progress);
			}
			public void done ()
			{
				BufferedImage bi;
				try
				{
					bi = get ();
						
					if (bi != null)
					{
						try
						{
							javax.imageio.ImageIO.write(bi, "png", new File (outfile));
						}
						catch (IOException e)
						{
							e.printStackTrace();
							javax.swing.JOptionPane.showMessageDialog(MapCreatorGui.this, "Error saving image: " + e.getMessage (),
							    "Error saving image", javax.swing.JOptionPane.ERROR_MESSAGE);
						}
					}
					else
						System.err.println ("While creating the map something went wrong");
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					javax.swing.JOptionPane.showMessageDialog(MapCreatorGui.this, "Error threading: " + e.getMessage (),
					    "Error threading", javax.swing.JOptionPane.ERROR_MESSAGE);
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
					javax.swing.JOptionPane.showMessageDialog(MapCreatorGui.this, "Error executing: " + e.getMessage (),
					    "Error executing", javax.swing.JOptionPane.ERROR_MESSAGE);
				}
				progress.setValue (progress.getMaximum ());
			}
		};
		worker.execute ();
		jButtonRun.setEnabled (true);
  }
  
  /**
   * Choose an input file.
   */
  private void chooseInput ()
  {
		javax.swing.JFileChooser fc = new javax.swing.JFileChooser (".");
		
		fc.setFileFilter (new javax.swing.filechooser.FileFilter ()
		{
			
			public boolean accept (java.io.File f)
			{
				return f.getName ().toLowerCase ().endsWith (".mzxml")
						|| f.isDirectory ();
			}
			

			public String getDescription ()
			{
				return "MZXML (*.mzxml)";
			}
		});

		int returnVal = fc.showOpenDialog (this);
		if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION)
		{
			java.io.File mzxmlFile = fc.getSelectedFile ();
			if (!mzxmlFile.exists ())
			{
				javax.swing.JOptionPane.showMessageDialog(this, "File not found...",
				    "File not found", javax.swing.JOptionPane.ERROR_MESSAGE);
				return;
			}
			jTextFieldInput.setText (mzxmlFile.getAbsolutePath ());
		}
  }
  
  /**
   * Choose an output file. 
   */
  private void chooseOutput ()
  {
		javax.swing.JFileChooser fc = new javax.swing.JFileChooser (".");
		String name = jTextFieldInput.getText ();
		if (name.length () > 0)
		{
			fc.setSelectedFile (new java.io.File (name + ".png"));
		}
		
		fc.setFileFilter (new javax.swing.filechooser.FileFilter ()
		{
			
			public boolean accept (java.io.File f)
			{
				return f.getName ().toLowerCase ().endsWith (".png")
						|| f.isDirectory ();
			}
			

			public String getDescription ()
			{
				return "PNG images (*.png)";
			}
		});
  	
		int returnVal = fc.showSaveDialog (this);
		if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION)
		{
			java.io.File imageFile = fc.getSelectedFile ();
			jTextFieldOutput.setText (imageFile.getAbsolutePath ());
		}
  }
  
  /**
   * Initialize the graphical components.
   */
  private void init ()
  {
  	setLocationByPlatform (true);
  	this.setTitle ("MS-Mapper - by Martin Scharm");
    setDefaultCloseOperation (javax.swing.WindowConstants.EXIT_ON_CLOSE);
    
    jLabelInput = new javax.swing.JLabel ("Input");
    jLabelOutput = new javax.swing.JLabel ("Output");
    jLabelColor = new javax.swing.JLabel ("Color");
    jLabelScale = new javax.swing.JLabel ("Scale");
    jLabelStartTime = new javax.swing.JLabel ("start time");
    jLabelEndTime = new javax.swing.JLabel ("end time");
    jLabelMinMz = new javax.swing.JLabel ("min m/z");
    jLabelMaxMz = new javax.swing.JLabel ("max m/z");
    jLabelMapWidth = new javax.swing.JLabel ("map width");
    jLabelMapHeight = new javax.swing.JLabel ("map height");
    jLabelLink = new Link ("http://binfalse.de");
    jCheckBoxLegend = new javax.swing.JCheckBox ("add legend");
    jButtonInput = new javax.swing.JButton ("choose");
    jButtonOutput = new javax.swing.JButton ("choose");
    jButtonRun = new javax.swing.JButton ("create map");
    jSeparatorBtm = new javax.swing.JSeparator ();
    progress = new javax.swing.JProgressBar ();
  	
    // defaults
    (jTextFieldInput = new javax.swing.JTextField ()).setEditable (false);
    (jTextFieldOutput = new javax.swing.JTextField ()).setEditable (false);
    jTextFieldStartTime = new javax.swing.JTextField ("0");
    jTextFieldEndTime = new javax.swing.JTextField ("3600");
    jTextFieldMinMz = new javax.swing.JTextField ("0");
    jTextFieldMaxMz = new javax.swing.JTextField ("1000");
    jTextFieldMapWidth = new javax.swing.JTextField ("500");
    jTextFieldMapHeight = new javax.swing.JTextField ("500");
    
    jComboBoxColor = new javax.swing.JComboBox ();
    jComboBoxScale = new javax.swing.JComboBox ();
    jComboBoxColor.setModel (new javax.swing.DefaultComboBoxModel (new String[] { "heat", "motley", "gray" }));
    jComboBoxScale.setModel (new javax.swing.DefaultComboBoxModel (new String[] { "natural logaritm", "square root", "none" }));

    jButtonRun.addActionListener (new java.awt.event.ActionListener ()
    { 
    	public void actionPerformed (java.awt.event.ActionEvent evt)
    	{ run (); }
    });

    jButtonInput.addActionListener (new java.awt.event.ActionListener ()
    { 
    	public void actionPerformed (java.awt.event.ActionEvent evt)
    	{ chooseInput (); }
    });

    jButtonOutput.addActionListener (new java.awt.event.ActionListener ()
    { 
    	public void actionPerformed (java.awt.event.ActionEvent evt)
    	{ chooseOutput (); }
    });
    
    
    

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jCheckBoxLegend)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(jLabelColor)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBoxColor, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabelStartTime)
                                .addComponent(jLabelMinMz)
                                .addComponent(jLabelMapWidth))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextFieldMinMz, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(jTextFieldMapWidth, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(jTextFieldStartTime))))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabelEndTime)
                                .addComponent(jLabelScale)
                                .addComponent(jLabelMaxMz)
                                .addComponent(jLabelMapHeight))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jComboBoxScale, 0, 125, Short.MAX_VALUE)
                                .addComponent(jTextFieldEndTime, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(jTextFieldMaxMz, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(jTextFieldMapHeight, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)))))
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabelOutput)
                        .addComponent(jLabelInput))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jTextFieldInput, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonInput))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jTextFieldOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonOutput))))
                .addComponent(jSeparatorBtm, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jLabelLink)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 200, Short.MAX_VALUE)
                    .addComponent(jButtonRun))
                .addComponent(progress, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelInput)
                .addComponent(jTextFieldInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButtonInput))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelOutput)
                .addComponent(jTextFieldOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButtonOutput))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelColor)
                .addComponent(jLabelScale)
                .addComponent(jComboBoxScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jComboBoxColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldStartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelStartTime)
                .addComponent(jLabelEndTime)
                .addComponent(jTextFieldEndTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldMinMz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelMinMz)
                .addComponent(jLabelMaxMz)
                .addComponent(jTextFieldMaxMz, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldMapWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelMapWidth)
                .addComponent(jLabelMapHeight)
                .addComponent(jTextFieldMapHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jCheckBoxLegend)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelLink)
                .addComponent(jButtonRun))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jSeparatorBtm, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    pack();
    
  }
  
	/**
	 * Entry.
	 * 
	 * @param args arguments
	 */
	public static void main (String[] args)
	{
    java.awt.EventQueue.invokeLater (new Runnable ()
    {
        public void run ()
        {
            new MapCreatorGui ().setVisible (true);
        }
    });
	}
	
}
