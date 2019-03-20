import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;


public class Driver {


//	Arguments it is looking for: -s sourcepath -d destinationpath -n nameoffile -t typeoffileextention
//	-p platformOS -c .csproj path -i idiom -f (force if files already in destination directroy)
	
	
//	Exit code 0 : finished execution	
//	Exit code 1 : no valid args
//	Exit code 2 : too many args passed
//	Exit code 3 : not enough args passed
//	Exit code 4 : missing valid csproj path
//	Exit code 5 : platform argument invalid
//	Exit code 6 : idiom arg invalid
//	Exit code 7 : type argument invalid
//	Exit code 8 : source path invalid
//	Exit code 9 : file name invalid
//	Exit code 10 : destination arg invalid
//	Exit code 11 : json creation error
//	Exit code 12 : xml update error
//	Exit code 13 : failed to move source files to destination
//  Exit code 14 : no available images to copy
//	
	public static void main(String[] args) throws TransformerConfigurationException {
//		test for git
//		im refactoring
		ArgumentParser parser = new ArgumentParser(args);
		System.out.println("Arguments passed in: "+parser.toString());

		// Argument validation; 	
		checkArgs(parser);

		// Get the files from source
		File[] filesToCopy = getFiles(parser.getValue("-s"), parser.getValue("-n"), parser.getValue("-t"), parser.getValue("-p"));

		if (filesToCopy.length == 0) {
			System.out.println("Didn't find any files in the source directory");
			System.exit(14);
		}

		//ArrayList<File> filesToCopy = getImageFiles(parser);
		// copy files to folder destination
		File directory = populateDestination(parser.getValue("-d"), parser.getValue("-n"), filesToCopy);
		createJsonImageSet(filesToCopy, parser.getValue("-i"), directory.toString());

		// make additions to the xml/csproj file
		modifyProjectFile(parser.getValue("-s"), directory, parser.getValue("-c"));

		System.exit(0);

	}

