//make another tmdn.js to allow to use api_key for evry access and 
// specify its function compared to the axios.js which is the base


import axios from "axios";
// from the file "axios" in npm



const tdmb = axios.create({
baseURL: "https::/api.themoviedb/org/3";
params:{   // http query (parameter to send)
    api_key: process.env.REACT_APP_TMBD_API_KEY,
    //process.env  -> environment variable
    lanuage: "en-US"
},


});

export default tdmb;
// not from the npm, whcih can be from "react" or "axios"
// all of the functions or variables should be exported to be imported 
