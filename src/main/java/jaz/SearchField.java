package jaz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

final class SearchField extends JTextField {
	private final TableRowSorter<? extends TableModel> _tableRowSorter;
	private final String _textIfEmpty;

	SearchField(JTable table, String textIfEmpty) {
		table.setAutoCreateRowSorter(true);
		_tableRowSorter = (TableRowSorter<? extends javax.swing.table.TableModel>)table.getRowSorter();

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
		});
	}

	private void search() {
		String text = getText();
		if (text.isEmpty()) {
			_tableRowSorter.setRowFilter(null);
		} else {
			_tableRowSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
				@Override
				public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
					for (int i = 0, length = entry.getValueCount(); i < length; i++) {
						String value = entry.getStringValue(i);
						if (value.contains(text)) {
							return true;
						}
					}
					return false;
				}
			});
		}
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
}
