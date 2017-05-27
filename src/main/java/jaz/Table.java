package jaz;

import java.awt.Component;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public final class Table extends Result implements TabContent {
	private final TableModel _tableModel;
	private final JTable _table;
	private final JScrollPane _scrollPane;

	Table() {
		_tableModel = new TableModel();
		_table = new JTable(_tableModel);
		_scrollPane = new JScrollPane(_table);
	}

	Table prepare() {
		SwingUtilities.invokeLater(() -> {
			_tableModel.clearColumns();
		});
		return this;
	}

	@Override
	public Component getComponent() {
		return _scrollPane;
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
			if (!_tableModel.hasHeader()) {
				_table.setTableHeader(null);
			}

			_tableModel.addRow(date, stackTraceElement, columns);

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
