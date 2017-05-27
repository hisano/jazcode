package jaz;

import java.awt.Component;

final class Tab {
	private final Window _window;
	private final String _name;

	private volatile TabContent _tabContent = TabContent.EMPTY;

	Tab(Window window, String name) {
		_window = window;
		_name = name;
	}

	String getName() {
		return _name;
	}

	Component getContentComponent() {
		return _tabContent.getComponent();
	}

	Table table() {
		if (!(_tabContent instanceof Table)) {
			_tabContent = new Table();
		}
		repaint();
		return ((Table)_tabContent).prepare();
	}

	private void repaint() {
		_window.repaintAsync(this);
	}
}
