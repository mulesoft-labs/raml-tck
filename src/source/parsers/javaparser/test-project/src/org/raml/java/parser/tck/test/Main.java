package org.raml.java.parser.tck.test;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.common.ValidationResult;

public class Main {
	private static PrintStream systemOut = System.out;
	
	private static int count = 0;
	private static int ignored = 0;
	private static int jsCount = 0;	
	private static int javaCount = 0;
	
	public static void main(String[] args) {
		testApis(args[0]);
	}
	
	private static void testApis(String jsonPath) {
		try {
			JSONArray inputJson = (JSONArray) new JSONParser().parse(new FileReader(jsonPath));
			
			for(Object item: inputJson) {
				

				JSONObject jsonItem = (JSONObject) item;
				
				String apiPath = (String) jsonItem.get("apiPath");

				String tckJSONPath = (String) jsonItem.get("tckJsonPath");
				JSONObject tckJson = (JSONObject) new JSONParser().parse(new FileReader(tckJSONPath));
				boolean passed = (boolean) jsonItem.get("passed");

				count++;

				if(passed) {
					jsCount++;
				}
				
				if(apiPath.contains("/RAML10/"))
				{
					List<String> errors = testApi(apiPath);
					if(errors != null) {

						JSONArray tckErrors = (JSONArray) tckJson.get("errors");
						
						if(errors.size() != tckErrors.size() && (errors.size() == 0 || tckErrors.size() == 0)) {
							System.out.println("java parser failed: " + apiPath);
							System.out.println("\texpected:" + ( tckErrors.isEmpty() ? " no errors" : ""));
							
							for(Object error: tckErrors) {
								String message = error.toString();
								
								System.out.println("\t\t" + message);
							}
							
							System.out.println("\tactually:" + (errors.isEmpty() ? " no errors" : ""));
							
							for(String error: errors) {
								String message = error.toString();
								
								System.out.println("\t\t" + message);
							}

							System.out.println();
						} else {
							javaCount++;
							
							System.out.println("java parser passed: " + apiPath);
						}
					} else {
						System.out.println("java parser failed to load: " + apiPath);
					}
				}
				else
				{
					ignored++;
				}
				
			}
			
			System.out.println("js parser TCK tests, passed " + jsCount + '/' + count + '.');
			System.out.println("java parser TCK tests, passed " + javaCount + '/' + (count - ignored) + '.');
			
		} catch (Throwable t) {
			
		}
	}
		
	private static List<String> testApi(String path) {
		turnSystemOutOff();
		ArrayList<String> result = new ArrayList<String>();
		
		try {
			RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(path);
			
			if(ramlModelResult.hasErrors()) {
				for(ValidationResult validationResult : ramlModelResult.getValidationResults()) {
					result.add(validationResult.getMessage());
				}
			}
		} catch(Throwable t) {
			turnSystemOutOn();
			return null;
		}

		turnSystemOutOn();
		return result;
	}
	
	private static void turnSystemOutOff() {
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) throws IOException {
				
			}
		}));
	}
	
	private static void turnSystemOutOn() {
		System.setOut(systemOut);
	}
}