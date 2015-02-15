package interfaceGraphique;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class JPanelImage extends JPanel {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	public void afficherImage(BufferedImage image) {
		this.image = image;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (image != null) {
			BufferedImage imageAjustée = ajusterTailleImage(image);
			g.drawImage(imageAjustée,
					Math.max(0, (getWidth() - imageAjustée.getWidth()) / 2),
					Math.max(0, (getHeight() - imageAjustée.getHeight()) / 2), this);
		}
	}

	public BufferedImage ajusterTailleImage(BufferedImage image) {
		int hauteur, largeur;
		if ((double) getWidth() / getHeight() > (double) image.getWidth()
				/ image.getHeight()) {
			hauteur = getHeight();
			largeur = image.getWidth() * getHeight() / image.getHeight();
		} else {
			hauteur = image.getHeight() * getWidth() / image.getTileWidth();
			largeur = getWidth();
		}
		BufferedImage imageRedimensionnée = new BufferedImage(largeur, hauteur,
				image.getType());
		Graphics2D g = imageRedimensionnée.createGraphics();
		g.drawImage(image, 0, 0, largeur, hauteur, null);
		g.dispose();
		return imageRedimensionnée;
	}
}
