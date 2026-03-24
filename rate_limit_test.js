import http from 'k6/http';
import {check} from 'k6';

import { Counter } from 'k6/metrics';

export const options = {
    vus: 5,
    duration: '10s' 
}


const ok200 = new Counter('http_200');
const rl429 = new Counter('http_429');
const other = new Counter('http_other');

export default function(){
    const url = 'http://localhost:8080/movies/trending';

    const res = http.get(url);  // send reqeust with get and get response

    if(res.status === 200) ok200.add(1);
    else if(res.status === 429) rl429.add(1);
    else other.add(1);

    check(res, {
        'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
    });

}



