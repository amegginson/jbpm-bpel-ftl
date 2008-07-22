package org.jbpm.gd.jpdl.properties;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.jbpm.gd.jpdl.model.EsbNode;

public class EsbGeneralConfigurationComposite implements FocusListener {
	
	public static EsbGeneralConfigurationComposite create(TabbedPropertySheetWidgetFactory widgetFactory, Composite parent) {
		EsbGeneralConfigurationComposite result = new EsbGeneralConfigurationComposite();
		result.widgetFactory = widgetFactory;
		result.parent = parent;
		result.create();
		return result;
	}
	
	private TabbedPropertySheetWidgetFactory widgetFactory;
	private Composite parent;
	
	private Label serviceNameLabel;
	private Text serviceNameText;
	private Label categoryNameLabel;
	private Text categoryNameText;
	
	private EsbNode esbNode;
	
	private EsbGeneralConfigurationComposite() {}
	
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
	
	private void hookListeners() {
		serviceNameText.addFocusListener(this);
		categoryNameText.addFocusListener(this);
	}
	
	private void unhookListeners() {
		serviceNameText.removeFocusListener(this);
		categoryNameText.removeFocusListener(this);
	}
	
	private void clearControls() {
		serviceNameText.setText("");
		categoryNameText.setText("");
	}
	
	private void updateControls() {
		serviceNameText.setText(esbNode.getServiceName() == null ? "" : esbNode.getServiceName());
		categoryNameText.setText(esbNode.getCategoryName() == null ? "" : esbNode.getCategoryName());
	}
	
	private void create() {
		serviceNameLabel = widgetFactory.createLabel(parent, "Service");
        serviceNameText = widgetFactory.createText(parent, "");
        categoryNameLabel = widgetFactory.createLabel(parent, "Category");
        categoryNameText = widgetFactory.createText(parent, "");
        serviceNameLabel.setLayoutData(createServiceNameLabelLayoutData());
        serviceNameText.setLayoutData(createServiceNameTextLayoutData());
        categoryNameLabel.setLayoutData(createCategoryNameLabelLayoutData());
        categoryNameText.setLayoutData(createCategoryNameTextLayoutData());
	}
	
	private FormData createServiceNameTextLayoutData() {
		FormData data = new FormData();
		data.left = new FormAttachment(categoryNameLabel, 5);
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		return data;
	}
	
	private FormData createCategoryNameTextLayoutData() {
		FormData data = new FormData();
		data.left = new FormAttachment(categoryNameLabel, 5);
		data.top = new FormAttachment(serviceNameText, 0);
		data.right = new FormAttachment(100, 0);
		return data;
	}
	
	private FormData createServiceNameLabelLayoutData() {
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 5);
		return data;
	}
	
	private FormData createCategoryNameLabelLayoutData() {
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(serviceNameText, 5);
		return data;
	}
	
	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		if (e.widget == serviceNameText) {
			updateServiceName();
		} else if (e.widget == categoryNameText) {
			updateCategoryName();
		}
	}
	
	private void updateServiceName() {
		String name = serviceNameText.getText();
		if ("".equals(name)) {
			name = null;
		}
		esbNode.setServiceName(name);
	}
	
	private void updateCategoryName() {
		String name = categoryNameText.getText();
		if ("".equals(name)) {
			name = null;
		}
		esbNode.setCategoryName(name);
	}

}
