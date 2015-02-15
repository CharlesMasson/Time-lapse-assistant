package interfaceGraphique.arborescenceDossiers;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import interfaceGraphique.TLAssistant;
import javax.swing.tree.TreeNode;

public class FileTree extends JPanel {

	protected JTree tree;
	protected DefaultTreeModel model;
	private TLAssistant fenêtre;

	public FileTree(FileTreeSelectionListener ftls) {
		super(new BorderLayout());

		TreeNode top = new FileTreeNode(File.listRoots());
		model = new DefaultTreeModel(top);
		tree = new JTree(model);
		tree.setRootVisible(false);
		tree.putClientProperty("JTree.lineStyle", "Angled");

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
//				FileTreeNode node = getFileTreeNode(e.getPath());
//				FileNode fnode = getFileNode(node);
//				if (fnode != null)
//					ftls.valueChanged(new FileTreeSelectionEvent(this, fnode.getFile()));
				ftls.valueChanged(new FileTreeSelectionEvent(this, ((FileTreeNode) e.getPath().getLastPathComponent()).getFile()));

			}
		});

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setEditable(false);

		JScrollPane s = new JScrollPane();
		s.getViewport().add(tree);
		add(s, BorderLayout.CENTER);

		setVisible(true);
	}

}
