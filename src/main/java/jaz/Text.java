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

	void setText(String newValue, String syntax) {
		if (!_textArea.getText().equals(newValue)) {
			int caretPosition = _textArea.getCaretPosition();
			_textArea.setText(newValue);
			_textArea.setCaretPosition(caretPosition);
		}
		_textArea.setSyntaxEditingStyle("text/" + syntax);
		_textArea.setCodeFoldingEnabled(true);
	}
}
