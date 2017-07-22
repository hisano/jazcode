package jaz;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Jaz {
	private static final Map<String, Window> _windows = new ConcurrentHashMap<String, Window>();

	public static Text text(String name) {
		Objects.requireNonNull(name);
		return showDefaultWindow().tab(name).text();
	}

	public static Table table(String name) {
		Objects.requireNonNull(name);
		return showDefaultWindow().tab(name).table();
	}

	private static Window showDefaultWindow() {
		return window("Jaz");
	}

	public static Window window(String name) {
		Objects.requireNonNull(name);
		synchronized (_windows) {
			if (!_windows.containsKey(name)) {
				_windows.put(name, new Window(name));
			}
			Window window = _windows.get(name);
			window.show();
			return window;
		}
	}
}
