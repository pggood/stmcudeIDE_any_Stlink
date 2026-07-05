package com.st.stm32cube.ide.mcu.debug.ui.views;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public class CubeIdeDebugColumnLabelProvider extends ColumnLabelProvider {
   public Point getToolTipShift(Object object) {
      return new Point(15, 5);
   }

   public int getToolTipDisplayDelayTime(Object object) {
      return 100;
   }

   public int getToolTipTimeDisplayed(Object object) {
      return 5000;
   }

   public Color getBackground(Object element) {
      Color color = null;
      if (element instanceof AbstractModelObject) {
         AbstractModelObject modelObj = (AbstractModelObject)element;
         if (modelObj.isInitialized() && modelObj.isDirty()) {
            color = DebugUIPlugin.getPreferenceColor("org.eclipse.debug.ui.PREF_CHANGED_VALUE_BACKGROUND");
         }
      }

      return color;
   }
}
