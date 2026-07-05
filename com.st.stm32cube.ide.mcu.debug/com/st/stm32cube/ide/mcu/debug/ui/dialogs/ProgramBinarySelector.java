package com.st.stm32cube.ide.mcu.debug.ui.dialogs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public class ProgramBinarySelector extends TwoPaneElementSelector {
   private boolean reset = false;

   public ProgramBinarySelector(Shell parent, ILabelProvider elementRenderer, ILabelProvider qualifierRenderer) {
      super(parent, elementRenderer, qualifierRenderer);
   }

   public Control createDialogArea(Composite parent) {
      Composite contents = (Composite)super.createDialogArea(parent);
      this.createCheckbox(contents, "Reset after program");
      return contents;
   }

   public Button createCheckbox(Composite parent, String name) {
      final Button checkReset = new Button(parent, 32);
      checkReset.setText(name);
      checkReset.addSelectionListener(new SelectionListener() {
         public void widgetSelected(SelectionEvent e) {
            ProgramBinarySelector.this.reset = checkReset.getSelection();
         }

         public void widgetDefaultSelected(SelectionEvent e) {
         }
      });
      return checkReset;
   }

   public boolean hasToReset() {
      return this.reset;
   }
}
