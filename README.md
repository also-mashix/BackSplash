# BackSplash Wallpaper Sync Project

#### A Note on Unsplash
Unsplash.com is a website devoted to high quality photography. Anyone can submit photos to be approved by a curation staff. Then users can treat photos a bit like Pinterest, creating collections of their favorite photos. One of the things that makes Unsplash special is its commitment to this library of photos being free to use and download where and how users wish. The website architecture prompts downloaders to credit photographers, but encourages them to use the photos far and wide.

#### A Way to Sync a Collection to a Computer
Right now I have a collection of photos on Unsplash that I use as wallpaper for my computer. I periodically compare my wallpaper folder on my computer to my collection on Unsplash to see if I should download anything I added recently. This side project is to automate that process. While the Unsplash API would make this a fairly simple task, it is not what the API ia really intended for. 

#### Methodology
I am approaching this problem in Java, using URL Objects to connect to the collection page. I then have the program read through the page source, splitting out all the links to the individual photo pages. Once the program is finished reading through the collection page, it appends "/download" to each link and follows the new link to a download redirect page, then saving the file to the folder in question.

#### Database
The program will check download links against a local database before downloading. If the database indicates the file has already been saved to the computer, it will be skipped.


## The Latest

#### Now Working
- scans local folder every time downloader is turned on
- separates out all image links on collection page into a list of the identifying strings
- identifies images that have already been saved locally
- downloads images that do not appear to be saved already

#### Glitches
- sometimes identifies one image that has already been saved as new
  - this image may be downloaded multiple times in one session
  - however it doesn't make duplicates, just overwrites local file

#### Next Up
- identify photographer names prior to download
  - append photographer name to file name
- choose your own collection to follow
- choose your own destination folder
