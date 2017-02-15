package com.jokrapp.server.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;

public class ConcatMp3 {
	public static void main(String args[]) throws IOException {
		FileInputStream fistream1 = new FileInputStream("finalCutResult.mp3"); // first
		// source
		// file
		FileInputStream fistream2 = new FileInputStream("laugh1.mp3");// second
		// source
		// file
		SequenceInputStream sistream = new SequenceInputStream(fistream1,
				fistream2);
		FileOutputStream fostream = new FileOutputStream("finalResult.mp3");// destinationfile

		int temp;

		while ((temp = sistream.read()) != -1) {
			// System.out.print( (char) temp ); // to print at DOS prompt
			fostream.write(temp); // to write to file
		}
		fostream.close();
		sistream.close();
		fistream1.close();
		fistream2.close();
	}

	/**
	 * 
	 * @param is1
	 *            first source
	 * @param is2
	 *            second source
	 * @throws IOException
	 */
	public static byte[] concat(byte[] input1, byte[] input2)
			throws IOException {

		SequenceInputStream sistream = new SequenceInputStream(
				new ByteArrayInputStream(input1), new ByteArrayInputStream(
						input2));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		int temp;

		while ((temp = sistream.read()) != -1) {
			outputStream.write(temp);
		}

		sistream.close();

		return outputStream.toByteArray();
	}
}
