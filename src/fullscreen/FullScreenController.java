package fullscreen;

import java.awt.Dimension;
import java.awt.Toolkit;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FullScreenController {
	
	// FXML�̃t�B�[���h
	// ���ׂẴA�C�e�����{�g���ɔz�u����2�Ԗ�2�Ԗڂ�VBox
	@FXML private VBox controlVBox;
	
	// �S��ʂ̃��f�B�A�r���[
	@FXML private MediaView fullMediaView;
	
	// �^�C���X���C�_�[
	@FXML private Slider longTimeSlider;
	@FXML private Label timeLabel;
	
	//�@�{�����[���X���C�_�[
	@FXML private Slider longVolumeSlider;
	@FXML private Label volumeLabel;
	
	// �t�B�[���h�ϐ�
	private MediaPlayer mediaPlayer;
	private Stage mediaStage;
	
	private SimpleDoubleProperty screenWidth, screenHeight;
	
	/**
	 * @param ms
	 * @param mp
	 */
	public void init(Stage ms, MediaPlayer mp) {
		
		// MediaPlayer��������n���ꂽ�ϐ����t�B�[���h�ɐݒ肷��
		mediaPlayer = mp;
		mediaStage = ms;
		
		// �S��ʂ̃T�C�Y���擾����
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		// ���f�B�A�r���[�ɑ΂��āA�S��ʂ̑傫���Ƃ̃o�C���f�B���O���s���B
		screenWidth = new SimpleDoubleProperty(screen.getWidth());
		screenHeight = new SimpleDoubleProperty(screen.getHeight());
		fullMediaView.fitWidthProperty().bind(screenWidth);
		fullMediaView.fitHeightProperty().bind(screenHeight);
		
		// �S��ʂ̔������^�C���X���C�_�[�̏����ݒ���s��
		longTimeSlider.setPrefWidth(screen.getWidth());
		setTimeSliderListener();
		
		// �S��ʂ̔������{�����[���X���C�_�[�̏����ݒ���s��
		volumeLabel.setText(Double.toString(mediaPlayer.getVolume()*100));
		setVolumeSliderListener();
		
		// ���f�B�A�v���C���[�����f�B�A�r���[�ɐݒ�
		fullMediaView.setMediaPlayer(mp);
		
		// StackPane�ɂ����ԏ��VBox�����S�����ɐݒ�
		controlVBox.setOpacity(0);

	}
	

	/**
	 * �^�C���X���C�_�[���ړ�����鎞�̃C�x���g
	 */
	@FXML
	private void setTimeSliderEvent() {
		//�ړ������ƃV�[�N����
		mediaPlayer.seek(Duration.seconds(longTimeSlider.getValue()));
	}

	/**
	 * �J�[�\����VBox�̊O�ł͊��S�����ɂ���
	 */
	@FXML
	private void makeOpaqueAndEditableByCursor() {
		controlVBox.setOpacity(0);
	}
	
	/**
	 * �J�[�\����VBox�̒��ł͕s�����ɂ���
	 */
	@FXML
	private void NotOpaqueAndEditableByCursor() {
		controlVBox.setOpacity(1);
	}
	
	/**
	 *�@�Đ��{�^��
	 */
	@FXML 
	private void playMediaEvent() {
		mediaPlayer.play();
	}
	
	/**
	 * �ꎞ��~�{�^��
	 */
	@FXML 
	private void pauseMediaEvent() {
		mediaPlayer.pause();
	}

	/**
	 * ��~�{�^��
	 */
	@FXML
	private void stopMediaEvent() {
		mediaPlayer.stop();
	}
	
	/**
	 * �t���X�N���[���I���{�^��
	 * @param e
	 */
	@FXML
	private void exitFullScreen(ActionEvent e) {
		// �S��ʂ̃X�e�[�W�I�u�W�F�N�g���擾
		Stage thisStage = (Stage) ((Node)e.getSource()).getScene().getWindow();
		thisStage.close();
		
		// ���f�B�A�v���C���[�X�e�[�W��\��
		mediaStage.show();
	}
	
	/**
	 * �C�x���g�n���h���Ƃ���
	 */
	private void setTimeSliderListener() {
		
		//�@����Đ����̃^�C���X���C�_�[�̏����l��o�^����
		longTimeSlider.setMax(mediaPlayer.getStopTime().toSeconds());
		longTimeSlider.setMin(mediaPlayer.getStartTime().toSeconds());

		// �s�N�Z�����E�̒�����N���ɂ���
		longTimeSlider.setSnapToPixel(true);

	
		// �Đ����ɃX���C�_�[���ړ�
		// �v���C���[�̍Đ����ɌĂяo����郊�X�i��o�^
		mediaPlayer.currentTimeProperty().addListener((ov, newVal, currentVal)->{
			
			// ���挻�ݎ��ԂƏI�����Ԃ��擾
			double currentTime = mediaPlayer.getCurrentTime().toSeconds();
			double endTime = mediaPlayer.getStopTime().toSeconds();
			
			// ����̏������x���ɏo��
			String curTimeStr = String.format("%3.0f:%02.0f", currentTime / 60, currentTime % 60 ) +"/" +
					String.format("%3.0f:%02.0f", endTime / 60, endTime % 60);
			timeLabel.setText(curTimeStr);
			
			// �X���C�_�[���ړ�
			longTimeSlider.setValue(currentTime);
		});

	}
	
	/**
	 * �C�x���g�n���h���Ƃ��āA�{�����[���X���C�_�[�ɓ���̏���񋟂���
	 */
	private void setVolumeSliderListener() {

		//�X���C�_�[�̏����l��ݒ肷��
		longVolumeSlider.setMin(0);
		longVolumeSlider.setMax(1);
		longVolumeSlider.setValue(mediaPlayer.getVolume());
		longVolumeSlider.setSnapToPixel(true);
		
		// �Đ����Ƀ{�����[����\��
		// �v���C���[�̃{�����[�����ύX����邽�тɌĂяo����郊�X�i��o�^
		longVolumeSlider.valueProperty().addListener((ov, newVal, curVal)->{
			
			// ����̃{�����[���������x���ɏo��
			String volumeStr = String.format("%3.1f", mediaPlayer.getVolume()*100);
			volumeLabel.setText(volumeStr);
			
			// �X���C�_�ɍ��킹�ă{�����[����ύX
			mediaPlayer.setVolume(longVolumeSlider.getValue());
		});
		
	}
	
}
