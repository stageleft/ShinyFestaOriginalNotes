import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AviFileParser {
	public static void main(String[] args) throws IOException
	{
		String target_file_name = "MovingImage";
		InputStream  is;
		OutputStream os;
		try
		{
			is = new FileInputStream("D:\\Videos\\"+target_file_name+".avi");
			os = new FileOutputStream("D:\\Videos\\Chunk_Structure_"+target_file_name+".txt");
			AviFileParser parser = new AviFileParser();
			parser.RiffHeaderParser(is, os, 0, 0, 0);
			System.out.println("*** Parse Completed. ***");
			os.close();
			is.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	long RiffHeaderParser(InputStream aviFileStream, OutputStream resultFileStream, long print_offset, long size, int depth) throws Exception
	{
		byte[] FourCC   = new byte[4]; 
		byte[] size_len = new byte[4];
		long read_bytes_in_this_section = 0;
		long read_bytes_in_sub_section  = 0;
		String depth_indent = new String();
		for(int i = 0; i < depth; i++)
		{
			depth_indent = depth_indent + "    ";
		}
		

		while(((size == 0) && (depth == 0)) || (read_bytes_in_this_section < size))
		{
			if(aviFileStream.available() == 0)
			{
				break;
			}
			aviFileStream.read(FourCC);
			aviFileStream.read(size_len);
			read_bytes_in_this_section = read_bytes_in_this_section + 8;
			long current_size = (((long)size_len[0])             & 0x000000FF)
					         + ((((long)size_len[1] * 256))      & 0x0000FF00)
					         + ((((long)size_len[2] * 65536))    & 0x00FF0000)
					         + ((((long)size_len[3] * 16777216)) & 0xFF000000);
			if (current_size == 0)
			{
				throw new Exception("Illegal Current Size");
			}
			if ("RIFF".compareTo(new String(FourCC)) == 0)
			{
				aviFileStream.read(FourCC);
				read_bytes_in_this_section = read_bytes_in_this_section + 4;
				String print_string = depth_indent.toString() + "RIFF type[" + new String(FourCC) + "] size[" + current_size + "] address["+ String.format("0x%08X", read_bytes_in_this_section + print_offset) + "]";
				System.out.println(print_string);
				resultFileStream.write((print_string + "\n").getBytes());
				read_bytes_in_sub_section  = RiffHeaderParser(aviFileStream, resultFileStream, read_bytes_in_this_section + print_offset, current_size - 4, depth + 1);
				read_bytes_in_this_section = read_bytes_in_this_section + read_bytes_in_sub_section;
			}
			else if ("LIST".compareTo(new String(FourCC)) == 0)
			{
				aviFileStream.read(FourCC);
				read_bytes_in_this_section = read_bytes_in_this_section + 4;
				String print_string = depth_indent.toString() + "LIST type[" + new String(FourCC) + "] size[" + current_size + "] address["+ String.format("0x%08X", read_bytes_in_this_section + print_offset) + "]";
				System.out.println(print_string);
				resultFileStream.write((print_string + "\n").getBytes());
				read_bytes_in_sub_section  = RiffHeaderParser(aviFileStream, resultFileStream, read_bytes_in_this_section + print_offset, current_size - 4, depth + 1);
				read_bytes_in_this_section = read_bytes_in_this_section + read_bytes_in_sub_section;
			}
			else
			{
				String print_string = depth_indent.toString() + "Chunk type[" + new String(FourCC) + "] size[" + current_size + "] address["+ String.format("0x%08X", read_bytes_in_this_section + print_offset) + "]";
				System.out.println(print_string);
				resultFileStream.write((print_string + "\n").getBytes());
				aviFileStream.read(new byte[(int)current_size]);
				read_bytes_in_this_section = read_bytes_in_this_section + current_size;
			}
		}
		if ((size > 0) && (read_bytes_in_this_section != size))
		{
			throw new Exception("*** ERROR: illegal size. FourCC[" + new String(FourCC) + "], setting[" + size + "], read[" + read_bytes_in_this_section + "] ***");
		}
		return read_bytes_in_this_section;
	}
}
