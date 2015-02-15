package coeur;

public class ExifToolFerméException extends Exception {

	private static final long serialVersionUID = 1L;

	public ExifToolFerméException() {
		super("ExifTool a été fermé. L'instruction ne peut donc pas être effectuée.");
	}
}
