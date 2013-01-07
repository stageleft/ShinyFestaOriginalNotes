package com.github.ShinyFestaOriginalNotes;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
//import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class AllNotes extends ShinyFestaOriginalNotes {
	String inputScoreFileName;
	String outputAviFileName;
	HashMap<String, Boolean>       isImageRotatedList;
	HashMap<String, BufferedImage> imageList;
	HashMap<String, SingleNote>    scoreList;
	HashMap<String, NoteOperator>  noteList;
	double bpm;         // 曲の速さ
	double x_speed;
	//
	int current_score_progress;
	int all_score_progress;
	//
	double parse_note_offset = 0;
	public AllNotes(String inputScoreFileName, String outputAviFileName) throws IOException
	{
		this.bpm = 138;
		this.x_speed =  1.0;
		this.inputScoreFileName = inputScoreFileName;
		this.outputAviFileName  = outputAviFileName;
	}
	public HashMap<String, NoteOperator> parseScoreFile(String inputScoreFileName) throws IOException
	{
		imageList          = new HashMap<String, BufferedImage>();
		isImageRotatedList = new HashMap<String, Boolean>();
		noteList           = new HashMap<String, NoteOperator>();

		BufferedReader score = new BufferedReader(new FileReader(new File(inputScoreFileName)));
		String line;
		while((line = score.readLine()) != null)
		{
			String[] text = line.split(" *# *");
			if ((text.length >= 1) && (text[0] != ""))
			{
				if (text[0].indexOf("bpm ") == 0)
				{
					bpm     = Double.valueOf(text[0].replaceFirst("bpm *",""));
				}
				else if (text[0].indexOf("high_speed ") == 0)
				{
					x_speed = Double.valueOf(text[0].replaceFirst("high_speed *",""));					
				}
				else if (text[0].indexOf("image ") == 0)
				{
					/*
					# image setup
					# command:
					#  image image_ID; image_file_name, isRotate
					 */
					String[] parameters = text[0].substring("image ".length()).split(" *; *");
					imageList.put(parameters[0], ImageIO.read(new File(parameters[1].split(" *, *")[0])));
					isImageRotatedList.put(parameters[0], ((parameters[1].split(" *, *")[1].compareTo("true") == 0) ? true: false));
				}
				else if (text[0].indexOf("score ") == 0)
				{
					String[] parameters = text[0].substring("score ".length()).split(" *; *");
					/*
					#  score score_ID; target_parameters; base_line_image; long_score_image; score_description
					#    score_ID       as String
					#    target_parameters as String:
					#      target_image,position_x,position_y
					#        targt_image as image_ID(String)
					#        position_x  as integer(0 to width_of_screen)
					#        position_y  as integer(0 to height_of_screen)
					#    base_line_image as String:
					#      base_line,base_line_end
					#        base_line      as image_ID(String)
					#        base_line_end  as image_ID(String)
					#    long_score_image as String:
					#      long_score,long_score_end
					#        long_score     as image_ID(String)
					#        long_score_end as image_ID(String)
					#    score_description as String:
					#        line_species:parameters,[line_species:parameters,...,line_species:parameters],Direction
					*/
					String Target = parameters[1].split(" *, *")[0];
					int pos_x = Integer.valueOf(parameters[1].split(" *, *")[1]);
					int pos_y = Integer.valueOf(parameters[1].split(" *, *")[2]);
					String BaseLine     = parameters[2].split(" *, *")[0];
					String BaseLineEnd  = parameters[2].split(" *, *")[1];
					String LongScoreLine     = parameters[3].split(" *, *")[0];
					String LongScoreLineEnd  = parameters[3].split(" *, *")[1];
					String[] lines_params = parameters[4].split(" *, *");
					SingleNote tmp_op = new SingleNote(lines_params.length - 1,
	                                                   pos_x, pos_y,
							                           imageList.get(Target),           isImageRotatedList.get(Target),
							                           imageList.get(BaseLine),         isImageRotatedList.get(BaseLine),
							                           imageList.get(BaseLineEnd),      isImageRotatedList.get(BaseLineEnd),
							                           imageList.get(LongScoreLine),    isImageRotatedList.get(LongScoreLine),
							                           imageList.get(LongScoreLineEnd), isImageRotatedList.get(LongScoreLineEnd));
					for (String lines : lines_params)
					{
						String            line_shapes = new String();
						ArrayList<Double> line_params = new ArrayList<Double>();
						for (String params : lines.split(" *: *"))
						{
							if (params.compareTo(lines.split(" *: *")[0]) == 0)
							{
								if(lines.split(" *: *").length == 1) // last: fix line_note(with 0 pixel)
								{
									line_shapes = "line_note";
									line_params.add(Double.valueOf(params));
								}
								else
								{
									line_shapes = params;
								}
							}
							else
							{
								line_params.add(Double.valueOf(params));
							}
						}
						tmp_op.addLineShape(line_shapes, line_params);
					}
					noteList.put(parameters[0], new NoteOperator(tmp_op));
				}
				else if (text[0].indexOf("note ") == 0)
				{
					String note_command = text[0].replaceFirst("note *","");
					/*
						#############################################
						# note                                      #
						# command:                                  #
						#  note offset,line,length,score            #
						#        offset as float(0 to end of music) #
						#        length as float(0 to end of music) #
						#        line   as score_ID(String)         #
						#        score  as image_ID(String)         #
						#############################################
						note  0.0, l_line,0  ,left_score        # [0]じゃじゃっ
					*/
					// ノートのオフセット計算
					String[] params_string = note_command.split(" *, *");
					parse_note_offset = parse_note_offset + Double.valueOf(params_string[0]);

					// ノートの処理
					if (params_string[1].compareTo("none") != 0)
					{
						double note_length = Double.valueOf(params_string[2]);
						if (note_length == 0)
						{
							note_length = 1 / getTimePerFrame();
						}
						noteList.get(params_string[1]).add_note(parse_note_offset, note_length,
								                                  imageList.get(params_string[3]), isImageRotatedList.get(params_string[3]));
					}
				}
			}
		}
		score.close();
		// 不要なscoreは削除。
		for (Iterator<String> note = noteList.keySet().iterator(); note.hasNext();)
		{
			if(noteList.get(note.next()).size() == 0)
			{
				note.remove();
			}
		}
		return noteList;
	}
	public void makeMovingNotes() throws IOException
	{
		HashMap<String, NoteOperator> operators  = parseScoreFile(inputScoreFileName);
		Double initial_note_offset = null;
		Double final_note_offset   = null;
		Double max_score_length    = null;

		for (Entry<String, NoteOperator> entry: operators.entrySet())
		{
			// 最初のノートを叩くタイミング抽出
			if ((initial_note_offset == null) || (initial_note_offset > (entry.getValue().getInitialOffset())))
			{
				initial_note_offset = new Double(entry.getValue().getInitialOffset());
				max_score_length    = new Double(entry.getValue().op.note_len * 1.5);
			}
			// 最後のノート抽出
			if ((final_note_offset == null)   || (final_note_offset   < (entry.getValue().getFinalOffset())))
			{
				final_note_offset   = new Double(entry.getValue().getFinalOffset());
			}
		}
		double allnote_length       = final_note_offset - (initial_note_offset - max_score_length);		// 全長：[4分音符個数系]
		double score_offset         = (initial_note_offset - max_score_length) * getTimePerScore();		// 最初のオフセット：[SFノート単位時間系]
		all_score_progress          = (int)(allnote_length * getTimePerFrame()) + 1;
		AviVideoGenerator outVideo = new AviVideoGenerator(outputAviFileName);
		outVideo.open((int)(all_score_progress));
		for (current_score_progress = 0; current_score_progress < all_score_progress; current_score_progress = current_score_progress + 1)	// t:[実時間フレーム数系]
		{
			double current_score_offset = current_score_progress / getTimePerFrame() * getTimePerScore();	// tを[SFノート単位時間系]に変換
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			// 音符の軌跡
			for (Entry<String, NoteOperator> entry: operators.entrySet())
			{
				entry.getValue().makeStaffImage(image, current_score_offset + score_offset);
			}
			// 音符
			for (Entry<String, NoteOperator> entry: operators.entrySet())
			{
				entry.getValue().makeScoreImage(image, current_score_offset + score_offset);
			}
			//ImageIO.write(image, "PNG", new File("D:/Pictures/tmp_m/MovingImage_"+String.format("%05d", t)+".png"));
			outVideo.addImage(image);
			image = null;
		}
		outVideo.close();
	}
	public double getTimePerFrame()	// 4分音符1個あたりのフレーム数
	{
		// bpm   = １分間に現れる四分音符の数
		// t : 音符アイコンが1dot進むための時間
		// 音符アイコンの直径=32
		//  すなわち、四分音符１個あたりtは64進むため、1フレーム(1/30min)でtは(64 * bpm / 1800)だけ進む。
		return ((60 * frame_rate) / (bpm * x_speed));
	}
	public int getProgress()
	{
		return (int)((double)current_score_progress / ((double)all_score_progress * getTimePerFrame()) * 100);
	}
}
