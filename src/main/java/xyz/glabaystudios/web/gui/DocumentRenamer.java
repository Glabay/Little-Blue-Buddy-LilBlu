package xyz.glabaystudios.web.gui;

import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import xyz.glabaystudios.net.NetworkExceptionHandler;
import xyz.glabaystudios.web.Controllers;

import java.io.File;
import java.util.List;

public class DocumentRenamer {

	public Button imgBtn;
	public Button wordBtn;
	public Button pdfBtn;
	public Button cxlBtn;

	private List<File> openDocumentChooser(String fileTypeDesc, String... extensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Document");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(fileTypeDesc, extensions));
		return fileChooser.showOpenMultipleDialog(Controllers.getDocumentRenamingWindow().getScene().getWindow());
	}

	public void openImageRenamer() {
		List<File> images = openDocumentChooser("Image Files", "*.png", "*.jpg", "*.jpeg");
		String prefix = requestPrefix("gallery-image-");
		renameFiles(images, prefix);
	}

	public void openWordDocRenamer() {
		List<File> docs = openDocumentChooser("Document Files", "*.docx", "*.doc", "*.dotx");
		String prefix = requestPrefix("attached-document-");
		renameFiles(docs, prefix);
	}

	public void openPdfRenamer() {
		List<File> pdfs = openDocumentChooser("PDF Files", "*.pdf");
		String prefix = requestPrefix("attached-pdf-");
		renameFiles(pdfs, prefix);
	}

	private String requestPrefix(String base) {
		TextInputDialog inputPrefix = new TextInputDialog(base);
		inputPrefix.setContentText("Prefix:");
		inputPrefix.showAndWait();
		String prefix = inputPrefix.getEditor().getText();
		return "/" + prefix + (prefix.endsWith("-") ? "" : "-");
	}

	private void renameFiles(List<File> documents, String prefixForNewDoc) {
		if (documents != null) {
			for (int index = 0; index < documents.size(); index++) {
				File image = documents.get(index);
				String filepath = image.getParentFile().toString();
				String[] splitName = image.getName().split("\\.");
				String fileType = splitName[splitName.length-1];
				File newImg = new File(filepath + prefixForNewDoc + index + "." + fileType);
				if (!image.renameTo(newImg)) NetworkExceptionHandler.handleException("renameFiles", null);
			}
		}

	}

	public void appendWindowClose() {
		Controllers.getDocumentRenamingWindow().getScene().getWindow().hide();
	}
}
