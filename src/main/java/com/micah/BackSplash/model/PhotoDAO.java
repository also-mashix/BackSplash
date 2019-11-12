package com.micah.BackSplash.model;

import java.util.List;

public interface PhotoDAO {

	List<Photo> getAllPhotos();
	List<Photo> getAllSavedPhotos();
	List<Photo> getAllUnsavedPhotos();
	
	boolean addPhoto(Photo p);
	
	boolean markPhotoAsSaved(Photo p);
}
