# YouTubeToSpotify

A command line application that allows the user to replicate the contents of a personal Youtube playlist to another playlist (new or existing) in his Spotify account.

--- 

## Prerequisites  

In order to use the Spotify and Youtube APIs we need the following:

1.**Spotify**: ClientID, Client Secret  
Go to https://developer.spotify.com/dashboard/login and create a new app  
After agreeing to the terms you will have access to the application's Client ID and Client Secret

2.**Youtube**: Api key  
Go to https://console.developers.google.com/apis/api/youtube.googleapis.com/credentials and click on  
**Create Credentials** -> **Api Key**  

## Setup  

Firstly, you need to have gradle installed. If you dont have it in your system then check this first:  
https://docs.gradle.org/current/userguide/installation.html  

Clone this repo and open a terminal window to the project's folder  
For the first time only run this command:  
   
`gradlew build`  
 
After that we are almost ready! We need to store the ClientID, Client Secret and Youtube Api key  
in our .properties file. To do so run the following commands:  

`gradlew run --args="credentials id YOUR_CLIENT_ID"`  
`gradlew run --args="credentials secret YOUR_CLIENT_SECRET"`  
`gradlew run --args="credentials youtube YOUR_API_KEY"`  
  
## Authorization  
  
By running the command gradlew run --args="auth" we get an authorization token that lasts for 1 hour.  
That basically means that you have to use this command every time you want to use this app for the next hour.  
  
## Transfer a playlist  
  
In order to transfer a playlist you need to run:  
  
`gradlew run --args="transfer URL PLAYLIST"`  
  
where URL is the youtube URL of your YouTube playlist and PLAYLIST the name of your  
Spotify playlist (new or existing) that you want to add the songs to.  
  
## Get Playlists  
  
`gradlew run --args="playlists"` : Shows all the Spotify playlists that you own  
  
## --help  
  
`gradlew run --args="-h"` or `gradlew run --args="--help"` : Shows help message