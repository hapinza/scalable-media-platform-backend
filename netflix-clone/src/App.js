import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import React, { useEffect, useState } from 'react';
import tmdb from './api/tmdb';
import reqests from './api/tmdbRequests';



function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/register" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </BrowserRouter>
  );
}

function App(){
  const [movies, setMovies] = useState([]);

useEffect(() => {
tmdb.get(requests.trending)
  .then(response => {   // async
    setMovies(response.data.results);
  })
  .catch(error => console.error("Error fetching trading movies"));
}, []);



return (
  <div> 
    <h1>Tredning Movies</h1>
    <div>
      {movies.map(movie => (
        <div key = {movie.id}>
          <h2>{movie.title}</h2>
          <img src = 
        </div>
      ))}
    </div>


  </div>


)






export default App;

// import logo from './logo.svg';
// import './App.css';

// function App() {
//   return (
//     <div className="App">
//       <header className="App-header">
//         <img src={logo} className="App-logo" alt="logo" />
//         <p>
//           Edit <code>src/App.js</code> and save to reload.
//         </p>
//         <a
//           className="App-link"
//           href="https://reactjs.org"
//           target="_blank"
//           rel="noopener noreferrer"
//         >
//           Learn React
//         </a>
//       </header>
//     </div>
//   );
// }

// export default App;
