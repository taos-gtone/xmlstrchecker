package com.taos.xmlstrchecker.parts;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import org.xml.sax.SAXException;


public class XmlStrView extends ViewPart {
	private Label myLabelInView;
	
	public static final String ID = "com.taos.xmlstrchecker.view";

    private Combo comboRecentFiles;
//    private Text txtFilePath;
    private Table errorTable;
    private Label lblStatus;

	@PostConstruct
	public void createPartControl(Composite parent) {
		/*
		System.out.println("Enter in SampleE4View postConstruct");

		myLabelInView = new Label(parent, SWT.BORDER);
		myLabelInView.setText("Welcome to XML Checker for STR Report document!");
		*/
		
		// 1열짜리 GridLayout: 위/중간/아래 세로로 쌓기
        parent.setLayout(new GridLayout(1, false));

        // 위쪽 영역 (파일 선택 + 체크 버튼)
        Composite topArea = new Composite(parent, SWT.NONE);
        topArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        topArea.setLayout(new GridLayout(4, false)); // 드롭다운, 텍스트, Browse, Check

//        // 가운데 영역 (설명/추가 정보)
//        Composite middleArea = new Composite(parent, SWT.NONE);
//        middleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//        middleArea.setLayout(new GridLayout(1, false));

        // 아래 영역 (에러 테이블 + 상태바)
        Composite bottomArea = new Composite(parent, SWT.NONE);
        bottomArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        bottomArea.setLayout(new GridLayout(1, false));

        createTopAreaControls(topArea);
//        createMiddleAreaControls(middleArea);
        createBottomAreaControls(bottomArea);
	}

	@Override
    public void setFocus() {
		comboRecentFiles.setFocus();
		
//        if (txtFilePath != null && !txtFilePath.isDisposed()) {
//            txtFilePath.setFocus();
//        }
    }

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not
	 * mix E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and
	 * you do not receive a ISelection
	 * 
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s==null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example
	 * we listen to a single Object (even the ISelection already captured in E3
	 * mode). <br/>
	 * You should change the parameter type of your received Object to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

		// Test if label exists (inject methods are called before PostConstruct)
		if (myLabelInView != null)
			myLabelInView.setText("Current single selection class is : " + o.getClass());
	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current array of objects received in case of multiple selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

