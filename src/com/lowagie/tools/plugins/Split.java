/*
 * $Id: Split.java,v 1.10 2006/08/24 10:51:06 blowagie Exp $
 * $Name:  $
 *
 * Copyright 2005 by Bruno Lowagie
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.tools.plugins;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JInternalFrame;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.tools.arguments.FileArgument;
import com.lowagie.tools.arguments.LabelAccessory;
import com.lowagie.tools.arguments.PdfFilter;
import com.lowagie.tools.arguments.ToolArgument;

/**
 * This tool lets you split a PDF in two separate PDF files.
 */
public class Split extends AbstractTool {

	static {
		addVersion("$Id: Split.java,v 1.10 2006/08/24 10:51:06 blowagie Exp $");
	}
	/**
	 * Constructs an Split object.
	 */
	public Split() {
		FileArgument f = new FileArgument(this, "srcfile", "The file you want to split", false, new PdfFilter());
		f.setLabel(new LabelAccessory());
		arguments.add(f);
		arguments.add(new FileArgument(this, "destfile1", "The file to which the first part of the original PDF has to be written", true, new PdfFilter()));
		arguments.add(new FileArgument(this, "destfile2", "The file to which the second part of the original PDF has to be written", true, new PdfFilter()));
		arguments.add(new ToolArgument(this, "pagenumber", "The pagenumber where you want to split", String.class.getName()));
	}

	/**
	 * @see com.lowagie.tools.plugins.AbstractTool#createFrame()
	 */
	protected void createFrame() {
		internalFrame = new JInternalFrame("Split", true, false, true);
		internalFrame.setSize(300, 80);
		internalFrame.setJMenuBar(getMenubar());
		System.out.println("=== Split OPENED ===");
	}

	/**
	 * @see com.lowagie.tools.plugins.AbstractTool#execute()
	 */
	public void execute() {
        try {
			if (getValue("srcfile") == null) throw new InstantiationException("You need to choose a sourcefile");
			File src = (File)getValue("srcfile");
        	if (getValue("destfile1") == null) throw new InstantiationException("You need to choose a destination file for the first part of the PDF");
        	File file1 = (File)getValue("destfile1");
        	if (getValue("destfile2") == null) throw new InstantiationException("You need to choose a destination file for the second part of the PDF");
        	File file2 = (File)getValue("destfile2");
        	int pagenumber = Integer.parseInt((String)getValue("pagenumber"));

        	// we create a reader for a certain document
			PdfReader reader = new PdfReader(src.getAbsolutePath());
			// we retrieve the total number of pages
			int n = reader.getNumberOfPages();
			System.out.println("There are " + n + " pages in the original file.");

			if (pagenumber < 2 || pagenumber > n) {
				throw new DocumentException("You can't split this document at page " + pagenumber + "; there is no such page.");
			}

			// step 1: creation of a document-object
			Document document1 = new Document(reader.getPageSizeWithRotation(1));
			Document document2 = new Document(reader.getPageSizeWithRotation(pagenumber));
			// step 2: we create a writer that listens to the document
			PdfWriter writer1 = PdfWriter.getInstance(document1, new FileOutputStream(file1));
			PdfWriter writer2 = PdfWriter.getInstance(document2, new FileOutputStream(file2));
			// step 3: we open the document
			document1.open();
			PdfContentByte cb1 = writer1.getDirectContent();
			document2.open();
			PdfContentByte cb2 = writer2.getDirectContent();
			PdfImportedPage page;
			int rotation;
			int i = 0;
			// step 4: we add content
			while (i < pagenumber - 1) {
				i++;
				document1.setPageSize(reader.getPageSizeWithRotation(i));
				document1.newPage();
				page = writer1.getImportedPage(reader, i);
				rotation = reader.getPageRotation(i);
				if (rotation == 90 || rotation == 270) {
					cb1.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).height());
				}
				else {
					cb1.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
				}
			}
			while (i < n) {
				i++;
				document2.setPageSize(reader.getPageSizeWithRotation(i));
				document2.newPage();
				page = writer2.getImportedPage(reader, i);
				rotation = reader.getPageRotation(i);
				if (rotation == 90 || rotation == 270) {
					cb2.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).height());
				}
				else {
					cb2.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
				}
			}
			// step 5: we close the document
			document1.close();
			document2.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * @see com.lowagie.tools.plugins.AbstractTool#valueHasChanged(com.lowagie.tools.arguments.ToolArgument)
	 */
	public void valueHasChanged(ToolArgument arg) {
		if (internalFrame == null) {
			// if the internal frame is null, the tool was called from the commandline
			return;
		}
		// represent the changes of the argument in the internal frame
	}


    /**
     * Split a PDF in two separate PDF files.
     * @param args
     */
	public static void main(String[] args) {
    	Split tool = new Split();
    	if (args.length < 4) {
    		System.err.println(tool.getUsage());
    	}
    	tool.setArguments(args);
        tool.execute();
	}

	/**
	 * @see com.lowagie.tools.plugins.AbstractTool#getDestPathPDF()
	 */
	protected File getDestPathPDF() throws InstantiationException {
		return (File)getValue("destfile1");
	}
}