	// checks all required flags and values are present
	private static void checkArgs(ArgumentParser parser) {
		// General argument checks
		if (parser.numFlags() == 0) {
			System.out.println("Please check your cmd line arguments, none found");
			System.exit(1);
		}		
		if (parser.numFlags() > 8) {
			System.out.println("Please check your cmd line arguments, too many flags were found");
			System.exit(2);
		}
		else if ( parser.numFlags() < 6) {
			System.out.println("Please check your cmd line arguments, too few flags were found");
			System.exit(3);
		}

		// csproj checks
		if (parser.hasFlag("-c")) {
			if(parser.getValue("-c") == null) {
				System.out.println("Must specify csproj path after flag -c");
				System.exit(4);
			} else {
				String m = parser.getValue("-c");
				// MAKE SURE THAT THIS IS A VALID PATH
				File validCs = new File(m);
				if (validCs.exists() && validCs.getName().endsWith(".csproj")) {
					System.out.println(".csproj file exists");
				} else {
					System.exit(4);
				}
			}
		}
		// Platform checks
		if (parser.hasFlag("-p")) {
			if(parser.getValue("-p") == null) {
				System.out.println("Must specify which platform after flag -p");
				System.exit(5);
			} else if (!parser.getValue("-p").equalsIgnoreCase("ios") && !parser.getValue("-p").equalsIgnoreCase("android")) {
				System.out.println("Valid arguments for flag -p are ios or android");
				System.exit(5);
			}
			if (parser.getValue("-p").equalsIgnoreCase("ios")) {
				if (!parser.hasValue("-i")) {
					System.exit(6);
				}
				if (!parser.getValue("-i").equalsIgnoreCase("universal") && !parser.getValue("-i").equalsIgnoreCase("iphone") &&
						!parser.getValue("-i").equalsIgnoreCase("ipad") && !parser.getValue("-i").equalsIgnoreCase("mac")
						&& !parser.getValue("-i").equalsIgnoreCase("applewatch")) {
					System.exit(6);
				}
			}
		} else {
			System.out.println("Must specify which platform you intend on using ImportTool for with flag -p 'argument'");
			System.exit(5);
		}
		
		// Type of image checks
		if (parser.hasFlag("-t")) {
			if (parser.getValue("-t") == null) {
				System.out.println("No type was defined therefore all files found will be used.");
			} else if (!parser.getValue("-t").equalsIgnoreCase("bitmap") && !parser.getValue("-t").equalsIgnoreCase("vector")){
				System.out.println("Valid arguments for flag -t are vector and bitmap");
				System.exit(7);
			}
		}
		// Source directory check
		if (parser.hasFlag("-s")) {
			if (parser.getValue("-s") == null) {
				System.out.println("Must specify source directory after flag -s");
				System.exit(8);
			} else {
				File sourceDirectory = new File(parser.getValue("-s"));
				if (!sourceDirectory.exists()) {
					System.out.println("Must specify a valid source directory after flag -s");
					System.exit(8);
				}
				if (!sourceDirectory.isDirectory()) {
					System.out.println("Source directory must be a directory");
					System.exit(8);
				}
			}
		} else {
			System.out.println("Must specify source directory with flag -s 'argument'");
			System.exit(8);
		}

		// Name of file checks
		if (parser.hasFlag("-n")) {
			if (parser.getValue("-n") == null) {
				System.out.println("Must specify filename after flag -n");
				System.exit(9);
			} else if (parser.getValue("-n").contains("~") || parser.getValue("-n").contains("#")
					|| parser.getValue("-n").contains("&") || parser.getValue("-n").contains("%")
					|| parser.getValue("-n").contains("*") || parser.getValue("-n").contains("}")
					|| parser.getValue("-n").contains("'\'") || parser.getValue("-n").contains(":")
					|| parser.getValue("-n").contains("<") || parser.getValue("-n").contains(">")
					|| parser.getValue("-n").contains("?") || parser.getValue("-n").contains("/")
					|| parser.getValue("-n").contains("+") || parser.getValue("-n").contains("{")
					|| parser.getValue("-n").contains("|")) {
				System.out.println("An invalid character was detected within your file name, please check your filename.");
				System.exit(9);
			} 
		} else {
			System.out.println("Must specify filename with flag -n 'argument'");
			System.exit(9);
		}

		// Destination directory checks
		if (parser.hasFlag("-d")) {
			if (parser.getValue("-d") == null) {
				System.out.println("Must specify destination directory after flag -d");
				System.exit(10);
			} else if (!parser.getValue("-d").endsWith(".xcassets")) {
				System.out.println("Destination folder must be a .xcassets file folder, please check yours");
				System.exit(10);
			}
			if (parser.hasValue("-d")) {
				// make sure that destination path parent folder is in Fishangler/resources
				// traverser destination. if one of file names has -n quit program

				
				// MAKE SURE THAT THIS IS A VALID PATH
				File destFolder = new File (parser.getValue("-d"));
				if (parser.getValue("-p").equals("ios")) {
					if (!destFolder.getParent().endsWith("Resources")) {
						// Do we need to add second parent for "fishangler.ios"
						System.out.println("Destination folder must be within project Resources folder");
						System.exit(10);
					}
					
					String destPath = parser.getValue("-d") + "/" + parser.getValue("-n") + ".imageset";
					Path path = Paths.get(destPath);
					if (Files.exists(path)) {
						System.out.println("A file in Destination path already contains a " + parser.getValue("-n")+ " file already");
						if (parser.hasFlag("-f")) {
							System.out.println("-f flag used: destination files will be overwritten");
						} else {
							System.exit(10);
						}
					}
				}
				// TODO: implement Android check
			}
		} else {
			System.out.println("Must specify destination directory with flag -d 'argument'");
			System.exit(10);
		}
	}

