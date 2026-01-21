package io.github.catimental.diexample.Test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.sevlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootTest
@AutoConfigureMockMvc
public class WatchlistContollerTest {
    
    @Autowired
    public MockMvc mockMvc;


    @Test 
    public void testAddToWatchlist throws Exception{
        mockMvc.perform(post("/watchlist/123"))
                .header("Authorization", "Bearer validToken")
                .andExpect(status().isOk());
    }

    @Test
    public void testGetWatchlist() throws Exception{
        Watchlist mockWatchlist = new Watchlist("123L", "01-20-2026");

        when(watchlistRepository.findAll()).thenReturn(Collections.singletonList(mockWatchlist));

        mockMvc.perform(get("/watchlist"))
                .header("Authorization", "Bearer validToken")
                .andExpect(jsonPath("$[0].movieId").value("123L"));

    }


}
