import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;




public class xmlUpdater {
	/**
	 * Uses XML DOM to create xml document builder in order to traverse csproj and add new references for ImageAssets
	 * @param path, csprojfile
	 *       
	 * path: path to be added to xml file
	 * csprojfile: xml file to be updated
	 */
	public static void ImageAsset(String path, String csprojfile) throws ParserConfigurationException, IOException, SAXException, TransformerConfigurationException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    String sour = csprojfile;
	    factory.setIgnoringElementContentWhitespace(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    File file = new File(sour);
	    Document doc = builder.parse(file);
	    // Do something with the document here.
	    
	    NodeList IA = doc.getElementsByTagName("ImageAsset");
	    File realPath = new File(path.substring(path.indexOf("Resources"), path.length()));
    
	    Node iaNode = IA.item(IA.getLength()-1);
	    Node itemGroup = iaNode.getParentNode();
	    Element add = doc.createElement("ImageAsset");
	    path = realPath.toString();
	    path = path.replace("/", "\\");
	    add.setAttribute("Include", path);
	    itemGroup.appendChild(add);
	    
	    DOMSource source = new DOMSource(doc);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(sour);
        try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			System.exit(12);
			e.printStackTrace();
			
		}
        
	}
	/**
	 * Uses XML DOM to create xml document builder in order to traverse csproj and add new references for new folders created
	 * @param path, csprojfile
	 *       
	 * path: path to be added to xml file
	 * csprojfile: xml file to be updated
	 */
	public static void FolderUpdate(String path, String csprojfile) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    String sour = csprojfile;
	    factory.setIgnoringElementContentWhitespace(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    File file = new File(sour);
	    Document doc = builder.parse(file);
		
	    NodeList FI = doc.getElementsByTagName("Folder");
	    Node fiNode = FI.item(FI.getLength()-1);
	    Node FolderGroup = fiNode.getParentNode();
	    NamedNodeMap temp = null;	
	    
	    File pp = new File(path.substring(path.indexOf("Resources"), path.length()-1));
	    path = pp.toString();
	    path = path.replace("/", "\\"); 
	    boolean add = true;
		for(int i =0; i<FI.getLength();i++) {
		    temp = FI.item(i).getAttributes();
		    String value = temp.getNamedItem("Include").getNodeValue().replace("/", "\\");
		    if(value.equalsIgnoreCase(path)) {
		    	add = false;
		    	break;
		    	}
		 }
		if(add == true) {
//			System.out.println("ADDING IMAGESET TO FOLDER GROUP = "+ pp);
    	    Element addFold = doc.createElement("Folder");
    	    addFold.setAttribute("Include", path);
    	    FolderGroup.appendChild(addFold);
		}
		
	    DOMSource source = new DOMSource(doc);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(sour);
        try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			System.exit(12);
			e.printStackTrace();
		}
	
	}
	
}
