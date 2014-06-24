package com.github.katjahahn.pecollector;

/*******************************************************************************
 * Copyright 2014 Katja Hahn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Collects PE files recursively from a given startfolder. You may use this to
 * collect clean samples.
 * 
 * @author Katja Hahn
 * 
 */
public class PECollector {

	private static int noPE = 0;
	private static int notLoaded = 0;
	private static int dirsRead = 0;
	private static int total = 0;
	private static int prevTotal = 0;
	private static int written = 0;

	private static File output = new File("pefiles");
	private static final String USAGE = "java -jar pecollector.jar <startfolder>";

	public static void main(String[] args) throws IOException {
		invokeCLI(args);
	}

	private static void invokeCLI(String[] args) {
		try {
			if (args.length > 0) {
				File folder = new File(args[0]);
				if (folder.exists() && folder.isDirectory()) {
					if (!output.exists()) {
						output.mkdir();
						System.out.println("output folder " + output.getName()
								+ " created!");
					}
					copyPEFiles(folder);
					System.out.println("Files found: " + total);
					System.out.println("No PE: " + noPE);
					System.out.println("PE files not loaded: " + notLoaded);
					System.out.println("PE files successfully copied: "
							+ written);
					System.out.println("Location of copied files: "
							+ output.getAbsolutePath());
					System.out.println("done");
				} else {
					System.err
							.println("given folder doesn't exist or is no directory");
				}
			} else {
				System.out.println(USAGE);
			}
		} catch (Exception e) {
			System.err.println("There was a problem: " + e.getMessage());
		}
	}

	public static void copyPEFiles(File startFolder) throws IOException {
		File[] files = startFolder.listFiles();
		if (files == null) {
			System.out.println("Skipped unreadable file: "
					+ startFolder.getCanonicalPath());
			return;
		}
		for (File file : files) {
			total++;
			if (file.isDirectory()) {
				copyPEFiles(file);
			} else {
				try {
					new PESignature(file).read();
					Files.copy(file.toPath(),
							Paths.get(output.getAbsolutePath(), file.getName()));
					written++;
				} catch (FileFormatException e) {
					noPE++;
				} catch (FileAlreadyExistsException e) {
					System.err.println("file already exists: " + file.getName()
							+ " not copied!");
					notLoaded++;
				} catch (Exception e) {
					System.err.println(e.getMessage());
					notLoaded++;
				}
				if (total != prevTotal && total % 1000 == 0) {
					prevTotal = total;
					System.out.println("Files found: " + total);
					System.out.println("PE Files found: " + written);
				}
			}
		}
		dirsRead++;
		if (dirsRead % 500 == 0) {
			System.out.println("Directories read: " + dirsRead);
			System.out.println("Current Directory finished: "
					+ startFolder.getAbsolutePath());
		}
	}

}
