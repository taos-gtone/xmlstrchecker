package com.taos.xmlstrchecker.parts;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.taos.xmlstrchecker.core.CheckResult;
import com.taos.xmlstrchecker.core.DependencyChecker;
import com.taos.xmlstrchecker.core.StrRuleValidatorService;
import com.taos.xmlstrchecker.core.XmlCheckerService;
import com.taos.xmlstrchecker.core.XmlParser;
import com.taos.xmlstrchecker.ui.ErrorTablePresenter;
import com.taos.xmlstrchecker.ui.StatusPresenter;


public class XmlStrView extends ViewPart {
	private Label myLabelInView;
	
	public static final String ID = "com.taos.xmlstrchecker.view";

    private Combo comboRecentFiles;
//    private Text txtFilePath;
    private Table errorTable;
    private Label lblStatus;
    
    // Presenters
    private StatusPresenter statusPresenter;
    private ErrorTablePresenter errorPresenter;

    // Core service
    private XmlCheckerService checker;
    
    private org.eclipse.swt.graphics.Font boldStatusFont;

	@PostConstruct
	public void createPartControl(Composite parent) {
		// 1열짜리 GridLayout: 위/중간/아래 세로로 쌓기
        parent.setLayout(new GridLayout(1, false));

        // 위쪽 영역 (파일 선택 + 체크 버튼)
        Composite topArea = new Composite(parent, SWT.NONE);
        topArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        topArea.setLayout(new GridLayout(4, false)); // 드롭다운, 텍스트, Browse, Check

        // 아래 영역 (에러 테이블 + 상태바)
        Composite bottomArea = new Composite(parent, SWT.NONE);
        bottomArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        bottomArea.setLayout(new GridLayout(1, false));

        createTopAreaControls(topArea);
        createBottomAreaControls(bottomArea);
        
        // presenters/service wiring (UI 생성 후)
        statusPresenter = new StatusPresenter(lblStatus);
        errorPresenter = new ErrorTablePresenter(errorTable);

        checker = new XmlCheckerService(new XmlParser(), new DependencyChecker(), new StrRuleValidatorService());

        // initial status
        statusPresenter.show(CheckResult.StatusKind.READY, "Ready.");
	}

	@Override
    public void setFocus() {
        if (comboRecentFiles != null && !comboRecentFiles.isDisposed()) {
            comboRecentFiles.setFocus();
        }
    }
	
	@Override
    public void dispose() {
        if (statusPresenter != null) {
            statusPresenter.dispose();
        }
        super.dispose();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI Builders
    // ─────────────────────────────────────────────────────────────────────────────
	
	private void createTopAreaControls(Composite parent) {
	    // topArea는 위/아래 두 줄만 가진다 (라벨, row)
		parent.setLayout(new GridLayout(1, false));

	    // ── 라벨 (첫 줄)
	    Label lblTitle = new Label(parent, SWT.NONE);
	    lblTitle.setText("Dependency Rules: v5.6 (250416)");

	    // ── 콤보 + 버튼들을 담을 row (둘째 줄)
	    Composite row = new Composite(parent, SWT.NONE);
	    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	    GridLayout rowLayout = new GridLayout(3, false); // ★ 한 줄에 3개
	    rowLayout.marginWidth = 0;
	    rowLayout.marginHeight = 0;
	    rowLayout.horizontalSpacing = 8;
	    row.setLayout(rowLayout);

	    // combo
	    comboRecentFiles = new Combo(row, SWT.DROP_DOWN | SWT.READ_ONLY);
	    comboRecentFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Browse 버튼 (파일 선택)
        Button btnBrowse = new Button(row, SWT.PUSH);
        btnBrowse.setText("Browse...");
        btnBrowse.setToolTipText("Select an XML file from your file system.");
        btnBrowse.addListener(SWT.Selection, e -> onBrowse(btnBrowse));

        // Check 버튼 (검사 시작)
        Button btnCheck = new Button(row, SWT.PUSH);
        btnCheck.setText("Check");
        btnCheck.setToolTipText("Validate XML format and dependency rules.");
        btnCheck.addListener(SWT.Selection, e -> runCheck());
    }
	
    private void createBottomAreaControls(Composite parent) {
        Label lblErrors = new Label(parent, SWT.NONE);
        lblErrors.setText("Error Messages:");

        errorTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        errorTable.setHeaderVisible(true);
        errorTable.setLinesVisible(true);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 150;
        errorTable.setLayoutData(gd);

        TableColumn colType = new TableColumn(errorTable, SWT.NONE);
        colType.setText("Type");
        colType.setWidth(80);

        TableColumn colMessage = new TableColumn(errorTable, SWT.NONE);
        colMessage.setText("Message");
        colMessage.setWidth(400);

        TableColumn colLocation = new TableColumn(errorTable, SWT.NONE);
        colLocation.setText("Location");
        colLocation.setWidth(150);

        lblStatus = new Label(parent, SWT.NONE);
        lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        lblStatus.setText("Ready.");
    }
    
    // ─────────────────────────────────────────────────────────────────────────────
    // Event handlers
    // ─────────────────────────────────────────────────────────────────────────────

    private void onBrowse(Button btnBrowse) {
        FileDialog dialog = new FileDialog(btnBrowse.getShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

        String selected = dialog.open();
        if (selected == null) return;

        addRecentFile(selected);

        // reset UI state
        if (errorPresenter != null) errorPresenter.clear();
        if (statusPresenter != null) statusPresenter.show(CheckResult.StatusKind.READY, "");
    }
    
    private void runCheck() {
    	if (errorPresenter != null) errorPresenter.clear();

        String path = getSelectedPath();

        CheckResult result = checker.check(path);

        // show errors
        result.getErrors().forEach(errorPresenter::add);

        // show status
        statusPresenter.show(result.getStatusKind(), result.getStatusMessage());
    }
    
	// ─────────────────────────────────────────────────────────────────────────────
    // Helpers (view-only)
    // ─────────────────────────────────────────────────────────────────────────────

    private String getSelectedPath() {
        if (comboRecentFiles == null || comboRecentFiles.isDisposed()) return null;
        int idx = comboRecentFiles.getSelectionIndex();
        if (idx < 0) return null;
        String path = comboRecentFiles.getItem(idx);
        return (path != null && !path.trim().isEmpty()) ? path.trim() : null;
    }

    private void addRecentFile(String path) {
    	// 이미 목록에 있으면 지우고 맨 위로 올리기
        int existingIndex = comboRecentFiles.indexOf(path);
        if (existingIndex >= 0) {
            comboRecentFiles.remove(existingIndex);
        }

        // 맨 앞(0번)에 추가
        comboRecentFiles.add(path, 0);

        // 콤보에서 이 항목을 선택 상태로 만들기
        comboRecentFiles.select(0);
    }    
}
