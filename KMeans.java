/*** Author :Vibhav Gogate
The University of Texas at Dallas
 *****/

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class KMeans {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out
					.println("Usage: Kmeans <input-image> <k> <output-image>");
			return;
		}

		try {
			// Read original image from file, run through kmeans, and output
			// results.
			BufferedImage originalImage = ImageIO.read(new File(args[0]));
			BufferedImage kmeansJpg = kmeans_helper(originalImage,
					Integer.parseInt(args[1]));
			ImageIO.write(kmeansJpg, "png", new File(args[2]));
		}

		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * @param originalImage
	 *            - An image to compress
	 * @param k
	 *            - The number of clusters to use to compress originalImage
	 * @return A compressed image using the kmeans algorithm
	 */
	private static BufferedImage kmeans_helper(BufferedImage originalImage,
			int k) {
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		BufferedImage kmeansImage = new BufferedImage(w, h,
				originalImage.getType());
		Graphics2D g = kmeansImage.createGraphics();
		g.drawImage(originalImage, 0, 0, w, h, null);

		// Read rgb values from the image.
		int[] imageRGB = new int[(w * h)];
		int counter = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				imageRGB[counter++] = kmeansImage.getRGB(i, j);
			}
		}

		// Call kmeans algorithm: update the rgb values to compress image.
		kmeans(imageRGB, k);

		// Write the new rgb values to the image.
		counter = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				kmeansImage.setRGB(i, j, imageRGB[counter++]);
			}
		}

		// Return the compressed image
		return kmeansImage;
	}

	private static void kmeans(int[] pixels, int k) {
		if (pixels.length < k) {
			System.out.println("You must supply at least k pixels.");
			return;
		}
		int[] prevCenters = new int[k]; // Previous iteration's cluster centers
		int[] centers = new int[k]; // Current iteration's cluster centers
		int[] clusterSize = new int[k]; // Number of pixels belonging to a
		int[] alpha = new int[k]; // Total alpha in a cluster
		int[] red = new int[k]; // Total red in a cluster
		int[] green = new int[k]; // Total green in a cluster
		int[] blue = new int[k]; // Total blue in a cluster
		int[] classification = new int[pixels.length]; // Cluster assignment
		double maxDist = Double.MAX_VALUE; // Used in k-means
		double curDist = 0; // Used in k-means
		int closestCenter = 0; // Used in k-means
		for (int i = 0; i < centers.length; i++) {
			Random rng = new Random();
			int newCenter = 0;
			while (exists(newCenter, prevCenters)) {
				newCenter = pixels[rng.nextInt(pixels.length)];
			}
			centers[i] = newCenter;
		}
		while (!converged(prevCenters, centers)) {
			for (int i = 0; i < centers.length; i++) {
				prevCenters[i] = centers[i];
				clusterSize[i] = 0;
				alpha[i] = 0;
				red[i] = 0;
				green[i] = 0;
				blue[i] = 0;
			}
			for (int i = 0; i < pixels.length; i++) {
				maxDist = Double.MAX_VALUE;
				for (int j = 0; j < centers.length; j++) {
					curDist = pixelDist(pixels[i], centers[j]);
					if (curDist < maxDist) {
						maxDist = curDist;
						closestCenter = j;
					}
				}

				classification[i] = closestCenter;
				clusterSize[closestCenter]++;
				alpha[closestCenter] += ((pixels[i] & 0xFF000000) >>> 24);
				red[closestCenter] += ((pixels[i] & 0x00FF0000) >>> 16);
				green[closestCenter] += ((pixels[i] & 0x0000FF00) >>> 8);
				blue[closestCenter] += ((pixels[i] & 0x000000FF) >>> 0);
			}
			for (int i = 0; i < centers.length; i++) {
				int averageAlpha = (int) ((double) alpha[i] / (double) clusterSize[i]);
				int averageRed = (int) ((double) red[i] / (double) clusterSize[i]);
				int averageGreen = (int) ((double) green[i] / (double) clusterSize[i]);
				int averageBlue = (int) ((double) blue[i] / (double) clusterSize[i]);

				centers[i] = ((averageAlpha & 0x000000FF) << 24)
						| ((averageRed & 0x000000FF) << 16)
						| ((averageGreen & 0x000000FF) << 8)
						| ((averageBlue & 0x000000FF) << 0);
			}
		}
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = centers[classification[i]];
		}
	}

	private static boolean exists(int value, int[] array) {
		for (int i = 0; i < array.length; i++)
			if (array[i] == value)
				return true;

		return false;
	}

	private static boolean converged(int[] a, int[] b) {
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;

		return true;
	}

	private static double pixelDist(int pixA, int pixB) {
		int deltaAlpha = ((pixA & 0xFF000000) >>> 24)
				- ((pixB & 0xFF000000) >>> 24);
		int deltaRed = ((pixA & 0x00FF0000) >>> 16)
				- ((pixB & 0x00FF0000) >>> 16);
		int deltaGreen = ((pixA & 0x0000FF00) >>> 8)
				- ((pixB & 0x0000FF00) >>> 8);
		int deltaBlue = ((pixA & 0x000000FF) >>> 0)
				- ((pixB & 0x000000FF) >>> 0);
		return Math.sqrt(deltaAlpha * deltaAlpha + deltaRed * deltaRed
				+ deltaGreen * deltaGreen + deltaBlue * deltaBlue);
	}
}