package com.micah.BackSplash;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.micah.BackSplash.DAO.FolderScanner;
import com.micah.BackSplash.DAO.JDBCPhotoDAO;
import com.micah.BackSplash.model.Photo;

public class Downloader {

	private static URL collectionPage;

	private static BasicDataSource dataSource;
	private static JDBCPhotoDAO dao;

	public static void main(String[] args) {
		//System.setProperty("http.keepAlive", "false");
		
		// first establish connection to collection page
		collectionPage = connectToCollection();

		// get list of photo keys
		List<String> photoKeys = readFromURL();

		// check if keys are already in database OR already saved on local drive
		List<Photo> unsavedPhotos = findNewPhotos(photoKeys);
		
		if(unsavedPhotos.size() == 0) {
			System.out.println("\nNo photos to download right now.\nEnding program.");
			System.exit(0);
		}
		
		// download new photos, then add info to database
		for (Photo p : unsavedPhotos) {
			Photo downloadedP = downloadPhoto(p);
			System.out.println("Successfully saved photo: " + downloadedP.getPhotoFilePath());
			//unsavedPhotos.remove(p);	
		}
		
		System.out.println("\nEnding program.");
		System.exit(0);
		
	}

	private static URL connectToCollection() {

		dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/unsplash_watcher");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");

		dao = new JDBCPhotoDAO(dataSource);

