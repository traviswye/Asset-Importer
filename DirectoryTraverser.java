import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class DirectoryTraverser {

	/**
	 * Method traverses trough all the files within the passed directory, It
	 * will collect all the paths of files that are text files (.jpeg, .png and .pdf) storing
	 * them in an ArrayList.
	 * 
	 * @param myfiles, path
	 *            ArrayList that all files are stored to
	 * @param path
	 *            to retrieve the listing, assumes a directory and not a file is
	 *            passed
	 * @throws IOException
	 */
	private static void traverseSource(ArrayList<Path> myfiles, Path path)
			throws IOException {
		/*
		 * The try-with-resources block makes sure we close the directory stream
		 * when done, to make sure there aren't any issues later when accessing
		 * this directory.
		 */

		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			// Efficiently iterate through the files and subdirectories.
			for (Path file : listing) {
				// If the current file ends with .txt or .TXT it adds the path
				// to the file array.
				if (file.toString().toLowerCase().endsWith(".png") || file.toString().toLowerCase().endsWith(".jpeg") 
						|| file.toString().toLowerCase().endsWith(".pdf")) {
					myfiles.add(file.toAbsolutePath().normalize());
				}
			}

		}
	}
	/**
	 * Safely starts the recursive traversal. Users of
	 * this class can access this method, so some validation is required.
	 * Outputs consol message if exception is thrown.
	 * @param directory
	 *            to traverse
	 * @throws IOException
	 */
	public static ArrayList<Path> traverseSource(Path directory) throws IOException {
		ArrayList<Path> files = new ArrayList<>();

		if (Files.isDirectory(directory)) {
			traverseSource(files, directory);
		} else {
			System.out.println(directory.getFileName()
					+ " is a file not a directory: Please check your source path");
			System.exit(8);
		}

		return files;
	}
	
	private static void traverseAllFiles(ArrayList<Path> myfiles, Path path)
			throws IOException {
		/*
		 * The try-with-resources block makes sure we close the directory stream
		 * when done, to make sure there aren't any issues later when accessing
		 * this directory.
		 */

		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			// Efficiently iterate through the files and subdirectories.
			for (Path file : listing) {
				// If the current file ends with .txt or .TXT it adds the path
				// to the file array.
				if (file.toString().toLowerCase().endsWith(".png") || file.toString().toLowerCase().endsWith(".jpeg") 
						|| file.toString().toLowerCase().endsWith(".pdf") || file.toString().toLowerCase().endsWith(".json")) {
					myfiles.add(Paths.get(file.toString().substring(file.toString().indexOf("Resources"))).normalize());
				}
				// If it is a subdirectory, recursively traverse.
				if (Files.isDirectory(file)) {
					traverseAllFiles(myfiles, file);
				}
			}

		}
	}
	public static ArrayList<Path> traverseAllFiles(Path directory) throws IOException {
		ArrayList<Path> files = new ArrayList<>();

		if (Files.isDirectory(directory)) {
			traverseAllFiles(files, directory);
		} else {
			System.out.println(directory.getFileName()
					+ " is a file not a directory");
			System.exit(8);
		}

		return files;
	}
	
	private static void traverseImageset(ArrayList<Path> myfiles, Path path)
			throws IOException {
		/*
		 * The try-with-resources block makes sure we close the directory stream
		 * when done, to make sure there aren't any issues later when accessing
		 * this directory.
		 */

		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			// Efficiently iterate through the files and subdirectories.
			for (Path file : listing) {
				// If the current file ends with .txt or .TXT it adds the path
				// to the file array.
				if (file.toString().toLowerCase().endsWith(".imageset")) {
					myfiles.add(Paths.get(file.toString().substring(file.toString().indexOf("Resources"))).normalize());
				}
				// If it is a subdirectory, recursively traverse.
				if (Files.isDirectory(file)) {
					traverseImageset(myfiles, file);
				}
			}

		}
	}
	public static ArrayList<Path> traverseImageset(Path directory) throws IOException {
		ArrayList<Path> files = new ArrayList<>();

		if (Files.isDirectory(directory)) {
			traverseImageset(files, directory);
		} else {
			System.out.println(directory.getFileName()
					+ " is a file not a directory");
			//System.exit(8);
		}

		return files;
	}



}