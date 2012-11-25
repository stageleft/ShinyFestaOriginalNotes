package com.github.ShinyFestaOriginalNotes;

public class ShinyFestaOriginalNotes {
	int width  = 854;		// 出力画像（動画）幅：出力動画サイズに合わせます。
	int height = 480;		// 出力画像（動画）高さ：出力動画サイズに合わせます。
	int frame_rate = 30;	// 出力動画フレームレート：出力動画サイズに合わせます。
	int note_virtual_size = 33;  // 音符アイコンの仮想サイズ
	public int getTimePerScore() // 4分音符1つの長さ
	{
		// bpm   = １分間に現れる四分音符の数
		// t : 音符アイコンが1dot進むための時間
		// 音符アイコンの直径=32
		//  すなわち、四分音符１個あたりtは64進むため、1フレーム(1/30min)でtは(64 * bpm / 1800)だけ進む。
		return (int)((note_virtual_size * 4));
	}
}
