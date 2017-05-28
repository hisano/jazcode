package jaz;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

final class TableModel extends AbstractTableModel {
	static final String DEFAULT_TIME_FORMAT = "HH:mm:ss.SSS";

	private final List<Column> _column = new LinkedList<>();
	private final List<Row> _rows = new LinkedList<>();

	private int _nextColumnIndex;
	private int _iconColumnLength;

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

	int getIconColumnLength() {
		return _iconColumnLength;
	}

	void clearColumns() {
		_nextColumnIndex = 0;
		_iconColumnLength = 0;
	}

	void addStarColumn() {
		addIconColumn(row -> row._star);
	}

	void addColorColumn() {
		addIconColumn(row -> COLOR.MARKER);
	}

	private void addIconColumn(Function<Row, Object> getValueAtFunction) {
		addColumn("", getValueAtFunction);
		_iconColumnLength++;
	}

	void addTimeColumn(String format) {
		addMetadataColumnAndRepaint("Time", row -> {
			try {
				return new SimpleDateFormat(format).format(row._date);
			} catch (IllegalArgumentException e) {
				return new SimpleDateFormat(DEFAULT_TIME_FORMAT).format(row._date);
			}
		});
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
		boolean isExtended = addColumn(headerName, getValueAtFunction);
		if (isExtended) {
			super.fireTableStructureChanged();
		}
	}

	boolean addColumn(String headerName, Function<Row, Object> getValueAtFunction) {
		boolean isExtended = true;
		if (_nextColumnIndex < _column.size()) {
			_column.remove(_nextColumnIndex);
			isExtended = false;
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
		return isExtended;
	}

	boolean addRow(Date date, StackTraceElement stackTraceElement, Object[] columns) {
		_rows.add(new Row(date, stackTraceElement, columns));

		boolean isExtended = false;
		for (int i = 0; i < columns.length; i++) {
			int columnIndex = i;
			isExtended |= addColumn("Parameter[" + i + "]", row -> row.getColumn(columnIndex));
		}
		if (isExtended) {
			super.fireTableStructureChanged();
		}

		super.fireTableRowsInserted(_rows.size() - 1, _rows.size() - 1);

		return isExtended;
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

	enum COLOR {
		MARKER;
	}

	class Row {
		final Date _date;
		final StackTraceElement _stackTraceElement;
		final Object[] _columns;

		Star _star = Star.OFF;
		Color _backgroundColor = Color.WHITE;

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
}
