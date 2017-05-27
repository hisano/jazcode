package jaz;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public final class Window {
	static final int BORDER_SIZE = 5;

	private final JFrame _frame;
	private final JTabbedPane _tabbedPane;

	private final Map<String, Tab> _tabs = new ConcurrentHashMap<String, Tab>();

	Window(String name) {
		_frame = new JFrame(name);
		_frame.setAlwaysOnTop(true);
		Rectangle desktopArea = getDesktopArea();
		_frame.setSize(desktopArea.width / 4, desktopArea.height / 3);
		_frame.setLocation(desktopArea.width - _frame.getWidth(), desktopArea.height - _frame.getHeight());

		_tabbedPane = new JTabbedPane();
		_tabbedPane.setBorder(new EmptyBorder(Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE));
		_frame.setContentPane(_tabbedPane);
	}

	private static Rectangle getDesktopArea() {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		Rectangle displayArea = gc.getBounds();

		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

		return new Rectangle(displayArea.x + screenInsets.left, displayArea.y + screenInsets.top, displayArea.width - screenInsets.left - screenInsets.right, displayArea.height - screenInsets.top - screenInsets.bottom);
	}

	Window show() {
		_frame.setVisible(true);
		return this;
	}

	public Tab tab(String name) {
		Objects.requireNonNull(name);

		synchronized(_tabs) {
			if (!_tabs.containsKey(name)) {
				_tabs.put(name, new Tab(this, name));
			}
			Tab tab = _tabs.get(name);
			repaintAsync(tab);
			return tab;
		}
	}

	private int getTabIndex(String name) {
		for (int i = 0; i < _tabbedPane.getTabCount(); i++) {
			if (name.equals(_tabbedPane.getTitleAt(i))) {
				return i;
			}
		}
		return -1;
	}

	void repaintAsync(Tab tab) {
		SwingUtilities.invokeLater(() -> {
			String name = tab.getName();
			int tabIndex = getTabIndex(name);
			if (tabIndex == -1) {
				_tabbedPane.addTab(name, tab.getContentComponent());
			} else {
				_tabbedPane.removeTabAt(tabIndex);
				_tabbedPane.insertTab(name, null, tab.getContentComponent(), null, tabIndex);
			}
		});
	}
}
