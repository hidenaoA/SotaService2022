package jp.vstone.sotasample;

import java.util.List;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.sotatalk.SpeechRecog;
import jp.vstone.sotatalk.SpeechRecog.RecogResult;
import jp.vstone.sotatalk.SpeechRecog.Sentence;
import jp.vstone.sotatalk.TextToSpeechSota;

public class TextToSpeechSample2 {
	static final String TAG = "SpeechRecSample2";
	public static void main(String[] args) {

		//VSMDと通信ソケット・メモリアクセス用クラス
		CRobotMem mem = new CRobotMem();
		//Sota用モーション制御クラス
		CSotaMotion motion = new CSotaMotion(mem);
		SpeechRecog recog = new SpeechRecog(motion);
		recog.setLang("en");

		CPlayWave.PlayWave(TextToSpeechSota.getTTS("英語で話しかけてください"),true);
		//CPlayWave.PlayWave(TextToSpeechSota.getTTS("僕の名前はSotaです。"),true);
		//byte[] data = TextToSpeechSota.getTTS("これから、よろしくね！");
		//if(data == null){
		//	CRobotUtil.Log(TAG,"ERROR");
		//}
		//CPlayWave.PlayWave(data,true);

		if(mem.Connect()){
			//Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();
			while(true){
				RecogResult result = recog.getRecognition(5000);

				if(result.recognized){
					List<Sentence> sentence_list = result.getSentencelist();
					for(int i=0; i<sentence_list.size(); i++) {
						System.out.println("Sentence: "+sentence_list.get(i).toString());
					}
					TextToSpeechSota.setLocalizeLang("en");
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile(result.getBasicResult()),true);
					if(result.getBasicResult().contains("stop")){
						TextToSpeechSota.cancelLocalize();
						CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("終了するよ"),true);
						break;
					}
				}
				else {
					TextToSpeechSota.cancelLocalize();
					CPlayWave.PlayWave(TextToSpeechSota.getTTSFile("何も聞き取れなかったので、終了します"),true);
					break;
				}
			}
		}


		//CPlayWave.PlayWave(TextToSpeechSota.getTTS("僕の名前はSotaです。僕の名前はSotaです。僕の名前はSotaです。僕の名前はSotaです。僕の名前はSotaです。僕の名前はSotaです。僕の名前はSotaです。僕の名前はSotaです。"),true);

		//CPlayWave.PlayWave(TextToSpeechSota.getTTS("Hello! World!"),true);

	}
}
