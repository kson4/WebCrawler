import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class webCrawler {
	private static final int MAX_PAGES = 1000;
	private static Set<String> pagesVisited = new HashSet<String>();
	private static Queue<String> pagesToVisit = new LinkedList<String>();
	private static Set<String> pagesToVisitQ = new HashSet<String>();
	private static Map<String, ArrayList<String>> list = new HashMap<String, ArrayList<String>>();
	
	public static void main(String args[]) throws IOException {
		String url = "https://tvtropes.org/pmwiki/pmwiki.php/Main/Webcomics?from=Main.Webcomic";
		//String url = "https://www.cpp.edu";
		bfs(url);
		showResults();
		writeResults();
	}
	
	private static void bfs(String root) throws IOException {
		pagesToVisit.add(root);
		BufferedReader br = null;
		boolean start = true;
		boolean valid = false;
		
		while(!pagesToVisit.isEmpty()) {
				
			String crawledUrl = pagesToVisit.poll();
			
			// skip the urls that are already crawled
			while(!valid) {
				if (pagesVisited.contains(crawledUrl)) {
					System.out.println(crawledUrl + " is already crawled");
					crawledUrl = pagesToVisit.poll();
				}
				else
					valid = true;
			}
			valid = false;

			System.out.println("\n**** Site crawled: " + crawledUrl + " *****");
			
			if (list.size() > MAX_PAGES)
				return;
			
			boolean ok = false;
			URL url = null;
			while (!ok) {
				try {
					url = new URL(crawledUrl);
					br = new BufferedReader(new InputStreamReader(url.openStream()));
					ok = true;
				} catch (MalformedURLException e) {
					System.out.println("Malformed URL: " + crawledUrl);
					crawledUrl = pagesToVisit.poll();
					ok = false;
				} catch (IOException ioe) {
					System.out.println("IOException URL: " + crawledUrl);
					crawledUrl = pagesToVisit.poll();
					ok = false;
				} 
			}
			// skip links that do not have outlinks
			// ruins the overall page rank and makes a negligible difference in the overall results
			//if (list.get(crawledUrl) == null && crawledUrl.contains("cpp") && !crawledUrl.contains("#") && !crawledUrl.contains("~") && !crawledUrl.contains("php")) {
			if (list.get(crawledUrl) == null && crawledUrl.contains("tvtropes") && crawledUrl.contains("/Webcomic/") && !crawledUrl.contains("#") && !crawledUrl.contains("action")) {
				list.put(crawledUrl, new ArrayList<String>());
				System.out.println(crawledUrl + " is added into the list");
			}
			
			request(crawledUrl, pagesToVisit, pagesVisited);
			pagesVisited.add(crawledUrl);
			System.out.println("added: " + crawledUrl + " to pagesVisited");

			System.out.println("Number of pages to visit: " + pagesToVisit.size());
			System.out.println("Number of pages visited: " + pagesVisited.size());
			
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void showResults() {
		System.out.println("Results: ");
		System.out.println("Sites crawled: " + pagesVisited.size());
		
		for (String s : pagesVisited) {
			//System.out.println(s);
		}
		
		for (String s : list.keySet()) {
			//System.out.println("root url: " + s);
			for (String r : list.get(s)) {
				//System.out.print(r + " ");
			}
			System.out.println();
		}
	}
	
	private static void writeResults() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter("results.csv");
		StringBuilder sb = new StringBuilder();
		
		for (String s : list.keySet()) {
			if (list.get(s) != null) {
				sb.append(s);
				sb.append(',');
			}
			System.out.println("root url: " + s);
			for (String r : list.get(s)) {
				if (list.get(r) != null) {
					//System.out.println("CSV: " + r + " is added to: " + s);
					sb.append(r);
					sb.append(',');
				}
			}
			sb.append("\n");
		}
		writer.write(sb.toString());
		writer.close();
	}
	
	private static void request(String url, Queue<String> pagesToVisit, Set<String> pagesVisited) {
		
		try {
			Document doc;
			doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").get();
			if (doc != null) {
				for (Element link : doc.select("a[href]")) {
					// next link holds the links that come out of the root url
					String nextlink = link.absUrl("href");
					// skip links that do not have outlinks
					// ruins the overall page rank and makes a negligible difference in the overall results
					//if (nextlink.contains("www.cpp.edu") && !nextlink.contains("#") && !nextlink.contains("pdf")) {
					if (nextlink.contains("tvtropes") && nextlink.contains("/Webcomic/") && !nextlink.contains("pdf") && !nextlink.contains("#") && !nextlink.contains("action")){
						//System.out.println("LIST: " + nextlink);
						if (!pagesToVisitQ.contains(nextlink)) {
							//System.out.println("added: " + nextlink + " to pagesToVisit");
							pagesToVisitQ.add(nextlink);
							pagesToVisit.add(nextlink);
							//System.out.println(nextlink + " is added to pagesToVisit");
						}
						//if (list.get(url) != null && nextlink.contains("www.cpp.edu") && !nextlink.contains("~") && !nextlink.contains("php") 
						if (list.get(url) != null && nextlink.contains("tvtropes") && nextlink.contains("/Webcomic/") && !nextlink.contains("action")
							&& !nextlink.contains("#") && url != nextlink && !list.get(url).contains(nextlink)){
							//System.out.println("added: " + nextlink + " to " + url);
							list.get(url).add(nextlink);
						}
					}
					//else
						//System.out.println("rejected: " + nextlink);
				}
			}
		} catch (IOException e) {
		}
	}
}