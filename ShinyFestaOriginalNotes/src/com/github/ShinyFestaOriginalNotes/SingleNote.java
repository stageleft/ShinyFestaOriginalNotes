package com.github.ShinyFestaOriginalNotes;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

//import java.util.logging.Logger;


public class SingleNote extends SingleNoteDirectionLibrary {
	/* fixed parameter */
	BufferedImage Target;        // 音符アイコンの目標地点
	boolean       isTargetRotate;
	BufferedImage Line;          // 音符アイコンの通り道
	boolean       isLineRotate;
	BufferedImage LineEnd;       // 音符アイコンの通り道の終端
	boolean       isLineEndRotate;
	BufferedImage ActiveLine;    // 音符アイコンの通り道
	boolean       isActiveLineRotate;
	BufferedImage ActiveLineEnd; // 音符アイコンの通り道の終端
	boolean       isActiveLineEndRotate;
	/* */
	Point  p_0;			// 終了地点
	ArrayList<BufferedImage> LineNotes;   // 出力：長押しラインの動き
	public SingleNote(int note_length, int pos_x, int pos_y) throws IOException
	{
		super(note_length);
		this.p_0           = new Point(pos_x, pos_y);	// 終了地点
	}
	public SingleNote(int note_length, int pos_x, int pos_y,
			          BufferedImage Target, boolean isTargetRotate,
			          BufferedImage Line, boolean isLineRotate,
			          BufferedImage LineEnd, boolean isLineEndRotate,
			          BufferedImage ActiveLine, boolean isActiveLineRotate,
			          BufferedImage ActiveLineEnd, boolean isActiveLineEndRotate) throws IOException
	{
		this(note_length, pos_x, pos_y);								// ノート長さ： 4分音符いくつ分か
		this.Target         = Target;
		this.isTargetRotate = isTargetRotate;
		this.Line         = Line;
		this.isLineRotate = isLineRotate;
		this.LineEnd         = LineEnd;
		this.isLineEndRotate = isLineEndRotate;
		this.ActiveLine         = ActiveLine;
		this.isActiveLineRotate = isActiveLineRotate;
		this.ActiveLineEnd         = ActiveLineEnd;
		this.isActiveLineEndRotate = isActiveLineEndRotate;
	}
	// 音符の軌跡
	// 引数 描画領域オブジェクト(BufferedImage), 時刻(単位時間、getDirection()向け), 生存時間(4分音符の個数、完全展開状態)
	// 戻り値 描画領域オブジェクト(引数と同一)
	public BufferedImage makeStaffImage(BufferedImage canvas, double t, double staff_len)
	{
		double note_len_fadein_time   = -getLineDelayLength() - getLineInitLength();			// 線譜の展開開始時刻
		double note_len_start_time    = -getLineDelayLength();									// 線譜の展開完了時刻
		// nop : 0																				// 最初の音符を叩くタイミング(makeScoreImage()と同じ基準)
		double note_len_end_time      = staff_len * getTimePerScore() -getLineDelayLength();	// 線譜の収束開始時刻(最後の音符が出現する時刻)
		double note_len_fadeout_time  = note_len_end_time + getLineDelayLength();				// 線譜の収束完了時刻
		if ((note_len_fadein_time <= t) && (t < note_len_start_time)) // 音符の軌跡の展開
		{
			makeLineNoteImage(canvas, Target, isTargetRotate, Line, true, LineEnd, true, p_0, 0, (int)((t - note_len_fadein_time) * 2));
		}
		else if ((note_len_start_time <= t) && (t < note_len_end_time)) // 音符の軌跡の保持
		{
			makeLineNoteImage(canvas, Target, isTargetRotate, Line, true, LineEnd, true, p_0, 0, (int)getLineDelayLength());
		}
		else if ((note_len_end_time <= t) && (t < note_len_fadeout_time)) // 音符の軌跡の収束
		{
			makeLineNoteImage(canvas, Target, isTargetRotate, Line, true, LineEnd, true, p_0, 0, (int)(note_len_fadeout_time - t));
		}
		return canvas;
	}
	// 音符
	// 引数 描画領域オブジェクト(BufferedImage), 時刻(単位時間、getDirection()向け), 長押し時間(4分音符の個数)
	// 戻り値 描画領域オブジェクト(引数と同一)
	public BufferedImage makeScoreImage(BufferedImage canvas,
			                            BufferedImage ActiveNote, boolean is_note_rotatable,
			                            double t, double push_len)
	{
		double note_start_time   = -getLineInitLength() * 2;
		// nop : 0 = 音符が目標地点に来たタイミング
		int push_len_end_time =  (int)(push_len * getTimePerScore());

		if ((note_start_time <= t) && (t <= 0))
		{
			makeLineNoteImage(canvas, ActiveNote, is_note_rotatable, ActiveLine, true, ActiveLineEnd, true,
            		          p_0, (int)t, (int)Math.min(push_len_end_time, t - note_start_time));
		}
		else if ((0 < t) && (t < push_len_end_time))
		{
			makeLineNoteImage(canvas, ActiveNote, is_note_rotatable, ActiveLine, true, ActiveLineEnd, true,
            		          p_0, 0, (int)(Math.min(push_len_end_time - t, note_len * getTimePerScore())));
		}
		return canvas;
	}
	public BufferedImage makeLineNoteImage(BufferedImage canvas,
			                               BufferedImage StartNote, boolean is_start_note_rotatable,
			                               BufferedImage LineNote,  boolean is_line_note_rotatable,
			                               BufferedImage EndNote,   boolean is_end_note_rotatable,
			                               Point p_0, int t_end, int t_len)
	{
		// 描画＠接続線(画像は後ろから重ねていく)
		for (int t = 0; t < t_len; t++) {
			makeSingleNoteImage(canvas, LineNote, is_line_note_rotatable, p_0, t_end - t);
		}
		// 描画＠終点
		makeSingleNoteImage(canvas, EndNote,   is_end_note_rotatable, p_0, t_end - t_len);
		// 描画＠始点
		makeSingleNoteImage(canvas, StartNote, is_start_note_rotatable, p_0, t_end);
		return canvas;
	}
	public BufferedImage makeSingleNoteImage(BufferedImage canvas, BufferedImage Note, boolean is_note_rotatable, Point p_0, int t_current)
	{
		AffineTransform op = new AffineTransform();
		// 操作2:平行移動 (Affine変換行列は後の操作を先に書く仕様らしい)
		op.translate((getPosition(t_current, p_0).x - Note.getWidth()/2),
		             (getPosition(t_current, p_0).y - Note.getHeight()/2));
		// 操作1:回転移動
		if (is_note_rotatable == true)
		{
			op.rotate(getDirection(t_current), (Note.getWidth()/2), (Note.getHeight()/2));
		}
		canvas.createGraphics().drawImage(Note, op, null);
		return canvas;
	}
	public double getLineInitLength()
	{
		return (note_len * getTimePerScore() / 2);
	}
	public double getLineDelayLength()
	{
		return (note_len * getTimePerScore());
	}
	public Point getPosition(int lastTime, Point p_0)
	{
		double x = p_0.x;
		double y = p_0.y;
		if (lastTime < 0)
		{
			for (int i = 0; lastTime < i; i--)
			{
				x = x - Math.cos(getDirection(i))/2;
				y = y - Math.sin(getDirection(i))/2;
			}
		}
		else
		{
			for (int i = 0; i < lastTime; i++)
			{
				x = x + Math.cos(getDirection(i))/2;
				y = y + Math.sin(getDirection(i))/2;
			}
		}
		return new Point((int)x, (int)y);
	}
}
