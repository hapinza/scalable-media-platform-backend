package io.github.catimental.diexample.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpUtil {
    private ClientIpUtil(){}


    // brower -> load balancer -> Spring server
    // load balancer: provess massive request
    // real IP vs load balancer IP different to protect the server
    public static String getClientIp(HttpServletRequest request){
        String xff = request.getHeader("X-Forwarded-For");
        if(xff != null && !xff.isBlank()){
            // ex) X-Forwarded-For: 203.0.113.10, 10.0.0.1, 10.0.0.2
            return xff.split(",")[0].trim();
        }

        // load balancer IP, not real IP
        return request.getRemoteAddr();

    }



}
