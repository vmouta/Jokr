package com.jokrapp.server.audio;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFormat;

import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;

public class ConvertPcmToMp3 {

	public static void main(String args[]) {

		Path path = Paths.get("demo.aiff");

		try {
			byte[] pcmData = Files.readAllBytes(path);

			byte[] mp3 = encodePcmToMp3(pcmData);

			FileOutputStream stream = new FileOutputStream("result.mp3");
			try {
				stream.write(mp3);
			} finally {
				stream.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static byte[] encodePcmToMp3(byte[] pcm) {

		AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);

		LameEncoder encoder = new LameEncoder(audioFormat, 128,
				MPEGMode.JOINT_STEREO, Lame.QUALITY_HIGHEST, false);

		ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
		byte[] buffer = new byte[encoder.getPCMBufferSize()];

		int bytesToTransfer = Math.min(buffer.length, pcm.length);
		int bytesWritten;
		int currentPcmPosition = 0;
		while (0 < (bytesWritten = encoder.encodeBuffer(pcm,
				currentPcmPosition, bytesToTransfer, buffer))) {
			currentPcmPosition += bytesToTransfer;
			bytesToTransfer = Math.min(buffer.length, pcm.length
					- currentPcmPosition);

			mp3.write(buffer, 0, bytesWritten);
		}
		encoder.close();
		return mp3.toByteArray();
	}
}
