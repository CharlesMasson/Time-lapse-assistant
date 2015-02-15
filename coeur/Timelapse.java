package coeur;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Timelapse {

	public Photo[] photos;
	public boolean[] références;
	public Tag[] tagsInterpolables;
	public boolean[] tagsInterpolés;
	public int nombreTagsInterpolables;

	public Timelapse(int nombrePhotos) {
		photos = new Photo[nombrePhotos];
		références = new boolean[nombrePhotos];
		tagsInterpolables = new Tag[Tag.values().length];
		tagsInterpolés = new boolean[Tag.values().length];
		nombreTagsInterpolables = 0;

	}

	public boolean tagConstant(Tag tag) {
		boolean constant = true;
		Object valeur = photos[0].lireMétadonnée(tag);
		for (Photo photo : photos) {
			constant = photo.lireMétadonnée(tag).equals(valeur);
			if (!constant)
				break;
		}
		return constant;
	}

	public void initialiserTags() {
		int i = 0;
		for (Tag tag : Tag.values())
			if (!tagConstant(tag)) {
				tagsInterpolables[i] = tag;
				tagsInterpolés[i++] = true;
			}
		nombreTagsInterpolables = i;
	}

	public Set<Tag> tagsInterpolés() {
		HashSet<Tag> tagsInterpolés = new HashSet<>();
		for (int i = 0; i < nombreTagsInterpolables; i++)
			if (this.tagsInterpolés[i])
				tagsInterpolés.add(tagsInterpolables[i]);
		return tagsInterpolés;
	}

	public boolean existeAuMoinsUneRéférence() {
		for (int i = 0; i < références.length; i++)
			if (références[i])
				return true;
		return false;
	}

	public void interpoler(Tag tag, TypeInterpolation type) {
		ArrayList<Integer> indicesRéférences = new ArrayList<>(photos.length);
		for (int i = 0; i < photos.length; i++)
			if (références[i])
				indicesRéférences.add(i);
		interpoler(tag, type, indicesRéférences.toArray(new Integer[0]));
	}

	public void interpoler(Tag tag, TypeInterpolation type, Integer[] indicesRéférences) {
		switch (tag.type) {
			case ENTIER:
				// Traitement des photos avant la première photo référence
				for (int i = 0; i < indicesRéférences[0]; i++)
					photos[i].métadonnées.put(tag, photos[indicesRéférences[0]].lireMétadonnée(tag));
				// Traitement des photos entre les première et dernière photos références
				for (int j = 0; j < indicesRéférences.length - 1; j++) {
					int m = indicesRéférences[j];
					int n = indicesRéférences[j + 1];
					double a = (int) photos[m].lireMétadonnée(tag);
					double b = (int) photos[n].lireMétadonnée(tag);
					for (int i = m + 1; i < n; i++)
						photos[i].métadonnées.put(tag, (int) (((b - a) * i + n * a - m * b) / (n - m)));
				}
				// Traitement des photos après la dernière photo référence
				for (int i = indicesRéférences[indicesRéférences.length - 1] + 1; i < photos.length; i++)
					photos[i].métadonnées.put(tag, photos[indicesRéférences[indicesRéférences.length - 1]]);
				break;
			case FLOTTANT:
				// Traitement des photos avant la première photo référence
				for (int i = 0; i < indicesRéférences[0]; i++)
					photos[i].métadonnées.put(tag, photos[indicesRéférences[0]].lireMétadonnée(tag));
				// Traitement des photos entre les première et dernière photos références
				for (int j = 0; j < indicesRéférences.length - 1; j++) {
					int m = indicesRéférences[j];
					int n = indicesRéférences[j + 1];
					double a = (double) photos[m].lireMétadonnée(tag);
					double b = (double) photos[n].lireMétadonnée(tag);
					for (int i = m + 1; i < n; i++)
						photos[i].métadonnées.put(tag, (int) (((b - a) * i + n * a - m * b) / (n - m)));
				}
				// Traitement des photos après la dernière photo référence
				for (int i = indicesRéférences[indicesRéférences.length - 1] + 1; i < photos.length; i++)
					photos[i].métadonnées.put(tag, photos[indicesRéférences[indicesRéférences.length - 1]]);
				break;
		}
	}

	public void déflicker(boolean luminositéConstante, int nombrePhotosMoyenne) {
		if (luminositéConstante) {
			float luminositéCible = 0f;
			for (Photo photo : photos)
				luminositéCible += photo.luminosité;
			luminositéCible /= photos.length;
			for (Photo photo : photos)
				photo.métadonnées.put(Tag.EXPOSITION,
						(double) photo.lireMétadonnée(Tag.EXPOSITION) + 2 * Math.log(luminositéCible / photo.luminosité) / Math.log(2));
		} else {
			float[] luminositéCible = new float[photos.length];
			for (int i = 0; i < photos.length; i++) {
				for (int j = Math.max(0, i - nombrePhotosMoyenne / 2); j < Math.min(photos.length, i + (nombrePhotosMoyenne + 1) / 2); j++)
					luminositéCible[i] += photos[j].luminosité;
				luminositéCible[i] /= Math.min(photos.length, i + (nombrePhotosMoyenne + 1) / 2) - Math.max(0, i - nombrePhotosMoyenne / 2);
				photos[i].métadonnées.put(
						Tag.EXPOSITION,
						(double) photos[i].lireMétadonnée(Tag.EXPOSITION) + 2 * Math.log(luminositéCible[i] / photos[i].luminosité)
						/ Math.log(2));
			}
		}
	}
}
