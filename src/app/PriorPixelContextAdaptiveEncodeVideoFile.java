package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class PriorPixelContextAdaptiveEncodeVideoFile {

	static int WINDOW_SIZE = 64*64;
	
	public static void main(String[] args) throws IOException {
		String input_file_name = "data/out.dat";
		String output_file_name = "data/compressed_vid.dat";

		int range_bit_width = 40;

		System.out.println("Encoding video file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_pixels = (int) new File(input_file_name).length();
				
		Integer[] pixels = new Integer[256];
		for (int i=0; i<256; i++) {
			pixels[i] = i;
		}

		// Create 256 models. Model chosen based on pixel value.
		PriorValuePixelModel[] models = new PriorValuePixelModel[256];
		
		for (int i=0; i != 256; i++) {
			// Create new model with default count of 1 for all pixels
			models[i] = new PriorValuePixelModel(pixels);
		}

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		
		// Initialize model variable.
		PriorValuePixelModel model;
		
		// Initialize variable to store previous pixel value from frame
		Integer PreviousFramePixels[] = new Integer[WINDOW_SIZE];
		for(int i = 0; i != WINDOW_SIZE; i++) {
			PreviousFramePixels[i] = 0;
		}

		for (int i=0; i != num_pixels; i++) {
			// Use model for previous pixel from that location
			model = models[PreviousFramePixels[i%(WINDOW_SIZE)]];
			
			int next_pixel = fis.read();
			encoder.encode(next_pixel, model, bit_sink);
			
			// Update model
			model.addToCount(next_pixel);
		}
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
	
}
