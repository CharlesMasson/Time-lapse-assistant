package interfaceGraphique.arborescenceDossiers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

class IconCellRenderer extends JLabel implements TreeCellRenderer {

	private static final Color SELECTION_FOREGROUND = UIManager.getColor("Tree.selectionForeground");
	private static final Color TEXT_FOREGROUND = UIManager.getColor("Tree.textForeground");
	private static final Color SELECTION_BACKGROUND = UIManager.getColor("Tree.selectionBackground");
	private static final Color TEXT_BACKGROUND = UIManager.getColor("Tree.textBackground");
	private static final Color SELECTION_BORDER_COLOR = UIManager.getColor("Tree.selectionBorderColor");

	protected boolean m_selected;

	public IconCellRenderer() {
		super();
		setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		setText(obj == null ? "Retrieving data..." : obj.toString());

//		if (obj instanceof Boolean)
//			setText("Retrieving data...");
//		if (obj == null)
//			setText("Retrieving data...");
//		if (obj instanceof IconData) {
//			IconData idata = (IconData) obj;
//			if (expanded)
//				;//setIcon(idata.getExpandedIcon());
//			else
//				setIcon(idata.getIcon());
//		} else
//			setIcon(null);
		setFont(tree.getFont());
		setForeground(sel ? SELECTION_FOREGROUND : TEXT_FOREGROUND);
		setBackground(sel ? SELECTION_BACKGROUND : TEXT_BACKGROUND);
		m_selected = sel;
		return this;
	}

	@Override
	public void paintComponent(Graphics g) {
		Color bColor = getBackground();
//		Icon icon = getIcon();

		g.setColor(bColor);
//		if (icon != null && getText() != null)
//			offset = (icon.getIconWidth() + getIconTextGap());
		g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

		if (m_selected) {
			g.setColor(SELECTION_BORDER_COLOR);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
		super.paintComponent(g);
	}
}
