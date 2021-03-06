package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.JsonObject;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ForbiddenUserException;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.document.DocumentPatchBuilder;
import com.marklogic.client.document.DocumentPatchBuilder.Position;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.marker.DocumentPatchHandle;
import com.marklogic.client.util.EditableNamespaceContext;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utilities for using the MarkLogic XML database.
 * 
 * @author Isidora
 *
 */
public class MarkLogicUtils {
	
	public static final int ACT_PROPOSAL = 0;
	public static final int AMENDMENT = 1;
	public static final int ACT_FINAL = 2;
	public static final int ARCHIVE = 3;
	private static final int DEV = 10;
	
	/**
	 * Collection ID of proposals.
	 */
	private static final String COLL_PROPOSAL = "proposals";
	/**
	 * Collection ID of amendments.
	 */
	private static final String COLL_AMENDMENT = "amendments";
	/**
	 * Collection ID of final docs.
	 */
	private static final String COLL_FINAL = "finals";
	/**
	 * Collection ID of archive.
	 */
	private static final String COLL_ARCHIVE = "archive";
	/**
	 * Collection ID of dev docs (our lists of uri-s).
	 */
	private static final String COLL_DEV = "dev";
	/**
	 * URI of proposals list.
	 */
	private static final String DOC_PROPOSAL = "proposals.xml";
	/**
	 * URI of amendments list.
	 */
	private static final String DOC_AMENDMENT = "amendments.xml";
	/**
	 * URI of finals list.
	 */
	private static final String DOC_FINAL = "finals.xml";
	private static final String TAG_PROPIS = "Propis";
	private static final String TAG_URI = "uri";
	private static final String TAG_PREDLOG_PROPISA = "Predlog_propisa";
	
	//---------------------------------------------------------------------------------------------------
	// XQuery handling
	//---------------------------------------------------------------------------------------------------
	
	public static void initDB() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			System.out.println("Initialising DB");
			builder = dbf.newDocumentBuilder();
			
			Document docProps = builder.newDocument();
			Element elemProps = docProps.createElement("Proposals");
			elemProps.setAttribute("Naziv", DOC_PROPOSAL);
			docProps.appendChild(elemProps);
			
			insertDocument(docProps, DEV, "app", true);
			
			Document docAmends = builder.newDocument();
			Element elemAmends = docAmends.createElement("Amendments");
			elemAmends.setAttribute("Naziv", DOC_AMENDMENT);
			docAmends.appendChild(elemAmends);
			
			insertDocument(docAmends, DEV, "app", true);
			
			Document docFinals = builder.newDocument();
			Element elemFinals = docFinals.createElement("Finals");
			elemFinals.setAttribute("Naziv", DOC_FINAL);
			docFinals.appendChild(elemFinals);
			
			insertDocument(docFinals, DEV, "app", true);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns all proposal data from DB.
	 * 
	 * @return
	 */
	public static ArrayList<JsonObject> getAllProposalsFromDB() {
		ArrayList<JsonObject> uris = new ArrayList<>();
		
		Document l = readDocument(DOC_PROPOSAL);
		NodeList nl = l.getElementsByTagName("uri");
		
		for(int i = 0; i < nl.getLength(); i++ ) {
			String uri = nl.item(i).getTextContent();
			
			Document d = readDocument(uri);
			JsonObject jo = new JsonObject();
			
			jo.addProperty("uri", uri);
			String uriHash = GeneralUtils.getHexHash(uri);
			jo.addProperty("uriHash", uriHash);
			
			jo.addProperty("username", d.getDocumentElement().getAttribute("Autor"));
			
			uris.add(jo);
		}
		
		return uris;
	}
	
	/**
	 * Returns all amendment data from DB.
	 * 
	 * @return
	 */
	public static ArrayList<JsonObject> getAllAmendmentsFromDB() {
		ArrayList<JsonObject> uris = new ArrayList<>();
		
		Document l = readDocument(DOC_AMENDMENT);
		NodeList nl = l.getElementsByTagName("uri");
		
		for(int i = 0; i < nl.getLength(); i++ ) {
			String uri = nl.item(i).getTextContent();
			
			Document d = readDocument(uri);
			JsonObject jo = new JsonObject();
			
			jo.addProperty("uri", uri);
			String uriHash = GeneralUtils.getHexHash(uri);
			jo.addProperty("uriHash", uriHash);
			
			jo.addProperty("username", d.getDocumentElement().getAttribute("Autor"));
			jo.addProperty("propis", d.getDocumentElement().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getTextContent());
			
			uris.add(jo);
		}
		
		return uris;
	}
	
