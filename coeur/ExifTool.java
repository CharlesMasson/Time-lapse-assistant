package coeur;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ExifTool {

	private boolean instructionsEnvoyées;
	private BufferedReader fluxEntrant;
	private OutputStreamWriter fluxSortant;

	public ExifTool(File exécutable) {
		Process processus = null;
		try {
			processus = new ProcessBuilder(exécutable.getAbsolutePath(),
					"-stay_open", "True", "-@", "-").start();
		} catch (IOException e) {
			System.err.println("Impossible de lancer ExifTool. Vérifiez qu'ExifTool "
					+ "est correctement installé et que le chemin spécifié "
					+ "CHEMIN_EXIF_TOOL est correct");
			e.printStackTrace();
		}
		fluxEntrant = new BufferedReader(new InputStreamReader(
				processus.getInputStream()));
		try {
			fluxSortant = new OutputStreamWriter(processus.getOutputStream(),
					"8859_1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		instructionsEnvoyées = false;
	}

	public void écrire(String... instructions) throws ExifToolFerméException {
		if (fluxSortant == null)
			throw new ExifToolFerméException();
		try {
			for (String instruction : instructions)
				fluxSortant.write(instruction + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exécuter() throws ExifToolFerméException {
		if (fluxSortant == null)
			throw new ExifToolFerméException();
		try {
			fluxSortant.write("-execute\n");
			fluxSortant.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		instructionsEnvoyées = true;
	}

	public ArrayList<String> lire() throws ExifToolFerméException {
		if (fluxSortant == null)
			throw new ExifToolFerméException();
		ArrayList<String> réponse = new ArrayList<>();
		if (instructionsEnvoyées) {
			String ligne = null;
			try {
				while (!(ligne = fluxEntrant.readLine()).equals("{ready}"))
					réponse.add(ligne);
			} catch (IOException e) {
				e.printStackTrace();
			}
			instructionsEnvoyées = false;
		}
		return réponse;
	}

	public ArrayList<String> lire(String... instructions)
			throws ExifToolFerméException {
		écrire(instructions);
		exécuter();
		return lire();
	}

	public void fermer() {
		if (fluxSortant != null) {
			try {
				fluxSortant.write("-stay_open\nFalse\n");
				fluxSortant.flush();
				fluxSortant.close();
				fluxEntrant.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fluxEntrant = null;
			fluxSortant = null;
		}
	}

	public static BufferedImage getPreviewImage(File file) {
		BufferedImage image = null;
		try {
			Process p = new ProcessBuilder("exiftool", file.getAbsolutePath(), "-b", "-PreviewImage").start();
			image = ImageIO.read(p.getInputStream());
		} catch (IOException ex) {
			Logger.getLogger(ExifTool.class.getName()).log(Level.SEVERE, null, ex);
		}
		return image;
	}
}
