package de.intranda.goobi.plugins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.goobi.production.enums.LogType;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.sub.goobi.helper.Helper;
import ugh.dl.Metadata;

public class HelperHttp {
	
	/**
	 * Check if the URL works
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static boolean checkUrl(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		//connection.setRequestMethod("HEAD");
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
		    return false;
		} else {
			return true;
		}
	}
	
	
	/**
	 * call url using GET with basic auth and return the status code
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static boolean checkUrlBasicAuth(String urlSuffix, SubnodeConfiguration config) throws ClientProtocolException, IOException {
        String password = config.getString("password");
        String user = config.getString("username");
        String url = config.getString("serviceAddress") + urlSuffix;
        Executor executor = Executor.newInstance().auth(user, password);
		int responseCode = executor.execute(Request.Get(url)).returnResponse().getStatusLine().getStatusCode();
		if (responseCode != 200) {
		    return false;
		} else {
			return true;
		}
	}
    
	
	/**
	 * call post url and send xml to it using basic auth
	 * @param doc
	 * @param urlSuffix
	 * @param config
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String postXmlBasicAuth(Document doc, String urlSuffix, SubnodeConfiguration config) throws ParseException, IOException {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    	String xmlString = outputter.outputString(doc);
        String password = config.getString("password");
        String user = config.getString("username");
        String url = config.getString("serviceAddress") + urlSuffix;
        Executor executor = Executor.newInstance().auth(user, password);
        Request r = Request.Post(url)
        		.addHeader("Content-Type", "application/xml;charset=UTF-8")
        		.useExpectContinue()
                .bodyString(xmlString, ContentType.APPLICATION_XML);
        
        HttpResponse hr = executor.execute(r).returnResponse();
        HttpEntity entity = hr.getEntity();
        String info = EntityUtils.toString(entity, "utf-8");
        int responseCode = hr.getStatusLine().getStatusCode();
        
        if (responseCode != 201) {
            return info;
        }
        return "";
	}
	
	/**
	 * call put url and send xml to it using basic auth
	 * @param doc
	 * @param urlSuffix
	 * @param config
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String putXmlBasicAuth(Document doc, String urlSuffix, SubnodeConfiguration config) throws ParseException, IOException {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    	String xmlString = outputter.outputString(doc);
        String password = config.getString("password");
        String user = config.getString("username");
        String url = config.getString("serviceAddress") + urlSuffix;
        Executor executor = Executor.newInstance().auth(user, password);
        Request r = Request.Put(url)
        		.addHeader("Content-Type", "application/xml;charset=UTF-8")
        		.useExpectContinue()
                .bodyString(xmlString, ContentType.APPLICATION_XML);
        
        HttpResponse hr = executor.execute(r).returnResponse();
        HttpEntity entity = hr.getEntity();
        String info = EntityUtils.toString(entity, "utf-8");
        int responseCode = hr.getStatusLine().getStatusCode();
        
        if (responseCode != 201) {
            return info;
        }
        return "";
	}
	
	/**
	 * call post url and send plaintext to it using basic auth
	 * @param doc
	 * @param urlSuffix
	 * @param config
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String putTxtBasicAuth(String text, String urlSuffix, SubnodeConfiguration config) throws ParseException, IOException {
        String password = config.getString("password");
        String user = config.getString("username");
        String url = config.getString("serviceAddress") + urlSuffix;
        Executor executor = Executor.newInstance().auth(user, password);
        Request r = Request.Put(url)
        		.addHeader("Content-Type", "text/plain;charset=UTF-8")
        		.useExpectContinue()
                .bodyString(text, ContentType.TEXT_PLAIN);
        
        HttpResponse hr = executor.execute(r).returnResponse();
        HttpEntity entity = hr.getEntity();
        String info = EntityUtils.toString(entity, "utf-8");
        int responseCode = hr.getStatusLine().getStatusCode();
        
        if (responseCode != 201) {
            return info;
        }
        return "";
	}
	
}
