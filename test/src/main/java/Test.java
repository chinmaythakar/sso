import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class Test {
    public static void main(String[] args) {

        IDCSConfig idcsConfig;
        O365Config o365Config;

        Scanner sc = new Scanner(System.in);

        String o365ClientId = "";
        String o365ClientSecret = "";
        String o365TenantId = "";

        //Get IDCS Details

        System.out.println("Enter IDCS Client ID:");
        String idcsClientId = sc.nextLine();
        System.out.println("Enter IDCS Client Secret:");
        String idcsClientSecret = sc.nextLine();
        System.out.println("Enter IDCS Base Url: ");
        String idcsBaseUrl = sc.nextLine();

        if (!idcsClientId.isEmpty() && !idcsClientSecret.isEmpty() && !idcsBaseUrl.isEmpty()) {

            //initialize and get auth token for IDCS
            idcsConfig = new IDCSConfig(idcsBaseUrl, idcsClientId, idcsClientSecret);
            idcsConfig.createAuthToken();


            int flag = 0;
            while (flag == 0) {
                System.out.println("Enter 1 to update single user");
                System.out.println("Enter 2 to update all users");
                System.out.println("Enter 3 to quit");
                System.out.println("Enter your choice:");
                int choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        Scanner sc1 = new Scanner(System.in);
                        System.out.println("Enter user email address:");
                        String email = sc1.nextLine();
                        System.out.println("Enter equivalent Immutable Id:");
                        String immutableId = sc1.nextLine();
                        if (!email.isEmpty() && !immutableId.isEmpty()) {
                            HashMap<String, String> user = idcsConfig.getUserDetails();
                            if (user.containsKey(email)) {
                                boolean patch = idcsConfig.patchUser(immutableId, user.get(email));
                                updateCheck(patch, email);
                            } else System.out.println("Email Does not exist");
                        }
                        break;

                    case 2:
                        Scanner sc2 = new Scanner(System.in);
                        if (o365ClientId.isEmpty() || o365ClientSecret.isEmpty() || o365TenantId.isEmpty()) {
                            System.out.println("Enter Client ID for O365: ");
                            o365ClientId = sc2.nextLine();
                            System.out.println("Enter Client Secret for O365: ");
                            o365ClientSecret = sc2.nextLine();
                            System.out.println("Enter tenant ID: ");
                            o365TenantId = sc2.nextLine();
                        }

                        if (!o365ClientId.isEmpty() && !o365ClientSecret.isEmpty() && !o365TenantId.isEmpty()) {
                            o365Config = new O365Config(o365ClientId, o365ClientSecret, o365TenantId);
                            o365Config.createAuthToken();
                            HashMap<String, String> idcsUsers = idcsConfig.getUserDetails();
                            HashMap<String, String> o365Users = o365Config.getUserDetails();
                            for (Map.Entry<String, String> entry : idcsUsers.entrySet()) {
                                if (o365Users.containsKey(entry.getKey())) {
                                    if (!o365Users.get(entry.getKey()).equals("")) {
                                        boolean patch = idcsConfig.patchUser(o365Users.get(entry.getKey()), entry.getValue());
                                        updateCheck(patch, entry.getKey());
                                    }
                                }
                            }
                            System.out.println("Update Complete, check logs for individual success/failure");
                        } else {
                            System.out.println("Office 365 parameter values cannot contain null");
                        }
                        break;

                    case 3:
                        flag = 1;
                        break;

                    default:
                        System.out.println("Invalid Input, Try Again ");
                }
            }
        } else {
            System.out.println("IDCS parameter values cannot contain null");
        }
    }


    private static void updateCheck(boolean patch, String email) {
        if (patch)
            System.out.println("User " + email + " updated");
        else
            System.out.println("Update of user " + email + " Unsuccessful");

    }
}