import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

public class O365Config {

    private String clientId;
    private String clientSecret;
    private String o365TenantId;
    private String authToken;

    public O365Config(String clientId, String clientSecret, String o365TenantId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.o365TenantId = o365TenantId;
    }

    public String createAuthToken(){

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "client_id="+clientId+"&client_secret="+ URLEncoder.encode(clientSecret) +"&Resource=https%3A%2F%2Fgraph.microsoft.com&grant_type=client_credentials&undefined=");
        Request request = new Request.Builder()
                .url("https://login.microsoftonline.com/"+o365TenantId+"/oauth2/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
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

    public HashMap<String, String> getUserDetails(){

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://graph.microsoft.com/beta/users")
                .get()
                .addHeader("Authorization", "Bearer "+authToken)
                .addHeader("IdType", "ImmutableId")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            HashMap<String, String> map = new HashMap<String, String>();
            String jsonData = response.body().string();
            JSONObject Jobject = new JSONObject(jsonData);
            JSONArray arr = Jobject.getJSONArray("value");
            for(int i=0; i < arr.length(); i++)
            {
                String onPremisesImmutableId;
                if(arr.getJSONObject(i).isNull("onPremisesImmutableId")){
                    onPremisesImmutableId = "";
                }
                else onPremisesImmutableId = arr.getJSONObject(i).getString("onPremisesImmutableId");
                String userPrincipalName = arr.getJSONObject(i).getString("userPrincipalName");
                map.put(userPrincipalName.toLowerCase(),onPremisesImmutableId.toString());
            }
            response.close();
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

