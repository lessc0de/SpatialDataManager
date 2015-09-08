/*******************************************************************************
 * Copyright 2015 hangum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.gaia3d.tadpole.spatial.data.core.spaitaldb.SpatiaDBFactory;
import com.gaia3d.tadpole.spatial.data.core.spaitaldb.db.SpatialDB;
import com.gaia3d.tadpole.spatial.data.core.ui.wizard.shapeimport.ShapeFileImportWizard;
import com.hangum.tadpole.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.engine.query.dao.system.UserDBDAO;
import com.hangum.tadpole.rdb.core.viewers.object.ExplorerViewer;

/**
 * Import shape file actionss
 * 
 * @author hangum
 *
 */
public class ImportShapeFileActions implements IViewActionDelegate {
	private static final Logger logger = Logger.getLogger(ImportShapeFileActions.class);
	protected IStructuredSelection sel;
	
	public ImportShapeFileActions() {
		super();
	}

	@Override
	public void run(IAction action) {
		final UserDBDAO userDB = (UserDBDAO)sel.getFirstElement();
		
		SpatiaDBFactory factory = new SpatiaDBFactory();
		SpatialDB spatialDB = factory.getSpatialDB(userDB);
		if(spatialDB == null || spatialDB.isSpatialDBImage() == null) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Shape file import", "This DB does not Spatial DB. so cat't import Shape file.");
			return;
		}
		
		// display import dialog
		ShapeFileImportWizard wizard = new ShapeFileImportWizard(userDB);
		WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard) {
			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setSize(500, 600);
				
				Display display = newShell.getDisplay();
				int x = (display.getBounds().width - newShell.getSize().x)/2;
				int y = (display.getBounds().height - newShell.getSize().y)/2;
				newShell.setLocation(x, y);
			}
		};

		if(WizardDialog.OK == wizardDialog.open()) {
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						ExplorerViewer ev = (ExplorerViewer)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ExplorerViewer.ID);
						ev.refreshCurrentTab(userDB, PublicTadpoleDefine.QUERY_DDL_TYPE.TABLE);
					} catch (PartInitException e) {
						logger.error("find explorer view", e);
					}
				}
			});	// end display
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		sel = (IStructuredSelection)selection;
	}

	@Override
	public void init(IViewPart view) {
	}


}