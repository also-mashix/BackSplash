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
	
//	For future iterations of this... when it can prompted from the UI
//	public FolderScanner(JDBCPhotoDAO dao) {
//		this.dao = dao;
//	}
	
	public static void main(String[] args) {
		// set-up
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/unsplash_watcher");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		
		jdbcTemplate = new JdbcTemplate(dataSource);
		JDBCPhotoDAO jdbcPhotoDAO = new JDBCPhotoDAO(dataSource);
		
		initialScanFolderForImages();
	}

	public static List<Photo> initialScanFolderForImages() {
		List<Photo> photosInFolder = new ArrayList<Photo>();
		
		List<String> listOfFileNames = makeListOfFileNames(directory);
		for(String s: listOfFileNames) {
			Photo p = mapTitleToPhoto(s);
			photosInFolder.add(p);
		}
		return photosInFolder;
	}

	private static Photo mapTitleToPhoto(String s) {
		String[] splitFileName = s.split("-");
		
		String hash = splitFileName[splitFileName.length-2];
		String photoFilePath = directory + "/" + s;
		
		Photo p = new Photo();
		p.setHash(hash);
		p.setPhotoFilePath(photoFilePath);
		p.setSaved(true);
		Photo insertedP = addPhotoToDatabase(p);
		
		if(insertedP.getSaved()) {
			System.out.println("Added " + s);
		} else {
			System.out.println("Could not add " + s);
		}
		
		return insertedP;
	}

	private static List<String> makeListOfFileNames(String directory) {
		List<String> resultList = null;

		try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

			resultList = walk.map(x -> x.toString()).filter(f -> f.endsWith(".jpg")).collect(Collectors.toList());

			resultList.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return resultList;
	}
	
	private static Photo addPhotoToDatabase(Photo p) {
		String sql = "INSERT INTO unsplash_watcher (id, unsplash_photo_hash, saved_boolean, file_path) VALUES (DEFAULT, ?, ?, ?) RETURNING id";
		
		String photoHash = p.getHash();
		Boolean saved = true;
		String filePath = p.getPhotoFilePath();
		
		try {
			SqlRowSet results = jdbcTemplate.queryForRowSet(sql, photoHash, saved, filePath);
			results.next();
			p.setId(results.getLong("id"));
			return p;
		} catch (Exception e) {
			p.setSaved(false);
		}
		
		return p;
	}

}
