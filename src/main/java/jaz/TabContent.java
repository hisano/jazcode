package jaz;

import java.awt.Component;

import javax.swing.JPanel;

abstract class TabContent extends Result {
	static final TabContent EMPTY = new TabContent() {
		Component _component = new JPanel();

		@Override
		public Component getComponent() {
			return _component;
		}
	};

	abstract Component getComponent();

	void prepareAsync() {
	}
}
