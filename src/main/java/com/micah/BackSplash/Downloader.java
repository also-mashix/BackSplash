package com.micah.BackSplash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.micah.BackSplash.DAO.JDBCPhotoDAO;
import com.micah.BackSplash.model.Photo;

public class Downloader {

	private static URL collectionPage;
	
	@Autowired
	private static JDBCPhotoDAO dao;

	public static void main(String[] args) {
		// first establish connection to collection page
		collectionPage = connectToCollection();

		// get list of photo keys
		List<String> photoKeys = readFromURL();

		// check if keys are already in database OR already saved on local drive
		List<Photo> unsavedPhotos = findNewPhotos(photoKeys);
		
		// download new photos, then add info to database

	}

	private static URL connectToCollection() {
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

					// splits line in two where the link to photo begins
					String[] splitLine = inputLine.split("href=\"/photos/");

					// selects second half of line and splits along "/" -- where the hash ends
					String[] splitEndOff = splitLine[1].split("/");

					// the first string in the array should contain the hash and nothing else
					String photoKey = splitEndOff[0];

					// adds the hash to the list, assuring unique hash
					photoKeys.add(photoKey);
				}

				// looks for the line that appears once on page, signalling the end of the
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
		
		// populate list with Photo objects that only have hash values
		for(String hash : photoKeys) {
			Photo p = generatePhotoFromHash(hash);
			newPhotos.add(p);
		}
		
		// get list of currently saved photos
		List<Photo> currentPhotos = dao.getAllPhotos();
		
		// removes photos that have already been saved from new Photo list
		newPhotos.removeAll(currentPhotos);

		return newPhotos;
	}
	
	private static Photo generatePhotoFromHash(String hash) {
		Photo p = new Photo();
		p.setHash(hash);
		
		return p;
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
