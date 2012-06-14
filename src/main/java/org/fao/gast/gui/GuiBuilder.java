//==============================================================================
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

package org.fao.gast.gui;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import jeeves.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.gast.gui.panels.FormPanel;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;

//==============================================================================

public class GuiBuilder
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GuiBuilder(ViewPanel view, WorkPanel work)
	{
		viewPanel = view;
		workPanel = work;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void build(URL guiSpec, Locale locale) throws Exception
	{
		Element root = Xml.loadFile(guiSpec);

		ResourceBundle resourceBundle = lookupResourceBundle(guiSpec, locale);

		localize(root, resourceBundle);

		packag = root.getChild("class").getAttributeValue("package");

		Element preselected = root.getChild("preselected");
		if (preselected != null) {
			selectedId = preselected.getText();
		}


		for (Object precon : root.getChildren("precon"))
			addPrecon((Element) precon);

		for (Object cont : root.getChildren("container"))
			addContainer((Element) cont);

		// if we have a preselected id then show the form that has that id
		if (selectedId != null) {
			workPanel.show(selectedId);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private ResourceBundle lookupResourceBundle(URL guiSpec, Locale locale) throws IOException 
	{

		String[] parts = { locale.getLanguage(), locale.getCountry(),
				locale.getVariant() };
		String baseString = guiSpec.toExternalForm();
		if (baseString.indexOf('.') > 0) {
			baseString = baseString.substring(0, baseString.lastIndexOf("."));
		}

		for (int i = parts.length; i >= 0; i--) {
			ResourceBundle bundle = locateResourceBundleFile(baseString, parts,
					i);
			if (bundle != null) {
				return bundle;
			}
		}
		throw new IllegalStateException("Unable to find gui.xml file");
	}

	public ResourceBundle locateResourceBundleFile(String baseURL,
			String[] args, int toUse) throws IOException 
	{
		StringBuilder builder = new StringBuilder(baseURL);

		for (int i = 0; i < toUse; i++) {
			if (args[i] != null) {
				builder.append('_');
				builder.append(args[i]);
			}
		}

		builder.append(".properties");
		try {
			final InputStream inputStream = new URL(builder.toString()).openStream();
			return new PropertyResourceBundle(inputStream);
		} catch (IOException e) {
			return null;
		}
	}
	
	private void localize(Element elem, ResourceBundle resourceBundle) 
	{
		List attributes = elem.getAttributes();
		for (Object attribute1 : attributes) {
			Attribute attribute = (Attribute) attribute1;
			attribute.setValue(localize(attribute.getValue(), resourceBundle));
		}
		if (elem.getText() != null && elem.getTextTrim().length() > 0)
			elem.setText(localize(elem.getText(), resourceBundle));

		List children = elem.getChildren();
		for (Object aChildren : children) {
			Content child = (Content) aChildren;
			if (child instanceof Element) {
				localize((Element) child, resourceBundle);
			}
		}
	}

	private String localize(String string, ResourceBundle resourceBundle) 
	{
		if (string == null) {
			return null;
		}
		String localized = string;
		while (true) {
			Matcher matcher = LOCALIZATION_KEY.matcher(localized);

			if (!matcher.find()) {
				return localized;
			}

			String group = matcher.group(1);
			String replacement = resourceBundle.getString(group);
			localized = localized.replaceAll("\\$\\{" + group + "\\}",
					replacement);
		}
	}

	private void addPrecon(Element precon)
	{
		Precon p = new Precon();

		p.type = precon.getAttributeValue("type");
		p.image= retrieveImage(precon.getChildText("image"));
		p.tip  = precon.getChildText("tip");

		hmPrecons.put(p.type, p);
	}

	//---------------------------------------------------------------------------

	private void addContainer(Element cont) throws Exception
	{
		String image = cont.getChildText("image");
		String label = cont.getChildText("label");

		Object node = viewPanel.addContainer(label, retrieveImage(image));

		for (Object form : cont.getChildren("form"))
			addForm(node, (Element) form);
	}

	//---------------------------------------------------------------------------

	private void addForm(Object cont, Element form) throws Exception
	{
		String id    = form.getChildText("id");
		String image = form.getChildText("image");
		String label = form.getChildText("label");
		String title = form.getChildText("title");
		String clazz = form.getChildText("class");
		String descr = form.getChildText("description");
		
		String param = form.getChildText("param");

		FormPanel     formPanel= buildForm(clazz, param);
		
		List<JButton> buttons  = buildButtons(form.getChildren("button"), formPanel);
		Precon        precon   = getPrecon(form.getChild("precon"));

		formPanel.init(title, descr, buttons, precon.image, precon.tip);
		workPanel.add(id, formPanel);
		boolean select = false;
		if (selectedId.equals(id)) select = true;
		viewPanel.addForm(cont, id, label, retrieveImage(image), select);
	}

	//---------------------------------------------------------------------------

	private FormPanel buildForm(String className, String param) throws Exception
	{
		Class clazz = Class.forName(packag +"."+className);
		
		FormPanel fp;
		
		if (param != null)
			fp = ((Constructor<FormPanel>) clazz.getConstructor(String.class)).newInstance(param);
		else 
			fp = (FormPanel) clazz.newInstance();

		return fp; 
	}

	//---------------------------------------------------------------------------

	private List<JButton> buildButtons(List buttons, FormPanel form)
	{
		ArrayList<JButton> al = new ArrayList<JButton>();

		for (Object button : buttons)
			al.add(buildButton((Element) button, form));

		return al;
	}

	//---------------------------------------------------------------------------

	private JButton buildButton(Element button, FormPanel form)
	{
		String image = button.getChildText("image");
		String label = button.getChildText("label");
		String action= button.getChildText("action");

		JButton btn = new JButton(label, retrieveImage(image));
		btn.setActionCommand(action);
		btn.addActionListener(form);

		return btn;
	}

	//---------------------------------------------------------------------------

	private Precon getPrecon(Element precon)
	{
		String type = precon.getAttributeValue("type");
		Precon p    = hmPrecons.get(type);

		return p;
	}

	//---------------------------------------------------------------------------

	private Icon retrieveImage(String imagePath)
	{
		if (imagePath == null)
			return null;

		if (hmImages.containsKey(imagePath))
			return hmImages.get(imagePath);
		
		ClassLoader classLoader = GuiBuilder.class.getClassLoader();
		try {
			System.out.println("Reading image (needs jai_imageio): images/"+imagePath);
			Image image = ImageIO.read(classLoader.getResource("images/"+imagePath));
			ImageIcon icon = new ImageIcon(image);
			hmImages.put(imagePath, icon);
			return icon;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String packag;
	private String selectedId;

	private ViewPanel viewPanel;
	private WorkPanel workPanel;

	private HashMap<String, Icon>   hmImages  = new HashMap<String, Icon>();
	private HashMap<String, Precon> hmPrecons = new HashMap<String, Precon>();

	//--------------------------------------------------------------------------
	// -
	// ---
	// --- Constants
	// ---
	//--------------------------------------------------------------------------
	// -

	private static final Pattern LOCALIZATION_KEY = Pattern
			.compile("\\$\\{([\\S&&[^\\$\\{\\}]]+)\\}");
}

//==============================================================================

class Precon
{
	public String type;
	public Icon   image;
	public String tip;
}

//==============================================================================


