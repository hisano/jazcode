package jaz;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public final class Table {
	private final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

	private final JTable _table;

	Table(JTable table) {
		_table = table;
	}

	public Result row(Object... columns) {
		Objects.requireNonNull(columns);

		Object[] columnsWithTimeAndLine = new Object[columns.length + 2];
		columnsWithTimeAndLine[0] = formatTime(new Date());
		StackTraceElement stackTraceElement = getTopStackTraceElement();
		columnsWithTimeAndLine[1] = stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber();
		System.arraycopy(columns, 0, columnsWithTimeAndLine, 2, columns.length);

		SwingUtilities.invokeLater(() -> {
			_table.setTableHeader(null);

			DefaultTableModel model = (DefaultTableModel) _table.getModel();
			model.setColumnCount(columnsWithTimeAndLine.length);
			model.addRow(columnsWithTimeAndLine);

			_table.scrollRectToVisible(_table.getCellRect(_table.getRowCount() - 1, 0, true));
		});
		return new Result();
	}

	private StackTraceElement getTopStackTraceElement() {
		return Arrays.stream(Thread.currentThread().getStackTrace()).filter(e -> !e.getMethodName().equals("getStackTrace") && !e.getClassName().startsWith(Table.class.getPackage().getName())).findFirst().get();
	}

	private String formatTime(Date date) {
		synchronized (TIME_FORMATTER) {
			return TIME_FORMATTER.format(date);
		}
	}
}
