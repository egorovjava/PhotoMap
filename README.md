# Photo Map

This is the graduate task for the UCSD Coursera MOOC Object Oriented Programming in Java: https://www.coursera.org/learn/object-oriented-java.  

Set any folder name to the PhotoMap.imageDirName variable and all photos saved in this folder and it's subfolders will be showed on World map.
If a photo marker was clicked, the coresponding photo is displayed at the applicatioin window. Next click any there remove the photo from the application window.
The date created filter allow hide old photo markers.

Initially, I try to use Google Drive Service as photos source, but it is quite difficult to get file download permission for an application. You mast create the application site and put here the application privacy policy, you also have to explain why do you need this permission, send request and wait some days for approvement. I managed to read photos metadata from Google Drive. But just showing the photo markers and some it's metadata is boring, so I deside to use local storage for data source.  
 
