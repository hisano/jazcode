package jaz;

import java.awt.Component;

import javax.swing.JPanel;

interface TabContent {
	TabContent EMPTY = new TabContent() {
		Component _component = new JPanel();

		@Override
		public Component getComponent() {
			return _component;
		}
	};

	Component getComponent();
}
