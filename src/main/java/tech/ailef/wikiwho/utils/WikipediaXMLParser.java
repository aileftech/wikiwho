package tech.ailef.wikiwho.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tech.ailef.wikiwho.data.IpRangeCollection;

public class WikipediaXMLParser extends DefaultHandler implements Closeable {
	private boolean inRevision = false;
	private boolean inIp = false;
	private boolean inId = false;
	private boolean inContributor = false;
	private boolean inTimestamp = false;
	
	private StringBuilder ipBuilder = new StringBuilder();
	private StringBuilder revisionIdBuilder = new StringBuilder();
	private StringBuilder timestamp = new StringBuilder();
	
	private PrintWriter out;
	
	private GzipReader dumpReader;
	
	private IpRangeCollection collection;
	
	private int countMatches = 0;
	
	private int countProcessed = 0;
	
	private int limit = 0;
	
	public WikipediaXMLParser(String inputFile, String outputFile) throws IOException {
		out = new PrintWriter(outputFile);
		dumpReader = new GzipReader(inputFile);
		collection = IpRangeCollection.fromFile(Paths.get("resources/ranges/ranges.json"));
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("revision")) {
			inRevision = true;
		}
		
		if (qName.equals("ip")) {
			inIp = true;
		}
		
		if (qName.equals("id")) {
			inId = true;
		}
		
		if (qName.equals("contributor")) {
			inContributor = true;
		}
		
		if (qName.equals("timestamp")) {
			inTimestamp = true;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("revision")) {
			countProcessed++;
			inRevision = false;

			if (countProcessed % 1000000 == 0) {
				System.out.println("[INFO] WikipediaXMLParser: matches=" + countMatches + ", progress=" + countProcessed);
			}
			
			if (countMatches >= limit && limit > 0)
				throw new SAXException("Parsed until reached limit " + limit);
			
			String revId = revisionIdBuilder.toString().trim();
			String ip = ipBuilder.toString().trim();
			
			if (!ip.isEmpty()) {
				String match = collection.match(ip);
				
				if (match != null) {
					out.println(revId + "\t" + ip + "\t" + match + "\t" + timestamp);
					out.flush();
					countMatches++;
				}
			}
			
			revisionIdBuilder.setLength(0);
			ipBuilder.setLength(0);
			timestamp.setLength(0);
		}
		
		if (qName.equals("ip")) {
			inIp = false;
		}
		
		if (qName.equals("id")) {
			inId = false;
		}
		
		if (qName.equals("contributor")) {
			inContributor = false;
		}
		
		if (qName.equals("timestamp")) {
			inTimestamp = false;
		}
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {
		if (inRevision && inId && !inContributor) {
			revisionIdBuilder.append(new String(ch, start, length));
		}

		if (inTimestamp) {
			timestamp.append(new String(ch, start, length));
		}
		
		if (inIp) {
			ipBuilder.append(new String(ch, start, length));
		}

	}

	public void process() throws IOException, ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		
		saxParser.parse(dumpReader.getInputStream(), this);
	}
	
	public void close() {
		out.close();
	}
}