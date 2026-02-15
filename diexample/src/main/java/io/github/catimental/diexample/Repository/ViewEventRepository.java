package io.github.catimental.diexample.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import io.github.catimental.diexample.domain.analytics.ViewEvent;


import java.time.LocalDateTime;

/*
How Query scans 
FROM: What enriry we are using
WHERE: filtering
GROUP BY : under what variable we sort it into group
SELECT : which variable we count
ORDER BY: how we order based on the count(SELECT)

 */

public interface ViewEventRepository extends JpaRepository<ViewEvent, Long>{
                                        // or count(v.movieId) as viewCount
                                        // since movieId is nullable = false
    @Query("""
            select v.movieId as movieId, count(v) as viewCount
            from ViewEvent v   
            where v.createdAt >= :from
            group by v.movieId
            order by count(v) desc
            """)
    /*
    Page(Spring provides) vs List
    method: getContent(List)
    getNumber: page number
    getSize: page size
    getTotalElement: total size
    getTotalPages: total size of pages
    hasnext()

    like we did in ResponseEntity<T> , it is Page<T>


    grap the data. not store
    */
    Page<TrendingProjection> findTrending(@Param("from") LocalDateTime from, Pageable pageable);



        interface TrendingProjection{
            Long getMovieId();
            Long getViewCount();
        }
    


    //  There is no meaning for combining the two DB
    // but first the variable movieId is connection between them
    // we just combine the two db and then grab the data we want
    @Query 
    ("""
     select 
     v.movieId as movieId,
     count(distinct v.id) as viewCount,
     count(distinct case when ml.like = true then ml.id end) as likeCount,
     (count(distinct v.id)
     + (count(distinct case when ml.like true then ml.id end) * :likeWeight)) as score
     from ViewEvent v
     left join MovieLike ml
            on ml.movieId = v.movieId
     where v.createdAt >= :from
     group by v.movieId
     order by score desc
            """
    )
    Page<TrendingScoreProjection> findTrendingWithScore(
        @Param("from") LocalDateTime from,
        @Param("likeWeight") Long likeWeight,
        Pageable pageable
    );

        interface TrendingScoreProjection{
        Long getMovieId();
        Long getViewCount();
        Long getLikeCount();
        Long getScore();   
    }
        

    


        

}
