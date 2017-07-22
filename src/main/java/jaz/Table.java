package jaz;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import jaz.TableModel.COLOR;
import jaz.TableModel.Row;
import jaz.TableModel.Star;

public final class Table extends TabContent {
	private final TableModel _tableModel;
	private final JTable _table;
	private final JScrollPane _scrollPane;

	private final SearchField _searchField;

	private final JPanel _content;

	private final int _minimumRowHeight;

	Table() {
		_tableModel = new TableModel();

		_table = new JTable(_tableModel);
		_table.setAutoCreateRowSorter(true);

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

	private void prepareCellRenderer() {
		_table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
				JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);
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
				} else if (value == COLOR.MARKER) {
					component.setText(null);
					component.setIcon(Icons.COLOR);
				} else {
					component.setIcon(null);
				}

				Row row = _tableModel.getRow(_table.convertRowIndexToModel(rowIndex));
				if (row._backgroundColor != null && !isSelected) {
					// Don't call setBackground because the call set unselectedBackground.
					// https://stackoverflow.com/questions/28800370/tablecellrenderer-sets-color-to-many-cells-and-not-just-one
					JLabel label = new JLabel(component.getText(), component.getIcon(), component.getHorizontalAlignment());
					label.setBorder(component.getBorder());
					label.setOpaque(true);
					label.setForeground(component.getForeground());
					label.setBackground(row._backgroundColor);
					label.setFont(component.getFont());
					return label;
				}

				return component;
			}
		});
	}

	private void prepareListeners() {
		_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 1) {
					return;
				}

				int viewRowIndex = _table.rowAtPoint(e.getPoint());
				int viewColumnIndex = _table.columnAtPoint(e.getPoint());
				if (viewRowIndex == -1 || viewColumnIndex == -1) {
					return;
				}

				int[] selectedViewRowIndexes = Arrays.copyOf(_table.getSelectedRows(), _table.getSelectedRows().length + 1);
				selectedViewRowIndexes[selectedViewRowIndexes.length - 1] = viewRowIndex;

				int modelRowIndex = _table.convertRowIndexToModel(viewRowIndex);
				int modelColumnIndex = _table.convertColumnIndexToModel(viewColumnIndex);
				Object value = _tableModel.getValueAt(modelRowIndex, modelColumnIndex);
				if (value instanceof Star) {
					for (int selectedViewRowIndex: selectedViewRowIndexes) {
						int selectedModelRowIndex = _table.convertRowIndexToModel(selectedViewRowIndex);
						if (value == Star.OFF) {
							_tableModel.getRow(selectedModelRowIndex)._star = Star.ON;
						} else {
							_tableModel.getRow(selectedModelRowIndex)._star = Star.OFF;
						}
						_tableModel.fireTableRowsUpdated(selectedModelRowIndex, selectedModelRowIndex);
					}
					_table.getRowSorter().allRowsChanged();
					_table.scrollRectToVisible(_table.getCellRect(_table.convertRowIndexToView(modelRowIndex), 0, true));
					_table.clearSelection();
				} else if (value == COLOR.MARKER) {
					Row row = _tableModel.getRow(modelRowIndex);
					Color newColor = JColorChooser.showDialog(Table.this._scrollPane, "Select color for cell background", row._backgroundColor);
					if (newColor != null) {
						for (int selectedViewRowIndex: selectedViewRowIndexes) {
							int selectedModelRowIndex = _table.convertRowIndexToModel(selectedViewRowIndex);
							_tableModel.getRow(selectedModelRowIndex)._backgroundColor = newColor;
							_tableModel.fireTableRowsUpdated(selectedModelRowIndex, selectedModelRowIndex);
						}
					}
					_table.clearSelection();
				}
			}
		});
	}

	@Override
	void prepare() {
		_tableModel.clearColumns();

		_tableModel.addStarColumn();
		_tableModel.addColorColumn();
	}

	@Override
	Component getComponent() {
		return _content;
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
			boolean isExtended = _tableModel.addRow(date, stackTraceElement, columns);
			if (isExtended) {
				for (int i = 0; i < _tableModel.getIconColumnLength(); i++) {
					_table.getColumnModel().getColumn(i).setMaxWidth(Icons.STAR_ON.getIconWidth() / 5 * 6);
				}
				_table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
			}

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

	private static StackTraceElement getCallerStackTraceElement() {
		return Arrays.stream(Thread.currentThread().getStackTrace()).filter(e -> !e.getMethodName().equals("getStackTrace") && !e.getClassName().startsWith(Table.class.getPackage().getName())).findFirst().get();
	}
}
