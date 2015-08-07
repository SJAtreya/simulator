package com.gto.iot.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.catalina.util.URLEncoder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RouteFinder {

	private static final String url = "https://maps.googleapis.com/maps/api/directions/json?origin=<origin>&destination=<destination>";

	public static ArrayList<Map> getStepDataForTrip(String startLocation,
			String endLocation) throws Exception {
		String locationUrl = url.replace("<origin>",
				new URLEncoder().encode(startLocation)).replace(
				"<destination>", new URLEncoder().encode(endLocation));
		return parse(httpGet(locationUrl));
	}

	public static ArrayList<Map> parse(String response)
			throws JsonParseException, JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map rootData = mapper.readValue(
				new JsonFactory().createJsonParser(response), Map.class);
		return getStepList(rootData);

	}

	private static ArrayList<Map> getStepList(Map map) {
		// TODO Auto-generated method stub
		return (ArrayList<Map>) ((LinkedHashMap) ((ArrayList) ((LinkedHashMap) ((ArrayList) map
				.get("routes")).get(0)).get("legs")).get(0)).get("steps");
	}

	public static String httpGet(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();

		conn.disconnect();
		return sb.toString();
	}
}