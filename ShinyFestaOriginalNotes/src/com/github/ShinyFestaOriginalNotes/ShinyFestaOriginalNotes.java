package com.github.ShinyFestaOriginalNotes;

public class ShinyFestaOriginalNotes {
	int width  = 854;		// �o�͉摜�i����j���F�o�͓���T�C�Y�ɍ��킹�܂��B
	int height = 480;		// �o�͉摜�i����j�����F�o�͓���T�C�Y�ɍ��킹�܂��B
	int frame_rate = 30;	// �o�͓���t���[�����[�g�F�o�͓���T�C�Y�ɍ��킹�܂��B
	int note_virtual_size = 33;  // �����A�C�R���̉��z�T�C�Y
	public int getTimePerScore() // 4������1�̒���
	{
		// bpm   = �P���ԂɌ����l�������̐�
		// t : �����A�C�R����1dot�i�ނ��߂̎���
		// �����A�C�R���̒��a=32
		//  ���Ȃ킿�A�l�������P������t��64�i�ނ��߁A1�t���[��(1/30min)��t��(64 * bpm / 1800)�����i�ށB
		return (int)((note_virtual_size * 4));
	}
}
