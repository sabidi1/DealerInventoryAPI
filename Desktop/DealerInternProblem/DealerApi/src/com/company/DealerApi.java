package com.company;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DealerApi {

    public static void main(String[] args) {
        try {
          DealerApi.GetDataSet();
          
        } catch (Exception e) {
        }
    }

    //api call data set   input: none  output: dataset id
    public static JSONObject GetDataSet() throws Exception {
        String url = "http://vautointerview.azurewebsites.net/api/datasetId";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
 
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject myResponse = new JSONObject(response.toString());

        //get dataset ID
        String dataSetID = (myResponse.getString("datasetId"));
      
        //Save vehicileID list in a string array
        JSONArray listVehicleID = GetVehicleID(dataSetID);
        
        String answerString = GetVehicleDetails(dataSetID,listVehicleID);
   
        GetPostMethod(dataSetID,answerString);

        return myResponse;
    }

    //API call dataset   INPUT:  datasetId  OUTPUT: VehicleIDlist           
    public static JSONArray GetVehicleID(String dataSetID) throws Exception {
        String url = "http://vautointerview.azurewebsites.net/api/"+ dataSetID +"/vehicles";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject myResponse = new JSONObject(response.toString());
        JSONArray getVehicleArray = myResponse.getJSONArray("vehicleIds");

        return getVehicleArray;
    }
    public static JSONObject dealers = null;
    
    public static JSONObject FindDealer(Integer dealerId) {
        try {            
            JSONArray dealerList = dealers.getJSONArray("dealers");
            for (int i = 0; i < dealerList.length(); i++) {
                JSONObject dealer = (JSONObject) dealerList.get(i);
                if (dealer.getInt("dealerId") == dealerId)
                {
                    return dealer;
                }
            }
        }
        catch (JSONException ex)
        {
            // Do nothing
        }
        
        return null;
    }
    //
    public static String GetVehicleDetails(String dataSetID,JSONArray listVehicleID) throws Exception{
                
        JSONArray vehicles = new JSONArray();
        //for each vehicle ID, print vehicle details
        for(int i = 0; i < listVehicleID.length(); i++ ) {
            Integer vehicleId = listVehicleID.getInt(i);
            String url = "http://vautointerview.azurewebsites.net/api/" + dataSetID + "/vehicles/" + vehicleId;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            
            con.setRequestMethod("GET");

           // int responseCode = con.getResponseCode();
           // System.out.println("\nSending 'GET' request to URL : " + url);
           // System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            JSONObject myResponse = new JSONObject(response.toString());
            Integer dealerId = myResponse.getInt("dealerId");
            JSONObject dealerObj = null;
            // Check if you have this dealer Id in the array
            if (dealers != null)
            {
                //System.out.println(dealers.get("dealers"));
                // if the dealer already exist then get that object from the array
                dealerObj = FindDealer(dealerId);
                if (dealerObj == null) {
                    dealerObj = new JSONObject();
                    dealerObj.put("dealerId", dealerId.toString());
                    dealerObj.put("name", GetDealerInformation(dataSetID,dealerId));
                    dealerObj.put("vehicles", new JSONArray());
                  dealers.append("dealers", dealerObj);
              
                }
            }
            else
            {
                dealers = new JSONObject();
                // add the new dealer object to array
                dealerObj = new JSONObject();
                dealerObj.put("dealerId", dealerId.toString());
                dealerObj.put("name", GetDealerInformation(dataSetID,dealerId));
                dealerObj.put("vehicles", new JSONArray());              //dealerObj = (org.json.JSONObject) {"dealerId":["373881864"],"Vehicles":[[]]}
                dealers.append("dealers", dealerObj);
            }
            myResponse.remove("dealerId");
            
            vehicles = (JSONArray) dealerObj.get("vehicles");
            vehicles.put(myResponse);
        }
        String jsonString = dealers.toString();
        
        return jsonString;
    }
    public static String GetDealerInformation(String dataSetID,int arrayDealerID) throws Exception {
        JSONObject myResponse = new JSONObject();
            
            String url = "http://vautointerview.azurewebsites.net/api/" + dataSetID + "/dealers/" +arrayDealerID;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //print in String
            
            myResponse = new JSONObject(response.toString());
            String dealerName = myResponse.getString("name");
           // System.out.println(dealerName);
        
        
        return dealerName;
    }

    public static void  GetPostMethod(String dataSetID,String answerString ) throws Exception {
         String query_url = "http://vautointerview.azurewebsites.net/api/"+dataSetID + "/answer";
           String json = answerString;
           try {
           URL url = new URL(query_url);
           HttpURLConnection conn = (HttpURLConnection) url.openConnection();
           conn.setConnectTimeout(5000);
           conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
           conn.setDoOutput(true);
           conn.setDoInput(true);
           conn.setRequestMethod("POST");
           OutputStream os = conn.getOutputStream();
           os.write(json.getBytes("UTF-8"));
           os.close(); 
           
            int responseCode = conn.getResponseCode();
          //  System.out.println("\nSending 'GET' request to URL : " + url);
           System.out.println("Response Code : " + responseCode);
          // System.out.println(json);
           conn.disconnect();
           } catch (IOException e) {
   			System.out.println(e);
   		}
    }
}
