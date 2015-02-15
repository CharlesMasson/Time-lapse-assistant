package coeur;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class Photo {

	private static final Pattern SÉPARATEUR = Pattern.compile(": ");

	public File fichier;
	public Map<Tag, Object> métadonnées;
	public BufferedImage prévisualisation;
	public float luminosité;

	public Photo(ExifTool exifTool, File dossierJPEG, File fichier)
			throws ExifToolFerméException {
		this.fichier = fichier;
		métadonnées = new HashMap<>();
		chargerMétadonnéesDepuisFichier(exifTool);
		//chargerJPEGDepuisFichier(exifTool, dossierJPEG);
		prévisualisation = ExifTool.getPreviewImage(fichier);
		calculerLuminosité();
	}

	public void chargerMétadonnéesDepuisFichier(ExifTool exifTool)
			throws ExifToolFerméException {
		for (String chaîne : exifTool.lire(fichier.getAbsolutePath(), "-S",
				"-xmp:all", "-a")) {
			String[] fragments = SÉPARATEUR.split(chaîne);
			Tag tag = Tag.tagCorrespondant(fragments[0]);
			if (tag != null)
				try {
					métadonnées.put(tag, Tag.convertirValeur(tag, fragments[1]));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	public void chargerJPEGDepuisFichier(ExifTool exifTool, File dossierJPEG)
			throws ExifToolFerméException {
		exifTool.lire(fichier.getAbsolutePath(), "-b", "-PreviewImage", "-w!",
				dossierJPEG + "\\%f.jpg");
		File imageJPEG = new File(dossierJPEG + "\\"
				+ fichier.getName().substring(0, fichier.getName().lastIndexOf("."))
				+ ".jpg");
		try {
			prévisualisation = ImageIO.read(imageJPEG);
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageJPEG.deleteOnExit();
	}

	public void enregistrerMédonnéesDansFichier(ExifTool exifTool, Set<Tag> tags)
			throws ExifToolFerméException {
		exifTool.écrire(fichier.getAbsolutePath(), "-overwrite_original");
		for (Tag tag : tags)
			exifTool.écrire("-xmp:" + tag.nomExifTool + "=" + lireMétadonnée(tag));
		exifTool.exécuter();
		exifTool.lire();
	}

	public Object lireMétadonnée(Tag tag) {
		if (métadonnées.get(tag) == null)
			return tag.valeurParDéfaut;
		else
			return métadonnées.get(tag);
	}

	public void calculerLuminosité() {
		int[] rGB = new int[prévisualisation.getWidth()
				* prévisualisation.getHeight()];
		rGB = prévisualisation.getRGB(0, 0, prévisualisation.getWidth(),
				prévisualisation.getHeight(), rGB, 0, prévisualisation.getWidth());
		float[] y = new float[3];
		float luminosité = 0f;
		for (int i = 0; i < rGB.length; i++) {
			y = (new Color(rGB[i])).getRGBColorComponents(y);
			luminosité += y[0] + y[1] + y[2];
		}
		this.luminosité = luminosité / (3f * rGB.length);
	}
}
