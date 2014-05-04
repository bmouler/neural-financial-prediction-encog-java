package batcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IOHelper {

	public static File[] fileListFromDirectoryTree(String directory, String endingFilter) {
		File rootDir = loadFile(directory);
		List<File> dirList = new ArrayList<File>();

		// make a list of directories within the root dir
		for (File f : rootDir.listFiles()) {
			dirList.add(f);

			// TODO should be recursive
			// add the files within the directories
			// no need to add the actual files themselves as this is handled in the following calls
			if (f.isDirectory()) {
				for (File f2 : rootDir.listFiles()) {
					dirList.add(f2);
				}
			}
		}
		
		// add the root dir in case there are files in there too
		dirList.add(rootDir);

		return fileListInDirectories(dirList, endingFilter);
	}

	public static File[] fileListInDirectories(String[] fileNames, String endingFilter) {
		List<File> files = new ArrayList<File>();
		for (String s : fileNames) {
			File f = loadFile(s);
			files.add(f);
		}

		return fileListInDirectories(files, endingFilter);
	}
	
	public static File[] fileListInDirectories(List<File> dirs, String endingFilter) {
		File[] filesArray = dirs.toArray(new File[dirs.size()]);
		return fileListInDirectories(filesArray, endingFilter);
	}

	private static File[] fileListInDirectories(File[] dirs, String endingFilter) {
		List<File> files = new ArrayList<File>();
		System.out.println(dirs.length);
		
		for (int i = 0; i < dirs.length; i++) {
			
			System.out.println(dirs[i].getPath());
			
			if (dirs[i].listFiles() == null) {
				return null;
			}
			if (dirs[i].listFiles().length == 0) {
				return null;
			}
			
			
			// make a list of all files to be processed
			for (File f : dirs[i].listFiles()) {
				
				System.out.println(dirs[i].getPath());

				// if the filter matches the ending
				// add
				if (endingFilter != null) {
					if (f.getName().endsWith(endingFilter)) {
						files.add(f);
					}
				}

				// if no filter
				// add no matter what
				else {
					files.add(f);
				}
			}
		}

		return files.toArray(new File[files.size()]);
	}

	public static File loadFile(String s) {
		File f = new File(s);
		// TODO handle errors for file access
		return f;
	}

	public static void createDirectories(int DEBUG_LEVEL, List<String> tags, String outputDir) {
		String[] tagsArray = tags.toArray(new String[tags.size()]);
		createDirectories(DEBUG_LEVEL, tagsArray, outputDir);
	}
	
	public static void createDirectories(int DEBUG_LEVEL, String[] tags, String outputDir) {
		for (int t = 0; t < tags.length; t++) {
			String tagDir = outputDir + "/" + tags[t];

			File theDir = new File(tagDir);

			if (!theDir.exists()) {
				boolean success = (theDir).mkdirs();
				if (!success) {
					System.out.println("\n\nFailed to create output directory: " + tagDir);
					System.exit(-1);
				}
			}
		}
	}

	public static void deleteDirectory(int DEBUG_LEVEL, String outputDir, boolean isOkNotExist) {
		File directory = new File(outputDir);

		// make sure directory exists
		if (!directory.exists() && !isOkNotExist) {
			if (DEBUG_LEVEL >= 0)
				System.out.println("Did not delete directory. Directory does not exist: "
						+ outputDir);
		} else {

			try {

				delete(DEBUG_LEVEL, directory);

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}

		if (DEBUG_LEVEL >= 1)
			System.out.println("Deletion completed");
	}

	private static void delete(int DEBUG_LEVEL, File file) throws IOException {

		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {

				file.delete();
				if (DEBUG_LEVEL >= 3)
					System.out.println("Directory is deleted : " + file.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(DEBUG_LEVEL, fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					if (DEBUG_LEVEL >= 3)
						System.out.println("Directory is deleted : " + file.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it
			file.delete();
			if (DEBUG_LEVEL >= 3)
				System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

}
