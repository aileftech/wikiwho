package tech.ailef.wikiwho.ops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;

import tech.ailef.wikiwho.server.WikiwhoServer;

public class Wikiwho {
	private static final PrintStream out = System.out;
	
	private static final String LATEST_DUMP = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-stub-meta-history.xml.gz";
	
	private static void usage() {
		out.println("If you need to create everything from scratch:");
		out.println("\twikiwho.jar --init\n");
		out.println("If you need to perform specific steps:");
		out.println("\twikiwho.jar [-c|-d|-s|-cr|-pd] ");
		out.println("\t-c\tcreate edits list");
		out.println("\t-d\tdownload diffs + create reports at the end");
		out.println("\t-pd\tonly process diffs + create reports");
		out.println("\t-s\tstart server");
		out.println("\t-cr\tcreate reports");
		out.println("\t-l\tcreate Lucene from MongoDB");
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 0) {
			usage();
		}
		
		if (args[0].equals("--init")) {
			System.out.println("Downloading latest Wikipedia dump from " + LATEST_DUMP);
			ProcessBuilder p = new ProcessBuilder(new String[] {"wget", LATEST_DUMP, "-O", "latest-dump.gz"});
			p.redirectOutput(Redirect.INHERIT);
			p.redirectError(Redirect.INHERIT);
			Process pp = p.start();
			pp.waitFor();
			
			String outputFile = "latest-dump-edits.tsv";
			String luceneIndexDir = "data/index";
			
			CreateEditsList.main(new String[] {"latest-dump.gz", outputFile});
			try {
				WikipediaDiffsDownloader.main(new String[] {outputFile});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			CreateReports.main(new String[] {});
			CreateLuceneIndex.main(new String[] {luceneIndexDir});
		} else if (args[0].equals("--full")) {
			try {
				String wikipediaDumpFile = args[1];
				String outputFile = args[2];
				String luceneIndexDir = args[3];
				
				CreateEditsList.main(new String[] {wikipediaDumpFile, outputFile});
				try {
					WikipediaDiffsDownloader.main(new String[] {outputFile});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				CreateReports.main(new String[] {});
				CreateLuceneIndex.main(new String[] {luceneIndexDir});
			} catch (IndexOutOfBoundsException e) {
				System.out.println("--full needs 3 args: Wiki dump file, TSV output file, Lucene index dir output");
				System.exit(0);
			}
			
		} else if (args[0].equals("-c")) {
			try {
				String wikipediaDumpFile = args[1];
				String outputFile = args[2];
				CreateEditsList.main(new String[] {wikipediaDumpFile, outputFile});
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Expecting two more params: 1) Wikipedia dump file path; 2) output TSV file path");
				System.exit(0);
			}
		} else if (args[0].equals("-d")) {
			try {
				String editsFile = args[1];
				try {
					WikipediaDiffsDownloader.main(new String[] {editsFile});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				CreateReports.main(new String[] {});
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Expecting one more param: 1) TSV file with edits, output of step -c");
				System.exit(0);
			}
		} else if (args[0].equals("-s")) {
			WikiwhoServer.main(new String[] {});
		} else if (args[0].equals("-cr")) {
			CreateReports.main(new String[] {});
		} else if (args[0].equals("-l")) {
			CreateLuceneIndex.main(new String[] {args[1]});
		}
	}
}
