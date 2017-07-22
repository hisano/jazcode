package jaz;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

abstract class TabContent extends Result {
	static final TabContent EMPTY = new TabContent() {
		Component _component = new JPanel();

		@Override
		public Component getComponent() {
			return _component;
		}
	};

	void prepare() {
	}

	abstract Component getComponent();

	<T> T executeOnEventDispatchThread(Runnable operation) {
		SwingUtilities.invokeLater(operation);
		return (T)this;
	}
}
