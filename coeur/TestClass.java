/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coeur;

import interfaceGraphique.JPanelImage;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author charles
 */
public class TestClass {

	public static void main1() throws IOException {
		//for (int i = 0; i < 10; i++) {
			Process processus = new ProcessBuilder("exiftool", "/home/charles/Pictures/2012-11-02 - Trocadéro/IMG_6491.dng", "-b", "-PreviewImage").start();
//		Process processus = new ProcessBuilder("exiftool", "-stay_open", "True", "-@", "-").start();
			//InputStream is = processus.getInputStream();
//			BufferedReader fluxEntrant = new BufferedReader(new InputStreamReader(is));
//			OutputStreamWriter fluxSortant = new OutputStreamWriter(processus.getOutputStream(), "8859_1");
//		fluxSortant.write("-ver\n");
//		fluxSortant.write("/home/charles/Pictures/2012-11-02 - Trocadéro/IMG_6491.dng\n");
//		fluxSortant.write("-b\n");
//		fluxSortant.write("-PreviewImage\n");;
//		fluxSortant.write("-execute\n");
//		fluxSortant.flush();
//		System.out.println(fluxEntrant.readLine());

			BufferedImage im = ImageIO.read((new ProcessBuilder("exiftool", "/home/charles/Pictures/2012-11-02 - Trocadéro/IMG_6491.dng", "-b", "-PreviewImage").start()).getInputStream());
			JFrame jf = new JFrame();
			jf.setMinimumSize(new Dimension(500, 500));
			JPanelImage jpi = new JPanelImage();
			jf.add(jpi);
			jf.setVisible(true);
			jpi.afficherImage(im);

			//fluxSortant.write("-stay_open\nFalse\n");
//			fluxSortant.flush();
//			fluxSortant.close();
		//}
		//fluxEntrant.close();
	}

	public static void main2() throws IOException {
		Process processus = new ProcessBuilder("exiftool", "-stay_open", "True", "-@", "-").start();
		InputStream is = processus.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
//		BufferedReader fluxEntrant = new BufferedReader(new InputStreamReader(is));
		OutputStreamWriter fluxSortant = new OutputStreamWriter(processus.getOutputStream(), "8859_1");
//		fluxSortant.write("-ver\n");
		fluxSortant.write("/home/charles/Pictures/2012-11-02 - Trocadéro/IMG_6491.dng\n");
		fluxSortant.write("-b\n");
		fluxSortant.write("-PreviewImage\n");
		fluxSortant.write("-execute\n");
		fluxSortant.flush();
//		System.out.println(fluxEntrant.readLine());
		BufferedImage im = ImageIO.read(is);
		JFrame jf = new JFrame();
		jf.setMinimumSize(new Dimension(500, 500));
		JPanelImage jpi = new JPanelImage();
		jf.add(jpi);
		jf.setVisible(true);
		//jpi.afficherImage(im);

		fluxSortant.write("-stay_open\nFalse\n");
		fluxSortant.flush();
		fluxSortant.close();
	}

	public static void test1(int n) throws IOException {

		for (int i = 0; i < n; i++) {
			Process processus = new ProcessBuilder("exiftool", "-ver").start();
			BufferedReader fluxEntrant = new BufferedReader(new InputStreamReader(processus.getInputStream()));
			System.out.println(fluxEntrant.readLine());
		}

	}

	public static void test2(int n) throws IOException {
		Process processus = new ProcessBuilder("exiftool", "-stay_open", "True", "-@", "-").start();
		InputStream is = processus.getInputStream();
		BufferedReader fluxEntrant = new BufferedReader(new InputStreamReader(is));
		OutputStreamWriter fluxSortant = new OutputStreamWriter(processus.getOutputStream(), "8859_1");
		for (int i = 0; i < n; i++) {
			fluxSortant.write("-ver\n");
			fluxSortant.write("-execute\n");
			fluxSortant.flush();
			System.out.println(fluxEntrant.readLine());
			System.out.println(fluxEntrant.readLine());
		}

	}

	public static void main(String[] args) throws IOException {
		main1();
	}
}