	/**
	 * Returns all final data from DB.
	 * 
	 * @return
	 */
	public static ArrayList<JsonObject> getAllFinalsFromDB() {
		ArrayList<JsonObject> uris = new ArrayList<>();
		
		Document l = readDocument(DOC_FINAL);
		NodeList nl = l.getElementsByTagName("uri");
		
		for(int i = 0; i < nl.getLength(); i++ ) {
			String uri = nl.item(i).getTextContent();
			
			Document d = readDocument(uri);
			JsonObject jo = new JsonObject();
			
			jo.addProperty("uri", uri);
			String uriHash = GeneralUtils.getHexHash(uri);
			jo.addProperty("uriHash", uriHash);
			
			jo.addProperty("username", d.getDocumentElement().getAttribute("Autor"));
			
			uris.add(jo);
		}
		
		return uris;
	}
	
	/**
	 * Convenience method for reading file contents into a string.
	 */
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	
	//---------------------------------------------------------------------------------------------------
	// Document handling
	//---------------------------------------------------------------------------------------------------
	
	/**
	 * Inserts a given XML {@link Document} into the XML database.
	 * Database is specified in the {@code connection.properties} file.
	 * 
	 * @param doc - {@link Document} object representing the XML document
	 * @param collection - one of four possible collections specified in the static fields
	 * @param dev - set to false
	 * @param user - who made it
	 */
	public static void insertDocument(Document doc, int collection, String user, boolean dev) {
		
		try {
			System.out.println("Beginning database insert:");
			
			// Collection
			String collectionID = "";
			String list = "";
			
			switch (collection) {
			case ACT_PROPOSAL:
				collectionID = COLL_PROPOSAL;
				list = DOC_PROPOSAL;
				break;
			case AMENDMENT:
				collectionID = COLL_AMENDMENT;
				list = DOC_AMENDMENT;
				break;
			case ACT_FINAL:
				collectionID = COLL_FINAL;
				list = DOC_FINAL;
				break;
			case ARCHIVE:
				collectionID = COLL_ARCHIVE;
				break;
			case DEV:
				collectionID = COLL_DEV;
				break;
			default:
				System.out.println(">> ERROR: Bad collection ID <<\n giving up\n");
				return;
			}
			
			// Connection parameters for the database.
			System.out.println("> Loading connection properties.");
			ConnectionProperties cn = loadProperties();
			DatabaseClient client = DatabaseClientFactory.newClient(cn.host, cn.port, cn.database, cn.user, cn.password, cn.authType);
			
			// Create a document manager to work with XML files.
			XMLDocumentManager xmlManager = client.newXMLDocumentManager();
			
			DocumentMetadataHandle metadata = new DocumentMetadataHandle();
			metadata.getCollections().add(collectionID);
			
			// Document section
			String documentID = doc.getDocumentElement().getAttribute("Naziv");
			if(documentID.equals("") || documentID == null) {
				documentID = documentID.concat(user).concat(String.valueOf(Calendar.getInstance().getTimeInMillis()));
			}
			
			documentID = documentID.replace(" ", "-");
			
			doc.getDocumentElement().setAttribute("Naziv", documentID);
			doc.getDocumentElement().setAttribute("Autor", user);
			if(!documentID.endsWith(".xml")) {
				documentID = documentID.concat(".xml");
			}
			System.out.println("Inserting: " + documentID);
			InputStreamHandle ish = new InputStreamHandle(createInputStream(doc, false));
			
			xmlManager.write(documentID, metadata, ish);
			client.release();
			
			// Preventing recursion.
			if(!dev) {
				// Insert URI into list.
				System.out.println("Inserting uri " + documentID + " into " + list);
				Document l = readDocument(list);
				Element root = l.getDocumentElement();
				Element newNode = l.createElement("uri");
				newNode.setTextContent(documentID);
//				newNode.setAttribute("uri", documentID);
				root.appendChild(newNode);
				insertDocument(l, DEV, "app", true);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Reads a XML entry into a {@link Document} from the MarkLogic database specified in the {@code connection.properties}.
	 * 
	 * @param documentID - Full URI of the entry
	 * @return {@link Document}
	 */
	public static Document readDocument(String documentID) {
		
		try {
			// Connection parameters for the database.
			System.out.println("> Loading connection properties.");
			ConnectionProperties cn = loadProperties();
			DatabaseClient client = DatabaseClientFactory.newClient(cn.host, cn.port, cn.database, cn.user, cn.password, cn.authType);
			
			// Create a document manager to work with XML files.
			XMLDocumentManager xmlManager = client.newXMLDocumentManager();
			
			// Handles
			DOMHandle content = new DOMHandle();
			DocumentMetadataHandle metadata = new DocumentMetadataHandle();
			
			// Document section
			System.out.println("Reading: " + documentID);
			xmlManager.read(documentID, metadata, content);
			Document doc = content.get();
			client.release();
			
			return doc;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void updateDocument(String documentID, Document amendment) {
		try {
			// Connection parameters for the database.
			System.out.println("> Loading connection properties.");
			ConnectionProperties cn = loadProperties();
			
			DatabaseClient client = DatabaseClientFactory.newClient(cn.host, cn.port, cn.database, cn.user, cn.password, cn.authType);
			
			// Create a document manager to work with XML files.
			XMLDocumentManager xmlManager = client.newXMLDocumentManager();
			
			// Defining namespace mappings
			System.out.println("> Namespace " + amendment.getDocumentElement().getPrefix() + " : " + amendment.getDocumentElement().getNamespaceURI());
			EditableNamespaceContext namespaces = new EditableNamespaceContext();
			namespaces.put(amendment.getDocumentElement().getPrefix(), amendment.getDocumentElement().getNamespaceURI());
			namespaces.put("fn", "http://www.w3.org/2005/xpath-functions");
			
			// Assigning namespaces to patch builder
			DocumentPatchBuilder patchBuilder = xmlManager.newPatchBuilder();
			patchBuilder.setNamespaces(namespaces);
			
			// Data
			Element predlogResenja = (Element)( amendment.getElementsByTagName("Predlog_resenja").item(0));
			String tipPredloga = predlogResenja.getAttribute("tippredloga");
			// Type of amendment
			switch (tipPredloga) {
			case "izmena": {
				String tipElementa = predlogResenja.getAttribute("tipElementa");			
				Element izmena = (Element)( predlogResenja.getElementsByTagName(tipElementa).item(0));
				
				String xpath = ((Element)(amendment.getElementsByTagName("Odredba").item(0))).getTextContent();
				
				patchBuilder.replaceFragment(xpath, izmena);
				
				DocumentPatchHandle patchHandle = patchBuilder.build();
				xmlManager.patch(documentID, patchHandle);
				client.release();
				
				break;
			}
			case "dopuna": {
				String tipElementa = predlogResenja.getAttribute("tipElementa");			
				Element izmena = (Element)( predlogResenja.getElementsByTagName(tipElementa).item(0));
				
				String xpath = ((Element)(amendment.getElementsByTagName("Odredba").item(0))).getTextContent();
				
				patchBuilder.insertFragment(xpath, Position.BEFORE, izmena);
				
				DocumentPatchHandle patchHandle = patchBuilder.build();
				xmlManager.patch(documentID, patchHandle);
				client.release();
				
				break;
			}
			case "brisanje": {				
				String xpath = ((Element)(amendment.getElementsByTagName("Odredba").item(0))).getTextContent();
				
				patchBuilder.delete(xpath);
				
				DocumentPatchHandle patchHandle = patchBuilder.build();
				xmlManager.patch(documentID, patchHandle);
				client.release();
				break;
			}
			default:
				System.out.println("ERROR: Wrong amendment type.");
				return;
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean removeDocument(String uri) {
		try {
			// Connection parameters for the database.
			System.out.println("> Loading connection properties.");
			ConnectionProperties cn = loadProperties();
			DatabaseClient client = DatabaseClientFactory.newClient(cn.host, cn.port, cn.database, cn.user, cn.password, cn.authType);
			
			// Create a document manager to work with XML files.
			XMLDocumentManager xmlManager = client.newXMLDocumentManager();
			
			// Document section
			// Read doc and check if it has a tag called 'Propis'
			Document doc = readDocument(uri);
			DocumentPatchBuilder xmlPatchBldr = xmlManager.newPatchBuilder();
			NodeList nl = doc.getElementsByTagName(TAG_PROPIS);
			// If such tag exists, the doc is an Act, so search for its amendments
			if(nl.getLength() > 0) {
				ArrayList<String> amndToDelete = new ArrayList<String>();
				ArrayList<String> nodeToDelete = new ArrayList<String>();
				Document amendments = readDocument(DOC_AMENDMENT);
				NodeList nlAm = amendments.getElementsByTagName(TAG_URI);
				for(int idx = 0; idx < nlAm.getLength(); idx++) {
					Node n = nlAm.item(idx);
					nodeToDelete.add(org.joox.JOOX.$(n).xpath());
					String uriAm = n.getTextContent();
					if(uriAm == null || uriAm.trim().equals("")) {
						continue;
					}
					
					Document amnd = readDocument(uriAm);
					NodeList amndNl = amnd.getElementsByTagName(TAG_PREDLOG_PROPISA);
					if(amndNl.getLength() > 0) {
						amndToDelete.add(uriAm);
					}
				}

				// Delete its amendments
				for(String delUri: amndToDelete) {
					System.out.println("Deleting " + delUri + " attached to " + uri);
					xmlManager.delete(delUri);
				}
				
				// Delete nodes for removed amendments
				for(String delNode: nodeToDelete) {
					System.out.println("Deleting " + delNode + " from " + DOC_AMENDMENT);
					if(delNode != null && !delNode.trim().equals("")) {
						xmlPatchBldr.delete(delNode);
					}
				}
			}

			DocumentPatchHandle handle = xmlPatchBldr.build();
			xmlManager.patch(DOC_AMENDMENT, handle);
			
			Document proposals = readDocument(DOC_PROPOSAL);
			NodeList nlPr = proposals.getElementsByTagName(TAG_URI);
			String propNodeToDel = "";
			for(int idx = 0; idx < nlPr.getLength(); idx++) {
				Node n = nlPr.item(idx);
				String textCnt = n.getTextContent();
				if(textCnt.equals(uri)) {
					propNodeToDel = org.joox.JOOX.$(n).xpath();
					continue;
				}
			}
			System.out.println("Deleting " + uri);
			xmlManager.delete(uri);
			
			if(!propNodeToDel.trim().equals("")) {
				xmlPatchBldr.delete(propNodeToDel);
			}
			
			xmlManager.patch(DOC_PROPOSAL, handle);
			
			client.release();
			
			String fileName = uri.substring(0, uri.length() - 4);
			String filePath = Constants.FOLDER_PUBLIC + Constants.FOLDER_XSLT_HTMLS + fileName + Constants.FILE_HTML;
			File f = new File(filePath);
			try {
				if(f.exists()) {
					System.out.println("Deleting " + filePath);
					f.delete();
				}
			} catch(SecurityException e) {
				System.out.println("Deleting " + fileName + " failed");
				e.printStackTrace();
			}
			
			return true;
		} catch (IOException | ResourceNotFoundException | ForbiddenUserException | FailedRequestException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Streams ----------------------------------------------------------------------
	
	 /**
	  * Creates an {@link InputStream} from the given {@link Document}.
	  * 
	  * @param document the document to convert
	  * @param prettyPrint prettyPrinted if true
	  * @return An input stream of the document
	  * @throws IOException
	  */
	public static InputStream createInputStream(Document document, boolean prettyPrint) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputFormat outputFormat = new OutputFormat(document);
		if (prettyPrint) {
			outputFormat.setIndenting(true);
			outputFormat.setIndent(2);
			outputFormat.setLineWidth(65);
			outputFormat.setPreserveSpace(false);
		}
		XMLSerializer serializer = new XMLSerializer(outputStream, outputFormat);
		serializer.serialize(document);
		return new ByteArrayInputStream(outputStream.toByteArray());
	}
	
	/**
	 * Creates an {@link OutputStream} from the given {@link Node}.
	 * This can then be used to print in the console or some other use.
	 *
	 * @param node - a node to be serialized, can also be a {@link Document}
	 * @param out - an output stream to write the serialized DOM representation to
	 * 
	 */
	public static void createOutputStream(Node node, OutputStream out) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DOMSource source = new DOMSource(node);
 
			StreamResult result = new StreamResult(out);

			transformer.transform(source, result);
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	
	//---------------------------------------------------------------------------------------------------
	// Connection handling
	//---------------------------------------------------------------------------------------------------
	
	/**
	 * Represents the connection parameters specified in a property file
	 */
	static public class ConnectionProperties {

		public String host;
		public int port = -1;
		public String user;
		public String password;
		public String database;
		public Authentication authType;

		/**
		 * Creates connection properties object from {@link Properties}.
		 * @param props {@link Properties}
		 */
		public ConnectionProperties(Properties props) {
			super();
			host = props.getProperty("conn.host").trim();
			port = Integer.parseInt(props.getProperty("conn.port"));
			user = props.getProperty("conn.user").trim();
			password = props.getProperty("conn.password").trim();
			database = props.getProperty("conn.database").trim();
			authType = Authentication.valueOf(props.getProperty("conn.authentication_type").toUpperCase().trim());
		}
	}

	/**
	 * Loads the connection properties from the property file for the specified MarkLogic database.
	 * 
	 * @return {@link ConnectionProperties}
	 */
	public static ConnectionProperties loadProperties() throws IOException {
		String propsName = "connection.properties";

		InputStream propsStream = openStream(propsName);
		if (propsStream == null) {
			throw new IOException("Could not read properties " + propsName);
		}

		Properties props = new Properties();
		props.load(propsStream);

		return new ConnectionProperties(props);
	}

	/**
	 * Read a resource for an example.
	 * 
	 * @param fileName
	 *            the name of the resource
	 * @return an input stream for the resource
	 * @throws IOException
	 */
	public static InputStream openStream(String fileName) throws IOException {
		return MarkLogicUtils.class.getClassLoader().getResourceAsStream(fileName);
	}
	
	

}
