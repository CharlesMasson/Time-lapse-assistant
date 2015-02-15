package interfaceGraphique.arborescenceDossiers;

import java.util.EventListener;

/**
 *
 * @author Charles Masson
 */
public interface FileTreeSelectionListener extends EventListener {

	void valueChanged(FileTreeSelectionEvent e);
}
