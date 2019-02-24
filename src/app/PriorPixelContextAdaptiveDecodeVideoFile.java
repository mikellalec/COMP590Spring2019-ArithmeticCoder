package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PriorPixelContextAdaptiveDecodeVideoFile {
	
	static int WINDOW_SIZE = 64*64;
	
	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/compressed_vid.dat";
		String output_file_name = "data/reuncompressed_vid.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] pixels = new Integer[256];
		
		for (int i=0; i<256; i++) {
			pixels[i] = i;
		}
		
		// Create 256 models. Model chosen depends on value of pixel in the prior frame.
		PriorValuePixelModel[] models = new PriorValuePixelModel[256];
		
		for(int i = 0; i != 256; i++) {
			// Create a new model with default count of 1 for all pixels
			models[i] = new PriorValuePixelModel(pixels);
		}
		// Set number of pixels to be read.
		int num_pixels = WINDOW_SIZE*30*10; // 64 by 64 30 FPS 10 seconds

		// Default range bit width of 40
		int range_bit_width = 40;
		
		// Default range bit width of 40
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded pixels: " + num_pixels);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

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
			
			// Decode
			int pix = decoder.decode(model, bit_source);
			fos.write(pix);
			
			// Update model
			model.addToCount(pix);
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
	
}
