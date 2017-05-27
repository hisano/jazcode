package jaz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

final class TableModel extends AbstractTableModel {
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

	private final List<Column> _columns = new LinkedList<>();
	private final List<Row> _rows = new LinkedList<>();

	@Override
	public int getColumnCount() {
		return _columns.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return _columns.get(columnIndex).getHeaderName();
	}

	@Override
	public int getRowCount() {
		return _rows.size();
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
		addColumnAndRepaint("Time", row -> formatTime(row._date));
	}

	void addClassColumn() {
		addColumnAndRepaint("Class", row -> {
			try {
				return Class.forName(row._stackTraceElement.getClassName()).getSimpleName();
			} catch (ClassNotFoundException e) {
				return "";
			}
		});
	}

	void addLocationColumn() {
		addColumnAndRepaint("Location", row -> row._stackTraceElement.getFileName() + ":" + row._stackTraceElement.getLineNumber());
	}

	void addColumnAndRepaint(String headerName, Function<Row, Object> getValueAtFunction) {
		addColumn(headerName, getValueAtFunction);
		super.fireTableStructureChanged();
	}

	void addColumn(String headerName, Function<Row, Object> getValueAtFunction) {
		_columns.add(new Column() {
			@Override
			public String getHeaderName() {
				return headerName;
			}

			@Override
			public Object getValueAt(int rowIndex) {
				return getValueAtFunction.apply(_rows.get(rowIndex));
			}
		});
	}

	void addRow(Date date, StackTraceElement stackTraceElement, Object[] columns) {
		_rows.add(new Row(date, stackTraceElement, columns));
		for (int i = 0; i < columns.length; i++) {
			int columnIndex = i;
			addColumn("Parameter[" + i + "]", row -> row._columns[columnIndex]);
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
