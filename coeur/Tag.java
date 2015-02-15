package coeur;

import java.util.HashMap;
import java.util.Map;

public enum Tag {

	BLANCS(Type.ENTIER, 0, "Whites2012", "blancs"), CLARTÉ(Type.ENTIER, 0,
			"Clarity2012", "clarté"), CONTRASTE(Type.ENTIER, 0, "Contrast2012",
					"contraste"), EXPOSITION(Type.FLOTTANT, 0., "Exposure2012", "exposition"), LUMINANCE_BLEU(
					Type.ENTIER, 0, "LuminanceAdjustmentBlue", "luminance bleu"), LUMINANCE_BLEU_VERT(
					Type.ENTIER, 0, "LuminanceAdjustmentAqua", "luminance bleu vert"), LUMINANCE_JAUNE(
					Type.ENTIER, 0, "LuminanceAdjustmentYellow", "luminance jaune"), LUMINANCE_MAGENTA(
					Type.ENTIER, 0, "LuminanceAdjustmentMagenta", "luminance magenta"), LUMINANCE_ORANGE(
					Type.ENTIER, 0, "LuminanceAdjustmentOrange", "luminance orange"), LUMINANCE_POURPRE(
					Type.ENTIER, 0, "LuminanceAdjustmentPurple", "luminance pourpre"), LUMINANCE_ROUGE(
					Type.ENTIER, 0, "LuminanceAdjustmentRed", "luminance rouge"), LUMINANCE_VERT(
					Type.ENTIER, 0, "LuminanceAdjustmentGreen", "luminance vert"), NETTETÉ_DETAIL(
					Type.ENTIER, 25, "SharpenDetail", "détail netteté"), NETTETÉ_GAIN(
					Type.ENTIER, 25, "Sharpness", "gain netteté"), NETTETÉ_MASQUAGE(
					Type.ENTIER, 0, "SharpenEdgeMasking", "masquage netteté"), NETTETÉ_RAYON(
					Type.FLOTTANT, 0., "SharpenRadius", "rayon netteté"), NOIRS(Type.ENTIER,
					0, "Blacks2012", "noirs"), RÉDUCTION_DU_BRUIT_COULEUR(Type.ENTIER, 0,
					"ColorNoiseReduction", "réduction du bruit couleur"), RÉDUCTION_DU_BRUIT_COULEUR_DÉTAIL(
					Type.ENTIER, 0, "ColorNoiseReductionDetail",
					"réduction du bruit couleur détail"), RÉDUCTION_DU_BRUIT_LUMINANCE(
					Type.ENTIER, 0, "LuminanceSmoothing", "réduction du bruit luminance"), RÉDUCTION_DU_BRUIT_LUMINANCE_CONTRASTE(
					Type.ENTIER, 0, "LuminanceNoiseReductionContrast",
					"réduction du bruit luminance contraste"), RÉDUCTION_DU_BRUIT_LUMINANCE_DETAIL(
					Type.ENTIER, 0, "LuminanceNoiseReductionDetail",
					"réduction du bruit luminance détail"), SATURATION(Type.ENTIER, 0,
					"Saturation", "saturation"), SATURATION_BLEU(Type.ENTIER, 0,
					"SaturationAdjustmentBlue", "saturation bleu"), SATURATION_BLEU_VERT(
					Type.ENTIER, 0, "SaturationAdjustmentAqua", "saturation bleu vert"), SATURATION_JAUNE(
					Type.ENTIER, 0, "SaturationAdjustmentYellow", "saturation jaune"), SATURATION_MAGENTA(
					Type.ENTIER, 0, "SaturationAdjustmentMagenta", "saturation magenta"), SATURATION_ORANGE(
					Type.ENTIER, 0, "SaturationAdjustmentOrange", "saturation orange"), SATURATION_POURPRE(
					Type.ENTIER, 0, "SaturationAdjustmentPurple", "saturation pourpre"), SATURATION_ROUGE(
					Type.ENTIER, 0, "SaturationAdjustmentRed", "saturation rouge"), SATURATION_VERT(
					Type.ENTIER, 0, "SaturationAdjustmentGreen", "saturation vert"), TEINTE(
					Type.ENTIER, 0, "Tint", "teinte"), TEINTE_BLEU(Type.ENTIER, 0,
					"HueAdjustmentBlue", "teinte bleu"), TEINTE_BLEU_VERT(Type.ENTIER, 0,
					"HueAdjustmentAqua", "teinte bleu vert"), TEINTE_JAUNE(Type.ENTIER, 0,
					"HueAdjustmentYellow", "teinte jaune"), TEINTE_MAGENTA(Type.ENTIER, 0,
					"HueAdjustmentMagenta", "teinte magenta"), TEINTE_ORANGE(Type.ENTIER, 0,
					"HueAdjustmentOrange", "teinte orange"), TEINTE_POURPRE(Type.ENTIER, 0,
					"HueAdjustmentPurple", "teinte pourpre"), TEINTE_ROUGE(Type.ENTIER, 0,
					"HueAdjustmentRed", "teinte rouge"), TEINTE_VERT(Type.ENTIER, 0,
					"HueAdjustmentGreen", "teinte vert"), TEMPÉRATURE(Type.ENTIER, 0,
					"ColorTemperature", "température"), TONS_CLAIRS(Type.ENTIER, 0,
					"Highlights2012", "tons clairs"), TONS_FONCÉS(Type.ENTIER, 0,
					"Shadows2012", "tons foncés"), VIBRANCE(Type.ENTIER, 0, "Vibrance",
					"vibrance");

	public Type type;
	public Object valeurParDéfaut;
	public String nomExifTool;
	public String texte;

	public static enum Type {

		ENTIER, FLOTTANT;
	};

	Tag(Type type, Object valeurParDéfaut, String texteExifTool, String texte) {
		this.type = type;
		this.valeurParDéfaut = valeurParDéfaut;
		this.nomExifTool = texteExifTool;
		this.texte = texte;
	}

	private static final Map<String, Tag> CORRESPONDANCES;

	static {
		Tag[] tableauTags = Tag.values();
		CORRESPONDANCES = new HashMap<String, Tag>();
		for (Tag tag : tableauTags)
			CORRESPONDANCES.put(tag.nomExifTool, tag);
	}

	public static Tag tagCorrespondant(String nomExifTool) {
		return CORRESPONDANCES.get(nomExifTool);
	}

	public static Object convertirValeur(Tag tag, String valeur) {
		switch (tag.type) {
			case ENTIER:
				try {
					return Integer.valueOf(valeur);
				} catch (NumberFormatException e) {
					System.out.println(tag.texte);
				}
			case FLOTTANT:
				return Double.valueOf(valeur);
			default:
				return null;
		}
	}

	public String toString() {
		return texte;
	}
}
