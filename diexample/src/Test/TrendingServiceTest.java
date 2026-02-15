package io.github.catimental.diexample.Test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.catimental.diexample.Service.TrendingService;

@SpringBootTest
public enum TrendingServiceTest {
    
    @Autowired
    private TrendingService trendingService;

    @Test
    public void testTrendingWithScore(){
        var response = trendingService.trendingWithScore(7, 0, 10);
        assertEquals(10, response.getContent().size());
        
        var firstItem = response.getContent().get(0); // grab TrendingItemScoreResponse
        assertNotNull(firstItem.movieId()); // page<TrendingItemScoreResponse>
        assertTrue(firstItem.score() >= 0); 

        for(int i =0; i < firstItem.getContext().size(); i++){
            assertTrue(firstItem.getContext().get(i-1).score() >= firstItem.getContent().get(i).score());
        }


    }

}
