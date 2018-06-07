package mediaplayer;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;


/**
 * @author Wayne Poon
 *
 */
public class Main extends Application {
	
	
	@Override
	public void start(Stage primaryStage) {
		
		//　FXMLファイルからシーンを取得するのローダーです。
		FXMLLoader loader = new FXMLLoader();
		
		try {
			// FXMLファイルのURLを取得、これをロードする
			loader.setLocation(getClass().getResource("MediaPlayer.fxml"));
			Parent root = loader.load();
			
			// シーンを作成
			Scene scene = new Scene(root);
			
			// CSSを取得
			scene.getStylesheets().add(getClass().getResource("MediaPlayer.css").toExternalForm());
			
			//　ステージを設定する。
			primaryStage.setTitle("My Video Player");
			primaryStage.setScene(scene);
			
			// プレイヤー動作している間、リサイズする時、Menuからステージの大きさを変更可能にする。
			primaryStage.setResizable(true);
			
			// ステージを表示
			primaryStage.show();
			
			// ステージオブジェクトをコントローラに引き渡し
			MediaPlayerController.setStage(primaryStage);
			
			// 誤操作を防止するため、×ボタンをクリックする際に警告メッセージを表示
			primaryStage.setOnCloseRequest(e->{
				e.consume();
				
				Alert alert = new Alert(AlertType.CONFIRMATION, "プレイヤーを閉じますか?", ButtonType.YES, ButtonType.NO);
				alert.showAndWait();
				
				if(alert.getResult() == ButtonType.YES) {
					primaryStage.close();
				}
			});
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	

}
