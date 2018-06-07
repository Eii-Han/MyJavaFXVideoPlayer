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
	
	// FXMLのフィールド
	// すべてのアイテムをボトムに配置する2番目2番目のVBox
	@FXML private VBox controlVBox;
	
	// 全画面のメディアビュー
	@FXML private MediaView fullMediaView;
	
	// タイムスライダー
	@FXML private Slider longTimeSlider;
	@FXML private Label timeLabel;
	
	//　ボリュームスライダー
	@FXML private Slider longVolumeSlider;
	@FXML private Label volumeLabel;
	
	// フィールド変数
	private MediaPlayer mediaPlayer;
	private Stage mediaStage;
	
	private SimpleDoubleProperty screenWidth, screenHeight;
	
	/**
	 * @param ms
	 * @param mp
	 */
	public void init(Stage ms, MediaPlayer mp) {
		
		// MediaPlayerから引き渡された変数をフィールドに設定する
		mediaPlayer = mp;
		mediaStage = ms;
		
		// 全画面のサイズを取得する
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		// メディアビューに対して、全画面の大きさとのバインディングを行う。
		screenWidth = new SimpleDoubleProperty(screen.getWidth());
		screenHeight = new SimpleDoubleProperty(screen.getHeight());
		fullMediaView.fitWidthProperty().bind(screenWidth);
		fullMediaView.fitHeightProperty().bind(screenHeight);
		
		// 全画面の半透明タイムスライダーの初期設定を行う
		longTimeSlider.setPrefWidth(screen.getWidth());
		setTimeSliderListener();
		
		// 全画面の半透明ボリュームスライダーの初期設定を行う
		volumeLabel.setText(Double.toString(mediaPlayer.getVolume()*100));
		setVolumeSliderListener();
		
		// メディアプレイヤーをメディアビューに設定
		fullMediaView.setMediaPlayer(mp);
		
		// StackPaneにある一番上のVBoxを完全透明に設定
		controlVBox.setOpacity(0);

	}
	

	/**
	 * タイムスライダーを移動される時のイベント
	 */
	@FXML
	private void setTimeSliderEvent() {
		//移動されるとシークする
		mediaPlayer.seek(Duration.seconds(longTimeSlider.getValue()));
	}

	/**
	 * カーソルがVBoxの外では完全透明にする
	 */
	@FXML
	private void makeOpaqueAndEditableByCursor() {
		controlVBox.setOpacity(0);
	}
	
	/**
	 * カーソルがVBoxの中では不透明にする
	 */
	@FXML
	private void NotOpaqueAndEditableByCursor() {
		controlVBox.setOpacity(1);
	}
	
	/**
	 *　再生ボタン
	 */
	@FXML 
	private void playMediaEvent() {
		mediaPlayer.play();
	}
	
	/**
	 * 一時停止ボタン
	 */
	@FXML 
	private void pauseMediaEvent() {
		mediaPlayer.pause();
	}

	/**
	 * 停止ボタン
	 */
	@FXML
	private void stopMediaEvent() {
		mediaPlayer.stop();
	}
	
	/**
	 * フルスクリーン終了ボタン
	 * @param e
	 */
	@FXML
	private void exitFullScreen(ActionEvent e) {
		// 全画面のステージオブジェクトを取得
		Stage thisStage = (Stage) ((Node)e.getSource()).getScene().getWindow();
		thisStage.close();
		
		// メディアプレイヤーステージを表示
		mediaStage.show();
	}
	
	/**
	 * イベントハンドラとして
	 */
	private void setTimeSliderListener() {
		
		//　動画再生時のタイムスライダーの初期値を登録する
		longTimeSlider.setMax(mediaPlayer.getStopTime().toSeconds());
		longTimeSlider.setMin(mediaPlayer.getStartTime().toSeconds());

		// ピクセル境界の調整を鮮明にする
		longTimeSlider.setSnapToPixel(true);

	
		// 再生中にスライダーを移動
		// プレイヤーの再生中に呼び出されるリスナを登録
		mediaPlayer.currentTimeProperty().addListener((ov, newVal, currentVal)->{
			
			// 動画現在時間と終了時間を取得
			double currentTime = mediaPlayer.getCurrentTime().toSeconds();
			double endTime = mediaPlayer.getStopTime().toSeconds();
			
			// 動画の情報をラベルに出力
			String curTimeStr = String.format("%3.0f:%02.0f", currentTime / 60, currentTime % 60 ) +"/" +
					String.format("%3.0f:%02.0f", endTime / 60, endTime % 60);
			timeLabel.setText(curTimeStr);
			
			// スライダーを移動
			longTimeSlider.setValue(currentTime);
		});

	}
	
	/**
	 * イベントハンドラとして、ボリュームスライダーに動画の情報を提供する
	 */
	private void setVolumeSliderListener() {

		//スライダーの初期値を設定する
		longVolumeSlider.setMin(0);
		longVolumeSlider.setMax(1);
		longVolumeSlider.setValue(mediaPlayer.getVolume());
		longVolumeSlider.setSnapToPixel(true);
		
		// 再生中にボリュームを表示
		// プレイヤーのボリュームが変更されるたびに呼び出されるリスナを登録
		longVolumeSlider.valueProperty().addListener((ov, newVal, curVal)->{
			
			// 動画のボリューム情報をラベルに出力
			String volumeStr = String.format("%3.1f", mediaPlayer.getVolume()*100);
			volumeLabel.setText(volumeStr);
			
			// スライダに合わせてボリュームを変更
			mediaPlayer.setVolume(longVolumeSlider.getValue());
		});
		
	}
	
}
