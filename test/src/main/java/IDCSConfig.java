import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class IDCSConfig {

    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String authToken;

    public IDCSConfig(String baseUrl, String clientId, String clientSecret) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String createAuthToken() {

        OkHttpClient client = new OkHttpClient();
        String authString = clientId + ":" + clientSecret;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String basicAuthToken = new String(authEncBytes);

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&scope=urn%3Aopc%3Aidm%3A__myscopes__&undefined=");
        Request request = new Request.Builder()
                .url(baseUrl + "/oauth2/v1/token")
                .post(body)
                .addHeader("Authorization", "Basic " + basicAuthToken)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            JSONObject Jobject = new JSONObject(jsonData);
            String authToken = Jobject.getString("access_token");
            this.authToken=authToken;
            response.close();
            return authToken;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean patchUser(String bulkIdValue, String userId) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n" +
                "  \"schemas\": [\n" +
                "    \"urn:ietf:params:scim:api:messages:2.0:PatchOp\"\n" +
                "  ],\n" +
                "  \"Operations\": [\n" +
                "        {\n" +
                "          \"op\": \"add\",\n" +
                "          \"path\": \"urn:ietf:params:scim:schemas:idcs:extension:custom:User:BulkId\",\n" +
                "          \"value\": \""+bulkIdValue+"\"\n" +
                "      }\n" +
                "  ]\n" +
                "}");
        Request request = new Request.Builder()
                .url(baseUrl + "/admin/v1/Users/" + userId)
                .patch(body)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        Response response;

        try {
            response = client.newCall(request).execute();
            boolean outcome = response.isSuccessful();
            response.close();
            return  outcome;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public HashMap<String, String> getUserDetails(){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(baseUrl + "/admin/v1/Users?attributes=emails")
                .get()
                .addHeader("Authorization", "Bearer "+ authToken)
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            JSONObject Jobject = new JSONObject(jsonData);
            JSONArray arr = Jobject.getJSONArray("Resources");
            HashMap<String, String> map = new HashMap<String, String>();
            for(int i=0; i < arr.length(); i++)
            {
                String id = arr.getJSONObject(i).getString("id");
                JSONArray emails = arr.getJSONObject(i).getJSONArray("emails");
                String email="";
                for(int j = 0 ; j < emails.length() ; j++)
                {
                    boolean primary = emails.getJSONObject(j).getBoolean("primary");
                    if(primary){
                        email = emails.getJSONObject(j).getString("value");
                    }
                }
                map.put(email.toLowerCase(),id);
            }
            response.close();
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