		try {
			collectionPage = new URL("https://unsplash.com/collections/4929343/landscape");
			URLConnection collectionPageConnection = collectionPage.openConnection();
			collectionPageConnection.connect();
			System.out.println("Connection established.");
			return collectionPage;
		} catch (MalformedURLException e) {
			System.out.println("Problem with link to collection");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("openConnection() to collection failed");
			e.printStackTrace();
		}
		System.out.println("Connection was NOT established.");
		return null;
	}

	private static List<String> readFromURL() {
		List<String> photoKeys = new ArrayList<String>();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(collectionPage.openStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String inputLine;
		try {
			while ((inputLine = in.readLine()) != null) {

				// check for link text that only precedes photo hash
				if (inputLine.contains("href=\"/photos/")) {

					// splits line at each spot where the link to photo begins
					String[] splitLine = inputLine.split("href=\"/photos/");
					
					for (int i = 1; i < splitLine.length; i++) {
						
					
						// selects second half of link and splits along " -- where the hash ends
						String[] splitEndOff = splitLine[i].split("\"");
	
						// the first string in the array should contain the hash and nothing else
						String photoKey = splitEndOff[0];
	
						// adds the hash to the list, assuring unique hash
						photoKeys.add(photoKey);
					}
				}

				// looks for the line that appears once on page, signaling the end of the
				// current collection
				if (inputLine.contains("You might also like")) {
					// stops going line by line through page because there should be nothing else to
					// look at
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return photoKeys;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return photoKeys;
	}

	private static List<Photo> findNewPhotos(List<String> photoKeys) {
		List<Photo> newPhotos = new ArrayList<Photo>();

		// Scan folder to update database
		FolderScanner folderScanner = new FolderScanner();
		folderScanner.scanFolderForImages();
		
		// get list of currently saved photos
		//List<Photo> currentPhotos = dao.getAllPhotos();
		
		for (String hash : photoKeys) {
			Photo p = new Photo();
			p.setHash(hash);
			boolean isAlreadyDownloaded = folderScanner.checkIfAlreadyInDatabase(p);
			if(!isAlreadyDownloaded) {
				newPhotos.add(p);
			}
		}
		System.out.println("\nThere are " + newPhotos.size() + " photos to be downloaded.\n");

		return newPhotos;
	}

	private static Photo downloadPhoto(Photo p) {

		String redirectUrl = "https://unsplash.com/photos/" + p.getHash() + "/download";
		
		// THIS IS REDIRECTING ON ITS OWN
		//String downloadUrl = parseRedirect(redirectUrl);
		
		String destinationFile = "/Users/micahkaufmanwright/OneDrive/Pictures/Wallpaper/" + p.getPhotographerName()
				+ "-" + p.getHash() + "-unsplash.jpg";

		saveImage(redirectUrl, destinationFile);
		p.setPhotoFilePath(destinationFile);

		return p;
	}

//	private static String parseRedirect(String redirectUrlString)  {
//		String responseURL = "";
//		URL initialDownloadURL = null;
//		
////		try {
////			preRedirectURL = new URL(redirectUrl);
////		} catch (MalformedURLException e1) {
////			System.out.println("Error: Could not turn '" + redirectUrl + "' into URL object");
////			e1.printStackTrace();
////		}
//		
//		HttpURLConnection redirectDownloadPageConnection = null;
//		try {
//			initialDownloadURL = new URL(redirectUrlString);
//			redirectDownloadPageConnection = (HttpURLConnection)initialDownloadURL.openConnection();
//			
////			redirectDownloadPageConnection.setRequestMethod("GET");
////			redirectDownloadPageConnection.setUseCaches(false);
////			redirectDownloadPageConnection.setDefaultUseCaches(false);
////			redirectDownloadPageConnection.setDoOutput(true);
////			redirectDownloadPageConnection.setRequestProperty("Accept", "text/html; charset=UTF-8");
////			redirectDownloadPageConnection.setInstanceFollowRedirects(true);
////			redirectDownloadPageConnection.setReadTimeout(500);
////			redirectDownloadPageConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
//			redirectDownloadPageConnection.connect();
//		} catch (IOException e2) {
//			System.out.println("Error: Could not .openConnection() with '" + redirectUrlString + "' as a URL object");
//			e2.printStackTrace();
//		}
//		
////		try {
////			redirectDownloadPageConnection.connect();
////			 
////			// had .getOutputStream before
////			//redirectDownloadPageConnection.getInputStream();
////		} catch (IOException e1) {
////			System.out.println("Error: Could not .connect() to '" + redirectUrlString + "' as URL object in URLConnection object");
////			e1.printStackTrace();
////		}
//
////		try {
////			if(redirectDownloadPageConnection.getResponseCode() != 302) {
////				
////				// throw something here
////			} else {
////				System.out.println(redirectDownloadPageConnection.getResponseCode());
////			}
////		}catch(IOException e3) {
////			// EAT YUM
////		}
//		
//		//return redirectDownloadPageConnection.getHeaderField("location");
//		
//		BufferedReader in = null;
//		try {
//			in = new BufferedReader(new InputStreamReader(redirectDownloadPageConnection.getInputStream()));
//		} catch (IOException e) {
//			System.out.println("Error: Could not read BufferedReader in");
//			e.printStackTrace();
//		}
//
//		
//		// *** ERROR HERE ***
//		// *** INPUTLINE ONLY RECEIVES THIS: ���� JFIF  H H  ��
//		// *** SHOULD RECEIVE REDIRECT PAGE SOURCE....
//		// *** FIX THIS
//		
//		String inputLine;
//		try {
//			while ((inputLine = in.readLine()) != null) {
//				// splits line in three around the two quotes enclosing the link
//				String[] splitLine = inputLine.split("\"");
//				responseURL = splitLine[1];
//				System.out.println("This is the split out response url: " + responseURL);
//			}
//		} catch (Exception e) {
//			System.out.println("Error: Could not readLine() or splitLine");
//			e.printStackTrace();
//		}
//
//		return responseURL;
//	}

	public static void saveImage(String imageUrl, String destinationFile) {
		URL url = null;
		try {
			url = new URL(imageUrl);
		} catch (MalformedURLException e) {
			System.out.println("Error with String imageUrl: '" + imageUrl +"'\nAnd/or with String destinationFile: " + destinationFile);
			e.printStackTrace();
		}
		InputStream ins = null;
		try {
			ins = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OutputStream outs = null;
		try {
			outs = new FileOutputStream(destinationFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] b = new byte[2048];
		int length;

		try {
			while ((length = ins.read(b)) != -1) {
				outs.write(b, 0, length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ins.close();
			outs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 * *** HARD MODE ***
	 * 
	 * Go to >>> "https://unsplash.com/collections/4929343/landscape" search page
	 * source for "href="/photos/" collect id that follows ie. "LBI7cgq3pbM"
	 * 
	 * enter into download link format:
	 * "https://unsplash.com/photos/LBI7cgq3pbM/download"
	 * 
	 * save to folder "/Users/micahkaufmanwright/OneDrive/Pictures/Wallpaper" (on TE
	 * mac laptop)
	 * 
	 */

}
