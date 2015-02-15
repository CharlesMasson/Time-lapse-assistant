package interfaceGraphique.arborescenceDossiers;

import java.io.File;
import java.util.EventObject;

/**
 *
 * @author Charles Masson
 */
public class FileTreeSelectionEvent extends EventObject {

	protected File file;
	
	public FileTreeSelectionEvent(Object source, File file) {
		super(source);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}
