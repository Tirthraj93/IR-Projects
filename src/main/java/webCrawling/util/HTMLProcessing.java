package webCrawling.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import webCrawling.controller.CrawlingController;
import webCrawling.util.Constants;
import crawlercommons.robots.SimpleRobotRulesParser;

/*1512 GREAT MODERNIST ARTISTS
http://en.wikipedia.org/wiki/List_of_modern_artists
http://www.ranker.com/list/famous-modernism-artists/reference
http://en.wikipedia.org/wiki/Constantin_Brâncuși
http://www.visual-arts-cork.com/sculpture/constantin-brancusi.htm
*/	
public class HTMLProcessing {
	
	private static SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
	
	public static void main(String[] args) {
		
		try {
			String url = URLEncoder.encode("http://en.wikivoyage.org/wiki/Special:Search/Constantin_Br%C3%83%C2%A2ncu%C3%88%C2%99i", "UTF-8");

			Response response = Jsoup.connect(URLDecoder.decode(url,"UTF-8"))
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
					.followRedirects(true)
					.timeout(2000)
					.ignoreHttpErrors(true)
					.execute();
			System.out.println(response.body());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*Set<String> allowedCanonicalizedOutLinks = getAllowedCanonicalizedOutLinks("http://en.wikipedia.org/wiki/List_of_modern_artists");
		System.out.println(allowedCanonicalizedOutLinks.size());*/
		/*System.out.println(canonicalizeURL("https://www.example.com:443//SomeFile.html#1fq35v23#456"));
		System.out.println(canonicalizeURL("https://www.example.com:443//SomeFile//AllFILE"));
		System.out.println(canonicalizeURL("https://www.example.com:443//SomeFile/OneFile//AllFILE"));
		System.out.println(canonicalizeURL("HTTP://www.Example.com/SomeFile.html"));
		System.out.println(canonicalizeURL("http://www.example.com:80"));
		System.out.println(canonicalizeURL("https://www.example.com:443"));
		System.out.println(canonicalizeURL("https://www.example.com:443/SomeFile.html#1fq35v23#456"));*/
	}
	
	public static Set<String> getOutLinks(Document document) {
		Set<String> canonicalizedOutLinks = new HashSet<String>();
		try {
			Elements links = document.select("a");
			for (Element element : links) {
				String urlString = element.attr("abs:href");
				if("".equals(urlString)) {
					continue;
				}
				String canonicalizedURL;
				if((canonicalizedURL = canonicalizeURL(URLDecoder.decode(urlString, "UTF-8"))) == null) {
					continue;
				}
				if(isCrawlingAllowed(canonicalizedURL)) {
					canonicalizedOutLinks.add(canonicalizedURL);
				}
				if(CrawlingController.urlLinkCountMap.containsKey(canonicalizedURL)) {
					int count = CrawlingController.urlLinkCountMap.get(canonicalizedURL);
					CrawlingController.urlLinkCountMap.remove(canonicalizedURL, count);
					CrawlingController.urlLinkCountMap.putIfAbsent(canonicalizedURL, ++count);
				}
				else {
					CrawlingController.urlLinkCountMap.putIfAbsent(canonicalizedURL, 1);
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return canonicalizedOutLinks;
	}
	
	public static String canonicalizeURL(String urlString) {
		String canonicalizedURL = "";
		
		//Remove the fragment, which begins with #: http://www.example.com/a.html#anything → http://www.example.com/a.html
		//Convert the scheme and host to lower case: HTTP://www.Example.com/SomeFile.html → http://www.example.com/SomeFile.html
		//Remove port 80 from http URLs, and port 443 from HTTPS URLs: http://www.example.com:80 → http://www.example.com
		//Remove duplicate slashes: http://www.example.com//a.html → http://www.example.com/a.html
		try {
			URL url = new URL(urlString.replaceAll("#.*", ""));
			String scheme = url.getProtocol().toLowerCase().replace("https", "http");
			if(!"http".equals(scheme)) {
				return null;
			}
			String host = url.getHost().toLowerCase();
			String path = url.getPath().replaceAll("//", "/");
			URL newURL = new URL(scheme + "://" + host  + path);
			canonicalizedURL = newURL.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return canonicalizedURL;
	}
	
	public static boolean isCrawlingAllowed(String urlString) {
		BaseRobotRules rules = null;
		String domain = getDomain(urlString);
		try {
			if(CrawlingController.domainRobotRulesMap.containsKey(domain)) {
				if(CrawlingController.domainRobotRulesMap.get(domain) == null) {
					return true;
				}
				else {
					rules = CrawlingController.domainRobotRulesMap.get(domain);
				}
			}
			else {
				/*HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
				HttpResponse response = new DefaultHttpClient(httpParams)
							.execute(new HttpGet(domain + "/" + "robots.txt"),
		                             new BasicHttpContext());*/
				Response response = Jsoup.connect(domain + "/" + "robots.txt")
						.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
						.followRedirects(true)
						.timeout(Constants.robotConnectionTimeout)
						.ignoreHttpErrors(true)
						.execute();
				
				if(response.statusCode() == 404 || response == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				}
				else {
					String robotContent = response.parse().body().text();
					InputStream content = new ByteArrayInputStream(robotContent.getBytes());
					rules = parser.parseContent(urlString, 
							IOUtils.toByteArray(content), 
							"text/plain", 
							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2");
				}
				CrawlingController.domainRobotRulesMap.putIfAbsent(domain, rules);
			}
		} catch(Exception e) {
			//System.out.println("Can't connect to domain - " + domain);
			CrawlingController.domainRobotRulesMap.putIfAbsent(domain, null);
			return true;
		} 
		return rules.isAllowed(urlString);
	}
	
	public static String getDomain(String urlString) {
		String domain = "";
		try {
			URL url = new URL(urlString);
			String protocol = url.getProtocol().toLowerCase().replace("https", "http");
			String host = url.getHost();
			int tempPort = url.getPort();
			String port = tempPort > -1 ? ":" + tempPort : "";
			domain = protocol + "://" + host + port;
		}
		catch(MalformedURLException e) {
			e.printStackTrace();
		}
		return domain;
	}
	
	public static boolean enforcePolitenessPolicy(String domain) {
		try {
			if(CrawlingController.domainAccessTimeMap.containsKey(domain)) {
				long prevTime = CrawlingController.domainAccessTimeMap.get(domain);
				long currTime = System.currentTimeMillis();
				long timeDiff = currTime - prevTime;
				if(timeDiff <= 1000) {
					//System.out.println("Waiting to call domain - "+domain);
					return true;
				}
				CrawlingController.domainAccessTimeMap.remove(domain, prevTime);
				CrawlingController.domainAccessTimeMap.putIfAbsent(domain, System.currentTimeMillis());
			}
			else {
				CrawlingController.domainAccessTimeMap.putIfAbsent(domain, System.currentTimeMillis());
			}
		}
		catch(Exception e) {
			CrawlingController.domainAccessTimeMap.putIfAbsent(domain, System.currentTimeMillis());
		}
		return false;
	}
}
