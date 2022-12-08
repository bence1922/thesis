package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Add orgs then press 0");
        List<String> orgs = new ArrayList<>();
        String input = "";
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        while(!input.equals("0")){
            orgs.add(input);
            try {
                input = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        orgs.remove(0);
        String json = "[\n";
        for (int i = 0; i < orgs.size(); i++) {
            for (int j = 0; j < 3; j++){
                json += "\t{";
                json += row("name", doubleLiner(nameCreator(orgs.get(i)+"MSP",j))) + ",";
                json += row("policy",doubleLiner("OR('"+ orgs.get(i)+"MSP.member')")) + ",";
                json += row("requiredPeerCount", "0") + ",";
                json += row("maxPeerCount", "1") + ",";
                json += row("blockToLive", "0") + ",";
                json += row("memberOnlyRead", readCreator(j)) + ",";
                json += row("memberOnlyWrite", writeCreator(j)) + ",";
                json += "\n\t\t" + doubleLiner("endorsementPolicy") + ": {" + "\n\t\t\t" + doubleLiner("majority") +
                    ": " + doubleLiner("OR('" + orgs.get(i) + "MSP.member')") + "\n\t\t}";
                json += "\n\t}";
                if (!(i == orgs.size()-1 && j == 2)){
                    json += ",";
                }
                json += "\n";
            }

        }
        json += "]";
        System.out.println(json);

    }
    public static String doubleLiner(String text){
        char doubleLine = 34;
        return doubleLine + text + doubleLine;
    }
    public static String row(String text1, String text2){
        return "\n" + "\t\t" + doubleLiner(text1) + ": " + text2 ;
    }
    public static String nameCreator(String org, int i){
        switch (i){
            case 0:
                return org + "_PrivateCollection";
            case 1:
                return org + "_PermissionedCollection";
            default:
                return org + "_ModifiableCollection";
        }
    }
    public static String readCreator(int i){
        switch (i){
            case 0:
            case 2:
                return "true";
            default:
                return "false";
        }
    }
    public static String writeCreator(int i){
        switch (i){
            case 0:
                return "true";
            default:
                return "false";
        }
    }
}