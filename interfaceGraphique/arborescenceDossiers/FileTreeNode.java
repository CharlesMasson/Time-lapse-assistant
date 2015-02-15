package interfaceGraphique.arborescenceDossiers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Charles Masson
 */
public class FileTreeNode implements TreeNode {

	private File file;
	private FileTreeNode parent;
	private ArrayList<FileTreeNode> children;

	public FileTreeNode(File file) {
		this.file = file;
	}

	public FileTreeNode(File[] files) {
		children = new ArrayList<>();
		for (File f : files)
			children.add(new FileTreeNode(f));
	}

	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		if (file == null)
			return "";
		return file.getName().length() > 0 ? file.getName() : file.getPath();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FileTreeNode))
			return false;
		if (getFile() == null)
			return ((FileTreeNode) obj).getFile() == null;
		return getFile().equals(((FileTreeNode) obj).getFile());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + Objects.hashCode(this.file);
		return hash;
	}

	@Override
	public TreeNode getChildAt(int index) {
		if (children == null)
			throw new ArrayIndexOutOfBoundsException("node has no children");
		return (TreeNode) children.get(index);
	}

	@Override
	public int getChildCount() {
		if (children == null)
			expand();
		return children.size();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		if (node == null || !(node instanceof FileTreeNode))
			throw new IllegalArgumentException();
		if (children == null)
			expand();
		return children.indexOf((FileTreeNode) node);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		if (children == null)
			expand();
		return children.isEmpty();
	}

	@Override
	public Enumeration<FileTreeNode> children() {
		if (children == null)
			expand();
		return Collections.enumeration(children);
	}

	private void expand() {
		if (children != null)
			return; // already expanded
		children = new ArrayList<>();
		if (!file.isDirectory())
			return;
		File[] files;
		try {
			files = file.listFiles();
			if (files == null)
				throw new Exception();
		} catch (Exception ex) {
			//JOptionPane.showMessageDialog(null, "Error reading directory " + file.getAbsolutePath(), "Warning",
			//		JOptionPane.WARNING_MESSAGE);
			return;
		}
		for (File f : files)
			children.add(new FileTreeNode(f));
		Collections.sort(children, (FileTreeNode o1, FileTreeNode o2) -> o1.file.compareTo(o2.file));
	}

}
