package io.github.catimental.diexample.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.Repository.ViewEventRepository;
import io.github.catimental.diexample.domain.analytics.ViewEvent;



@Service
@Transactional
public class ViewEventService {
    private final ViewEventRepository viewEventRepository;

    public ViewEventService(ViewEventRepository viewEventRepository){
        this.viewEventRepository = viewEventRepository;
    }


    public void track(Long memberIdorNull, Long movieId){
        viewEventRepository.save(new ViewEvent(memberIdorNull, movieId));
    }
}
