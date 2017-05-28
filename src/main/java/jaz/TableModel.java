package jaz;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

final class TableModel extends AbstractTableModel {
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

	private final List<Column> _column = new LinkedList<>();
	private final List<Row> _rows = new LinkedList<>();

	private int _nextColumnIndex;

	@Override
	public int getColumnCount() {
		return _column.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return _column.get(columnIndex).getHeaderName();
	}

	@Override
	public int getRowCount() {
		return _rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return _column.get(columnIndex).getValueAt(rowIndex);
	}

	void clearColumns() {
		_nextColumnIndex = 0;
	}

	void addStarColumn() {
		addColumn("", row -> row._star);
	}

	void addTimeColumn() {
		addMetadataColumnAndRepaint("Time", row -> formatTime(row._date));
	}

	void addClassColumn() {
		addMetadataColumnAndRepaint("Class", row -> {
			try {
				return Class.forName(row._stackTraceElement.getClassName()).getSimpleName();
			} catch (ClassNotFoundException e) {
				return "";
			}
		});
	}

	void addFullClassColumn() {
		addMetadataColumnAndRepaint("Full Class", row -> row._stackTraceElement.getClassName());
	}

	void addMethodColumn() {
		addMetadataColumnAndRepaint("Method", row -> row._stackTraceElement.getMethodName());
	}

	void addFileColumn() {
		addMetadataColumnAndRepaint("File", row -> row._stackTraceElement.getFileName());
	}

	void addLineNumberColumn() {
		addMetadataColumnAndRepaint("Line Number", row -> row._stackTraceElement.getLineNumber());
	}

	void addLocationColumn() {
		addMetadataColumnAndRepaint("Location", row -> row._stackTraceElement.getFileName() + ":" + row._stackTraceElement.getLineNumber());
	}

	void addMetadataColumnAndRepaint(String headerName, Function<Row, Object> getValueAtFunction) {
		addColumn(headerName, getValueAtFunction);
		super.fireTableStructureChanged();
	}

	void addColumn(String headerName, Function<Row, Object> getValueAtFunction) {
		if (_nextColumnIndex < _column.size()) {
			_column.remove(_nextColumnIndex);
		}
		_column.add(_nextColumnIndex, new Column() {
			@Override
			public String getHeaderName() {
				return headerName;
			}

			@Override
			public Object getValueAt(int rowIndex) {
				return getValueAtFunction.apply(_rows.get(rowIndex));
			}
		});
		_nextColumnIndex++;
	}

	void addRow(Date date, StackTraceElement stackTraceElement, Object[] columns) {
		_rows.add(new Row(date, stackTraceElement, columns));
		for (int i = 0; i < columns.length; i++) {
			int columnIndex = i;
			addColumn("Parameter[" + i + "]", row -> row.getColumn(columnIndex));
		}
		super.fireTableStructureChanged();
	}

	Row getRow(int rowIndex) {
		return _rows.get(rowIndex);
	}

	List<Row> getAllRows() {
		return _rows;
	}

	private interface Column {
		String getHeaderName();
		Object getValueAt(int rowIndex);
	}

	enum Star {
		ON, OFF;
	}

	class Row {
		final Date _date;
		final StackTraceElement _stackTraceElement;
		final Object[] _columns;

		Star _star = Star.OFF;

		Row(Date date, StackTraceElement stackTraceElement, Object[] columns) {
			_date = date;
			_stackTraceElement = stackTraceElement;
			_columns = columns;
		}

		Object getColumn(int columnIndex) {
			if (_columns.length <= columnIndex) {
				return null;
			}
			return _columns[columnIndex];
		}

		List<Object> getAllColumns() {
			return Arrays.asList(_columns);
		}
	}

	private static String formatTime(Date date) {
		synchronized (TIME_FORMATTER) {
			return TIME_FORMATTER.format(date);
		}
	}
}
