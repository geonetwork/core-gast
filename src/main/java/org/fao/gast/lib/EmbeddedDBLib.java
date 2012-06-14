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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fao.gast.boot.Config;
import org.fao.gast.boot.Util;
import org.fao.geonet.util.McKoiDB;

//=============================================================================

public class EmbeddedDBLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public EmbeddedDBLib() throws IOException
	{
		lines = Lib.text.load(MCKOI_CONFIG);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getPort()
	{
		return Lib.text.getProperty(lines, "jdbc_server_port");
	}

	//---------------------------------------------------------------------------

	public String getUser()
	{
		try
		{
			List<String> lines = Lib.text.load(MCKOI_ACCOUNT);

			return Lib.text.getProperty(lines, "username");
		}
		catch (IOException e)
		{
			return null;
		}
	}

	//---------------------------------------------------------------------------

	public String getPassword()
	{
		try
		{
			List<String> lines = Lib.text.load(MCKOI_ACCOUNT);

			return Lib.text.getProperty(lines, "password");
		}
		catch (IOException e)
		{
			return null;
		}
	}

	//---------------------------------------------------------------------------

	public void setPort(String port)
	{
		Lib.text.setProperty(lines, "jdbc_server_port", port);
	}

	//---------------------------------------------------------------------------

	public void save() throws FileNotFoundException, IOException
	{
		Lib.text.save(MCKOI_CONFIG, lines);
	}

	//---------------------------------------------------------------------------

	public void createDB() throws Exception
	{
		//--- first : remove old files

		Lib.io.cleanDir(new File(MCKOI_DATA));

		//--- second : generate a new random account

		String user = Lib.text.getRandomString(8);
		String pass = Lib.text.getRandomString(8);

		//--- third : create database files

		McKoiDB mcKoi = new McKoiDB();
		mcKoi.setConfigFile(MCKOI_CONFIG);

		mcKoi.create(user, pass);

		//--- fourth : save it to a file

		ArrayList<String> al = new ArrayList<String>();
		al.add("#--- DO NOT EDIT : file automatically generated");
		al.add("username="+ user);
		al.add("password="+ pass);

		Lib.text.save(MCKOI_ACCOUNT, al);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private List<String> lines;

	private static final String MCKOI_CONFIG = Config.getConfig().getEmbeddedDb()+"/db.conf";
	private static final String MCKOI_ACCOUNT= Config.getConfig().getEmbeddedDb()+"/account.prop";
	private static final String MCKOI_DATA   = Config.getConfig().getEmbeddedDb()+"/data";
}

//=============================================================================

