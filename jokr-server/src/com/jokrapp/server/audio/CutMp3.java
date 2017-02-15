package com.jokrapp.server.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CutMp3 {
	public static void main(String args[]) throws IOException {

		File f = new File("result.mp3");

		FileInputStream fistream1 = new FileInputStream("result.mp3"); // first
																		// source
																		// file
		FileOutputStream fostream = new FileOutputStream("finalCutResult.mp3"); // destinationfile

		int temp;
		System.out.println("cutting...");

		// 1 sec cut is 16348 (128 Kbit/s / 8 = 16 KB/s, 16*1024= 16348
		// bytes/second)
		int i = 0;
		int aQuarterSecond = 16348 / 4;
		long fileLength = f.length();
		int headerLength = 32;

		System.out.println("file length: " + fileLength);
		while ((temp = fistream1.read()) != -1) {
			if ((i < headerLength || i > aQuarterSecond)
					&& i < fileLength - aQuarterSecond) {
				System.out.println(temp);
				// System.out.print( (char) temp ); // to print at DOS prompt
				fostream.write(temp); // to write to file
			}
			i++;
		}
		fostream.close();
		fistream1.close();
	}

	/**
	 * cutting
	 * 
	 * @param inputFile
	 * @return
	 */
	public static byte[] cut(byte[] inputFile, boolean cutEnd) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(inputFile);

		int temp;

		// 1 sec cut is 16348 (128 Kbit/s / 8 = 16 KB/s, 16*1024= 16348
		// bytes/second)
		int i = 0;
		int aQuarterSecond = 16348 / 4;
		long fileLength = inputFile.length;

		while ((temp = inputStream.read()) != -1) {
			if ((i + 418 > aQuarterSecond)) {
				if (!cutEnd || i < fileLength - aQuarterSecond)
					outputStream.write(temp); // to write to file
			}
			i++;
		}

		return outputStream.toByteArray();
	}
}
