package jaz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class Text extends TabContent {
	private final RSyntaxTextArea _textArea;
	private final RTextScrollPane _scrollPane;

	private final AbstractSearchField _searchField;

	private final JPanel _content;

	Text() {
		_textArea = new RSyntaxTextArea();
		_scrollPane = new RTextScrollPane(_textArea);

		_searchField = new SearchField();

		_content = new JPanel();
		_content.setBorder(
				new EmptyBorder(Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE));
		_content.setLayout(new BorderLayout());
		_scrollPane
				.setBorder(new CompoundBorder(new EmptyBorder(0, 0, Window.BORDER_SIZE, 0), _searchField.getBorder()));
		_content.add(_scrollPane, BorderLayout.CENTER);
		_content.add(_searchField, BorderLayout.SOUTH);
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
			_searchField.search(_searchField.getText());
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

	private final class SearchField extends AbstractSearchField {
		SearchField() {
			super("Search Text...");
		}

		@Override
		void search(String text) {
			_textArea.getHighlighter().removeAllHighlights();

			if (text.isEmpty()) {
				return;
			}

			try {
				boolean isFirstFound = true;
				Matcher matcher = Pattern.compile(text).matcher(_textArea.getText());
				while (matcher.find()) {
					if (isFirstFound) {
						isFirstFound = false;
						Rectangle viewRect = _textArea.modelToView(matcher.start());
						_textArea.scrollRectToVisible(viewRect);
					}

					_textArea.getHighlighter().addHighlight(matcher.start(), matcher.end(), DefaultHighlighter.DefaultPainter);
				}
			} catch (PatternSyntaxException e) {
				// ignore exception
			} catch (BadLocationException e) {
				new AssertionError();
			}
		}
	}
}
