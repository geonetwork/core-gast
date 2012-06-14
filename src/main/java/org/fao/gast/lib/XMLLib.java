//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.gast.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

//=============================================================================

public class XMLLib
{
	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public Document load(String file) throws JDOMException, IOException
	{
		return load(new File(file));
	}

	//---------------------------------------------------------------------------

	public Document load(File file) throws JDOMException, IOException
	{
		SAXBuilder builder = new SAXBuilder();
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
		Document   jdoc    = builder.build(file);

		return jdoc;
	}

	//---------------------------------------------------------------------------

	public void save(String file, Document doc) throws FileNotFoundException, IOException
	{
		FileOutputStream os = new FileOutputStream(file);

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		try
		{
			outputter.output(doc, os);
		}
		finally
		{
			os.close();
		}
	}
}

//=============================================================================

