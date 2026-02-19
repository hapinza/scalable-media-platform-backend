package io.github.catimental.diexample.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// import org.aspectj.bridge.Message;



public class TokenHashUtil {

    private TokenHashUtil(){}

    public static String sha256Hex(String raw){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        }catch(Exception e){
            throw new IllegalStateException("SHA-256 hashing failed", e);
        }
    }


    private static String toHex(byte[] bytes){
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for(byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    


}

