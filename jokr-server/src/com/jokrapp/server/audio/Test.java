package com.jokrapp.server.audio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Test {

	public static void main(String[] args) {

		Path path = Paths.get("laugh1.wav");
		Path laughP = Paths.get("clapping1.mp3");
		Path clappingP = Paths.get("clapping1.mp3");

		try {
			byte[] pcmData = Files.readAllBytes(path);

			byte[] mp3 = ConvertPcmToMp3.encodePcmToMp3(pcmData);
			System.out.println("encoded");

			byte[] cuttedMp3 = Arrays.copyOfRange(mp3, 418, mp3.length);

		//	byte[] cuttedMp3 = Arrays.copyOfRange(mp3, 4087, mp3.length - 4087);

		//	 byte[] cuttedMp3 = CutMp3.cut(mp3, false);

		/*	byte[] laugh = Files.readAllBytes(laughP);

			byte[] clapping = Files.readAllBytes(clappingP);

			byte[] withL = ConcatMp3.concat(clapping, cuttedMp3);

			byte[] finished = ConcatMp3.concat(withL, laugh);
*/
			FileOutputStream stream = new FileOutputStream("result2.mp3");
			try {
				stream.write(cuttedMp3);
			} finally {
				stream.close();
			}

			System.out.println("finished.");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
