package com.congxiaoyao.cmd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件拖拽监听，可与CommandWindow绑定，将会监听拖入CommandWindow的文件
 * 并将文件通过{@code onFileDrop}函数通知外界
 * 
 * @version 1.0
 * @author congxiaoyao
 * @date 2016.1.24
 */
public class FileDropHelper {
	
	public FileDropHelper(Component c) {
		
		new DropTarget(c, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					dtde.getTransferable();
					if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
						@SuppressWarnings("unchecked")
						List<File> list = (List<File>) (dtde.getTransferable()
								.getTransferData(DataFlavor.javaFileListFlavor));
						//只支持一次拖入一个文件
						File file = (File) list.get(0);
						onFileDrop(file);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
				}
			}
		});
	}
	
	public void onFileDrop(File file){}
}