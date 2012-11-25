package com.github.ShinyFestaOriginalNotes;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

//import java.util.logging.Logger;


public class SingleNote extends SingleNoteDirectionLibrary {
	/* fixed parameter */
	BufferedImage Target;        // �����A�C�R���̖ڕW�n�_
	boolean       isTargetRotate;
	BufferedImage Line;          // �����A�C�R���̒ʂ蓹
	boolean       isLineRotate;
	BufferedImage LineEnd;       // �����A�C�R���̒ʂ蓹�̏I�[
	boolean       isLineEndRotate;
	BufferedImage ActiveLine;    // �����A�C�R���̒ʂ蓹
	boolean       isActiveLineRotate;
	BufferedImage ActiveLineEnd; // �����A�C�R���̒ʂ蓹�̏I�[
	boolean       isActiveLineEndRotate;
	/* */
	Point  p_0;			// �I���n�_
	ArrayList<BufferedImage> LineNotes;   // �o�́F���������C���̓���
	public SingleNote(int note_length, int pos_x, int pos_y) throws IOException
	{
		super(note_length);
		this.p_0           = new Point(pos_x, pos_y);	// �I���n�_
	}
	public SingleNote(int note_length, int pos_x, int pos_y,
			          BufferedImage Target, boolean isTargetRotate,
			          BufferedImage Line, boolean isLineRotate,
			          BufferedImage LineEnd, boolean isLineEndRotate,
			          BufferedImage ActiveLine, boolean isActiveLineRotate,
			          BufferedImage ActiveLineEnd, boolean isActiveLineEndRotate) throws IOException
	{
		this(note_length, pos_x, pos_y);								// �m�[�g�����F 4��������������
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
	// �����̋O��
	// ���� �`��̈�I�u�W�F�N�g(BufferedImage), ����(�P�ʎ��ԁAgetDirection()����), ��������(4�������̌��A���S�W�J���)
	// �߂�l �`��̈�I�u�W�F�N�g(�����Ɠ���)
	public BufferedImage makeStaffImage(BufferedImage canvas, double t, double staff_len)
	{
		double note_len_fadein_time   = -getLineDelayLength() - getLineInitLength();			// �����̓W�J�J�n����
		double note_len_start_time    = -getLineDelayLength();									// �����̓W�J��������
		// nop : 0																				// �ŏ��̉�����@���^�C�~���O(makeScoreImage()�Ɠ����)
		double note_len_end_time      = staff_len * getTimePerScore() -getLineDelayLength();	// �����̎����J�n����(�Ō�̉������o�����鎞��)
		double note_len_fadeout_time  = note_len_end_time + getLineDelayLength();				// �����̎�����������
		if ((note_len_fadein_time <= t) && (t < note_len_start_time)) // �����̋O�Ղ̓W�J
		{
			makeLineNoteImage(canvas, Target, isTargetRotate, Line, true, LineEnd, true, p_0, 0, (int)((t - note_len_fadein_time) * 2));
		}
		else if ((note_len_start_time <= t) && (t < note_len_end_time)) // �����̋O�Ղ̕ێ�
		{
			makeLineNoteImage(canvas, Target, isTargetRotate, Line, true, LineEnd, true, p_0, 0, (int)getLineDelayLength());
		}
		else if ((note_len_end_time <= t) && (t < note_len_fadeout_time)) // �����̋O�Ղ̎���
		{
			makeLineNoteImage(canvas, Target, isTargetRotate, Line, true, LineEnd, true, p_0, 0, (int)(note_len_fadeout_time - t));
		}
		return canvas;
	}
	// ����
	// ���� �`��̈�I�u�W�F�N�g(BufferedImage), ����(�P�ʎ��ԁAgetDirection()����), ����������(4�������̌�)
	// �߂�l �`��̈�I�u�W�F�N�g(�����Ɠ���)
	public BufferedImage makeScoreImage(BufferedImage canvas,
			                            BufferedImage ActiveNote, boolean is_note_rotatable,
			                            double t, double push_len)
	{
		double note_start_time   = -getLineInitLength() * 2;
		// nop : 0 = �������ڕW�n�_�ɗ����^�C�~���O
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
		// �`�恗�ڑ���(�摜�͌�납��d�˂Ă���)
		for (int t = 0; t < t_len; t++) {
			makeSingleNoteImage(canvas, LineNote, is_line_note_rotatable, p_0, t_end - t);
		}
		// �`�恗�I�_
		makeSingleNoteImage(canvas, EndNote,   is_end_note_rotatable, p_0, t_end - t_len);
		// �`�恗�n�_
		makeSingleNoteImage(canvas, StartNote, is_start_note_rotatable, p_0, t_end);
		return canvas;
	}
	public BufferedImage makeSingleNoteImage(BufferedImage canvas, BufferedImage Note, boolean is_note_rotatable, Point p_0, int t_current)
	{
		AffineTransform op = new AffineTransform();
		// ����2:���s�ړ� (Affine�ϊ��s��͌�̑�����ɏ����d�l�炵��)
		op.translate((getPosition(t_current, p_0).x - Note.getWidth()/2),
		             (getPosition(t_current, p_0).y - Note.getHeight()/2));
		// ����1:��]�ړ�
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
