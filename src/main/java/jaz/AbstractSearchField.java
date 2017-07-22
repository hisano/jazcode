package jaz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

abstract class AbstractSearchField extends JTextField {
	private final String _textIfEmpty;

	AbstractSearchField(String textIfEmpty) {
		_textIfEmpty = textIfEmpty;

		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				repaint();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				repaint();
			}
		});

		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				search();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				search();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				search();
			}

			private void search() {
				AbstractSearchField.this.search(getText());
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (!hasFocus() && "".equals(getText())) {
			Graphics2D g2d = (Graphics2D) g;

			Color oldColor = g.getColor();
			g.setColor(UIManager.getColor("textInactiveText"));

			RenderingHints oldRenderingHint = g2d.getRenderingHints();
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			try {
				g2d.drawString(_textIfEmpty, getInsets().left, getInsets().top + g2d.getFontMetrics().getAscent());
			} finally {
				g2d.setRenderingHints(oldRenderingHint);
				g2d.setColor(oldColor);
			}
		}
	}

	abstract void search(String text);
}
