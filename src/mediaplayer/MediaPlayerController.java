package mediaplayer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import fullscreen.FullScreenController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @author Wayne Poon
 *
 */
public class MediaPlayerController implements Initializable {
	
	// ����p�l���̃X���C�_
	@FXML private Slider timeSlider;
	@FXML private Slider volumeSlider;
	@FXML private Label timeInfoLabel;
	@FXML private Label volumeInfoLabel;
	
	// ����p�l���̃{�^���F�Đ��A�ꎞ��~�A��~�A�J�Ԃ�
	@FXML private Button newMediaButton;
	@FXML private Button playButton;
	@FXML private Button pauseButton;
	@FXML private Button stopButton;
	@FXML private ToggleButton repeatButton;
	
	//�@Menu��̃A�C�e���F�Đ��A�ꎞ��~�A��~�A�J�Ԃ�
	@FXML private MenuItem playMenuItem;
	@FXML private MenuItem pauseMenuItem;
	@FXML private MenuItem stopMenuItem;
	@FXML private MenuItem repeatMenuItem;
	
	//�@�É�
	@FXML private MenuItem muteMenuItem;
	
	//�@�X�^�[�g���j���[�̃A�C�e��
	@FXML private MenuItem newMediaMenuItem;
	@FXML private MenuItem closeMenuItem;
	
	// BorderPane
	@FXML private BorderPane innerBorderPane;
	@FXML private BorderPane outerBorderPane;
	
	//�g�b�v�̔z�u�ƃ{�g���̔z�u
	@FXML private VBox topmostVBox;
	@FXML private HBox bottomHBox;
	
	// �f����\�����郁�f�B�A�r���[
	@FXML private MediaView mediaView;
	
	//�@�Đ����̃��f�B�A�̃v���C���[
	private MediaPlayer mediaPlayer;
	
	// �p�\�R���̃T�C�Y���L������I�u�W�F�N�g
	private Dimension screenSize;
	
	// �v���C���[��\������X�e�[�W
	private static Stage stage;
	
	// �v���C���[�����鎞�ƂȂ����̓���𐧌䂷�邽�߂ɒ�`���ꂽBoolean
	private boolean haveMediaPlayer = false;
	
	// �_�u���N���b�N���L�����ǂ����𔻒肷�鎞�Ɏg���t�B�[���h�ł��B
	private long firstClickTime = 0;
	private long secondClickTime = 0;

	/* 
	 * �v���C���[��������
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//�@�p�\�R���̉�ʃT�C�Y���擾
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// �����l�Ƃ��ĉ�ʂ̔�����ݒ肷��
		outerBorderPane.setPrefSize(screenSize.getWidth() * 0.5 , screenSize.getHeight() * 0.5 );
		
		// ���f�B�A�r���[��BorderPane�̃o�C���f�B���O��ݒ肷��
		innerBorderPane.prefWidthProperty().bind(mediaView.fitWidthProperty());
		innerBorderPane.prefHeightProperty().bind(mediaView.fitHeightProperty());

		mediaView.fitWidthProperty().bind(outerBorderPane.widthProperty().multiply(1).subtract(20));
		mediaView.fitHeightProperty().bind(outerBorderPane.heightProperty().multiply(1).
				subtract(topmostVBox.getHeight() + bottomHBox.getHeight()+70));   // �g�b�v�ƃ{�g���ɔz�u������̂̃I�t�Z�b�g��ݒ肷��
		
		// ���f�B�A���܂����[�h����Ă��Ȃ���Ԃő���ł���͂��̂Ȃ����̂𖳌�������
		setItemsDisable();
	}
	
	/**
	 * �Đ��{�^���ƍĐ����j���[�̓�����`
	 */
	@FXML 
	private void playMediaEvent() {
		mediaPlayer.play();
	}
	
	/**
	 * �ꎞ��~�{�^���ƈꎞ��~���j���[�̓�����`
	 */
	@FXML 
	private void pauseMediaEvent() {
		mediaPlayer.pause();
	}

	/**
	 * ��~�{�^���ƒ�~���j���[�̓�����`
	 */
	@FXML
	private void stopMediaEvent() {
		mediaPlayer.stop();
	}
	
