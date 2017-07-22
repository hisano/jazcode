package jaz;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

final class Tab {
	private final Window _window;
	private final String _name;

	private volatile TabContent _tabContent = TabContent.EMPTY;
	private volatile boolean _isTabContentChanged;

	Tab(Window window, String name) {
		_window = window;
		_name = name;
	}

	Table table() {
		if (!(_tabContent instanceof Table)) {
			_tabContent = new Table();
			_isTabContentChanged = true;
		}

		prepareAndShowAsync();

		return (Table)_tabContent;
	}

	Text text(String value, String syntax) {
		if (!(_tabContent instanceof Text)) {
			_tabContent = new Text();
			_isTabContentChanged = true;
		}

		((Text)_tabContent).setText(value, syntax);

		prepareAndShowAsync();

		return (Text)_tabContent;
	}

	private void prepareAndShowAsync() {
		SwingUtilities.invokeLater(() -> {
			_tabContent.prepare();
			showTab();
		});
	}

	private void showTab() {
		JTabbedPane tabbedPane = _window.getTabbedPane();
		int tabIndex = -1;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (_name.equals(tabbedPane.getTitleAt(i))) {
				tabIndex = i;
				break;
			}
		}
		if (tabIndex == -1) {
			tabbedPane.addTab(_name, _tabContent.getComponent());
		} else if (_isTabContentChanged) {
			_isTabContentChanged = false;
			tabbedPane.removeTabAt(tabIndex);
			tabbedPane.insertTab(_name, null, _tabContent.getComponent(), null, tabIndex);
		}
	}
}
