package jaz;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class Text extends TabContent {
	private final RSyntaxTextArea _textArea;
	private final RTextScrollPane _scrollPane;

	private final JPanel _content;

	Text() {
		_textArea = new RSyntaxTextArea();
		_scrollPane = new RTextScrollPane(_textArea);

		_content = new JPanel();
		_content.setBorder(new EmptyBorder(Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE));
		_content.setLayout(new BorderLayout());
		_content.add(_scrollPane, BorderLayout.CENTER);
	}

	@Override
	Component getComponent() {
		return _content;
	}

	public Text value(String value) {
		return executeOnEventDispatchThread(() -> {
			if (_textArea.getText().equals(value)) {
				return;
			}

			int oldCaretPosition = _textArea.getCaretPosition();
			_textArea.setText(value);
			_textArea.setCaretPosition(Math.max(_textArea.getDocument().getLength(), oldCaretPosition));
		});
	}

	public Text syntax(String syntax) {
		return executeOnEventDispatchThread(() -> {
			String styleKey = "text/" + syntax;
			if (styleKey.equals(_textArea.getSyntaxEditingStyle())) {
				return;
			}

			_textArea.setSyntaxEditingStyle(styleKey);
			_textArea.setCodeFoldingEnabled(true);
		});
	}

	public Text lineWrap() {
		return executeOnEventDispatchThread(() -> {
			if (_textArea.getLineWrap()) {
				return;
			}

			_textArea.setLineWrap(true);
		});
	}
}
