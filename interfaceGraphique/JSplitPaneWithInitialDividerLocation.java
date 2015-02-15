package interfaceGraphique;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JSplitPane;

public class JSplitPaneWithInitialDividerLocation extends JSplitPane {

	private static final long serialVersionUID = 1L;
	private boolean painted;
	private double dividerLocation;

	public JSplitPaneWithInitialDividerLocation(double dividerLocation) {
		super();
		this.dividerLocation = dividerLocation;
	}

	public JSplitPaneWithInitialDividerLocation(int arg0, double dividerLocation) {
		super(arg0);
		this.dividerLocation = dividerLocation;
	}

	public JSplitPaneWithInitialDividerLocation(int arg0, boolean arg1,
			double dividerLocation) {
		super(arg0, arg1);
		this.dividerLocation = dividerLocation;
	}

	public JSplitPaneWithInitialDividerLocation(int arg0, Component arg1,
			Component arg2, double dividerLocation) {
		super(arg0, arg1, arg2);
		this.dividerLocation = dividerLocation;
	}

	public JSplitPaneWithInitialDividerLocation(int arg0, boolean arg1,
			Component arg2, Component arg3, double dividerLocation) {
		super(arg0, arg1, arg2, arg3);
		this.dividerLocation = dividerLocation;
	}

	@Override
	public void paint(Graphics g) {
		if (!painted) {
			painted = true;
			setDividerLocation(dividerLocation);
		}
		super.paint(g);
	}
}
