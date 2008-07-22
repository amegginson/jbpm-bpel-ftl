package org.jbpm.gd.jpdl.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.jbpm.gd.common.model.GenericElement;
import org.jbpm.gd.jpdl.model.EsbNode;

public class EsbConfigurationComposite implements SelectionListener {
	
	public static EsbConfigurationComposite create(TabbedPropertySheetWidgetFactory widgetFactory, Composite parent) {
		EsbConfigurationComposite result = new EsbConfigurationComposite();
		result.widgetFactory = widgetFactory;
		result.parent = parent;
		result.create();
		return result;
	}
	
	private TabbedPropertySheetWidgetFactory widgetFactory;
	private Composite parent;
	private EsbNode esbNode;
	private GenericElement[] savedEsbToJbpmMappings;
		
	private Button oneWayButton;
	private CTabFolder actionTabFolder;
	private CTabItem outputTabItem;
	private Composite outputTabControl;
	private EsbGeneralConfigurationComposite esbGeneralConfigurationComposite;
	private EsbInputOutputConfigurationComposite esbInputConfigurationComposite;
	private EsbInputOutputConfigurationComposite esbOutputConfigurationComposite;
	
	
	public void setEsbNode(EsbNode esbNode) {
		if (this.esbNode == esbNode) return;
		unhookListeners();
		clearControls();
		this.esbNode = esbNode;
		if (esbNode != null) {
			updateControls();
			hookListeners();
		}
	}
	
	public EsbNode getEsbNode() {
		return esbNode;
	}
	
	private void unhookListeners() {
		oneWayButton.removeSelectionListener(this);
	}
	
	private void hookListeners() {
		oneWayButton.addSelectionListener(this);
	}
	
	private void clearControls() {
		oneWayButton.setSelection(false);
		esbGeneralConfigurationComposite.setEsbNode(null);
		esbInputConfigurationComposite.setEsbNode(null);
		if (outputTabItem != null) {
			esbOutputConfigurationComposite.setEsbNode(null);
			outputTabItem.dispose();
		}
	}
	
	private void updateControls() {
		oneWayButton.setSelection(esbNode.isOneWay());
		esbGeneralConfigurationComposite.setEsbNode(esbNode);
		esbInputConfigurationComposite.setEsbNode(esbNode);
		if (!esbNode.isOneWay()) {
			createOutputTabItem();
			esbOutputConfigurationComposite.setEsbNode(esbNode);
		}
	}
	
	private void create() {
		oneWayButton = widgetFactory.createButton(parent, "One Way", SWT.CHECK);
		oneWayButton.setLayoutData(createOneWayButtonLayoutData());
		actionTabFolder = widgetFactory.createTabFolder(parent, SWT.TOP | SWT.BORDER);
		actionTabFolder.setLayoutData(createEsbNodeTabFolderLayoutData());
		createGeneralTabItem();
		createInputTabItem();
		actionTabFolder.setSelection(0);
	}
	
	private void createGeneralTabItem() {
		CTabItem generalTabItem = widgetFactory.createTabItem(actionTabFolder, SWT.NORMAL);
		generalTabItem.setText("General");		
		Composite generalTabControl = widgetFactory.createFlatFormComposite(actionTabFolder);
		esbGeneralConfigurationComposite = 
			EsbGeneralConfigurationComposite.create(widgetFactory, generalTabControl);
		generalTabItem.setControl(generalTabControl);
	}
	
	private void createInputTabItem() {
		CTabItem inputTabItem = widgetFactory.createTabItem(actionTabFolder, SWT.NORMAL);
		inputTabItem.setText("Input");
		Composite inputTabControl = widgetFactory.createFlatFormComposite(actionTabFolder);
		esbInputConfigurationComposite = 
			EsbInputOutputConfigurationComposite.create(
					widgetFactory, 
					inputTabControl, 
					EsbInputOutputConfigurationComposite.INPUT_CONFIGURATION);
		inputTabItem.setControl(inputTabControl);
	}
	
	private void createOutputTabItem() {
		outputTabItem = widgetFactory.createTabItem(actionTabFolder, SWT.NORMAL);
		outputTabItem.setText("Output");
		if (outputTabControl == null) {
			outputTabControl = widgetFactory.createFlatFormComposite(actionTabFolder);
			esbOutputConfigurationComposite = 
				EsbInputOutputConfigurationComposite.create(
						widgetFactory, 
						outputTabControl,
						EsbInputOutputConfigurationComposite.OUTPUT_CONFIGURATION);
		}
		outputTabItem.setControl(outputTabControl);		
	}
	
	private FormData createOneWayButtonLayoutData() {
		FormData result = new FormData();
		result.left = new FormAttachment(0, 0);
		result.right = new FormAttachment(100, 0);
		result.top = new FormAttachment(0, 0);
		return result;
	}
	
	private FormData createEsbNodeTabFolderLayoutData() {
		FormData result = new FormData();
		result.left = new FormAttachment(0, 0);
		result.right = new FormAttachment(100, 0);
		result.top = new FormAttachment(oneWayButton, 5);
		result.bottom = new FormAttachment(100, 0);
		return result;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == oneWayButton) {
			handleOneWayButtonSelected();
		}
	}
	
	private void handleOneWayButtonSelected() {
		if (oneWayButton.getSelection()) {
			esbOutputConfigurationComposite.setEsbNode(null);
			outputTabItem.dispose();
			saveEsbToJbpmMappings();
		} else {
			createOutputTabItem();
			restoreEsbToJbpmMappings();
			esbOutputConfigurationComposite.setEsbNode(esbNode);
		}
		esbNode.setOneWay(oneWayButton.getSelection());
	}
	
	private void restoreEsbToJbpmMappings() {
		if (savedEsbToJbpmMappings == null) return;
		for (int i = 0; i < savedEsbToJbpmMappings.length; i++) {
			esbNode.addEsbToJbpmMapping(savedEsbToJbpmMappings[i]);
		}
	}

	private void saveEsbToJbpmMappings() {
		GenericElement[] esbToJbpmMappings = esbNode.getEsbToJbpmMappings();
		savedEsbToJbpmMappings = new GenericElement[esbToJbpmMappings.length];	
		for (int i = 0; i < esbToJbpmMappings.length; i++) {
			savedEsbToJbpmMappings[i] = esbToJbpmMappings[i];
			esbNode.removeEsbToJbpmMapping(esbToJbpmMappings[i]);
		}
	}
	
}