		// Test if label exists (inject methods are called before PostConstruct)
		if (myLabelInView != null)
			myLabelInView.setText("This is a multiple selection of " + selectedObjects.length + " objects");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void createTopAreaControls(Composite parent) {
        // 1) 최근 파일 드롭다운 (콤보박스)
        comboRecentFiles = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboRecentFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboRecentFiles.setToolTipText("최근에 사용한 XML 파일 목록");

        // 예시로 몇 개 넣어두기 (나중에 필요 없으면 지워도 됨)
//        comboRecentFiles.add("C:/temp/sample1.xml");
//        comboRecentFiles.add("C:/temp/sample2.xml");

        comboRecentFiles.addListener(SWT.Selection, e -> {
//            int idx = comboRecentFiles.getSelectionIndex();
//            if (idx >= 0) {
//                String path = comboRecentFiles.getItem(idx);
//                txtFilePath.setText(path);
//            }
        });

        // 2) 파일 경로 입력 텍스트
//        txtFilePath = new Text(parent, SWT.BORDER);
//        txtFilePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//        txtFilePath.setMessage("XML 파일 경로를 입력하세요");

        // 3) Browse 버튼 (파일 선택)
        Button btnBrowse = new Button(parent, SWT.PUSH);
        btnBrowse.setText("Browse...");
        btnBrowse.setToolTipText("파일 탐색기에서 XML 파일을 선택합니다.");
        btnBrowse.addListener(SWT.Selection, e -> {
            // ✅ 여기서 getSite() 쓰지 말고, 버튼에서 직접 Shell 얻기
            org.eclipse.swt.widgets.Shell shell = btnBrowse.getShell();

            FileDialog dialog = new FileDialog(shell, SWT.OPEN);
            dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

            String selected = dialog.open();
            if (selected != null) {
//                txtFilePath.setText(selected);
                addRecentFile(selected);
                
                // 👇 파일 선택 시 status 초기화
                lblStatus.setText("");
            }
        });

        // 4) Check 버튼 (검사 시작)
        Button btnCheck = new Button(parent, SWT.PUSH);
        btnCheck.setText("Check");
        btnCheck.setToolTipText("XML 형식 및 의존성을 검사합니다.");
        btnCheck.addListener(SWT.Selection, e -> {
            runCheck();
        });
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
    
//    private void createMiddleAreaControls(Composite parent) {
//        Label lblInfo = new Label(parent, SWT.NONE);
//        lblInfo.setText("※ 이 뷰는 XML 형식과 간단한 의존성을 검사합니다.\n"
//                + "1) 위에서 파일을 선택하고\n"
//                + "2) Check 버튼을 눌러보세요.");
//    }
    
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

    private void clearErrors() {
        if (errorTable != null && !errorTable.isDisposed()) {
            errorTable.removeAll();
        }
    }
    
    private void runCheck() {
        clearErrors();

        int idx = comboRecentFiles.getSelectionIndex();
        if (idx < 0) {
            addError("ERROR", "파일이 선택되지 않았습니다.", "-");
            lblStatus.setText("경고: 파일을 선택하세요.");
            return;
        }

        String path = comboRecentFiles.getItem(idx);

        if (path.isEmpty()) {
            addError("ERROR", "파일 경로가 비어 있습니다.", "-");
            lblStatus.setText("경고: 파일 경로를 입력하세요.");
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            addError("ERROR", "파일이 존재하지 않습니다.", path);
            lblStatus.setText("에러: 파일이 없습니다.");
            return;
        }

        try {
            // XML을 파싱하면서 문법을 함께 검사
            Document doc = parseXml(file);

            // 3단계: 의존성/메타데이터 검사
            checkDependencies(doc);

            lblStatus.setText("성공: XML 형식 및 의존성 검사 통과.");
        } catch (Exception e) {
            addError("ERROR", e.getMessage(), path);
            lblStatus.setText("에러: " + e.getMessage());
        }
    }

    private void addError(String type, String message, String location) {
        if (errorTable == null || errorTable.isDisposed()) {
            return;
        }
        TableItem item = new TableItem(errorTable, SWT.NONE);
        item.setText(0, type != null ? type : "");
        item.setText(1, message != null ? message : "");
        item.setText(2, location != null ? location : "");
    }
    
    private Document parseXml(File file) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // XML 문법 에러를 잡기 위한 ErrorHandler
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override
                public void error(org.xml.sax.SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void fatalError(org.xml.sax.SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void warning(org.xml.sax.SAXParseException e) throws SAXException {
                    // 원하면 경고도 에러로 처리 가능
                    // throw e;
                }
            });

            return builder.parse(file);
        } catch (SAXException e) {
            throw new Exception("XML 문법 오류: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("파일 읽기 오류: " + e.getMessage(), e);
        }
    }
    
    private void checkDependencies(Document doc) {
        NodeList items = doc.getElementsByTagName("item");

        // 1) 모든 item의 id 모으기
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            String id = el.getAttribute("id");
            if (id == null || id.isEmpty()) {
                addError("ERROR", "<item> 요소에 id 속성이 없습니다.", getElementLocation(el));
            } else {
                ids.add(id);
            }
        }

        // 2) ref가 있으면 ids 안에 존재하는지 확인
        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            String ref = el.getAttribute("ref");
            if (ref != null && !ref.isEmpty()) {
                if (!ids.contains(ref)) {
                    addError("ERROR",
                            "ref=\"" + ref + "\"에 해당하는 id를 가진 <item>이 없습니다.",
                            getElementLocation(el));
                }
            }
        }
    }

    private String getElementLocation(Element el) {
        String tag = el.getTagName();
        String id = el.getAttribute("id");
        if (id != null && !id.isEmpty()) {
            return "<" + tag + " id=\"" + id + "\">";
        }
        return "<" + tag + ">";
    }
}
