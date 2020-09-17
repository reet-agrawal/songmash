# TEAM MEMBERS:

Geoffrey Glass (gglass)
	Strengths: some machine learning experience, Java, Python, music enthusiast
	Weaknesses: frontend

Jason Senthil (jsenthil)

Strengths: Project Planning, facilitating teamwork, broad experience with most languages (Java, Python, JavaScript, React, etc)
	Weaknesses: Git, frontend, design

Reet Agrawal (ragrawal)
	Strengths: Design, UIUX, Java, Experience with React.js, robotics
	Weaknesses: algorithms, project structuring

Michael Freeman (mfreema3)
Strengths: Spotify API, web development (React.js), databases (Postgres), music industry experience
	Weaknesses: time management, Java Generics, Git

# Project: Spotify Average Listener Playlists

Want to know, on average, what your friends are listening to on Spotify? Use case: automatically create a party playlist that will satisfy the musical preferences of as many attendees as possible. 

Our project aims to generate such playlists based on the contents of your friends’ playlists. For example, if ten of your fifteen friends added song A to a playlist, this song should be added to the ‘average’ playlist. On the other hand, if only one of your fifteen friends have song B in a playlist, this song should not be added to the ‘average’ playlist. ‘Similarity’ between listeners can be as simple as tallying the frequency of appearances of songs across playlists or as complicated as relating songs that are likely to be agreeable based on related artists and songs. In their backend, Spotify uses metrics of song and artist similarity to suggest new material to individual users. However, one cannot use these similarity metrics to create a playlist for groups of people.  

# Critical Features: 
Create a single Spotify playlist based off a users' preferences. Using the Spotify API, we can create the playlist through an authorized user’s account and use Spotify’s front end to view and play it. 
Anticipated Challenges: determining listener habit similarity is a data analysis problem. We will need to relate a large set of data points to determine a metric of similarity between songs and artists based on the contexts in which they appear (playlists). Appearing together frequently in playlists, collaborations, or ‘related artist’ pages on Spotify may mean higher similarity while never appearing together in any of these contexts may mean lower similaarity but not necessarily. The challenge is creating a reasonable algorithm for determining playlist, song, and artist similarity based on existing public Spotify playlists and/or private playlists to which the user grants our application access. 
Why this feature is included: it is the basic functionality of our project.
Nice to have Features: 

# Requirements: 
Integrate with Spotify API
- Connect to user’s profile and friends’ profile
- Read the contents of user’s and friends’ playlists
- Create a new playlist under the authorized user’s profile
Analyze Similarity of Playlist Contents for n people
- Given n people’s Spotify playlists, construct a playlist that represents the average listening habits of all n people
Methods: clustering, song similarity based on playlist appearances (songs that appear more frequently together in public playlists are considered similar), other metrics of similarity (acousticness, instrumentality, energy, etc. values describing the audio content that are calculated and retrievable from Spotify API - Audio Features)
Resources
Over 1 million public Spotify playlists can be accessed here: Chartmetric Playlists
Directory of clusters of playlists by listener demographics and genre maintained by Spotify’s head data scientist here: Everynoise
Spotify Global/Viral Charts can be found here: Charts

PROJECT DESIGN PRESENTATION: https://docs.google.com/presentation/d/1UKDOrkBZLV4Ei0rHdsoyWlPI43oNs9LqX5CQRCwsJV0/edit?usp=sharing


# Other Links
Spotify Developer Dashboard App: https://developer.spotify.com/dashboard/applications/2b4e890519954a35aae9db3e3f640eee
- we use this application to make requests to the Spotify API