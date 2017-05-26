package jaz;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public final class Tab {
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

	public Result image(BufferedImage image) {
		Objects.requireNonNull(image);
		_component = new JLabel(new ImageIcon(image));
		_content = _component;
		repaint();
		return new Result();
	}

	public Table table() {
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
