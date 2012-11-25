package com.github.ShinyFestaOriginalNotes;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// Generate RGBA Video file(.avi without coding)
// See http://www.usagi-goten.com/jp/AVI_structure.html, http://msdn.microsoft.com/ja-jp/library/cc352264.aspx
public class AviVideoGenerator extends ShinyFestaOriginalNotes {
	String           filename;
	FileOutputStream outfile;
	long avi_section_size  = 0x40000000;	// RIFF AVI/AVIXセクションのサイズ。1GBに設定。
	int movi_start_offset  = 0x2000; 	// LIST moviの最初のチャンクの「データ領域」の位置。
	int max_riff_section_count = 0x100; // RIFF AVI/AVIX最大数。とりあえず256セクションと設定。
	int view_size = 4 * width * height;
	int view_size_with_header = view_size + 8 + 8;	// 画像サイズ + {"00db" chunk header} + {"ix00" list size}
	int frame_count_align = (int)((avi_section_size - movi_start_offset - 32) / view_size_with_header);	//RIFFセクション１つあたりのフレーム数。{-32}はix00セクションのサイズ
	int frame_count = 1;	// 出力動画フレーム数
	int frame_count_written = 0; // 出力済み動画フレーム数
	public AviVideoGenerator(String filename) throws IOException
	{
		this.filename = filename;
		this.outfile  = new FileOutputStream(filename);
	}
	// library
	public void LongToByteArray(OutputStream stream, long write_data) throws IOException
	{
		stream.write((byte)((write_data      ) & 0xFF));
		stream.write((byte)((write_data >> 8 ) & 0xFF));
		stream.write((byte)((write_data >> 16) & 0xFF));
		stream.write((byte)((write_data >> 24) & 0xFF));
		stream.write((byte)((write_data >> 32) & 0xFF));
		stream.write((byte)((write_data >> 40) & 0xFF));
		stream.write((byte)((write_data >> 48) & 0xFF));
		stream.write((byte)((write_data >> 56) & 0xFF));
		return;
	}
	public void IntToByteArray(OutputStream stream, int write_data) throws IOException
	{
		stream.write((byte)((write_data      ) & 0xFF));
		stream.write((byte)((write_data >> 8 ) & 0xFF));
		stream.write((byte)((write_data >> 16) & 0xFF));
		stream.write((byte)((write_data >> 24) & 0xFF));
		return;
	}
	public void ShortToByteArray(OutputStream stream, short write_data) throws IOException
	{
		stream.write((byte)((write_data      ) & 0xFF));
		stream.write((byte)((write_data >> 8 ) & 0xFF));
		return;
	}
	// AVIMAINHEADER
	// See http://msdn.microsoft.com/ja-jp/library/cc352261.aspx
	private void getAviMainHeaderData(OutputStream stream, int frame_count_of_this_section) throws IOException
	{
		stream.write("avih".getBytes());						// fcc
		IntToByteArray(stream, 4 * 14);							// cb(avih header size)
		IntToByteArray(stream, (int)(1000000 / frame_rate));	// dwMicroSecPerFrame
		IntToByteArray(stream, view_size * frame_rate);			// dwMaxBytesPerSec 
		IntToByteArray(stream, 0);								// dwPaddingGranularity
		IntToByteArray(stream, 0x0810);							// dwFlags [AVIF_HASINDEX:ON]idx1セクションをつける。  [AVIF_TRUSTCKTYPE:ON]OpenDML準拠
		IntToByteArray(stream, frame_count_of_this_section);	// dwTotalFrames
		IntToByteArray(stream, 0);								// dwInitialFrames : obsoleted
		IntToByteArray(stream, 1);								// dwStreams(ex. 1: only Video, 2: 1 Video and 1 Audio) 
		IntToByteArray(stream, view_size);						// dwSuggestedBufferSize
		IntToByteArray(stream, width);							// dwWidth
		IntToByteArray(stream, height);							// dwHeight
		IntToByteArray(stream, 0);								// dwReserved[4]
		IntToByteArray(stream, 0);
		IntToByteArray(stream, 0);
		IntToByteArray(stream, 0);
		return;
	}
	private void getAviStreamHeaderData(OutputStream stream) throws IOException
	{
		stream.write("strh".getBytes());				// fcc
		IntToByteArray(stream, 4 * 13 + 4);		// cb(strh header size)
		stream.write("vids".getBytes());				// fccType
		stream.write("DIB ".getBytes());				// fccHandler
		IntToByteArray(stream, 0);				// dwFlags
		IntToByteArray(stream, 0);				// dwPriority(wPriority/wLanguage)
		IntToByteArray(stream, 0);				// dwInitialFrames
		IntToByteArray(stream, 1);				// dwScale
		IntToByteArray(stream, frame_rate);		// dwRate
		IntToByteArray(stream, 0);				// dwStart
		//	IntToByteArray(stream, frame_count_of_this_section);		// dwLength
		IntToByteArray(stream, frame_count);	// dwLength
		IntToByteArray(stream, view_size);		// dwSuggestedBufferSize
		IntToByteArray(stream, 0);				// dwQuality
		IntToByteArray(stream, 0);				// dwSampleSize
		ShortToByteArray(stream, (short)0);		// rcFrame.left
		ShortToByteArray(stream, (short)0);		// rcFrame.top
		ShortToByteArray(stream, (short)width);	// rcFrame.right
		ShortToByteArray(stream, (short)height);	// rcFrame.bottom
		return;
	}
	private void getAviStreamFormatData(OutputStream stream) throws IOException
	{
		stream.write("strf".getBytes());		// fcc
		IntToByteArray(stream, 4 * 9 + 2 * 2);	// cb(strf header size)
		IntToByteArray(stream, 4 * 9 + 2 * 2);	// biSize(strf header size)
		IntToByteArray(stream, width);			// biWidth
		IntToByteArray(stream, height);			// biHeight
		ShortToByteArray(stream, (short)1);		// biPlanes
		ShortToByteArray(stream, (short)0x20);	// biBitCount
		IntToByteArray(stream, 0);				// biCompression
		IntToByteArray(stream, view_size);		// biSizeImage
		IntToByteArray(stream, 0);				// biXPelsPerMeter
		IntToByteArray(stream, 0);				// biYPelsPerMeter
		IntToByteArray(stream, 0);				// biClrUsed
		IntToByteArray(stream, 0);				// biClrImportant
		return;
	}
	private void getAviSuperIndexAIndexData(OutputStream stream, int riff_section_index, int frame_count_of_this_index) throws IOException
	{
		long first_standard_index = (long)((movi_start_offset - 4) + ((view_size + 8) * frame_count_of_this_index) - 4);
		LongToByteArray(stream, avi_section_size * riff_section_index + first_standard_index);	// aIndex[n-1].qwBaseOffset  Standard Indexの位置
		IntToByteArray(stream, 8 * frame_count_of_this_index + 24);								// aIndex[n-1].dwSize Standard Indexのサイズ
		IntToByteArray(stream, frame_count_of_this_index);										// aIndex[n-1].dwDuration フレーム数		
	}	
	private void getAviSuperIndexData(OutputStream stream) throws IOException
	{
		// 準備： nEntriesInUse算出
		int total_riff_sections = (int)((frame_count + 1) / (frame_count_align)) + 1;
		if (total_riff_sections >= max_riff_section_count)
		{
			throw new IOException("too large output avi size(too many RIFF section for me). you=[" + total_riff_sections + "] max=[255]");
		}
		// 書き込み
		stream.write("indx".getBytes());				// fcc
		IntToByteArray(stream, 4*5 + 2 + 1*2 + 16*256);	// cb(strf header size)
		ShortToByteArray(stream, (short)4);				// wLongsPerEntry
		stream.write((byte)0x00);						// bIndexSubType: 0 (target standard index: AVISTDINDEX)
		stream.write((byte)0x00);						// bIndexType: AVI_INDEX_OF_INDEXES (this section as Super Index)
		IntToByteArray(stream, total_riff_sections);	// nEntriesInUse (size of aIndex[])
		stream.write("00db".getBytes());				// dwChunkId
		IntToByteArray(stream, 0);						// dwReserved
		IntToByteArray(stream, 0);						// dwReserved
		IntToByteArray(stream, 0);						// dwReserved
		for (int i = 0; i < total_riff_sections - 1; i++)
		{
			getAviSuperIndexAIndexData(stream, i, frame_count_align);
		}
		getAviSuperIndexAIndexData(stream, total_riff_sections - 1, (frame_count % frame_count_align));
		for (int i = total_riff_sections; i < 256; i++)	// Padding (aviutlを参照。不正な処理がない限り解釈できないはずのデータだが……）
		{
			LongToByteArray(stream, 0);
			IntToByteArray(stream, 0);
			IntToByteArray(stream, 0);
		}
		return;
	}
	private void getAviStreamIndexData(OutputStream stream, int frame_count_of_this_section, boolean need_avi1section) throws IOException
	{
		// "ix00" セクション
		// 準備
		long qwBaseOffset = avi_section_size * ((int)((frame_count_written - frame_count_of_this_section) / (frame_count_align))) + movi_start_offset;
		// 書き込み
		stream.write("ix00".getBytes());								// fcc
		IntToByteArray(stream, 8 * frame_count_of_this_section + 24);	// cb(strf header size)
		ShortToByteArray(stream, (short)2);								// wLongsPerEntry
		stream.write((byte)0);											// bIndexSubType: 0
		stream.write((byte)0x01);										// bIndexType: AVI_INDEX_OF_CHUNKS (this section as Standard Index)
		IntToByteArray(stream, frame_count_of_this_section);			// nEntriesInUse (size of aIndex[])
		stream.write("00db".getBytes());								// dwChunkId
		LongToByteArray(stream, qwBaseOffset);							// qwBaseOffset
		IntToByteArray(stream, 0);										// dwReserved
		for (int i = 0; i < frame_count_of_this_section; i++) {
			IntToByteArray(stream, (view_size + 8) * i);				//aIndex[i].dwOffset データ領域の場所(qwBaseOffsetからの場所)
			IntToByteArray(stream, view_size);							// aIndex[i].dwSize データ領域のサイズ
		}
		// "idx1" セクション
		if (need_avi1section == true)
		{
			stream.write("idx1".getBytes());
			int write_data = 16 * frame_count_of_this_section;
			IntToByteArray(stream, write_data);
			for (int i = 0; i < frame_count_of_this_section; i++)
			{
				stream.write("00db".getBytes());
				IntToByteArray(stream, 0x10);					// set AVIIF_KEYFRAME:ON
				IntToByteArray(stream, (view_size + 8) * i + 4);	// Offset of Data Chunk
				IntToByteArray(stream, view_size);				// Size of Data Chunk
			}
		}
		return;
	}
	// see http://vm104.xen.klab.org/doc/avifileformat.html
	public byte[] getRIFFheader() throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		// RIFF-AVI/AVIX List
		stream.write("RIFF".getBytes());																				// RIFF header
		int frame_count_of_this_section;
		if ((frame_count - frame_count_written) > frame_count_align)	// LAST RIFF section
		{
			frame_count_of_this_section = frame_count_align;
			IntToByteArray(stream, (int)(avi_section_size - 8));
		}
		else
		{
			frame_count_of_this_section = frame_count - frame_count_written;
			if (frame_count_written == 0)
			{
				IntToByteArray(stream, (view_size_with_header + 16) * frame_count_of_this_section + 36 + 8 + 8 + (movi_start_offset - 40 + 8) + 4);	// RIFF header size
			}
			else
			{
				IntToByteArray(stream, view_size_with_header * frame_count_of_this_section + 36 + 8 + (movi_start_offset - 40 + 8) + 4);	// RIFF header size
			}
		}
		if (frame_count_written == 0) {
			stream.write("AVI ".getBytes());																				// RIFF header AVI  FourCC
			// LIST-hdrl List
			stream.write("LIST".getBytes());	// LIST header
			IntToByteArray(stream, 4588);		// LIST header size
			stream.write("hdrl".getBytes());	// LIST header hdrl FourCC
				getAviMainHeaderData(stream, frame_count_of_this_section);	// "avih" chunk
				// LIST-strl header
				stream.write("LIST".getBytes());	// LIST header
				IntToByteArray(stream, 4244);		// LIST header size
				stream.write("strl".getBytes());	// LIST header strl FourCC
					getAviStreamHeaderData(stream);	// "strh" chunk
					getAviStreamFormatData(stream);								// "strf" chunk
					getAviSuperIndexData(stream);								// "indx" chunk
				// LIST-odml header and dmlh chunk
				stream.write("LIST".getBytes());	// LIST header
				IntToByteArray(stream, 260);	// LIST header size
				stream.write("odml".getBytes());	// LIST header odml FourCC
				stream.write("dmlh".getBytes());	// dmlh chunk
				IntToByteArray(stream, 248);	// dmlh chunk size
				IntToByteArray(stream, frame_count);
				stream.write(new byte[244]);
			// JUNK header
			stream.write("JUNK".getBytes());
			int write_data = movi_start_offset - 4616 - 20;
			IntToByteArray(stream, write_data);
			stream.write(new byte[write_data]);
		} else {
			stream.write("AVIX".getBytes());																				// RIFF header AVIX FourCC
			stream.write("JUNK".getBytes());
			int write_data = movi_start_offset - 40;
			IntToByteArray(stream, write_data);
			stream.write(new byte[write_data]);
		}
		// LIST header
		stream.write("LIST".getBytes());
		IntToByteArray(stream, view_size_with_header * frame_count_of_this_section + 36); // add: {"movi"(4) + "ix01"(32)}
		stream.write("movi".getBytes());
		// write LIST-movi to return addImage() with new frame image
		return stream.toByteArray();
	}
	public void open(int frame_count) throws IOException
	{
		this.frame_count = frame_count;
	}
	public void addImage(BufferedImage image) throws IOException
	{
		if ((frame_count_written % frame_count_align) == 0)
		{
			outfile.write(getRIFFheader());							// current "RIFF" header
		}
		outfile.write("00db".getBytes());
		IntToByteArray(outfile, view_size);
		byte[] write_data = new byte[width * height * 4];
		for (int i = 0; i < write_data.length; i = i + 4)
		{
			int rgblist = image.getRGB((int)((i/4) % width), (int)(height - 1 - ((i/4) / width)));

			write_data[i]   = (byte)( rgblist        & 0xFF); // Red
			write_data[i+1] = (byte)((rgblist >>  8) & 0xFF); // Green
			write_data[i+2] = (byte)((rgblist >> 16) & 0xFF); // Blue
			write_data[i+3] = (byte)((rgblist >> 24) & 0xFF); // Alpha-Channel
		}
		outfile.write(write_data);
		frame_count_written = frame_count_written + 1;
		if ((frame_count_written % frame_count_align) == 0)
		{
			if(frame_count_written < frame_count)	// JUNK chunk
			{
				int junk_padding;
				boolean need_avi1section;
				if (frame_count_written == frame_count_align)
				{
					junk_padding = (int)(avi_section_size - (movi_start_offset - 8 + (view_size_with_header * frame_count_align + 32 + 8) + (16 * frame_count_align + 8)));
					need_avi1section = true;
				}
				else
				{
					junk_padding = (int)(avi_section_size - (movi_start_offset - 8 + (view_size_with_header * frame_count_align + 32 + 8)));
					need_avi1section = false;
				}
				getAviStreamIndexData(outfile, frame_count_align, need_avi1section);	// "ix00" chunk
				outfile.write("JUNK".getBytes());
				IntToByteArray(outfile, junk_padding);
				outfile.write(new byte[junk_padding]);
			}
			// 2GBの壁対策に、ファイル出力バッファflushのためクローズ→再オープンする
			outfile.close();
			outfile = null;
			outfile = new FileOutputStream(filename, true);	// append mode
		}
	}
	public void close() throws IOException
	{
		boolean need_avi1section;
		if (frame_count_written <= frame_count_align)
		{
			need_avi1section = true;
		}
		else
		{
			need_avi1section = false;
		}
		getAviStreamIndexData(outfile, (frame_count_written % frame_count_align), need_avi1section);	// "indx" chunk
		outfile.close();
		outfile = null;
	}
}
