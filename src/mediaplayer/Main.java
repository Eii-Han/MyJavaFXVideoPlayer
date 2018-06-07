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
		
		//�@FXML�t�@�C������V�[�����擾����̃��[�_�[�ł��B
		FXMLLoader loader = new FXMLLoader();
		
		try {
			// FXML�t�@�C����URL���擾�A��������[�h����
			loader.setLocation(getClass().getResource("MediaPlayer.fxml"));
			Parent root = loader.load();
			
			// �V�[�����쐬
			Scene scene = new Scene(root);
			
			// CSS���擾
			scene.getStylesheets().add(getClass().getResource("MediaPlayer.css").toExternalForm());
			
			//�@�X�e�[�W��ݒ肷��B
			primaryStage.setTitle("My Video Player");
			primaryStage.setScene(scene);
			
			// �v���C���[���삵�Ă���ԁA���T�C�Y���鎞�AMenu����X�e�[�W�̑傫����ύX�\�ɂ���B
			primaryStage.setResizable(true);
			
			// �X�e�[�W��\��
			primaryStage.show();
			
			// �X�e�[�W�I�u�W�F�N�g���R���g���[���Ɉ����n��
			MediaPlayerController.setStage(primaryStage);
			
			// �둀���h�~���邽�߁A�~�{�^�����N���b�N����ۂɌx�����b�Z�[�W��\��
			primaryStage.setOnCloseRequest(e->{
				e.consume();
				
				Alert alert = new Alert(AlertType.CONFIRMATION, "�v���C���[����܂���?", ButtonType.YES, ButtonType.NO);
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
