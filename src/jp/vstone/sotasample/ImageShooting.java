package jp.vstone.sotasample; //src→jp.vstone.sotasampleのフォルダに置く
import java.awt.Color;
import java.io.IOException;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotPose;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.camera.CRoboCamera;
import jp.vstone.camera.CameraCapture;

/**
撮影した画像をアップロード
 *
 */
public class ImageShooting {

	static final String TAG = "Image Shooting ";
	//アップロード先のPHPスクリプトのURL
	static final String upload_php_url = "https://athena.abe-lab.jp/~学籍番号/zemi2021/file_uploader/file_upload_db_学籍番号.php";
	//→アップロード結果はhttps://athena.abe-lab.jp/~学籍番号/zemi2021/file_uploader/uploader_file_list_学籍番号.htmlで確認

	public static void main(String args[]){
		CRobotUtil.Log(TAG, "Start " + TAG);

		CRobotPose pose;
		//VSMDと通信ソケット・メモリアクセス用クラス
		CRobotMem mem = new CRobotMem();
		//Sota用モーション制御クラス
		CSotaMotion motion = new CSotaMotion(mem);

		CRoboCamera cam = new CRoboCamera("/dev/video0", motion);

		if(mem.Connect()){
			//Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();

			CRobotUtil.Log(TAG, "Rev. " + mem.FirmwareRev.get());

			//サーボモータを現在位置でトルクOnにする
			CRobotUtil.Log(TAG, "Servo On");
			motion.ServoOn();

			//すべての軸を動作
			pose = new CRobotPose();
			pose.SetPose(new Byte[] {1   ,2   ,3   ,4   ,5   ,6   ,7   ,8}	//id
					,  new Short[]{0   ,-900,0   ,900 ,0   ,0   ,0   ,0}				//target pos
					);
			//LEDを点灯（左目：赤、右目：赤、口：Max、電源ボタン：赤）
			pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.GREEN);

			motion.play(pose, 500);
			CRobotUtil.wait(500);

			int photocnt = 0;

			// 現在日時を取得
			//LocalDateTime date = LocalDateTime.now();

			// 表示形式を指定
			//DateTimeFormatter date_format =DateTimeFormatter.ofPattern("yyyyMMdd");
			//String date_str = date_format.format(date); //format(d)のdは、11行目のd

			//LEDだけ先に変更
			pose.setLED_Sota(Color.ORANGE, Color.ORANGE, 255, Color.GREEN);
			//playに任意のKeyを指定すると、
			motion.play(pose, 100,"FACE_LED");

			//写真を取る前のポーズ＋音声
			CPlayWave.PlayWave("./sound/take_a_photo.wav");
			pose = new CRobotPose();	//@<BlockInfo>jp.vstone.block.pose,208,80,208,80,False,2,コメント@</BlockInfo>
			pose.SetPose(	new Byte[]{1,2,3,4,5},
							new Short[]{-1,71,-895,1,769}
							);
			motion.play(pose,1000);

			long start = System.currentTimeMillis();

			//撮影用に初期化(CameraCapture.画像サイズでサイズを指定)
			cam.initStill(new CameraCapture(CameraCapture.CAP_IMAGE_SIZE_VGA, CameraCapture.CAP_FORMAT_MJPG));
			CRobotUtil.wait(100); //ピントを合わせるのに少し0.何秒か時間を置く

			//撮影時のポーズ＋音声
			CPlayWave.PlayWave("./sound/pasha.wav");

			String img_name = "/home/root/SotaService2022/bin/tmp_02"; //画像のファイル名（.jpgは後から付ける）
			if(CRobotUtil.isRpi()) {
				img_name="/home/pi/SotaService2022/bin/tmp_01";
			}
			String img_file = img_name + ".jpg";
			//撮影
			cam.StillPicture(img_name);


			long end = System.currentTimeMillis();
			System.out.println("撮影時間: "+(end-start)+"msec.");

			//curlコマンドを利用して画像ファイルを送信する
			Runtime runtime = Runtime.getRuntime(); // ランタイムオブジェクトを取得する

			String[] Command = {"curl", "-X", "POST", "-k", upload_php_url,"-F" ,"upload_file=@"+img_file, "-F", "title=sota_upload", "-F", "description=sota_upload"}; // 起動コマンドを指定する
			try {
				runtime.exec(Command); // 指定したコマンドを実行する
			} catch (IOException e) {
				e.printStackTrace();
			}

			pose.SetPose(new Byte[] {1   ,2   ,3   ,4   ,5   ,6   ,7   ,8}	//id
			,  new Short[]{0   ,-900,0   ,900 ,0   ,0   ,0   ,0}				//target pos
			);
			//LEDを点灯（左目：赤、右目：赤、口：Max、電源ボタン：赤）
			pose.setLED_Sota(Color.BLUE, Color.BLUE, 255, Color.GREEN);

			motion.play(pose, 500);
			CRobotUtil.wait(600);
		}

		motion.ServoOff();
	}
}
