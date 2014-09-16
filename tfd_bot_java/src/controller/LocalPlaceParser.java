package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.naver.NaverSearch;

public class LocalPlaceParser {
	private final String[] locals = {"홍대", "강남", "종로", "이태원"};
	private DBController dbcon;
	
	public LocalPlaceParser() {
		dbcon = DBController.newInstance(DBController.Type.TFD);
	}
	
	public void start() {
		List<String> parsedPlaces;
		for(String local : locals) {
			try {
				parsedPlaces = parsePlace(local, getPlaceNameList(local + ".txt"));
				writeParsedPlaces(local, parsedPlaces);
			} catch (FileNotFoundException e) {}
		}
	}
	
	private void writeParsedPlaces(String local, List<String> parsedPlaceList) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(local + "_result.txt");
			for(String place : parsedPlaceList)
				writer.write(place + "\n");
		} catch (IOException e) {
//			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {}
		}
	}
	
	private List<String> getPlaceNameList(String localFileName) throws FileNotFoundException {
		BufferedReader reader = new BufferedReader(new FileReader(localFileName));
		List<String> result = new ArrayList<String>();
		String temp;
		try {
			while((temp = reader.readLine()) != null)
				result.add(temp);
			return result;
		} catch (IOException e) {
//			e.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {}
		}
	}
	
	private List<String> parsePlace(String local, List<String> placeList) {
		NaverSearch placeSearch = NaverSearch.getInstance(NaverSearch.SearchType.NAVER_LOCAL);
		List<String> parsedPlaces = new ArrayList<String>();
		
		dbcon.queryExecute("insert into local_2 values('" + local + "')");
		for(String place : placeList) {
			@SuppressWarnings("unchecked")
			List<Map<String, String>> searchResult = (List<Map<String, String>>) placeSearch.getResult(local + " " + place);
			
			if(searchResult != null) {
				List<Map<String, String>> categories = new ArrayList<Map<String, String>>();
				for(Map<String, String> searchedPlace : searchResult) {
					searchedPlace.put("local", local);
					parsedPlaces.add(searchedPlace.get("name"));
					
					Map<String, String> category = new HashMap<String, String>();
					category.put("sub_group", searchedPlace.get("category"));
					categories.add(category);
				}
				dbcon.insertData("category_2", categories);
				dbcon.insertData("place_info_2", searchResult);
			}
		}
		
		if(parsedPlaces.size() > 0)
			return parsedPlaces;
		return null;
	}
	
	public static void main(String[] args) {
		new LocalPlaceParser().start();
	}
}
