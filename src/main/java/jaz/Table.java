package jaz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public final class Table extends Result implements TabContent {
	private final TableModel _tableModel;
	private final JTable _table;
	private final JScrollPane _scrollPane;
	private final SearchField _searchField;
	private final JPanel _content;

	private final int _minimumRowHeight;

	Table() {
		_tableModel = new TableModel();
		_table = new JTable(_tableModel);
		_scrollPane = new JScrollPane(_table);
		_searchField = new SearchField(_table, "Search Table...");

		_content = new JPanel();
		_content.setBorder(new EmptyBorder(Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE, Window.BORDER_SIZE));
		_content.setLayout(new BorderLayout());
		_scrollPane.setBorder(new CompoundBorder(new EmptyBorder(0, 0, Window.BORDER_SIZE, 0), _searchField.getBorder()));
		_content.add(_scrollPane, BorderLayout.CENTER);
		_content.add(_searchField, BorderLayout.SOUTH);

		_minimumRowHeight = new JLabel("A").getPreferredSize().height;

		prepareCellRenderer();
	}

	private void prepareCellRenderer() {
		TableCellRenderer defaultRenderer = _table.getDefaultRenderer(Object.class);
		_table.setDefaultRenderer(Object.class, new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
				JLabel component = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);
				if (value instanceof Image) {
					component.setText(null);
					component.setIcon(new ImageIcon((Image)value));
				} else {
					component.setIcon(null);
				}
				return component;
			}
		});
	}

	@Override
	public Component getComponent() {
		return _content;
	}

	Table prepare() {
		SwingUtilities.invokeLater(() -> {
			_tableModel.clearColumns();
		});
		return this;
	}

	public Table timeColumn() {
		return executeOnEventDispatchThread(_tableModel::addTimeColumn);
	}

	public Table classColumn() {
		return executeOnEventDispatchThread(_tableModel::addClassColumn);
	}

	public Table locationColumn() {
		return executeOnEventDispatchThread(_tableModel::addLocationColumn);
	}

	public Table columns(Object... columns) {
		Objects.requireNonNull(columns);

		Date date = new Date();
		StackTraceElement stackTraceElement = getCallerStackTraceElement();
		return executeOnEventDispatchThread(() -> {
			_tableModel.addRow(date, stackTraceElement, columns);

			List<TableModel.Row> rows = _tableModel.getAllRows();
			for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
				OptionalInt maxHeightOptional = rows.get(rowIndex).getAllColumns().stream().filter(column -> column instanceof Image).mapToInt(column -> ((Image)column).getHeight(null)).max();
				if (maxHeightOptional.isPresent()) {
					_table.setRowHeight(rowIndex,  Math.max(_minimumRowHeight, maxHeightOptional.getAsInt()));
				} else {
					_table.setRowHeight(rowIndex,  _minimumRowHeight);
				}
			}

			_table.scrollRectToVisible(_table.getCellRect(_table.getRowCount() - 1, 0, true));
		});
	}

	private Table executeOnEventDispatchThread(Runnable operation) {
		SwingUtilities.invokeLater(operation);
		return this;
	}

	private static StackTraceElement getCallerStackTraceElement() {
		return Arrays.stream(Thread.currentThread().getStackTrace()).filter(e -> !e.getMethodName().equals("getStackTrace") && !e.getClassName().startsWith(Table.class.getPackage().getName())).findFirst().get();
	}
}
