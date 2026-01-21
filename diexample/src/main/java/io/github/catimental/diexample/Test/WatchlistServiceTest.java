package io.github.catimental.diexample.Test;
import org.junit.jupiter.api.BeforeEach;

import io.github.catimental.diexample.Repository.MemberRepository;
import io.github.catimental.diexample.Service.WatchlistService;
import io.github.catimental.diexample.Repository.WatchlistRepository;



import static org.mockito.Mockito.*;



public class WatchlistServiceTest {
    
    private WatchlistService watchlistService;
    private WatchlistRepository watchlistRepository;
    private MemberRepository memberRepository;



   
    @BeforeEach
    public void setUp(){
        watchlistRepository = mock(WatchlistRepository.class);
        memberRepository = mock(MemberRepository.class);
        watchlistService = new WatchlistService(WatchlistRepository, memberRepository);
    }

    @Test
    public void testAddToWatchlist(){
        Long memberId = 1L;
        Long movieId = 123L;

        when(watchlistRepository.existsByMemberIdAndMovieId(memberId, movieId)).thenReturn(false);
        when(memberRepository.findById(memerId)).thenReturn(Optional.of(new Member()));


        watchlistService.add(memberId, movieId);

        verify(watchlistRepository, times(1)).save(any(Watchlist.class));

    }


    @Test
    public void testAddToWatchlist_AlreadyExists(){
        Long memberId = 1L;
        Long movieId = 123L;

        when(watchlistRepository.findByMemberIdAndMovieId(memberId, movieId)).thenReturn(true);

        assertThrows(ApiException.class, () -> watchlistService.add(memberId, movieId));


    }




}
