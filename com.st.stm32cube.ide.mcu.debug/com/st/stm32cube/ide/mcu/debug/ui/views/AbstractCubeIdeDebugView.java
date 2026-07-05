package com.st.stm32cube.ide.mcu.debug.ui.views;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public abstract class AbstractCubeIdeDebugView extends ViewPart implements IDebugContextManagerListener {
   private DebugContextManager fContextManager = new DebugContextManager(this);
   private Job fUpdateJob;
   private ILock lock = Job.getJobManager().newLock();

   public void createPartControl(Composite parent) {
      this.fContextManager.start();
   }

   public void setFocus() {
   }

   public DebugContextManager getContextManager() {
      return this.fContextManager;
   }

   public DebugContextManager.Debugger getDebugger() {
      return this.fContextManager.getDebugger();
   }

   public void handleSession(String sessionIdentifier) {
      if (sessionIdentifier != null) {
         this.handleDebugContext();
         if (this.targetSuspended()) {
            this.scheduleDataModelUpdate();
         }
      } else {
         this.handleUnsupportedContext();
      }

   }

   public void handleSuspend() {
      this.scheduleDataModelUpdate();
   }

   public void handleResume() {
   }

   public void handleExited() {
   }

   protected void handleUnsupportedContext() {
   }

   protected void handleDebugContext() {
   }

   protected void updateData() {
   }

   protected void refreshGUI() {
   }

   private void scheduleDataModelUpdate() {
      if (this.fUpdateJob != null) {
         this.fUpdateJob.cancel();
      }

      this.fUpdateJob = new AbstractCubeIdeDebugView.UpdateJob(Messages.AbstractCubeIdeDebugView_debugger_update_job);
      this.fUpdateJob.setSystem(true);
      this.fUpdateJob.setPriority(10);
      this.getProgressService().schedule(this.fUpdateJob);
   }

   protected boolean targetSuspended() {
      return this.getContextManager().targetSuspended();
   }

   private IWorkbenchSiteProgressService getProgressService() {
      Object siteService = this.getSite().getAdapter(IWorkbenchSiteProgressService.class);
      return siteService != null ? (IWorkbenchSiteProgressService)siteService : null;
   }

   public void dispose() {
      this.fContextManager.dispose();
      super.dispose();
   }

   public DsfExecutor getExecutor() {
      return this.getContextManager().getExecutor();
   }

   public class UpdateJob extends Job {
      boolean isCancelled = false;

      public UpdateJob(String name) {
         super(name);
      }

      protected IStatus run(IProgressMonitor monitor) {
         try {
            AbstractCubeIdeDebugView.this.lock.acquire();
            if (this.isCancelled) {
               AbstractCubeIdeDebugView.this.lock.release();
               IStatus var3 = Status.OK_STATUS;
               return var3;
            }

            AbstractCubeIdeDebugView.this.updateData();
            Display.getDefault().asyncExec(new Runnable() {
               public void run() {
                  AbstractCubeIdeDebugView.this.refreshGUI();
               }
            });
         } finally {
            AbstractCubeIdeDebugView.this.lock.release();
         }

         return Status.OK_STATUS;
      }

      protected void canceling() {
         this.isCancelled = true;
      }
   }
}
