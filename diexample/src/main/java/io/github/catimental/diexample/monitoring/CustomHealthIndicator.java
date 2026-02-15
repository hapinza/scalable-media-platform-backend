package io.github.catimental.diexample.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class CustomHealthIndicator implements HealthIndicator{

    @Override
    public Health health(){
        // check if external API work 

        boolean isExternalServiceUp = checkExternalService();

        if(isExternalServiceUp){
            return Health.up().withDetail("ExternalService" , "Available").build();
        }else{
            return Health.down().withDetail("ExternalService", "Not Available").build();
        }
    }

    private boolean checkExternalService(){
        return true; // temp
    }



    



}