	private static File[] getFiles(String path, String assetName, String type, String platform) {
		File folder = new File(path);
		final String lowerAssetName = assetName.toLowerCase();
		final ArrayList<String> extensions = new ArrayList<String>();
		if (type.equals("bitmap")) { // it is a bitmap, and it doesn't matter the platform
			extensions.add(".png");
			extensions.add(".jpg");
			extensions.add(".jpeg");
		} else if (platform.equals("ios")) { // it is vector and ios
			extensions.add(".pdf");
		} else { // it is vector and android
			extensions.add(".svg");
		}
		
		File[] files = folder.listFiles(new FilenameFilter() {
			 public boolean accept(File dir, String name) {
				if (!name.toLowerCase().startsWith(lowerAssetName))
					return false;

				for (final String extension : extensions) {
					if (name.toLowerCase().endsWith(extension)) {
						return true;
					}
				}
				return false;
			}
		});

		return files;
	}

	// creates new directory to copy file to 
	private static File populateDestination(String destinationDir, String assetName, File[] filesToCopy){
		// creates imageset folder inside of xcassets
		File xDir = new File(destinationDir);
		File imDir2 = new File(destinationDir + "/" + assetName + ".imageset");
		if (!xDir.exists()) {
			xDir.mkdir();
			System.out.println("Directory created at " + xDir.getAbsolutePath());
			createRootJson(xDir.toString());
		} 
		if (!imDir2.exists()) {
			imDir2.mkdir();
			System.out.println("Directory created at " + imDir2.getAbsolutePath());
		}

		for (File curFile : filesToCopy) {
			try {
				Files.copy(curFile.toPath(), imDir2.toPath().resolve(curFile.getName()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				System.exit(13);
				e.printStackTrace();
			}
		}

		return imDir2;
	}

	// creates json file in the new directroy
	private static void createJsonImageSet(File[] files, String idiom, String path) {
		ObjectMapper mapper = new ObjectMapper();
		// mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		ImageSet jsonFile = new ImageSet(files, idiom);
		// Write to path
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path + "/Contents.json"), jsonFile);
		} catch (IOException e) {
			System.exit(11);
			e.printStackTrace();
		}
	}

	private static void createRootJson(String path) {
		ObjectMapper mapper = new ObjectMapper();
		ImageSet imageSet = new ImageSet();

		// Write to path
		try {
			System.out.println(path);
			mapper.writer().withDefaultPrettyPrinter().writeValue(new File(path + "/Contents.json"), imageSet);
		} catch (IOException e) {
			System.exit(11);
			e.printStackTrace();
		}
	}

	private static void modifyProjectFile(String assetName, File destinationDir, String csprojPath) {
		//ArrayList<Path> imagePaths = new ArrayList<Path>();
		//ArrayList<Path> setPaths = new ArrayList<Path>();
		try {
			File imagesetFolder = new File(destinationDir.getAbsolutePath() + "/" + assetName + ".imageset");
			File[] assetFiles = imagesetFolder.listFiles();

			//imagePaths = DirectoryTraverser.traverseAllFiles(Paths.get(directory.toString()+"/"+arg.getValue("-n")+".imageset"));

			//setPaths = DirectoryTraverser.traverseImageset(Paths.get(directory.toString()));
			for (File assetFile : assetFiles){
				System.out.println("adding " + assetFile.getPath().toString() + " to xml");
				xmlUpdater.ImageAsset(assetFile.getPath().toString(), csprojPath);
			}
			//TODO: review why this is needed
			// for (Path folderPath : setPaths) {
			// 	if(folderPath.toString().contains(arg.getValue("-n")+".imageset")) {
			// 		xmlUpdater.FolderUpdate(folderPath.toString(), arg.getValue("-c"));
			// 	}
			// }

		} catch (IOException | TransformerConfigurationException | ParserConfigurationException | SAXException e) {
			System.exit(12);
			e.printStackTrace();
		}
	}

}
