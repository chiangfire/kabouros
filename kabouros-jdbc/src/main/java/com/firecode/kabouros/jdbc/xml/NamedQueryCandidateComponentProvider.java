package com.firecode.kabouros.jdbc.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author jiang
 */
public class NamedQueryCandidateComponentProvider{
	
	private static final Log LOG = LogFactory.getLog(NamedQueryCandidateComponentProvider.class);
	
	//JAXP attribute used to configure the schema language for validation.
	private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	//JAXP attribute value indicating the XSD schema language.
	private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
	//Default file name
	private static final String DEFAULT_FILE_NAME = ".XML";
	//root node name
	private static final String RROT_TARGET_NAME = "named-query";
	private static final String NAMESPACE_NAME = "namespace";
	private static final String QUERY_TARGET_NAME = "query";
	private static final String NODE_PRIMARY_NAME = "id";
	
	public Map<String,String> loadNamedQueryDocument(String path,boolean isFailFast){
		LOG.info("Read named-query XML file to start.");
		Map<String,String> queryMap = new HashMap<>();
		/*StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stacks[stacks.length-1];
		String applicationClassName = stackTraceElement.getClassName();
		try {
			Class<?> applicationClass = Class.forName(applicationClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.join(" ",applicationClassName,"not found."),e);
		}*/
		Set<File> files = new HashSet<File>();
		URL resource = getClass().getClassLoader().getResource(StringUtils.isEmpty(path) ? RROT_TARGET_NAME : path);
		if(null != resource){
			File directory = new File(resource.getFile());
			try {
				loaderXmlFiles(directory,files);
				if(!files.isEmpty()){
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
					for(Iterator<File> it = files.iterator();it.hasNext();){
						File file = it.next();
						try {
							DocumentBuilder builder = factory.newDocumentBuilder();
							Document document = builder.parse(file);
							NodeList root = document.getElementsByTagName(RROT_TARGET_NAME);
							Node namedQuery = null;
							if(null != root&&(namedQuery = root.item(0)) != null){
								String namespace = namedQuery.getAttributes().getNamedItem(NAMESPACE_NAME).getNodeValue();
								try {
									Class<?> namespaceClass = Class.forName(namespace);
									if(!namespaceClass.isInterface()) throw new IllegalArgumentException(String.join(" ",namespace,"is not interface."));
									NodeList querys = namedQuery.getChildNodes();
									for(int i=0,length=querys.getLength();i<length;i++){
										Node item = querys.item(i);
										if(null != item && QUERY_TARGET_NAME.equals(item.getNodeName())){
											String id = item.getAttributes().getNamedItem(NODE_PRIMARY_NAME).getNodeValue();
											if(StringUtils.isEmpty(id) || id.startsWith(".")) throw new IllegalArgumentException(" named-query id is empty Or \".\" in starts with.");
											queryMap.put(String.join("", namespaceClass.getName(),".",id), item.getFirstChild().getNodeValue().trim());
										}
									}
								} catch (ClassNotFoundException e) {
									throw new IllegalArgumentException(String.join(" ",namespace,"class not found."),e);
								}
							}
						} catch (ParserConfigurationException | SAXException e) {
							throw new IllegalArgumentException(String.join(" ", file.getPath(),"parsing failure."),e);
						}
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("named-query XML file read failure.",e);
			}
		}else if(isFailFast) throw new IllegalArgumentException(String.join(" ","classpath:",path," The file directory does not exist. Or there is no file in the directory"));
		LOG.info(String.join(" ", "Read named-query XML file end.","A total of",String.valueOf(files.size()),"files"));
		return queryMap;
	}
	
	private void loaderXmlFiles(File file,Set<File> sets){
		File[] listFiles = file.listFiles();
		for(int i=0,length=listFiles.length;i<length;i++){
			File childFile = listFiles[i];
			if(childFile.isDirectory()){
				loaderXmlFiles(childFile,sets);
			}else if(childFile.getName().toUpperCase().endsWith(DEFAULT_FILE_NAME)){
				sets.add(childFile);
			}
		}
	}
}
