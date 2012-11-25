package com.github.ShinyFestaOriginalNotes;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NoteOperator extends ShinyFestaOriginalNotes {
	SingleNote op;
	Double initial_offset;
	Double final_offset;
	Double max_length;
	ArrayList<Double> note_offset;
	ArrayList<Double> note_length;
	ArrayList<BufferedImage> note_shapes;
	ArrayList<Boolean>       is_note_rotate;
	public NoteOperator(SingleNote operator) {
		this.op = operator;
		this.note_offset    = new ArrayList<Double>();
		this.note_length    = new ArrayList<Double>();
		this.note_shapes    = new ArrayList<BufferedImage>();
		this.is_note_rotate = new ArrayList<Boolean>();
		this.initial_offset = null;
		this.final_offset   = null;
		this.max_length     = null;
	}
	// offset 4分音符いくつ分かで指定。
	// length　4分音符いくつ分か で指定。
	// shapes 0:通常のノート、1以上:同時用ノート、-1未満:スペシャルノート
	public void add_note(double offset, double length, BufferedImage shapes, boolean is_rotate) {
		note_offset.add(new Double(offset));
		note_length.add(new Double(length));
		note_shapes.add(shapes);
		is_note_rotate.add(is_rotate);
		if ((initial_offset == null) || (initial_offset > (offset)))
		{
			initial_offset = new Double(offset);
		}
		if ((final_offset == null) || (final_offset < (offset + length)))
		{
			final_offset   = new Double(offset + length);
		}
		if ((max_length == null) || (max_length < (length)))
		{
			max_length = new Double(length);
		}
	}
	public void makeStaffImage(BufferedImage canvas, double time)
	{
		op.makeStaffImage(canvas,
		                  time - (initial_offset * getTimePerScore()),
				          final_offset - initial_offset);
	}
	public void makeScoreImage(BufferedImage canvas, double time)
	{
		for (int i = 0; i < this.size(); i++)
		{
			op.makeScoreImage(canvas,
			                  note_shapes.get(i), is_note_rotate.get(i),
					          time - (note_offset.get(i) * getTimePerScore()),
						      note_length.get(i));
		}
	}
	public SingleNote getOperator(){
		return op;
	}
	public double getOffset(int index){
		return note_offset.get(index);
	}
	public double getLength(int index){
		return note_length.get(index);
	}
	public BufferedImage getShapes(int index){
		return note_shapes.get(index);
	}
	public double getInitialOffset(){
		return initial_offset.doubleValue();
	}
	public double getFinalOffset(){
		return final_offset.doubleValue();
	}
	public double getMaxNoteLength(){
		return max_length.doubleValue();
	}
	public int size(){
		return note_offset.size();
	}
}