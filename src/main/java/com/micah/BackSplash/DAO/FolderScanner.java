package com.micah.BackSplash.DAO;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.micah.BackSplash.model.Photo;

//scans folder and populates database
public class FolderScanner {

	private static final String directory = "/Users/micahkaufmanwright/OneDrive/Pictures/Wallpaper";
	private static JdbcTemplate jdbcTemplate;

	public FolderScanner() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/unsplash_watcher");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		
		jdbcTemplate = new JdbcTemplate(dataSource);
//		JDBCPhotoDAO jdbcPhotoDAO = new JDBCPhotoDAO(dataSource);
	}
	
//	public static void main(String[] args) {
//		// set-up
//		BasicDataSource dataSource = new BasicDataSource();
//		dataSource.setUrl("jdbc:postgresql://localhost:5432/unsplash_watcher");
//		dataSource.setUsername("postgres");
//		dataSource.setPassword("postgres1");
//		
//		jdbcTemplate = new JdbcTemplate(dataSource);
//		JDBCPhotoDAO jdbcPhotoDAO = new JDBCPhotoDAO(dataSource);
//		
//		scanFolderForImages();
//	}

	public List<Photo> scanFolderForImages() {
		List<Photo> photosInFolder = new ArrayList<Photo>();
		
		List<String> listOfFileNames = makeListOfFileNames(directory);
		for(String s: listOfFileNames) {
			Photo p = mapTitleToPhoto(s);
			photosInFolder.add(p);
		}
		return photosInFolder;
	}

	private Photo mapTitleToPhoto(String s) {
		String[] splitFileName = s.split("-");
		
		String hash = splitFileName[splitFileName.length-2];
		//String photoFilePath = directory + "/" + s;
		String photographerName = "";
		
		for(int i = 0; i < splitFileName.length - 2; i++) {
			if(i==0) {
				photographerName = splitFileName[i];
			} else {
				photographerName = " " + splitFileName[i];
			}
		}
		
		Photo p = new Photo();
		p.setHash(hash);
		p.setPhotoFilePath(s);
		p.setPhotographerName(photographerName);
		// p.setSaved(true); // leaving this to the addPhotoToDatabase helper method
		Photo insertedP = addPhotoToDatabase(p);
		
		if(insertedP.getSaved()) {
			System.out.println("Added " + s);
		} else {
			System.out.println("Could not add " + s);
		}
		
		return insertedP;
	}

	private List<String> makeListOfFileNames(String directory) {
		List<String> resultList = null;

		try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

			resultList = walk.map(x -> x.toString()).filter(f -> f.endsWith(".jpg")).collect(Collectors.toList());

			// resultList.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return resultList;
	}
	
	public Photo addPhotoToDatabase(Photo p) {
		
		if (checkIfAlreadyInDatabase(p)) {
			p.setSaved(true);
			return p;
		}
		
		
		String sql = "INSERT INTO unsplash_watcher (id, unsplash_photo_hash, saved_boolean, file_path, photographer_name) VALUES (DEFAULT, ?, ?, ?, ?)";
		
		String photoHash = p.getHash();
		Boolean saved = true;
		String filePath = p.getPhotoFilePath();
		String photographerName = p.getPhotographerName();
		
		if(1 == jdbcTemplate.update(sql, photoHash, saved, filePath, photographerName)) {
			p.setSaved(true);
			return p;
		}
		p.setSaved(false);
		return p;
	}

	public boolean checkIfAlreadyInDatabase(Photo p) {
		String sql = "SELECT COUNT(unsplash_photo_hash) FROM unsplash_watcher WHERE unsplash_photo_hash LIKE ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, p.getHash());
		results.next();
		int rowCount = results.getInt("count");
		// checks if this is already in the db
		// if it is, this should return 1, for its row in the db
		return (rowCount == 1);
	}

}
