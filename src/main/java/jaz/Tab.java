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
		return createTab(Table.class);
	}

	Text text() {
		return createTab(Text.class);
	}

	private <T extends TabContent> T createTab(Class<T> clazz) {
		if (!clazz.isInstance(_tabContent)) {
			try {
				_tabContent = clazz.getDeclaredConstructor().newInstance();
			} catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
				throw new AssertionError();
			}
			_isTabContentChanged = true;
		}

		prepareAndShowAsync();

		return (T)_tabContent;
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
