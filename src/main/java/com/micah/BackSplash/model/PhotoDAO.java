package com.micah.BackSplash.model;

import java.util.List;

public interface PhotoDAO {

	List<Photo> getAllPhotos();
	
	boolean addPhoto(Photo p);
	
	// not necessary to MVP, but could be useful later on
	List<Photo> getAllSavedPhotos();
	List<Photo> getAllUnsavedPhotos();
	boolean markPhotoAsSaved(Photo p);
}
