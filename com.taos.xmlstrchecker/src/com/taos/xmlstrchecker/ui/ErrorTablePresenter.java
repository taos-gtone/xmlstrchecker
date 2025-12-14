package com.taos.xmlstrchecker.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.taos.xmlstrchecker.core.CheckError;

public class ErrorTablePresenter {

    private final Table table;

    public ErrorTablePresenter(Table table) {
        this.table = table;
    }

    public void clear() {
        if (table != null && !table.isDisposed()) table.removeAll();
    }

    public void add(CheckError e) {
        if (table == null || table.isDisposed() || e == null) return;

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, e.type != null ? e.type : "");
        item.setText(1, e.message != null ? e.message : "");
        item.setText(2, e.location != null ? e.location : "");
    }
}
