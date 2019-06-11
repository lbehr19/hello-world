package rich_text_experiment;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class RunWindow extends Application {

	private CodeArea _input = new CodeArea("RISC-V input");
	private CodeArea _output = new CodeArea("Machine Code output");
	private CodeArea _iof = new CodeArea("Error logs and input/output go here!");
	
	public static void main(String[] args) {
		launch();
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		Stage stage = new Stage();
		BorderPane window = new BorderPane();
		
		VirtualizedScrollPane<CodeArea> inPane = new VirtualizedScrollPane<>(_input);
		_input.setParagraphGraphicFactory(LineNumberFactory.get(_input));
		
		VirtualizedScrollPane<CodeArea> outPane = new VirtualizedScrollPane<CodeArea>(_output);
		_output.setParagraphGraphicFactory(LineNumberFactory.get(_output));
		outPane.setPrefWidth(400);
		
		VirtualizedScrollPane<CodeArea> ioPane = new VirtualizedScrollPane<>(_iof);
		
		window.setCenter(inPane);
		window.setRight(outPane);
		window.setBottom(ioPane);
		
		Scene scene = new Scene(window, 800, 450);
		scene.getStylesheets().add(RunWindow.class.getResource("/resources/run-window.css").toExternalForm());
		
		stage.setTitle("CodeArea experimentation");
		stage.setScene(scene);
		
		stage.show();
	}
	
}
