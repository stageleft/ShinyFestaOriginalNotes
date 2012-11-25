package com.github.ShinyFestaOriginalNotes;

import java.util.ArrayList;
import java.util.HashMap;

// getDirection()制御クラス
public class SingleNoteDirectionLibrary extends ShinyFestaOriginalNotes {
	int note_len;	// ノート自身：4分音符いくつ分か
	HashMap<String, SingleNoteDirection> note_operator;
	ArrayList<SingleNoteDirection> note_type;
	ArrayList<ArrayList<Double>>            parameters;
	// 線種定義
	interface SingleNoteDirection {
		public double getDirection(int time, ArrayList<Double> parameter);
	}
	private class LineDirection implements SingleNoteDirection {
		@Override
		public double getDirection(int time, ArrayList<Double> parameter) {
			// TODO Auto-generated method stub
			if (parameter.size() >= 1)
			{
				return (parameter.get(0) * Math.PI);
			}
			else
			{
				throw new IllegalArgumentException("too few parameter: need 1 parameter when set LINE note.");
			}
		}
	}
	private class CircleDirection implements SingleNoteDirection {
		@Override
		public double getDirection(int time, ArrayList<Double> parameter) {
			// TODO Auto-generated method stub
			if (parameter.size() >= 2)
			{
				double direction_len = getTimePerScore();
				double zero_note_direction = parameter.get(0);
				double max_note_direction  = parameter.get(1);
				return (((max_note_direction - zero_note_direction) * (time/direction_len) + zero_note_direction) * Math.PI);
			}
			else
			{
				throw new IllegalArgumentException("too few parameter: need 2 parameter when set CIRCLE note.");
			}
		}
	}
	private class HeartDirection implements SingleNoteDirection {
		@Override
		public double getDirection(int time, ArrayList<Double> parameter) {
			// TODO Auto-generated method stub
			if (parameter.size() >= 4)
			{
				double zero_note_direction = parameter.get(0);
				double max_note_direction  = parameter.get(1);
				double direction_offset    = parameter.get(2) * getTimePerScore();
				double direction_len       = parameter.get(3) * getTimePerScore();
				return (((max_note_direction - zero_note_direction) * ((time-direction_offset)/direction_len)*((time-direction_offset)/direction_len) + zero_note_direction) * Math.PI);
			}
			else
			{
				throw new IllegalArgumentException("too few parameter: need 2 parameter when set HEART note.");
			}
		}
	}
	// 関数
	public SingleNoteDirectionLibrary(int note_length)
	{
		super();
		note_len        = note_length;
		note_type       = new ArrayList<SingleNoteDirection>();
		parameters      = new ArrayList<ArrayList<Double>>();
		note_operator   = new HashMap<String, SingleNoteDirection>();
		note_operator.put("line_note",   new LineDirection());
		note_operator.put("circle_note", new CircleDirection());
		note_operator.put("heart_note",  new HeartDirection());
	}
	public double getDirection(int time)
	{
		int index  = (int)Math.min((time + (note_len * getTimePerScore())) / (int)getTimePerScore(), note_type.size());
		int offset =          (int)(time                                 ) % (int)getTimePerScore();
		if ((index < 0) || (note_type.size() <= index))
		{
			index = note_type.size() - 1;
		}
		return note_type.get(index).getDirection(offset, parameters.get(index));
	}
	public void addLineShape(String type, ArrayList<Double> params)
	{
		if (note_type.size() <= note_len)
		{
			note_type.add(note_operator.get(type));
			parameters.add(params);
		}
		else
		{
			throw new IllegalArgumentException("too many adding parameters. current size[" + note_type.size() + "], max size[" + note_len + "]");
		}
	}
}
