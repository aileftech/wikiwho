package tech.ailef.wikiwho.ops;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import tech.ailef.wikiwho.utils.WikipediaXMLParser;

public class CreateEditsList {
	
	private static final String BASE_DIR = "/mnt/data/wikipedia-data/";
	
	private static final String DUMP_PATH = BASE_DIR + "enwiki-20200120-stub-meta-history.xml.gz";
	
	private static final String OUTPUT_PATH = BASE_DIR + "filtered_edits.tsv";
	
	public static void main(String[] args)  {
		String dumpPath = DUMP_PATH;
		
		if (args.length > 0) {
			dumpPath = args[0];
		}
		
		String outputPath = OUTPUT_PATH;
		if (args.length > 1) {
			outputPath = args[1];
		}
		
		System.out.println("Creating edit list from " + dumpPath + " with output in TSV file " + outputPath);
		createEditList(dumpPath, outputPath);
	}
	
	private static void createEditList(String inputPath, String outputPath) {
		WikipediaXMLParser w = null;
		try {
			w = new WikipediaXMLParser(inputPath, outputPath);
			w.process();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			if (w != null) w.close();
		}
	}
}
