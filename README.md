# NetflixClone
I’m building a Netflix-style web app as a personal project to practice full-stack development.  
It uses React for the front-end and connects to the TMDB API to show live movie and TV data.  
Later, I plan to connect it with a Spring Boot backend for login and data storage.


# Overview
The main goal is to recreate the browsing experience of a streaming platform.  
Right now, I’m focusing on learning how APIs work and how to structure reusable React components.  
Once I get the backend working, I’ll start integrating both sides together.

# What is used
FrontEnd: React, Tailwind CSS, Axios, React Router  
BackEnd: Spring Boot, MySQL, JPA
API: The Movie database API
Tools: VS Code, Git

# Features / Prgress
Netflix-style browsing interface (React + Tailwind CSS)
Watchlist and favorite collections 
Spring Boot backend for member registration and login (prsent)

1. Ser Spring Boot project structure and tested enviorment (completed)
2. Built basic member registration and login endpoints using REST API (completed)
3. Connecting backend logic with database via JPA/MySQL, implementing JWT based authentication (progress)
4. Integrate the API with React frontend using Axios (planned)
5. UI design with Tailwind CSS (planned)
   
# Authentication & Security 
1. JWT based authentication with Spring security
2. hashing the password with BCryptPasswordEncoder
3. Stateless authentication with access token
4. Authentication principal extraction for user specific operations
5. Securing APIs with Spring security filter chain
