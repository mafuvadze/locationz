package com.score.senz.utils;

import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.pojos.Senz;

import java.util.HashMap;

/**
 * Created by eranga on 8/27/15.
 */
public class SenzParser {

    public static Senz parse(String senzMessage) {
        // init sez with
        Senz senz = new Senz();
        senz.setAttributes(new HashMap<String, String>());

        // part except the signature of the senz message is the payload
        String senzPayload = senzMessage.substring(0, senzMessage.lastIndexOf(" "));
        senz.setPayload(senzPayload);

        // parse senz
        String[] tokens = senzMessage.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i == 0) {
                // query type at first (SHARE, GET, DATA)
                senz.setSenzType(SenzTypeEnum.valueOf(token.toUpperCase()));
            } else if (i == tokens.length - 1) {
                // signature at the end
                senz.setSignature(token);
            } else if (tokens[i].startsWith("@")) {
                // @0775432012
                senz.setReceiver(token.substring(1));
            } else if (token.startsWith("^")) {
                // ^senz, ^0775432015
                senz.setSender(token.substring(1));
            } else if (token.startsWith("#")) {
                if (token.equals("#time") || token.equals("pubkey")) {
                    // #time 2453234, #pubkey ac23edf432fdg
                    senz.getAttributes().put(token, tokens[i + 1]);
                    i++;
                } else {
                    if (senz.getSenzType() == SenzTypeEnum.DATA) {
                        // #lat 3.2343 #lon 4.3434
                        senz.getAttributes().put(token, tokens[i + 1]);
                        i++;
                    } else {
                        // #lat #lon
                        senz.getAttributes().put(token, "");
                    }
                }
            }
        }

        System.out.println(senz.getSender());
        System.out.println(senz.getReceiver());
        System.out.println(senz.getSenzType());
        System.out.println(senz.getSignature());
        System.out.println(senz.getAttributes().entrySet());

        return senz;
    }

    public static String getSenzPayload(Senz senz) {
        // add senz type to payload
        String payload = senz.getSenzType().toString();

        // add attributes to payload
        for (String key : senz.getAttributes().keySet()) {
            if (key.equalsIgnoreCase(senz.getAttributes().get(key))) {
                // GET or SHARE query
                // param and value equal since no value to store (SHARE #lat #lon)
                payload = payload.concat(" ").concat("#").concat(senz.getAttributes().get(key));
            } else {
                // DATA query
                payload = payload.concat(" ").concat("#").concat(key).concat(" ").concat(senz.getAttributes().get(key));
            }
        }

        // add sender and receiver
        payload.concat(" ").concat("@").concat(senz.getReceiver());
        payload.concat(" ").concat("^").concat(senz.getSender());

        return payload;
    }

    public static String getSenzMessage(String payload, String signature) {
        String senzMessage = payload + " " + signature;

        return senzMessage.replaceAll("\n", "").replaceAll("\r", "");
    }

    public static void main(String args[]) {
        String senzMessage1 = "DATA" + " " +
                "#pubkey" + " " + "keyyyyy" + " " +
                "#time" + " " + "timestamp" + " " +
                "@senz" + " " +
                "^0775432015" + " " +
                "signatureeee";

        String senzMessage2 = "SHARE" + " " +
                "#lat" + " " +
                "#lon" + " " +
                "#time" + " " + "timestamp" + " " +
                "@senz" + " " +
                "^0775432015" + " " +
                "signatureeee";

        parse(senzMessage1);
        parse(senzMessage2);
    }
}