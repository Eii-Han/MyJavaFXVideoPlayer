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
	
	// 操作パネルのスライダ
	@FXML private Slider timeSlider;
	@FXML private Slider volumeSlider;
	@FXML private Label timeInfoLabel;
	@FXML private Label volumeInfoLabel;
	
	// 操作パネルのボタン：再生、一時停止、停止、繰返し
	@FXML private Button newMediaButton;
	@FXML private Button playButton;
	@FXML private Button pauseButton;
	@FXML private Button stopButton;
	@FXML private ToggleButton repeatButton;
	
	//　Menu上のアイテム：再生、一時停止、停止、繰返し
	@FXML private MenuItem playMenuItem;
	@FXML private MenuItem pauseMenuItem;
	@FXML private MenuItem stopMenuItem;
	@FXML private MenuItem repeatMenuItem;
	
	//　静音
	@FXML private MenuItem muteMenuItem;
	
	//　スタートメニューのアイテム
	@FXML private MenuItem newMediaMenuItem;
	@FXML private MenuItem closeMenuItem;
	
	// BorderPane
	@FXML private BorderPane innerBorderPane;
	@FXML private BorderPane outerBorderPane;
	
	//トップの配置とボトムの配置
	@FXML private VBox topmostVBox;
	@FXML private HBox bottomHBox;
	
	// 映像を表示するメディアビュー
	@FXML private MediaView mediaView;
	
	//　再生中のメディアのプレイヤー
	private MediaPlayer mediaPlayer;
	
	// パソコンのサイズを記憶するオブジェクト
	private Dimension screenSize;
	
	// プレイヤーを表示するステージ
	private static Stage stage;
	
	// プレイヤーがある時とない時の動作を制御するために定義されたBoolean
	private boolean haveMediaPlayer = false;
	
	// ダブルクリックが有効かどうかを判定する時に使うフィールドです。
	private long firstClickTime = 0;
	private long secondClickTime = 0;

	/* 
	 * プレイヤーを初期化
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		//　パソコンの画面サイズを取得
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// 初期値として画面の半分を設定する
		outerBorderPane.setPrefSize(screenSize.getWidth() * 0.5 , screenSize.getHeight() * 0.5 );
		
		// メディアビューとBorderPaneのバインディングを設定する
		innerBorderPane.prefWidthProperty().bind(mediaView.fitWidthProperty());
		innerBorderPane.prefHeightProperty().bind(mediaView.fitHeightProperty());

		mediaView.fitWidthProperty().bind(outerBorderPane.widthProperty().multiply(1).subtract(20));
		mediaView.fitHeightProperty().bind(outerBorderPane.heightProperty().multiply(1).
				subtract(topmostVBox.getHeight() + bottomHBox.getHeight()+70));   // トップとボトムに配置するもののオフセットを設定する
		
		// メディアがまだロードされていない状態で操作できるはずのないものを無効化する
		setItemsDisable();
	}
	
	/**
	 * 再生ボタンと再生メニューの動作を定義
	 */
	@FXML 
	private void playMediaEvent() {
		mediaPlayer.play();
	}
	
	/**
	 * 一時停止ボタンと一時停止メニューの動作を定義
	 */
	@FXML 
	private void pauseMediaEvent() {
		mediaPlayer.pause();
	}

	/**
	 * 停止ボタンと停止メニューの動作を定義
	 */
	@FXML
	private void stopMediaEvent() {
		mediaPlayer.stop();
	}
	
	/**
	 * 新規メディアボタン・メニューがクリックされる時の動作を定義
	 */
	@FXML
	private void newMediaLoadingEvent() {
		
		// まず、メディアファイルをロードしてみる。正常完了すれば、プレイヤーを返す。
		Optional<MediaPlayer> optionalPlayer = Optional.ofNullable(returnNewPlayer());
		
		// プレイヤーが存在する場合のみ、下記のコードを実行する
		if(optionalPlayer.isPresent()) {
			
			// プレイヤーをゲットする
			mediaPlayer = optionalPlayer.get();
			
			// メディアビューに設定
			mediaView.setMediaPlayer(mediaPlayer);

			// 初期値として無効化されたすべてのものを有効にする
			setItemsEnable();
			
			// 再生終了した時の動作を設定
			mediaPlayer.setOnEndOfMedia(this::repeatMediaEvent);
			
			// 再生時間スライダーの初期値を設定
			timeSlider.setMin(0);
			timeSlider.setMax(1);

			// Ready状態でのスライダープロパティを設定
			setTimeSliderEventOnReady();
			
			//　ボリュームスライダーの初期値を設定
			volumeSlider.setMin(0);
			volumeSlider.setMax(1);

			
			// Ready状態でのスライダープロパティを設定
			setVolumeSliderEventOnReady();
			
			// メディアのロードが成功であるため、真に設定する。
			haveMediaPlayer = true;
		}
	}
	
	/**
	 * 連続再生とメニューの動作を設定
	 */
	@FXML
	private void repeatMenuPressed() {
		// 連続再生ボタンの状態を設定
		if(repeatButton.isSelected()) {
			repeatButton.setSelected(false);
		}else {
			repeatButton.setSelected(true);
		}
	}
	
	/**
	 * 時間スライダー移動時のイベントを定義
	 */
	@FXML
	private void setEventOnTimeSlider() {
		// スライダーを操作するとシークする
		mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
	}
	
	/**
	 * クローズメニューの動作を定義する。
	 */
	@FXML
	private void closeMenuItemClicked() {
		// 最初のステージをクローズ
		stage.close();
	}
	
	/**
	 * 動画削除メニュの動作を設定
	 */
	@FXML
	private void removeMediaClicked() {

		// 現在プレイヤーのステータスを取得
		switch(mediaPlayer.getStatus()) {
		// 再生中状態の場合は動作削除をできないようにする
		case PLAYING:
			Alert alert1 = new Alert(AlertType.ERROR, "再生中の動画は削除できません");
			mediaPlayer.pause();
			alert1.showAndWait();
			mediaPlayer.play();
			break;
		//　そもそも動画がロードされていない場合
		case UNKNOWN:
			Alert alert2 = new Alert(AlertType.INFORMATION, "動画はありません。");
			alert2.show();
			break;
		// その他の状態は削除処理を進める
		default:
			removeMedia();
			break;
		}

	}
	
	/**
	 * 全画面表示モード
	 */
	@FXML
	private void fullScreen(Event event) {
		
		//　プレイヤーに動画があるかどうかを確認する
		if(haveMediaPlayer) {
			// メディアビューでは2回クリック、メニューバーではアイテムを選択
			if(checkMouseClicked() | selectedfromMenu(event)) {
				
				// FXMLファイルからシーンを取得
				FXMLLoader loader = new FXMLLoader();
				try {
					Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
					loader.setLocation(fullscreen.FullScreenController.class.getResource("FullScreen.fxml"));
					Parent root = loader.load();
					
					//　シーンのサイズを端末の画面の大きさに設定
					Scene scene = new Scene(root,screen.getWidth(),screen.getHeight());

					// 全画面のコントローラーを取得
					FullScreenController controller = loader.getController();

					// ステージスタイルはなんの装飾がないものに設定する
					Stage newsStage = new Stage(StageStyle.UNDECORATED);
					newsStage.setScene(scene);
					
					// 全画面をOnにする
					newsStage.setFullScreen(true);
					newsStage.initModality(Modality.APPLICATION_MODAL);
					
					// 全画面を表示すると同時に、現在の画面を隠れる
					newsStage.show();
					stage.hide();

					// 全画面でESCキーを押下したら、プレイヤー画面に戻る
					scene.setOnKeyPressed(e->{
						if(e.getCode()==KeyCode.ESCAPE) {
							newsStage.close();
							stage.show();
						}
					});
					
					// 現在のステージとメディアプレイヤーを全画面のコントローラーに引き渡す
					controller.init(stage , mediaPlayer);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * １/４画面
	 */
	@FXML
	private void oneQuarterScreenClicked(){
		
		// バインディングを再設定。
		mediaView.fitWidthProperty().bind(new SimpleDoubleProperty(screenSize.getWidth()/4));
		mediaView.fitHeightProperty().bind(new SimpleDoubleProperty(screenSize.getHeight()/4));

		// BorderPaneのサイズを再設定
		outerBorderPane.setPrefSize(screenSize.getWidth()/4 + 50,  //横のサイズ、50pxはオフセット
				screenSize.getHeight() / 4 - (topmostVBox.getHeight() + bottomHBox.getHeight()+70)); //縦のサイズ、VBoxとHBoxのオフセットあり
		
		// リサイズ
		stage.sizeToScene();
	}
	
	/**
	 * 1/2画面
	 * コメントは同上。
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
	 * 3/4画面
	 * コメントは同上。
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
	 * 画面サイズ変更のメニューの動作を定義
	 */
	@FXML
	private void follwingBySizeOfStage() {
		//　バインディングを元に戻る
		mediaView.fitWidthProperty().bind(outerBorderPane.widthProperty().multiply(1).subtract(20));
		mediaView.fitHeightProperty().bind(outerBorderPane.heightProperty().multiply(1).subtract(topmostVBox.getHeight() + bottomHBox.getHeight()+70));
	}
	
	/**
	 * 静音メニューの動作を定義
	 */
	@FXML
	private void setMuteEvent() {
		
		//MuteがOnである場合
		if(mediaPlayer.isMute()) {
			mediaPlayer.setMute(false);
			muteMenuItem.setText("Mute");
		}
		//MuteがOffの場合
		else {
			mediaPlayer.setMute(true);
			muteMenuItem.setText("Undo Mute");
		}
	}
	
	
	
	
	
	/**
	 * 連続再生ボタンの動作を設定
	 */
	private void repeatMediaEvent() {
		
		// 連続再生ボタンの状態を確認
		if(repeatButton.isSelected()) {
			//　頭出して再生
			mediaPlayer.seek(mediaPlayer.getStartTime());
			mediaPlayer.play();
		}else {
			// 頭出して停止
			mediaPlayer.seek(mediaPlayer.getStartTime());
			mediaPlayer.stop();
		}
		
	}
	
	/**
	 * 動画削除の手続き
	 */
	private void removeMedia() {
		// 動画を停止
		mediaPlayer.stop();
		
		// 直接メディアプレイヤーをNullに設定してしまえば、ChangeListenerとの競合が
		// 発生し、例外が投げられる。 バックグラウンド上で1秒をスリープし、Nullに設定すれば、この問題は回避できる
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

		// 操作できないコンポーネントを無効化にする
		setItemsDisable();
		haveMediaPlayer = false;
	}

	/**
	 * 動画読み込み時に呼び出される関数です
	 * MediaPlayerを返す。またはNullを返す。
	 * @return
	 */
	private MediaPlayer returnNewPlayer() {
		
		//　FileChooserポップアップ用のステージ
		Stage stage = new Stage();
		
		//	FileChooserインスタンスを作成
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open new Video");
		
		//　対応可能なファイル拡張子を定義
		List<String> videoFileExtension = Arrays.asList("*.mp4","*.mpg","*.wmv");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Video File(\"*,mp4\", \"*.mpg\",\"*.wmv\")", videoFileExtension));
		
		//　スタートディレクトリをデフォルトのビデオに設定
		String userDirectoryString = System.getProperty("user.home") + "//Videos";
		File userDirectory = new File(userDirectoryString);
		
		//　設定が失敗した時は、最初のディレクトリに設定
		if(!userDirectory.canRead()) {
			userDirectory = new File("C://");
		}
		
		//　ディレクトリを設定
		fileChooser.setInitialDirectory(userDirectory);
		
		//　メディアファイルを格納する
		Optional<File> file = Optional.ofNullable(fileChooser.showOpenDialog(stage));
		
		// 存在するかどうかを確認
		if(file.isPresent()) {
			Media media;
			
			// 規格が支援されてるかどうかを確認
			try {
				media = new Media(file.get().toURI().toString());
			}catch(MediaException e) {
				// 規格が支援されていない場合は警告を出て、Nullを返す
				Alert alert = new Alert(AlertType.ERROR, "Unsupported Format");
				alert.show();
				return null;
			}
			MediaPlayer localPlayer = new MediaPlayer(media);
			return localPlayer;
		}
		else {
			// それ以外の場合はNullを返す。
			return null;
		}
	}
	
	/**
	 * メディアを読み込んが成功した場合のみに呼び出される。
	 * イベントハンドラとして、時間表示スライダーに動画の情報を提供する
	 */
	private void setTimeSliderEventOnReady() {
		
		// 再生準備完了時に各種情報を取得する関数を登録
		Optional<Runnable> beforeFunc = Optional.ofNullable(mediaPlayer.getOnReady());
		Runnable readyFunc = ()->{
			
			//　先に登録された関数を実行
			if(beforeFunc.isPresent()) {
				beforeFunc.get().run();
			}
			
			//　プレイヤーに保持されるメディアの情報を取得し、スライダに設定
			timeSlider.setMin(mediaPlayer.getStartTime().toSeconds());
			timeSlider.setMax(mediaPlayer.getStopTime().toSeconds());
			timeSlider.setSnapToPixel(true);
		};
		mediaPlayer.setOnReady(readyFunc);
		
		// 再生中にスライダーを移動
		// プレイヤーの再生中に呼び出されるリスナを登録
		ChangeListener<? super Duration> playListener =(ov, newVal, currentVal)->{
			
			// 動画の情報をラベル出力
			String timeInfoStr = String.format("%3.0f:%02.0f", mediaPlayer.getCurrentTime().toSeconds() / 60, 
					mediaPlayer.getCurrentTime().toSeconds() % 60) + "/" + String.format("%3.0f:%02.0f",
					mediaPlayer.getTotalDuration().toSeconds()/60, mediaPlayer.getTotalDuration().toSeconds()%60);
			timeInfoLabel.setText(timeInfoStr);
			
			// スライダーを移動
			timeSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
					
		};
		mediaPlayer.currentTimeProperty().addListener(playListener);
	}
	
	/**
	 * メディアを読み込んが成功した場合のみに呼び出される。
	 * イベントハンドラとして、ボリュームスライダーに動画の情報を提供する
	 */
	private void setVolumeSliderEventOnReady() {
		
		// 再生準備完了時に各種情報を取得する関数を登録
		Optional<Runnable> beforeFunc = Optional.ofNullable(mediaPlayer.getOnReady());
		Runnable readyFunc = ()->{
			
			// 先に登録された関数を実行
			if(beforeFunc.isPresent()) {
				beforeFunc.get().run();
			}
			volumeSlider.setValue(mediaPlayer.getVolume());
		};
		mediaPlayer.setOnReady(readyFunc);
		
		// 再生中にボリュームを表示
		// ボリュームスライダーの値が変更されるたびに呼び出されるリスナを登録
		ChangeListener<? super Number> volumeSliderListener = (ov,newVal,currentValue)->{
			
			// 動画のボリューム情報をラベルに出力
			String volumeStr = String.format("%3.1f", mediaPlayer.getVolume()*100);
			volumeInfoLabel.setText(volumeStr);
			
			mediaPlayer.setVolume(volumeSlider.getValue());
		};
		volumeSlider.valueProperty().addListener(volumeSliderListener);
		
	}
	
	/**
	 * プレイヤー中にメディアプレイヤーのない状態に無効化すべきコンポーネントを一括して無効化できる関数です
	 */
	private void setItemsDisable() {
		//ボタン
		playButton.setDisable(true);
		pauseButton.setDisable(true);
		stopButton.setDisable(true);
		repeatButton.setDisable(true);
		
		//スライダー
		timeSlider.setDisable(true);
		volumeSlider.setDisable(true);
		
		//メニューアイテム
		playMenuItem.setDisable(true);
		pauseMenuItem.setDisable(true);
		stopMenuItem.setDisable(true);
		repeatMenuItem.setDisable(true);
		
		// 時間とボリュームスライダの値を表示するラベルの初期値を設定
		// メディアが削除される時も初期値に戻る
		timeInfoLabel.setText("0:00/0:00");
		volumeInfoLabel.setText("0.0");
	}
	
	private void setItemsEnable() {
		
		//ボタン
		playButton.setDisable(false);
		pauseButton.setDisable(false);
		stopButton.setDisable(false);
		repeatButton.setDisable(false);
		
		//スライダー
		timeSlider.setDisable(false);
		volumeSlider.setDisable(false);
		
		//メニューアイテム
		playMenuItem.setDisable(false);
		pauseMenuItem.setDisable(false);
		stopMenuItem.setDisable(false);
		repeatMenuItem.setDisable(false);
	}
	
	public static void setStage(Stage inStage) {
		stage = inStage;
	}
	
	/**
	 * 現在fullScreen関数を呼び出すソースコンポーネントは全画面のメニューアイテムからかどうかを判定する関数です。
	 * @param event
	 * @return　boolean(true/false)
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
	 * クリックが連続２回かどうかを判定する
	 * @return　boolean(true/false)
	 */
	private boolean checkMouseClicked() {

		// １回目
		if(firstClickTime == 0) {
			// 1回目のシステムタイムを記録する。そして、偽を返す
			firstClickTime = System.currentTimeMillis();
			return false;
		}
		// 2回目
		else {
			// ２回目のシステムタイムを記録する
			secondClickTime = System.currentTimeMillis();
			
			// 750ミリ秒を有効時間にする。時間間隔は750を越えない
			if(secondClickTime - firstClickTime > 750) {
				//　真の場合は１回目のタイムを２回目のタイムに上書きする。そして、偽を返す。
				firstClickTime = secondClickTime;
				secondClickTime = 0; //２回目のタイムを０に設定
				return false;
			}
			else {
				// 偽の場合はタイムをリセットし、真を返す
				firstClickTime = 0;
				secondClickTime = 0;
				return true;
			}
		}
	}
	
}
