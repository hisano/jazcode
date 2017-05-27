package jaz;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

final class Tab {
	private final Window _window;
	private final String _name;

	private volatile Component _component = new JPanel();
	private volatile Component _content = _component;

	Tab(Window window, String name) {
		_window = window;
		_name = name;
	}

	String getName() {
		return _name;
	}

	Component getContent() {
		return _content;
	}

	Table table() {
		if (!(_component instanceof JTable)) {
			_component = new JTable();
			_content = new JScrollPane(_component);
		}
		repaint();
		return new Table((JTable)_component);
	}

	private void repaint() {
		_window.repaintAsync(this);
	}
}