	/**
	 * �V�K���f�B�A�{�^���E���j���[���N���b�N����鎞�̓�����`
	 */
	@FXML
	private void newMediaLoadingEvent() {
		
		// �܂��A���f�B�A�t�@�C�������[�h���Ă݂�B���튮������΁A�v���C���[��Ԃ��B
		Optional<MediaPlayer> optionalPlayer = Optional.ofNullable(returnNewPlayer());
		
		// �v���C���[�����݂���ꍇ�̂݁A���L�̃R�[�h�����s����
		if(optionalPlayer.isPresent()) {
			
			// �v���C���[���Q�b�g����
			mediaPlayer = optionalPlayer.get();
			
			// ���f�B�A�r���[�ɐݒ�
			mediaView.setMediaPlayer(mediaPlayer);

			// �����l�Ƃ��Ė��������ꂽ���ׂĂ̂��̂�L���ɂ���
			setItemsEnable();
			
			// �Đ��I���������̓����ݒ�
			mediaPlayer.setOnEndOfMedia(this::repeatMediaEvent);
			
			// �Đ����ԃX���C�_�[�̏����l��ݒ�
			timeSlider.setMin(0);
			timeSlider.setMax(1);

			// Ready��Ԃł̃X���C�_�[�v���p�e�B��ݒ�
			setTimeSliderEventOnReady();
			
			//�@�{�����[���X���C�_�[�̏����l��ݒ�
			volumeSlider.setMin(0);
			volumeSlider.setMax(1);

			
			// Ready��Ԃł̃X���C�_�[�v���p�e�B��ݒ�
			setVolumeSliderEventOnReady();
			
			// ���f�B�A�̃��[�h�������ł��邽�߁A�^�ɐݒ肷��B
			haveMediaPlayer = true;
		}
	}
	
	/**
	 * �A���Đ��ƃ��j���[�̓����ݒ�
	 */
	@FXML
	private void repeatMenuPressed() {
		// �A���Đ��{�^���̏�Ԃ�ݒ�
		if(repeatButton.isSelected()) {
			repeatButton.setSelected(false);
		}else {
			repeatButton.setSelected(true);
		}
	}
	
	/**
	 * ���ԃX���C�_�[�ړ����̃C�x���g���`
	 */
	@FXML
	private void setEventOnTimeSlider() {
		// �X���C�_�[�𑀍삷��ƃV�[�N����
		mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
	}
	
	/**
	 * �N���[�Y���j���[�̓�����`����B
	 */
	@FXML
	private void closeMenuItemClicked() {
		// �ŏ��̃X�e�[�W���N���[�Y
		stage.close();
	}
	
	/**
	 * ����폜���j���̓����ݒ�
	 */
	@FXML
	private void removeMediaClicked() {

		// ���݃v���C���[�̃X�e�[�^�X���擾
		switch(mediaPlayer.getStatus()) {
		// �Đ�����Ԃ̏ꍇ�͓���폜���ł��Ȃ��悤�ɂ���
		case PLAYING:
			Alert alert1 = new Alert(AlertType.ERROR, "�Đ����̓���͍폜�ł��܂���");
			mediaPlayer.pause();
			alert1.showAndWait();
			mediaPlayer.play();
			break;
		//�@�����������悪���[�h����Ă��Ȃ��ꍇ
		case UNKNOWN:
			Alert alert2 = new Alert(AlertType.INFORMATION, "����͂���܂���B");
			alert2.show();
			break;
		// ���̑��̏�Ԃ͍폜������i�߂�
		default:
			removeMedia();
			break;
		}

	}
	
