/*******************************************************************************
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 * <p>
 * Contributors:
 * OpenWorm - http://openworm.org/people.html
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package uk.ac.vfb.geppetto;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.geppetto.core.datasources.GeppettoDataSourceException;
import org.geppetto.core.datasources.IQueryProcessor;
import org.geppetto.core.features.IFeature;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.services.GeppettoFeature;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.DataSource;
import org.geppetto.model.ProcessQuery;
import org.geppetto.model.QueryResults;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.ModelUtility;
import org.geppetto.model.values.ArrayElement;
import org.geppetto.model.values.ArrayValue;
import org.geppetto.model.values.HTML;
import org.geppetto.model.values.Image;
import org.geppetto.model.values.ImageFormat;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;

/**
 * @author robertcourt
 *
 */
public class AddImportTypesSynonymQueryProcessor implements IQueryProcessor
{

	private enum SynonymIcons
	{

		EXACT("fa-bullseye"), BROAD("fa-expand"), NARROW("fa-compress"), RELATED("fa-link"), DEFAULT("fa-question");

		private SynonymIcons(final String text)
		{
			this.text = text;
		}

		private final String text;

		public String getIcon()
		{
			return text;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.datasources.IQueryProcessor#process(org.geppetto.model.ProcessQuery, org.geppetto.model.variables.Variable, org.geppetto.model.QueryResults)
	 */
	@Override
	public QueryResults process(ProcessQuery query, DataSource dataSource, Variable variable, QueryResults results, GeppettoModelAccess geppettoModelAccess) throws GeppettoDataSourceException
	{

		try
		{

			// retrieving the metadatatype
			CompositeType metadataType = (CompositeType) ModelUtility.getTypeFromLibrary(variable.getId() + "_metadata", dataSource.getTargetLibrary());

			Type htmlType = geppettoModelAccess.getType(TypesPackage.Literals.HTML_TYPE);

			System.out.println("Processing Items...");

			// running through returned items - references.
			if(results.getValue("relationship", 0) != null)
			{

				String synonymLinks = "";
				String defRefs = "";
				String relat = "";
				String temp = "";
				String typeLink = "";

				int i = 0;
				while(results.getValue("relationship", i) != null)
				{ // synonyms and refs:
					if((String) ((Map) results.getValue("relationship", i)).get("synonym") != null)
					{
						synonymLinks += "<a href=\"#\" instancepath=\"" + variable.getId() + "\">";
						if(((Map) results.getValue("relationship", i)).get("scope") != null)
						{
							temp = (String) ((Map) results.getValue("relationship", i)).get("scope");
							String icon = SynonymIcons.DEFAULT.getIcon();
							try
							{
								icon = SynonymIcons.valueOf(temp).getIcon();
							}
							catch(IllegalArgumentException ex)
							{
							}

							synonymLinks += "<i class=\"popup-icon fa " + icon + "\" title=\"synonym scope: " + temp + "\" aria-hidden=\"true\"></i>";
						}
						synonymLinks += (String) ((Map) results.getValue("relationship", i)).get("synonym") + "</a>";
						if(results.getValue("relRef", i) != null)
						{
							synonymLinks += " (" + (String) results.getValue("relRef", i) + ")";
							if(results.getValue("relFBrf", i) != null)
							{
								synonymLinks += " <a href=\"http://flybase.org/reports/" + (String) results.getValue("relFBrf", i) + "\" target=\"_blank\" >"
										+ "<i class=\"popup-icon-link gpt-fly\" title=\"FlyBase:"+(String) results.getValue("relFBrf", i)+"\" aria-hidden=\"true\"></i></a>";
							}
							if(results.getValue("relPMID", i) != null)
							{
								synonymLinks += " <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/?term=" + (String) results.getValue("relPMID", i) + "\" target=\"_blank\" >"
										+ "<i class=\"popup-icon-link gpt-pubmed\" title=\"PMID:"+(String) results.getValue("relPMID", i)+"\" aria-hidden=\"true\"></i></a>";
							}
							if(results.getValue("relDOI", i) != null)
							{
								synonymLinks += " <a href=\" http://dx.doi.org/" + (String) results.getValue("relDOI", i) + "\" target=\"_blank\" >"
										+ "<i class=\"popup-icon-link gpt-doi\" title=\"doi:"+(String) results.getValue("relDOI", i)+"\" aria-hidden=\"true\"></i></a>";
							}

						}
						synonymLinks += "<br/>";
					}
					else
					{
						// definition refs:
						if("def".equals((String) ((Map) results.getValue("relationship", i)).get("typ")))
						{
							if(results.getValue("relRef", i) != null)
							{
								defRefs += (String) results.getValue("relRef", i);
								if(results.getValue("relFBrf", i) != null)
								{
									defRefs += " <a href=\"http://flybase.org/reports/" + (String) results.getValue("relFBrf", i) + "\" target=\"_blank\" >"
											+ "<i class=\"popup-icon-link gpt-fly\" title=\"FlyBase:"+(String) results.getValue("relFBrf", i)+"\" aria-hidden=\"true\"></i></a>";
								}
								if(results.getValue("relPMID", i) != null)
								{
									defRefs += " <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/?term=" + (String) results.getValue("relPMID", i) + "\" target=\"_blank\" >"
											+ "<i class=\"popup-icon-link gpt-pubmed\" title=\"PMID:"+(String) results.getValue("relPMID", i)+"\" aria-hidden=\"true\"></i></a>";
								}
								if(results.getValue("relDOI", i) != null)
								{
									defRefs += " <a href=\" http://dx.doi.org/" + (String) results.getValue("relDOI", i) + "\" target=\"_blank\" >"
										+ "<i class=\"popup-icon-link gpt-doi\" title=\"doi:"+(String) results.getValue("relDOI", i)+"\" aria-hidden=\"true\"></i></a>";
								}
								defRefs += "<br/>";
							}
						}
						else
						{
							// relationships and refs:
							if("Related".equals((String) ((Map) results.getValue("relationship", i)).get("__type__")))
							{
								if(((Map) results.getValue("relationship", i)).get("label") != null)
								{
									temp = ((String) ((Map) results.getValue("relationship", i)).get("label")).replace("_"," ");
									relat += temp.substring(0, 1).toUpperCase() + temp.substring(1) + " ";
								}
								if(results.getValue("relName", i) != null)
								{
									if(results.getValue("relId", i) != null)
									{
										relat += "<a href=\"#\" instancepath=\"" + (String) results.getValue("relId", i) + "\">" + (String) results.getValue("relName", i) + "</a> ";
									}
									else
									{
										relat += (String) results.getValue("relName", i) + " ";
									}
								}
								if(results.getValue("relRef", i) != null)
								{
									relat += "(" + (String) results.getValue("relRef", i) + ")";
									if(results.getValue("relFBrf", i) != null)
									{
										relat += " <a href=\"http://flybase.org/reports/" + (String) results.getValue("relFBrf", i) + "\" target=\"_blank\" >"
												+ "<i class=\"popup-icon-link gpt-fly\" title=\"FlyBase:"+(String) results.getValue("relFBrf", i)+"\" aria-hidden=\"true\"></i></a>";
									}
									if(results.getValue("relPMID", i) != null)
									{
										relat += " <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/?term=" + (String) results.getValue("relPMID", i) + "\" target=\"_blank\" >"
												+ "<i class=\"popup-icon-link gpt-pubmed\" title=\"PMID:"+(String) results.getValue("relPMID", i)+"\" aria-hidden=\"true\"></i></a>";
									}
									if(results.getValue("relDOI", i) != null)
									{
										relat += " <a href=\" http://dx.doi.org/" + (String) results.getValue("relDOI", i) + "\" target=\"_blank\" >"
												+ "<i class=\"popup-icon-link gpt-doi\" title=\"doi:"+(String) results.getValue("relDOI", i)+"\" aria-hidden=\"true\"></i>";
									}
								}
								relat += "<br/>";
							}
							else if("SubClassOf".equals((String) ((Map) results.getValue("relationship", i)).get("__type__")))
							{
								typeLink += "<a href=\"#\" instancepath=\"" + (String) results.getValue("relId", i) + "\">";
								typeLink += (String) results.getValue("relName", i) + "</a>";
								typeLink += "<br/>";
							}
						}
					}
					i++;
				}

				
				// set parent Type:
				if(typeLink != "")
				{
					System.out.println("Type:\n" + typeLink);
					Variable type = VariablesFactory.eINSTANCE.createVariable();
					type.setId("type");
					type.setName("Type");
					type.getTypes().add(htmlType);
					geppettoModelAccess.addVariableToType(type, metadataType);
					HTML typeValue = ValuesFactory.eINSTANCE.createHTML();
					typeValue.setHtml(typeLink);
					type.getInitialValues().put(htmlType, typeValue);
				}
				
				// set Synonyms with any related references:
				if(synonymLinks != "")
				{
					System.out.println("Synonyms:\n" + synonymLinks);
					Variable synonyms = VariablesFactory.eINSTANCE.createVariable();
					synonyms.setId("synonyms");
					synonyms.setName("Alternative names");
					synonyms.getTypes().add(htmlType);
					geppettoModelAccess.addVariableToType(synonyms, metadataType);
					HTML synonymsValue = ValuesFactory.eINSTANCE.createHTML();
					synonymsValue.setHtml(synonymLinks);
					synonyms.getInitialValues().put(htmlType, synonymsValue);
				}

				// set Definition references:
				if(defRefs != "")
				{
					System.out.println("References:\n" + defRefs);
					Variable defReferences = VariablesFactory.eINSTANCE.createVariable();
					defReferences.setId("references");
					defReferences.setName("Definition References");
					defReferences.getTypes().add(htmlType);
					geppettoModelAccess.addVariableToType(defReferences, metadataType);
					HTML defReferencesValue = ValuesFactory.eINSTANCE.createHTML();
					defReferencesValue.setHtml(defRefs);
					defReferences.getInitialValues().put(htmlType, defReferencesValue);
				}

				// set Relationships with any related references:
				if(relat != "")
				{
					System.out.println("Relationships:\n" + relat);
					Variable relationships = VariablesFactory.eINSTANCE.createVariable();
					relationships.setId("relationships");
					relationships.setName("Relationships");
					relationships.getTypes().add(htmlType);
					geppettoModelAccess.addVariableToType(relationships, metadataType);
					HTML relationshipsValue = ValuesFactory.eINSTANCE.createHTML();
					relationshipsValue.setHtml(relat);
					relationships.getInitialValues().put(htmlType, relationshipsValue);
				}
				
				if ("<a href=\"#\" instancepath=\"VFB_10000005\">cluster</a><br/>".equals(typeLink)){
					i = 0;
					String tempId = "";
					String tempThumb = "";
					String tempName = "";
		
					int j = 0;
					Variable exampleVar = VariablesFactory.eINSTANCE.createVariable();
					exampleVar.setId("examples");
					exampleVar.setName("Members");
					exampleVar.getTypes().add(geppettoModelAccess.getType(TypesPackage.Literals.IMAGE_TYPE));
					geppettoModelAccess.addVariableToType(exampleVar, metadataType);
					ArrayValue images = ValuesFactory.eINSTANCE.createArrayValue();
					while(results.getValue("relationship", i) != null)
					{ 
						if("has_member".equals((String) ((Map) results.getValue("relationship", i)).get("label")))
						{
							if(results.getValue("relName", i) != null)
							{
								tempId = (String) results.getValue("relId", i);
								tempThumb = "http://www.virtualflybrain.org/data/VFB/i/" + tempId.substring(4, 8) + "/" + tempId.substring(8) + "/thumbnail.png";
								tempName = (String) results.getValue("relName", i);
								System.out.println("Adding Cluster Image: " + tempId + " " + tempName + " " + tempThumb);
								if (checkURL(tempThumb)){
									addImage(tempThumb, tempName, tempId, images, j);
									j++;
								}
							}
						}
						i++;
					}
					exampleVar.getInitialValues().put(geppettoModelAccess.getType(TypesPackage.Literals.IMAGE_TYPE), images);
				}
				
			}

		}
		catch(GeppettoVisitingException e)
		{
			System.out.println(e);
			throw new GeppettoDataSourceException(e);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}

		return results;
	}

	/**
	 * @param data
	 * @param name
	 * @param images
	 * @param i
	 */
	private void addImage(String data, String name, String reference, ArrayValue images, int i)
	{
		Image image = ValuesFactory.eINSTANCE.createImage();
		image.setName(name);
		image.setData(data);
		image.setReference(reference);
		image.setFormat(ImageFormat.PNG);
		ArrayElement element = ValuesFactory.eINSTANCE.createArrayElement();
		element.setIndex(i);
		element.setInitialValue(image);
		images.getElements().add(element);
	}
	/**
	 * @param urlString
	 */
	private boolean checkURL(String urlString){
		try{
			URL url = new URL(urlString);
			HttpURLConnection huc =  (HttpURLConnection)  url.openConnection();
			huc.setRequestMethod("HEAD");
			huc.setInstanceFollowRedirects(false);
			return (huc.getResponseCode() == HttpURLConnection.HTTP_OK);
		}catch(Exception e){
			System.out.println("Error checking url (" + urlString + ") " + e.toString());
			return false;
		}
	}
	@Override
	public void registerGeppettoService() throws Exception
	{
		ServicesRegistry.registerQueryProcessorService(this);
	}

	@Override
	public boolean isSupported(GeppettoFeature feature)
	{
		return false;
	}

	@Override
	public IFeature getFeature(GeppettoFeature feature)
	{
		return null;
	}

	@Override
	public void addFeature(IFeature feature)
	{

	}

}
