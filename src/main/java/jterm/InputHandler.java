package main.java.jterm;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class InputHandler {

	private static LinkedList<String> fileNames = new LinkedList<>(); // list of files for tab rotation and printing options
	private static String command = ""; // stores JTerm.command while rotating through above list
	private static int startComplete = 0; // length of original input to be completed

	/**
	 * Process()
	 *
	 * Calls appropriate method for handling input read from Input class, using booleans in JTerm class to determine
	 * what OS the program is running on
	 */
	static void Process() {
		char input = 0;
		try {
			input = (char)Input.read(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (JTerm.isWin)
			ProcessWin(input);
		else if (JTerm.isUnix)
			ProcessUnix(input);
	}

	/**
	 * ProcessUnix()
	 *
	 * Processes input provided by Input class, and operates based on the input it receives, using the characters
	 * values used in Unix systems
	 *
	 * @param input last character input by user
	 */

	private static void ProcessUnix(char input) {
		boolean clearFilesList = true;
		if (input != 20 && input != 127 && input != 9) // do not output tabs, caps lock and backspace chars
			System.out.print(input);
		if (input == 20) { // caps lock
			JTerm.capsOn = !JTerm.capsOn;
		} else if (input == 127) { // back space
			if (JTerm.command.length() > 0) {
				JTerm.command = JTerm.command.substring(0, JTerm.command.length() - 1);
				System.out.print("\b \b"); // delete char, add white space and move back again
			}
		} else if (",./\\-_+=~".contains(String.valueOf(input))) { // special chars, more can be added
			JTerm.command += input;
		} else if (input == '\t' && JTerm.command.length() > 0) { // tab
			clearFilesList = false;
			String[] commandArr = JTerm.command.split(" "); // split into sections
			String currText = commandArr[commandArr.length - 1]; // get last element
			if (commandArr.length > 1 && !JTerm.command.endsWith(" ")) // if more than one element, autocomplete file
				FileAutocomplete(currText);
			else if (commandArr.length == 1 && !JTerm.command.endsWith(" ")) // if one element, autocomplete command (to be implemented)
				CommandAutocomplete(currText);
			else if (JTerm.command.endsWith(" ")) // if last input was space, print all options
				FileAutocomplete(" ");
		} else if (input == '\n') { // enter, or new line
			if (JTerm.command.length() > 0)
				JTerm.Parse(JTerm.command);
			JTerm.command = "";
			System.out.println();
		} else if (Character.isLetter(input)) { // its a letter
			if (!JTerm.capsOn)
				JTerm.command += input;
			else
				JTerm.command += Character.toUpperCase(input);
		} else if (Character.isDefined(input)) { // just print it if it is defined
			JTerm.command += input;
		}
		if (fileNames.size() > 0 && clearFilesList)
			fileNames.clear();
	}

	/**
	 * ProcessWin()
	 *
	 * Processes input provided by Input class, and operates based on the input it receives, using the characters
	 * values used in Windows systems
	 * { // backspace
			if (JTerm.command.length() > 0) {
				JTerm.command = JTerm.command.substring(0, JTerm.command.length() - 1);
				System.out.print("\b \b"); // delete char, add white space and move back again
			}
		} else if (input == 9 && JTerm.c
	 * @param input last character input by user
	 */
	private static void ProcessWin(char input) {
		boolean clearFilesList = true;
		if (input != 20 && input != 8 && input != 9)
			System.out.print(input);
		if (input == 20) { // caps lock
			JTerm.capsOn = !JTerm.capsOn;
		} else if (input == 8) { // backspace
			if (JTerm.command.length() > 0) {
				JTerm.command = JTerm.command.substring(0, JTerm.command.length() - 1);
				System.out.print("\b \b"); // delete char, add white space and move back again
			}
		} else if (input == 9 && JTerm.command.length() > 0) { // tab
			clearFilesList = false;
			String[] commandArr = JTerm.command.split(" "); // split into sections
			String currText = commandArr[commandArr.length - 1]; // get last element
			if (commandArr.length > 1 && !JTerm.command.endsWith(" ")) // if more than one element, autocomplete file
				FileAutocomplete(currText);
			else if (commandArr.length == 1 && !JTerm.command.endsWith(" ")) // if one element, autocomplete command (to be implemented)
				CommandAutocomplete(currText);
			else if (JTerm.command.endsWith(" ")) // if last input was space, print all options
				FileAutocomplete(" ");
		} else if (input == 13) { // new line
			System.out.println("\r\n");
			if (JTerm.command.length() > 0)
				JTerm.Parse(JTerm.command);
			JTerm.command = "";
			System.out.println("\r\n");
		} else if (Character.isLetter(input)) { // its a letter
			if (!JTerm.capsOn)
				JTerm.command += input;
			else
				JTerm.command += Character.toUpperCase(input);
		} else if (Character.isDefined(input)) { // just print it if it is defined
			JTerm.command += input;
		}
		if (fileNames.size() > 0 && clearFilesList)
			fileNames.clear();
	}

	/**
	 * FileAutocomplete()
	 *
	 * Using a string of text representing what has been typed presently, displays all files that match the current input
	 * Currently does not complete any input due to incapability to move input cursor, would require own UI... WIP
	 *
	 * @param currText file that is to be completed
	 */
	private static void FileAutocomplete(String currText) {
		boolean newList = false;
		File currFolder = new File(JTerm.currentDirectory);
		File[] files = currFolder.listFiles();

		// get all file names for comparison
		if (fileNames.size() == 0) {
			newList = true; // for tab rotation, true means no tab rotation, false means rotate through list
			command = JTerm.command; // stores original command so that JTerm.command does not keep adding to itself
			startComplete = currText.length(); // for autocomplete in tab rotation
			for (File f : files)
				if (f.getName().startsWith(currText))
					fileNames.add(f.getName());
		}

		if (fileNames.size() != 1) {
			if (fileNames.size() > 0 || currText.equals(" "))
				ClearLine(JTerm.command); // clear line

			// print matching file names
			if (newList)
				for (String s : fileNames)
					System.out.print(s + "\t");
			else {
				ClearLine(JTerm.command);
				String currFile = fileNames.pollFirst(); // get first file or dir name
				JTerm.command = command + currFile.substring(startComplete, currFile.length()); // autocomplete with first file or dir name
				System.out.print(JTerm.command); // print to screen
				fileNames.add(currFile); // add file or dir name at end of list
			}
			if (fileNames.size() > 0 && newList) {
				System.out.println();
				System.out.print(JTerm.command); // re-output command after clearing lines
			}

			// if no input, just output all files and folders
			if (currText.equals(" ")) {
				if (newList) {
					for (File f : files) {
						System.out.print(f.getName() + " \t");
						fileNames.add(f.getName());
					}
					System.out.println("\n"); // improve readability
					System.out.print(JTerm.command); // re-output command after clearing lines
				}
				else {
					ClearLine(JTerm.command);
					String currFile = fileNames.pollFirst(); // get first file or dir name
					JTerm.command = command + currFile.substring(startComplete, currFile.length()); // autocomplete with first file or dir name
					System.out.print(JTerm.command); // print to screen
					fileNames.add(currFile); // add file or dir name at end of list
				}
			}
		} else {
			String fileName = fileNames.getFirst();
			JTerm.command += fileName.substring(currText.length(), fileName.length()) + " ";
			System.out.print(fileName.substring(currText.length(), fileName.length()) + " ");
			System.out.println(); // improve readability
		}
	}

	/**
	 * ClearLine()
	 *
	 * Clears a line in the console of size line.length()
	 * @param line line to be cleared
	 */
	private static void ClearLine(String line) {
		for (int i = 0; i < line.length(); i++)
			System.out.print("\b");
		for (int i = 0; i < line.length(); i++)
			System.out.print(" ");
		for (int i = 0; i < line.length(); i++)
			System.out.print("\b");
	}

	/**
	 * CommandAutocomplete()
	 *
	 * @param currText command that is to be completed
	 */
	private static void CommandAutocomplete(String currText) {
		// will autocomplete commands
	}
}
