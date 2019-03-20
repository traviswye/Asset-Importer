import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ImageSet {
	@JsonInclude(Include.NON_NULL)
	private List<imageInfo> images;
	private AssetInfo info;
	
	/**
	 * Creates object that is serialized to Json file
	 * @param files, idiom
	 *       
	 *files: each image file that is within the folder the json represents
	 *idiom: type of device assets are intended for
	 */
	public ImageSet(File[] files, String idiom) {
		this.images = new ArrayList<imageInfo>();
		this.info = new AssetInfo();
		Populate(files, idiom);
	}

	/**
	 * Creates object that is serialized to Json file
	 * for root json files
	 */
	public ImageSet() {
		this.info = new AssetInfo();
	}
	
	public List<imageInfo> getImages() {
		return images;
	}

	public AssetInfo getinfo() {
		return info;
	}

	/**
	 * Populates Json object 
	 * @param files, idiom
	 *       
	 *files: each image file that is within the folder the json represents
	 *idiom: type of device assets are intended for
	 */
	public void Populate(File[] files, String idiom) {
		for(File curFile : files) {
			String file = curFile.getName().toString();
			System.out.println("File for json: " + file);
			imageInfo info = new imageInfo();
			info.setIdiom(idiom);
			info.setName(file);
			if (file.contains("@")) {
				int index = file.indexOf("@");
				info.setScale(file.substring(index+1, file.length() - 4));
			} else {
				info.setScale("1x");
			}
			images.add(info);
		}
		System.out.println("images contains: "+ images.size());
	}
	
	/**
	 * subclass that wraps all info for each image being added to the json
	 *       info containing; filename, scale, idiom
	 */
	public class imageInfo {
		String filename;
		String scale;
		String idiom;

		public imageInfo() {
			this.filename = null;
			this.scale = null;
			this.idiom = null;
		}
		
		private void setName(String name) {
			this.filename = name;
		}

		private void setScale(String scale) {
			this.scale= scale;
		}

		private void setIdiom(String idiom) {
			this.idiom = idiom;
		}

		public String getFilename() {
			return this.filename;
		}

		public String getScale() {
			return this.scale;
		}

		public String getIdiom() {
			return this.idiom;
		}
	}
	
	/**
	 * Subclass for root json files containing version and author attributes
	 */
	public class AssetInfo {
		int version;
		String author;
		private AssetInfo() {
			this.author = "xcode";
			this.version = 1;
		}

		public String getAuthor() {
			return this.author;
		}

		public int getVersion() {
			return this.version;
		}
	}
}
