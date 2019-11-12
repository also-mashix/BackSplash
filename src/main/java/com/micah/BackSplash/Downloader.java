package com.micah.BackSplash;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.micah.BackSplash.DAO.JDBCPhotoDAO;
import com.micah.BackSplash.model.Photo;

public class Downloader {

	private static URL collectionPage;

	private static BasicDataSource dataSource;
	private static JDBCPhotoDAO dao;

	public static void main(String[] args) {
		// first establish connection to collection page
		collectionPage = connectToCollection();

		// get list of photo keys
		List<String> photoKeys = readFromURL();

		// check if keys are already in database OR already saved on local drive
		List<Photo> unsavedPhotos = findNewPhotos(photoKeys);

		// download new photos, then add info to database
		for (Photo p : unsavedPhotos) {
			if (downloadPhoto(p)) {
				System.out.println("Successfully saved photo: " + p.getPhotoFilePath());
				unsavedPhotos.remove(p);
			} else {
				System.out.println("ERROR: Could not save photo: " + p.getHash());
			}
		}
	}

	private static URL connectToCollection() {
		
		dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/unsplash_watcher");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		
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
		for (String hash : photoKeys) {
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

	private static boolean downloadPhoto(Photo p) {

		String redirectUrl = "https://unsplash.com/photos/" + p.getHash() + "/download";
		String downloadUrl = parseRedirect(redirectUrl);
		String destinationFile = "/Users/micahkaufmanwright/OneDrive/Pictures/Wallpaper/" + p.getPhotographerName()
				+ "-" + p.getHash() + "-unsplash.jpg";

		try {
			saveImage(downloadUrl, destinationFile);
		} catch (IOException e) {
			// redundant 
			// System.out.println("ERROR: Could not save to computer -> " + p.getHash());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private static String parseRedirect(String redirectUrl) {
		String[] splitRedirect = redirectUrl.split("\"");
		return splitRedirect[1];
	}

	public static void saveImage(String imageUrl, String destinationFile) throws IOException {
		URL url = new URL(imageUrl);
		InputStream ins = url.openStream();
		OutputStream outs = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = ins.read(b)) != -1) {
			outs.write(b, 0, length);
		}

		ins.close();
		outs.close();
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
