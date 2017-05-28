package jaz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import jaz.TableModel.Star;

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

		_minimumRowHeight = Math.max(new JLabel("A").getPreferredSize().height, Icons.STAR_ON.getIconHeight());

		prepareCellRenderer();
		prepareListeners();
	}

	private void prepareListeners() {
		_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 1) {
					return;
				}

				int viewRowIndex = _table.getSelectedRow();
				int viewColumnIndex = _table.getSelectedColumn();
				if (viewRowIndex == -1 || viewColumnIndex == -1) {
					return;
				}

				int modelRowIndex = _table.convertRowIndexToModel(viewRowIndex);
				int modelColumnIndex = _table.convertColumnIndexToModel(viewColumnIndex);
				Object value = _tableModel.getValueAt(modelRowIndex, modelColumnIndex);
				if (value instanceof Star) {
					if (value == Star.OFF) {
						_tableModel.getRow(modelRowIndex)._star = Star.ON;
					} else {
						_tableModel.getRow(modelRowIndex)._star = Star.OFF;
					}
					_tableModel.fireTableCellUpdated(modelRowIndex, modelColumnIndex);
					_table.getRowSorter().allRowsChanged();
					_table.scrollRectToVisible(_table.getCellRect(_table.getSelectedRow(), 0, true));
				}
			}
		});
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
				} else if (value instanceof Star) {
					component.setText(null);
					if ((Star)value == Star.ON) {
						component.setIcon(Icons.STAR_ON);
					} else {
						component.setIcon(Icons.STAR_OFF);
					}
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

	@Override
	public void prepareAsync() {
		SwingUtilities.invokeLater(() -> {
			_tableModel.clearColumns();

			_tableModel.addStarColumn();
		});
	}

	public Table timeColumn() {
		return timeColumn(TableModel.DEFAULT_TIME_FORMAT);
	}

	public Table timeColumn(String format) {
		Objects.requireNonNull(format);
		return executeOnEventDispatchThread(() -> _tableModel.addTimeColumn(format));
	}

	public Table classColumn() {
		return executeOnEventDispatchThread(_tableModel::addClassColumn);
	}

	public Table fullClassColumn() {
		return executeOnEventDispatchThread(_tableModel::addFullClassColumn);
	}

	public Table methodColumn() {
		return executeOnEventDispatchThread(_tableModel::addMethodColumn);
	}

	public Table fileColumn() {
		return executeOnEventDispatchThread(_tableModel::addFileColumn);
	}

	public Table lineNumberColumn() {
		return executeOnEventDispatchThread(_tableModel::addLineNumberColumn);
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

			_table.getColumnModel().getColumn(0).setMaxWidth(Icons.STAR_ON.getIconWidth() / 5 * 6);

			_table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));

			List<TableModel.Row> rows = _tableModel.getAllRows();
			for (int modelRowIndex = 0; modelRowIndex < rows.size(); modelRowIndex++) {
				int viewRowIndex = _table.getRowSorter().convertRowIndexToView(modelRowIndex);
				if (viewRowIndex != -1) {
					OptionalInt maxHeightOptional = rows.get(modelRowIndex).getAllColumns().stream().filter(column -> column instanceof Image).mapToInt(column -> ((Image)column).getHeight(null)).max();
					if (maxHeightOptional.isPresent()) {
						_table.setRowHeight(viewRowIndex,  Math.max(_minimumRowHeight, maxHeightOptional.getAsInt()));
					} else {
						_table.setRowHeight(viewRowIndex,  _minimumRowHeight);
					}
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