	/**
	 * �S��ʕ\�����[�h
	 */
	@FXML
	private void fullScreen(Event event) {
		
		//�@�v���C���[�ɓ��悪���邩�ǂ������m�F����
		if(haveMediaPlayer) {
			// ���f�B�A�r���[�ł�2��N���b�N�A���j���[�o�[�ł̓A�C�e����I��
			if(checkMouseClicked() | selectedfromMenu(event)) {
				
				// FXML�t�@�C������V�[�����擾
				FXMLLoader loader = new FXMLLoader();
				try {
					Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
					loader.setLocation(fullscreen.FullScreenController.class.getResource("FullScreen.fxml"));
					Parent root = loader.load();
					
					//�@�V�[���̃T�C�Y��[���̉�ʂ̑傫���ɐݒ�
					Scene scene = new Scene(root,screen.getWidth(),screen.getHeight());

					// �S��ʂ̃R���g���[���[���擾
					FullScreenController controller = loader.getController();

					// �X�e�[�W�X�^�C���͂Ȃ�̑������Ȃ����̂ɐݒ肷��
					Stage newsStage = new Stage(StageStyle.UNDECORATED);
					newsStage.setScene(scene);
					
					// �S��ʂ�On�ɂ���
					newsStage.setFullScreen(true);
					newsStage.initModality(Modality.APPLICATION_MODAL);
					
					// �S��ʂ�\������Ɠ����ɁA���݂̉�ʂ��B���
					newsStage.show();
					stage.hide();

					// �S��ʂ�ESC�L�[������������A�v���C���[��ʂɖ߂�
					scene.setOnKeyPressed(e->{
						if(e.getCode()==KeyCode.ESCAPE) {
							newsStage.close();
							stage.show();
						}
					});
					
					// ���݂̃X�e�[�W�ƃ��f�B�A�v���C���[��S��ʂ̃R���g���[���[�Ɉ����n��
					controller.init(stage , mediaPlayer);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * �P/�S���
	 */
	@FXML
	private void oneQuarterScreenClicked(){
		
		// �o�C���f�B���O���Đݒ�B
		mediaView.fitWidthProperty().bind(new SimpleDoubleProperty(screenSize.getWidth()/4));
		mediaView.fitHeightProperty().bind(new SimpleDoubleProperty(screenSize.getHeight()/4));

		// BorderPane�̃T�C�Y���Đݒ�
		outerBorderPane.setPrefSize(screenSize.getWidth()/4 + 50,  //���̃T�C�Y�A50px�̓I�t�Z�b�g
				screenSize.getHeight() / 4 - (topmostVBox.getHeight() + bottomHBox.getHeight()+70)); //�c�̃T�C�Y�AVBox��HBox�̃I�t�Z�b�g����
		
		// ���T�C�Y
		stage.sizeToScene();
	}
	
	/**
	 * 1/2���
	 * �R�����g�͓���B
	 */
	@FXML
	private void halfScreenClicked() {
		
		mediaView.fitWidthProperty().bind(new SimpleDoubleProperty(screenSize.getWidth()*0.5));
		mediaView.fitHeightProperty().bind(new SimpleDoubleProperty(screenSize.getHeight()*0.5));

		outerBorderPane.setPrefSize(screenSize.getWidth()*0.5 + 50,  
				screenSize.getHeight() *0.75 - (topmostVBox.getHeight() + bottomHBox.getHeight()+70));
		
		stage.sizeToScene();
	}
	
	/**
	 * 3/4���
	 * �R�����g�͓���B
	 */
	@FXML
	private void threeQuarterScreenClicked() {
		
		mediaView.fitWidthProperty().bind(new SimpleDoubleProperty(screenSize.getWidth()*0.75));
		mediaView.fitHeightProperty().bind(new SimpleDoubleProperty(screenSize.getHeight()*0.75));

		outerBorderPane.setPrefSize(screenSize.getWidth()*0.75 + 50,  
				screenSize.getHeight() *0.75 - (topmostVBox.getHeight() + bottomHBox.getHeight()+70));
		
		stage.sizeToScene();
		
	}
	
	/**
	 * ��ʃT�C�Y�ύX�̃��j���[�̓�����`
	 */
	@FXML
	private void follwingBySizeOfStage() {
		//�@�o�C���f�B���O�����ɖ߂�
		mediaView.fitWidthProperty().bind(outerBorderPane.widthProperty().multiply(1).subtract(20));
		mediaView.fitHeightProperty().bind(outerBorderPane.heightProperty().multiply(1).subtract(topmostVBox.getHeight() + bottomHBox.getHeight()+70));
	}
	
	/**
	 * �É����j���[�̓�����`
	 */
	@FXML
	private void setMuteEvent() {
		
		//Mute��On�ł���ꍇ
		if(mediaPlayer.isMute()) {
			mediaPlayer.setMute(false);
			muteMenuItem.setText("Mute");
		}
		//Mute��Off�̏ꍇ
		else {
			mediaPlayer.setMute(true);
			muteMenuItem.setText("Undo Mute");
		}
	}
	
	
	
	
	
	/**
	 * �A���Đ��{�^���̓����ݒ�
	 */
	private void repeatMediaEvent() {
		
		// �A���Đ��{�^���̏�Ԃ��m�F
		if(repeatButton.isSelected()) {
			//�@���o���čĐ�
			mediaPlayer.seek(mediaPlayer.getStartTime());
			mediaPlayer.play();
		}else {
			// ���o���Ē�~
			mediaPlayer.seek(mediaPlayer.getStartTime());
			mediaPlayer.stop();
		}
		
	}
	
	/**
	 * ����폜�̎葱��
	 */
	private void removeMedia() {
		// ������~
		mediaPlayer.stop();
		
		// ���ڃ��f�B�A�v���C���[��Null�ɐݒ肵�Ă��܂��΁AChangeListener�Ƃ̋�����
		// �������A��O����������B �o�b�N�O���E���h���1�b���X���[�v���ANull�ɐݒ肷��΁A���̖��͉���ł���
		Task<Boolean> task = 
		new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				Thread.sleep(1000);
				mediaPlayer = null;
				return true;
			}
		};
		Thread thread = new Thread(task);
		thread.start();

		// ����ł��Ȃ��R���|�[�l���g�𖳌����ɂ���
		setItemsDisable();
		haveMediaPlayer = false;
	}

	/**
	 * ����ǂݍ��ݎ��ɌĂяo�����֐��ł�
	 * MediaPlayer��Ԃ��B�܂���Null��Ԃ��B
	 * @return
	 */
	private MediaPlayer returnNewPlayer() {
		
		//�@FileChooser�|�b�v�A�b�v�p�̃X�e�[�W
		Stage stage = new Stage();
		
		//	FileChooser�C���X�^���X���쐬
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open new Video");
		
		//�@�Ή��\�ȃt�@�C���g���q���`
		List<String> videoFileExtension = Arrays.asList("*.mp4","*.mpg","*.wmv");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Video File(\"*,mp4\", \"*.mpg\",\"*.wmv\")", videoFileExtension));
		
		//�@�X�^�[�g�f�B���N�g�����f�t�H���g�̃r�f�I�ɐݒ�
		String userDirectoryString = System.getProperty("user.home") + "//Videos";
		File userDirectory = new File(userDirectoryString);
		
		//�@�ݒ肪���s�������́A�ŏ��̃f�B���N�g���ɐݒ�
		if(!userDirectory.canRead()) {
			userDirectory = new File("C://");
		}
		
		//�@�f�B���N�g����ݒ�
		fileChooser.setInitialDirectory(userDirectory);
		
		//�@���f�B�A�t�@�C�����i�[����
		Optional<File> file = Optional.ofNullable(fileChooser.showOpenDialog(stage));
		
		// ���݂��邩�ǂ������m�F
		if(file.isPresent()) {
			Media media;
			
			// �K�i���x������Ă邩�ǂ������m�F
			try {
				media = new Media(file.get().toURI().toString());
			}catch(MediaException e) {
				// �K�i���x������Ă��Ȃ��ꍇ�͌x�����o�āANull��Ԃ�
				Alert alert = new Alert(AlertType.ERROR, "Unsupported Format");
				alert.show();
				return null;
			}
			MediaPlayer localPlayer = new MediaPlayer(media);
			return localPlayer;
		}
		else {
			// ����ȊO�̏ꍇ��Null��Ԃ��B
			return null;
		}
	}
	
	/**
	 * ���f�B�A��ǂݍ��񂪐��������ꍇ�݂̂ɌĂяo�����B
	 * �C�x���g�n���h���Ƃ��āA���ԕ\���X���C�_�[�ɓ���̏���񋟂���
	 */
	private void setTimeSliderEventOnReady() {
		
		// �Đ������������Ɋe������擾����֐���o�^
		Optional<Runnable> beforeFunc = Optional.ofNullable(mediaPlayer.getOnReady());
		Runnable readyFunc = ()->{
			
			//�@��ɓo�^���ꂽ�֐������s
			if(beforeFunc.isPresent()) {
				beforeFunc.get().run();
			}
			
			//�@�v���C���[�ɕێ�����郁�f�B�A�̏����擾���A�X���C�_�ɐݒ�
			timeSlider.setMin(mediaPlayer.getStartTime().toSeconds());
			timeSlider.setMax(mediaPlayer.getStopTime().toSeconds());
			timeSlider.setSnapToPixel(true);
		};
		mediaPlayer.setOnReady(readyFunc);
		
		// �Đ����ɃX���C�_�[���ړ�
		// �v���C���[�̍Đ����ɌĂяo����郊�X�i��o�^
		ChangeListener<? super Duration> playListener =(ov, newVal, currentVal)->{
			
			// ����̏������x���o��
			String timeInfoStr = String.format("%3.0f:%02.0f", mediaPlayer.getCurrentTime().toSeconds() / 60, 
					mediaPlayer.getCurrentTime().toSeconds() % 60) + "/" + String.format("%3.0f:%02.0f",
					mediaPlayer.getTotalDuration().toSeconds()/60, mediaPlayer.getTotalDuration().toSeconds()%60);
			timeInfoLabel.setText(timeInfoStr);
			
			// �X���C�_�[���ړ�
			timeSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
					
		};
		mediaPlayer.currentTimeProperty().addListener(playListener);
	}
	
	/**
	 * ���f�B�A��ǂݍ��񂪐��������ꍇ�݂̂ɌĂяo�����B
	 * �C�x���g�n���h���Ƃ��āA�{�����[���X���C�_�[�ɓ���̏���񋟂���
	 */
	private void setVolumeSliderEventOnReady() {
		
		// �Đ������������Ɋe������擾����֐���o�^
		Optional<Runnable> beforeFunc = Optional.ofNullable(mediaPlayer.getOnReady());
		Runnable readyFunc = ()->{
			
			// ��ɓo�^���ꂽ�֐������s
			if(beforeFunc.isPresent()) {
				beforeFunc.get().run();
			}
			volumeSlider.setValue(mediaPlayer.getVolume());
		};
		mediaPlayer.setOnReady(readyFunc);
		
		// �Đ����Ƀ{�����[����\��
		// �{�����[���X���C�_�[�̒l���ύX����邽�тɌĂяo����郊�X�i��o�^
		ChangeListener<? super Number> volumeSliderListener = (ov,newVal,currentValue)->{
			
			// ����̃{�����[���������x���ɏo��
			String volumeStr = String.format("%3.1f", mediaPlayer.getVolume()*100);
			volumeInfoLabel.setText(volumeStr);
			
			mediaPlayer.setVolume(volumeSlider.getValue());
		};
		volumeSlider.valueProperty().addListener(volumeSliderListener);
		
	}
	
	/**
	 * �v���C���[���Ƀ��f�B�A�v���C���[�̂Ȃ���Ԃɖ��������ׂ��R���|�[�l���g���ꊇ���Ė������ł���֐��ł�
	 */
	private void setItemsDisable() {
		//�{�^��
		playButton.setDisable(true);
		pauseButton.setDisable(true);
		stopButton.setDisable(true);
		repeatButton.setDisable(true);
		
		//�X���C�_�[
		timeSlider.setDisable(true);
		volumeSlider.setDisable(true);
		
		//���j���[�A�C�e��
		playMenuItem.setDisable(true);
		pauseMenuItem.setDisable(true);
		stopMenuItem.setDisable(true);
		repeatMenuItem.setDisable(true);
		
		// ���Ԃƃ{�����[���X���C�_�̒l��\�����郉�x���̏����l��ݒ�
		// ���f�B�A���폜����鎞�������l�ɖ߂�
		timeInfoLabel.setText("0:00/0:00");
		volumeInfoLabel.setText("0.0");
	}
	
	private void setItemsEnable() {
		
		//�{�^��
		playButton.setDisable(false);
		pauseButton.setDisable(false);
		stopButton.setDisable(false);
		repeatButton.setDisable(false);
		
		//�X���C�_�[
		timeSlider.setDisable(false);
		volumeSlider.setDisable(false);
		
		//���j���[�A�C�e��
		playMenuItem.setDisable(false);
		pauseMenuItem.setDisable(false);
		stopMenuItem.setDisable(false);
		repeatMenuItem.setDisable(false);
	}
	
	public static void setStage(Stage inStage) {
		stage = inStage;
	}
	
	/**
	 * ����fullScreen�֐����Ăяo���\�[�X�R���|�[�l���g�͑S��ʂ̃��j���[�A�C�e�����炩�ǂ����𔻒肷��֐��ł��B
	 * @param event
	 * @return�@boolean(true/false)
	 */
	private boolean selectedfromMenu(Event event) {
		
		if(event.getSource() instanceof MenuItem ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * �N���b�N���A���Q�񂩂ǂ����𔻒肷��
	 * @return�@boolean(true/false)
	 */
	private boolean checkMouseClicked() {

		// �P���
		if(firstClickTime == 0) {
			// 1��ڂ̃V�X�e���^�C�����L�^����B�����āA�U��Ԃ�
			firstClickTime = System.currentTimeMillis();
			return false;
		}
		// 2���
		else {
			// �Q��ڂ̃V�X�e���^�C�����L�^����
			secondClickTime = System.currentTimeMillis();
			
			// 750�~���b��L�����Ԃɂ���B���ԊԊu��750���z���Ȃ�
			if(secondClickTime - firstClickTime > 750) {
				//�@�^�̏ꍇ�͂P��ڂ̃^�C�����Q��ڂ̃^�C���ɏ㏑������B�����āA�U��Ԃ��B
				firstClickTime = secondClickTime;
				secondClickTime = 0; //�Q��ڂ̃^�C�����O�ɐݒ�
				return false;
			}
			else {
				// �U�̏ꍇ�̓^�C�������Z�b�g���A�^��Ԃ�
				firstClickTime = 0;
				secondClickTime = 0;
				return true;
			}
		}
	}
	
}
