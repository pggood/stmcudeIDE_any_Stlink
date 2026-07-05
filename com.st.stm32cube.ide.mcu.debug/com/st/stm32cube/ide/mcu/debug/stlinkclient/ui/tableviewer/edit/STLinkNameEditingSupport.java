package com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.edit;

import com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.model.STLinkModel;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;

public class STLinkNameEditingSupport extends EditingSupport {
   private final CheckboxTableViewer fViewer;
   private final CellEditor fEditor;

   public STLinkNameEditingSupport(CheckboxTableViewer viewer) {
      super(viewer);
      this.fViewer = viewer;
      this.fEditor = new TextCellEditor(viewer.getTable());
   }

   protected CellEditor getCellEditor(Object element) {
      return this.fEditor;
   }

   protected boolean canEdit(Object element) {
      return !(element instanceof STLinkModel) || ((STLinkModel)element).isBlinkable();
   }

   protected Object getValue(Object element) {
      return ((STLinkModel)element).getName();
   }

   protected void setValue(Object element, Object value) {
      String newValue = String.valueOf(value);
      if (newValue.isEmpty()) {
         newValue = ((STLinkModel)element).getID();
      }

      ((STLinkModel)element).setName(newValue);
      this.fViewer.update(element, (String[])null);
   }
}
