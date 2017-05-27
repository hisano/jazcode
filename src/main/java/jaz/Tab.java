package jaz;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

final class Tab {
	private final Window _window;
	private final String _name;

	private volatile TabContent _tabContent = TabContent.EMPTY;

	Tab(Window window, String name) {
		_window = window;
		_name = name;
	}

	void showAsync() {
		SwingUtilities.invokeLater(() -> {
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
			} else {
				tabbedPane.removeTabAt(tabIndex);
				tabbedPane.insertTab(_name, null, _tabContent.getComponent(), null, tabIndex);
			}
		});
	}

	Table table() {
		if (!(_tabContent instanceof Table)) {
			_tabContent = new Table();
		}

		_tabContent.prepareAsync();
		showAsync();

		return (Table)_tabContent;
	}
}
