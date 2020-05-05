package com.gmail.egorovsonalexey;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileSystemDirectory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.*;
import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PImage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.List;

/** HelloWorld
  * An application with two maps side-by-side zoomed in on different locations.
  * Author: UC San Diego Coursera Intermediate Programming team
  * @author Some guy, I don't know him.
  * Date: July 17, 2015
  * */
public class PhotoMap extends PApplet
{
	private static final long serialVersionUID = 1L;
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	private static final boolean offline = false;
	//private static final String imageDirName = "C:\\Users\\Alexey\\Pictures\\Temp";
	private static final String imageDirName = "C:\\Users\\Alexey\\Pictures";
	//private static final String imageDirName = "C:\\Users\\Пользовательь\\Pictures\\Nokia";

	UnfoldingMap map;
	List<Marker> markers;

	private PhotoMarker lastSelected;
	private PhotoMarker lastClicked;
	private Date minCreatedDate;
	private int dateCreateFilter = 3;

	private int xbase = 25;
	private int ybase = 25;
	private Drive service;
	private PImage img;
	private Map<String, Integer> deviceList;
	int keyHeight;
	int keyWidth;

	@Override
	public void setup() {
		//size(1366, 768, OPENGL);
		size(1280, 1024);
		this.background(200, 100, 100);
		this.minCreatedDate = new Date(0);
		this.deviceList = new HashMap<>();
		Random _rnd = new Random();

		AbstractMapProvider provider = new OpenStreetMap.OpenStreetMapProvider();
		//AbstractMapProvider provider = new Microsoft.RoadProvider();
		int zoomLevel = 3;

		if (offline) {
			provider = new MBTilesMapProvider(mbTilesString);
			//zoomLevel = 3;
		}

		markers = new ArrayList<>();
		List<java.io.File> files = getAllFiles(imageDirName);
		System.out.println("Files count: " + files.size());
		for (java.io.File file : files) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);

				GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
				if (gps == null) {
					continue;
				}
				printMetadata(metadata);
				GeoLocation _location = gps.getGeoLocation();
				if (_location != null) {
					double _lat = _location.getLatitude();
					double _lon = _location.getLongitude();
					if (_lat == 0 && _lon == 0) {
						continue;
					}
					Location _loc = new Location(_lat, _lon);
					FileSystemDirectory fileSystem =
							metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
					long _fileSize = fileSystem.getLong(FileSystemDirectory.TAG_FILE_SIZE);
					PhotoMarker _marker =
							new PhotoMarker(_loc, new HashMap<>());
					_marker.setProperty("size", _fileSize);
					_marker.setProperty("fileName", file.getName());
					_marker.setProperty("url", file.getAbsolutePath());
					_marker.setProperty("createDate", fileSystem.getDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE));
					ExifIFD0Directory _exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
					if (_exif != null) {
						String _deviceName = _exif.getString(ExifIFD0Directory.TAG_MAKE);
						String _model = _exif.getString(ExifIFD0Directory.TAG_MODEL);
						String _device = String.format("%s %s", _deviceName, _model);
						if (!deviceList.containsKey(_device)) {
							// Random color without any transparency.
							// Transparency make the same color markers look like different dependency of background.
							deviceList.put(_device, 0xff000000 + _rnd.nextInt(0x00ffffff));
						}
						_marker.setProperty("device", _device);
						_marker.setColor(this.deviceList.get(_device));
						System.out.printf("%s: %s, %s\n", file.getName(), _location.toDMSString(), _device);
					}
					this.markers.add(_marker);
				}
			} catch (ImageProcessingException | IOException | MetadataException | IndexOutOfBoundsException ex) {
				System.out.println(ex);
				System.out.println(file.getAbsolutePath());
			}
		}
		System.out.println("Markers count: " + markers.size());
		this.keyHeight = 120 + this.deviceList.size() * 20;
		this.keyWidth = 3 * this.deviceList.keySet().stream().map(x -> (int) this.textWidth(x)).max(Integer::compare).get() / 2;

		map = new UnfoldingMap(this,
				xbase + this.keyWidth, ybase, 1150, 600, provider);
		map.zoomAndPanTo(zoomLevel, new Location(53f, 35f));
		MapUtils.createDefaultEventDispatcher(this, map);
		map.addMarkers(markers);
	}

	@Override
	public void draw() {
		if (lastClicked == null) {
			map.draw();
			this.addKey();
		} else {
//			try {

//				String fileId = lastClicked.getFileId();
//				OutputStream outputStream = new ByteArrayOutputStream();
//				service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
//
//				ImageInputStream stream = ImageIO.createImageInputStream(outputStream);
//				ImageReader reader = ImageIO.getImageReaders(stream).next(); // TODO: Test hasNext()
//				reader.setInput(stream);
//
//				ImageTypeSpecifier spec = reader.getImageTypes(0).next(); // TODO: Test hasNext();
//
//				ImageReadParam param = reader.getDefaultReadParam();
//				BufferedImage image = reader.read(0, param);

			if (img == null) {
				img = loadImage(lastClicked.getUrl());
			}

			if (img.height > 0) {
				img.resize(0, (int)map.getHeight());  //resize loaded image to full height of map
				image(img, xbase + 140, ybase);        //display image
			}
//			} catch (IOException ex) {
//				System.out.println(ex);
//			}
		}
	}

	private void printMetadata(Metadata metadata) {
		List<String> _lines = new ArrayList<>();
		for (Directory directory : metadata.getDirectories()) {
			for (Tag tag : directory.getTags()) {
				_lines.add(String.format("[%s] - %s = %s\n",
						directory.getName(), tag.getTagName(), tag.getDescription()));
			}
		}
		Path _outputPath = Paths.get("C:\\Users\\Alexey\\Documents\\metadata.txt");
		try {
			Files.write(_outputPath, _lines, StandardOpenOption.CREATE);
		} catch (IOException ex){
			System.out.println(ex);
		}
	}

	private void initMarkersFromGoogleDrive() {
		try {
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			service = new Drive.Builder(
					HTTP_TRANSPORT,
					GoogleDriveSetup.JSON_FACTORY,
					GoogleDriveSetup.getCredentials(HTTP_TRANSPORT)
			)
					.setApplicationName(GoogleDriveSetup.APPLICATION_NAME)
					.build();

			FileList result = service.files().list()
					.setPageSize(100)
					.setFields("*")
					.execute();
			List<File> files = result.getFiles();
			if (files == null || files.isEmpty()) {
				System.out.println("No files found.");
			} else {
				System.out.println("Files:");
				for (File file : files) {
					System.out.printf("%s (%s)\n", file.getName(), file.getId());
					File.ImageMediaMetadata _metaData = file.getImageMediaMetadata();
					if (_metaData != null) {
						File.ImageMediaMetadata.Location _location = _metaData.getLocation();
						if (_location != null) {
							Location _loc = new Location(_location.getLatitude(), _location.getLongitude());
							PhotoMarker _marker =
									new PhotoMarker(_loc, new HashMap<>());
							_marker.setProperty("fileName", file.getName());
							_marker.setProperty("url", file.getWebContentLink());
							_marker.setProperty("createDate", new Date(file.getCreatedTime().getValue()));
							_marker.setProperty("fileId", file.getId());
							_marker.setProperty("size", file.getSize());
							this.markers.add(_marker);
						}
					}
				}
			}
		} catch (IOException | GeneralSecurityException ex) {
			System.out.println(ex);
		}
	}

	private List<java.io.File> getAllFiles(String dirName) {
		List<java.io.File> files = new ArrayList<>();
		java.io.File dir = new java.io.File(dirName);
		Deque<java.io.File> dirs = new ArrayDeque<>(1000);
		dirs.add(dir);
		while (dirs.size() > 0) {
			java.io.File item = dirs.removeLast();
			java.io.File[] fileNames = item.listFiles();
			if (fileNames == null) {
				continue;
			}

			for (java.io.File file : fileNames) {
				if (file.isDirectory()) {
					dirs.addFirst(file);
				} else {
					//String extension = FilenameUtils.getExtension(file.getName());
					//if("jpg".equals(extension) || "jpeg".equals(extension)) {
					try {
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
						FileType fileType = FileTypeDetector.detectFileType(in);
						if (fileType != FileType.Unknown)
							files.add(file);
					} catch (IOException ex) {
						System.out.println(ex);
					}
				}
			}
		}
		return files;
	}


	@Override
	public void mouseMoved()
	{
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(markers);
	}

	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			lastClicked = null;
			img = null;
		}
		else {
			checkPhotoMarkerForClick();
		}

		checkRadioButtonToClick();
	}

	private void selectMarkerIfHover(List<Marker> markers)
	{
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers)
		{
			PhotoMarker marker = (PhotoMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}

	private void checkPhotoMarkerForClick()
	{
		if (lastClicked != null) return;

		for (Marker marker : markers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (PhotoMarker)marker;
				return;
			}
		}
	}

	private void checkRadioButtonToClick() {
		if(getMousePosition().distance(xbase+20, ybase+40) < 10) {
			Calendar cln = Calendar.getInstance();
			cln.add(Calendar.MONTH, -1);
			this.minCreatedDate = cln.getTime();
			this.dateCreateFilter = 1;
		}

		if(getMousePosition().distance(xbase+20, ybase+60) < 10) {
			Calendar cln = Calendar.getInstance();
			cln.add(Calendar.YEAR, -1);
			this.minCreatedDate = cln.getTime();
			this.dateCreateFilter = 2;
		}

		if(getMousePosition().distance(xbase+20, ybase+80) < 10) {
			this.minCreatedDate = new Date(0);
			this.dateCreateFilter = 3;
		}

		for(Marker marker : markers) {
			boolean isBefore = ((PhotoMarker)marker).getCreateDate().before(this.minCreatedDate);
			marker.setHidden(isBefore);
		}
	}

	private void addKey() {

		this.pushStyle();
		fill(255, 255, 255);
		rect(25, 25, this.keyWidth, this.keyHeight);
		fill(0);
		strokeWeight(2);
		text("Date create filter:", xbase + 15, ybase + 20);
		text("last month", xbase + 40, ybase + 40);
		text("last year", xbase + 40, ybase + 60);
		text("all", xbase + 40, ybase + 80);
		fill(0xffffff);
		ellipse(xbase + 20, ybase + 40, 14, 14);
		ellipse(xbase + 20, ybase + 60, 14, 14);
		ellipse(xbase + 20, ybase + 80, 14, 14);
		fill(0);
		switch (dateCreateFilter) {
			case 1 : {
				ellipse(xbase + 20, ybase + 40, 6, 6);
				break;
			}
			case 2 : {
				ellipse(xbase + 20, ybase + 60, 6, 6);
				break;
			}
			case 3 : {
				ellipse(xbase + 20, ybase + 80, 6, 6);
				break;
			}
		}

		text("Devices:", xbase + 40, ybase + 100);

		int count = 0;
		for(Map.Entry<String, Integer> _device: this.deviceList.entrySet()) {
			fill(_device.getValue());
			int y = ybase + 120 + count * 20;
			ellipse(xbase + 20, y, 14, 14);
			fill(0);
			text(_device.getKey(), xbase + 40, y);
			count++;
		}

		this.popStyle();
	}
	
}
