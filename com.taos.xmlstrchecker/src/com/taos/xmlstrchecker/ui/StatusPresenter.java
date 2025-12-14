package com.taos.xmlstrchecker.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Label;

import com.taos.xmlstrchecker.core.CheckResult.StatusKind;

public class StatusPresenter {

    private final Label lblStatus;
    private Font boldFont;

    public StatusPresenter(Label lblStatus) {
        this.lblStatus = lblStatus;
    }

    public void show(StatusKind kind, String message) {
        if (lblStatus == null || lblStatus.isDisposed()) return;

        lblStatus.setText(message != null ? message : "");
        lblStatus.setFont(lblStatus.getDisplay().getSystemFont());
        lblStatus.setForeground(lblStatus.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        switch (kind) {
            case SUCCESS:
                lblStatus.setForeground(lblStatus.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                //lblStatus.setFont(getBoldFont());
                break;
            case WARNING:
                lblStatus.setForeground(lblStatus.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
                break;
            case ERROR:
                lblStatus.setForeground(lblStatus.getDisplay().getSystemColor(SWT.COLOR_RED));
                break;
            case READY:
            default:
                break;
        }

        lblStatus.getParent().layout();
    }

    private Font getBoldFont() {
        if (boldFont != null && !boldFont.isDisposed()) return boldFont;

        FontData[] fds = lblStatus.getFont().getFontData();
        for (FontData fd : fds) fd.setStyle(SWT.BOLD);

        boldFont = new Font(lblStatus.getDisplay(), fds);
        return boldFont;
    }

    public void dispose() {
        if (boldFont != null && !boldFont.isDisposed()) boldFont.dispose();
    }
}
