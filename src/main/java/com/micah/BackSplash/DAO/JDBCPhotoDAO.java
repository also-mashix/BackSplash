package com.micah.BackSplash.DAO;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.micah.BackSplash.model.Photo;
import com.micah.BackSplash.model.PhotoDAO;

public class JDBCPhotoDAO implements PhotoDAO {

	private JdbcTemplate dao;

	public JDBCPhotoDAO(BasicDataSource dataSource) {
		dao = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Photo> getAllPhotos() {
		List<Photo> allPhotos = new ArrayList<Photo>();

		String sql = "SELECT id, unsplash_photo_hash, unsplash_link, photographer_name, photographer_page_link, saved_boolean, file_path FROM unsplash_watcher";
		SqlRowSet results = dao.queryForRowSet(sql);

		while (results.next()) {
			Photo p = mapRowToPhoto(results);
			allPhotos.add(p);
		}

		return allPhotos;
	}

	@Override
	public List<Photo> getAllSavedPhotos() {
		List<Photo> allSavedPhotos = new ArrayList<Photo>();

		String sql = "SELECT id, unsplash_photo_hash, unsplash_link, photographer_name, photographer_page_link, saved_boolean, file_path FROM unsplash_watcher WHERE saved_boolean IS TRUE";
		SqlRowSet results = dao.queryForRowSet(sql);

		while (results.next()) {
			Photo p = mapRowToPhoto(results);
			allSavedPhotos.add(p);
		}

		return allSavedPhotos;
	}

	@Override
	public List<Photo> getAllUnsavedPhotos() {
		List<Photo> allUnavedPhotos = new ArrayList<Photo>();

		String sql = "SELECT id, unsplash_photo_hash, unsplash_link, photographer_name, photographer_page_link, saved_boolean, file_path FROM unsplash_watcher WHERE saved_boolean IS FALSE";
		SqlRowSet results = dao.queryForRowSet(sql);

		while (results.next()) {
			Photo p = mapRowToPhoto(results);
			allUnavedPhotos.add(p);
		}

		return allUnavedPhotos;
	}

	@Override
	public boolean addPhoto(Photo p) {
		String sql = "INSERT INTO unsplash_watcher (id, unsplash_photo_hash, unsplash_link, photographer_name, photographer_page_link, saved_boolean, file_path) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?) RETURNING id";
		
		String photoHash = p.getHash();
		String unsplashLink = p.getPhotoLink();
		String photogName = p.getPhotographerName();
		String photogLink = p.getPhotographerLink();
		Boolean saved = false;
		String filePath = null;
		
		try {
			SqlRowSet results = dao.queryForRowSet(sql, photoHash, unsplashLink, photogName, photogLink, saved, filePath);
			results.next();
			p.setId(results.getLong("id"));
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return false;
	}

	@Override
	public boolean markPhotoAsSaved(Photo p) {
		String sql = "UPDATE unsplash_watcher SET (saved_bool, file_path) = (true, ?) WHERE id = ?";
		
		String filePath = p.getPhotoFilePath();
		Long fileID = p.getId();
		
		int success = dao.update(sql, filePath, fileID);

		return (success == 1);
	}

	private Photo mapRowToPhoto(SqlRowSet results) {
		Photo p = new Photo();

		p.setId(results.getLong("id"));
		p.setHash(results.getString("unsplash_photo_hash"));
		p.setPhotoLink(results.getString("unsplash_link"));
		p.setPhotographerName(results.getString("photographer_name"));
		p.setPhotographerLink(results.getString("photographer_link"));
		p.setSaved(results.getBoolean("saved_boolean"));
		p.setPhotoFilePath(results.getString("file_path"));

		return p;
	}
}
