package com.iteration.oldvlc.components;
import java.util.Vector;

public class Decoder {
	// clk��ϵͳ�б���Ӳ��ʹ�õ�Ƶ��
	// data��MIC������������
	// fs���ֻ�������õĲ���Ƶ��
	// samples��ʾÿ������������ȫ�����������ܲ������ĵ�ĸ���
	private int clk = 8000;
	private int[] data;
	private int fs;
	private int samples;

	// ���캯��
	Decoder(int[] a, int b) {
		data = a;
		fs = b;
		samples = fs / clk;
	}

	// ��������ݾ�ֵ�������о��ߵ͵�ƽ
	private int avg(int[] data) {
		int length = data.length;
		int sum = 0;
		for (int i = 0; i < length; i++) {
			sum = sum + data[i];
		}
		sum = sum / length;
		return sum;
	}

	// ����ID
	public int getID() {
		int id = decoder();
		return id;
	}

	// �������庯��
	public int decoder() {
		// ���ݾ�ֵ��ֵ�����γɸߵ͵�ƽ
		int avg = avg(data);
		for (int i = 0; i < data.length; i++) {
			if (data[i] >= avg)
				data[i] = 0;
			else
				data[i] = 1;
		}
		for (int i = 0; i < data.length; i++) {
//			System.out.println(data[i]);
		}
		// �ó���ƽ���������λ�ã�Machester���е�ƽ���������1bit����
		Vector<Integer> pos = new Vector<Integer>();
		for (int i = 0; i < data.length - 1; i++) {
			if (data[i] != data[i + 1])
				pos.add(i);
		}
		// �����������������λ�ã������м�����ݵó�ʵ�ʷ��͵�����
		// ��������൱�ڽ�����
		Vector<Integer> v = new Vector<Integer>();
		int flag = data[0];
		for (int i = 0; i < pos.size(); i++) {
			if (i == 0) {
				if (pos.elementAt(i) >= samples + 1) {
					v.add(flag);
					v.add(flag);
				} else
					v.add(flag);
			} else {
				if (pos.elementAt(i) - pos.elementAt(i - 1) >= samples + 3) {
					v.add(flag);
					v.add(flag);
				} else if (pos.elementAt(i) - pos.elementAt(i - 1) >= 2)
					v.add(flag);
				else
					return 0;
			}
			if (flag == 1)
				flag = 0;
			else
				flag = 1;
		}
		for (int i = 0; i < v.size(); i = i + 1) {
//			System.out.println(v.elementAt(i));
		}
		// �õ��������ݺ󣬸���Manchester���������н���
		// ���ǵ��������ݿ��ܽ�ĳbit���ݲ��ˣ������Ҫ�ж�ֱ�ӽ���ʹ�λ�����������Σ���flag1��ʾ
		int len = (int) v.size() / 2;
		int[] a = new int[len];
		boolean flag1 = true;
		if (flag1) {
			for (int i = 0; i < v.size() - 2; i = i + 2) {
				if (v.elementAt(i) == 0 && v.elementAt(i + 1) == 1)
					a[i / 2] = 1;
				else if (v.elementAt(i) == 1 && v.elementAt(i + 1) == 0)
					a[i / 2] = 0;
				else {
					if (i < 80)
						flag1 = false;
					break;
				}
//				System.out.println(i);
			}
		}
		if (!flag1) {
			for (int i = 1; i < v.size() - 2; i = i + 2) {
				if (v.elementAt(i) == 0 && v.elementAt(i + 1) == 1)
					a[(i - 1) / 2] = 1;
				else if (v.elementAt(i) == 1 && v.elementAt(i + 1) == 0)
					a[(i - 1) / 2] = 0;
				else {
					return 0;
				}
			}
		}

		// �����Ѱ��֡ͷ8��0��֡β8��1�������õ�8λID
		int head = len;
		for (int i = 0; i < len - 25; i++) {
			if (sum(a, i, 8) == 0 && sum(a, i + 16, 8) == 8) {
				head = i;
				break;
			}
		}
		if (head == len)// δ�ҵ�֡ͷ return 0
			return 0;
		int[] IDArray = new int[8];// IDArray�������8λID
		for (int i = 0; i < 8; i++) {
			IDArray[i] = a[head + 8 + i];
		}
		// ������תʮ����
		int id = 128 * IDArray[0] + 64 * IDArray[1] + 32 * IDArray[2] + 16 * IDArray[3] +
				 8 * IDArray[4] + 4 * IDArray[5] + 2 * IDArray[6] + IDArray[7];
		return id;
	}

	// һ����ͺ����������ж�֡ͷ֡β
	private int sum(int[] a, int h, int t) {
		int sum = 0;
		for (int i = h; i < h + t; i++) {
			sum = sum + a[i];
		}
		return sum;
	}
}