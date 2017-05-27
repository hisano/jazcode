package jaz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

final class TableModel extends AbstractTableModel {
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

	private final List<Column> _columns = new LinkedList<>();
	private final List<Row> _rows = new LinkedList<>();

	@Override
	public int getRowCount() {
		return _rows.size();
	}

	@Override
	public int getColumnCount() {
		return _columns.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return _columns.get(columnIndex).getValueAt(rowIndex);
	}

	boolean hasHeader() {
		return false;
	}

	void clearColumns() {
		_columns.clear();
	}

	void addTimeColumn() {
		_columns.add(new Column() {
			@Override
			public String getHeaderName() {
				return "Time";
			}

			@Override
			public String getValueAt(int rowIndex) {
				return formatTime(_rows.get(rowIndex)._date);
			}
		});
		super.fireTableStructureChanged();
	}

	void addClassColumn() {
		_columns.add(new Column() {
			@Override
			public String getHeaderName() {
				return "Class";
			}

			@Override
			public Object getValueAt(int rowIndex) {
				try {
					return Class.forName(_rows.get(rowIndex)._stackTraceElement.getClassName()).getSimpleName();
				} catch (ClassNotFoundException e) {
					return "";
				}
			}
		});
		super.fireTableStructureChanged();
	}

	void addLocationColumn() {
		_columns.add(new Column() {
			@Override
			public String getHeaderName() {
				return "Location";
			}

			@Override
			public Object getValueAt(int rowIndex) {
				StackTraceElement stackTraceElement = _rows.get(rowIndex)._stackTraceElement;
				return stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber();
			}
		});
		super.fireTableStructureChanged();
	}

	void addRow(Date date, StackTraceElement stackTraceElement, Object[] columns) {
		_rows.add(new Row(date, stackTraceElement, columns));
		for (int i = 0; i < columns.length; i++) {
			int columnIndex = i;
			_columns.add(new Column() {
				@Override
				public String getHeaderName() {
					return "";
				}

				@Override
				public String getValueAt(int rowIndex) {
					return _rows.get(rowIndex)._columns[columnIndex].toString();
				}
			});
		}
		super.fireTableStructureChanged();
	}

	private interface Column {
		String getHeaderName();
		Object getValueAt(int rowIndex);
	}

	private class Row {
		final Date _date;
		final StackTraceElement _stackTraceElement;
		final Object[] _columns;

		Row(Date date, StackTraceElement stackTraceElement, Object[] columns) {
			_date = date;
			_stackTraceElement = stackTraceElement;
			_columns = columns;
		}
	}

	private static String formatTime(Date date) {
		synchronized (TIME_FORMATTER) {
			return TIME_FORMATTER.format(date);
		}
	}
}
