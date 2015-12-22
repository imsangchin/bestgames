package irdeto.software;
//该程序在gf(10)的域程序执行不正确,即MM!=10
//The program run in error when mm = 10 
public class Rscode {
	public static int MM; // 0<MM<=16 RS code over GF(2**MM)

	public static int KK; // KK=2*MM-2t-1 mumber of information
							// symbols
	//
	public int NN; // =2^MM-1
	public int B0;

	public boolean Init_var = false;

	private int[] Pp;
	int Alpha_to[];
	int Index_of[];
	private int A0;
	private int Gg[];

	private int modnn(int x) {
		while (x >= NN) {
			x -= NN;
			x = (x >> MM) + (x & NN);
		}
		return x;
	}

	private void CLEAR(int[] a, int n) {
		int ci;
		for (ci = (n) - 1; ci >= 0; ci--)
			(a)[ci] = 0;
	}

	private void COPY(int[] a, int[] b, int n) {
		int ci;
		for (ci = (n) - 1; ci >= 0; ci--)
			(a)[ci] = (b)[ci];
	}

	void down_array(int[] a, int n) {
		int ci;
		for (ci = (n) - 1; ci >= 0; ci--)
			a[ci + 1] = a[ci];
	}

	private int encode_rs(int[] data, int[] bb) {
		int i, j;
		int feedback;

		CLEAR(bb, NN - KK);
		for (i = 0; i <= KK - 1; i++) {
			feedback = Index_of[(int) (data[i] ^ bb[0])];
			if (feedback != A0) {
				for (j = 1; j <= NN - KK - 1; j++)
					if (Gg[j] != A0)
						bb[j - 1] = bb[j] ^ Alpha_to[modnn(Gg[j] + feedback)];
					else
						bb[j - 1] = bb[j];
				bb[NN - KK - 1] = Alpha_to[modnn(Gg[NN - KK] + feedback)];
			} else {
				for (j = 1; j < NN - KK; j++)
					bb[j - 1] = bb[j];
				bb[NN - KK - 1] = 0;
			}
		}
		return 0;
	}

	void generate_gf() {
		int i, mask;

		mask = 1;
		Alpha_to[MM] = 0;
		for (i = 0; i < MM; i++) {
			Alpha_to[i] = mask;
			Index_of[Alpha_to[i]] = i;
			if (Pp[i] != 0)
				Alpha_to[MM] ^= mask;
			mask <<= 1;
		}
		Index_of[Alpha_to[MM]] = MM;
		mask >>= 1;
		for (i = MM + 1; i < NN; i++) {
			if (Alpha_to[i - 1] >= mask)
				Alpha_to[i] = Alpha_to[MM] ^ ((Alpha_to[i - 1] ^ mask) << 1);
			else
				Alpha_to[i] = Alpha_to[i - 1] << 1;
			Index_of[Alpha_to[i]] = i;

		}
		Index_of[0] = A0;
		Alpha_to[NN] = 0;
	}

	void gen_poly() {
		int i, j;
		Gg[0] = Alpha_to[modnn(11 * B0)];
		Gg[1] = 1;
		for (i = 2; i <= NN - KK; i++) {
			Gg[i] = 1;

			for (j = i - 1; j > 0; j--)
				if (Gg[j] != 0)
					Gg[j] = Gg[j - 1]
							^ Alpha_to[modnn((Index_of[Gg[j]]) + 11
									* (B0 + i - 1))];
				else
					Gg[j] = Gg[j - 1];
			Gg[0] = Alpha_to[modnn((Index_of[Gg[0]]) + 11 * (B0 + i - 1))];
		}

		for (i = 0; i <= NN - KK; i++) {
			Gg[i] = Index_of[Gg[i]];
		}
	}

	void init_rs() {
		generate_gf();
		gen_poly();
	}

	public void encode(int[] data) {
		if (!Init_var) {
			init_rs();
			Init_var = true;
		}
		int[] b = new int[NN - KK];
		for (int i = KK; i < NN; i++)
			b[i - KK] = data[i];
		encode_rs(data, b); // RS encodeing
		for (int i = KK; i < NN; i++)
			data[i] = b[i - KK];
	}

	int min(int deg_lambda, int k) {
		if (deg_lambda < k)
			return deg_lambda;
		return k;
	}

	int eras_dec_rs(int[] data, int[] eras_pos, int no_eras) {
		int deg_lambda, el, deg_omega;
		int i, j, r;
		int u, q, tmp, num1, num2, den, discr_r;
		int[] recd = new int[NN];
		int[] lambda = new int[NN - KK + 1];
		int[] s = new int[NN - KK + 1];
		int[] b = new int[NN - KK + 1];
		int[] t = new int[NN - KK + 1];
		int[] omega = new int[NN - KK + 1];
		int[] root = new int[NN - KK];
		int[] reg = new int[NN - KK + 1];
		int[] loc = new int[NN - KK];
		int syn_error, count;
		// string msg,temp;
		boolean bCorrect = false;
		for (i = NN - 1; i >= 0; i--) {
			recd[i] = Index_of[(int) data[i]];
		}

		syn_error = 0;
		for (i = 1; i <= NN - KK; i++) {
			tmp = 0;
			for (j = 0; j < NN; j++)
				if (recd[j] != A0)
					tmp ^= Alpha_to[modnn(recd[j] + 11 * (B0 + i - 1) * j)];
			syn_error |= tmp;
			s[i] = Index_of[tmp];
		}

		if (syn_error == 0) {
			return 0;
		}
		// CLEAR(&lambda[1],NN-KK);
		for (int ci = NN - KK - 1; ci >= 0; ci--)
			lambda[ci + 1] = 0;
		lambda[0] = 1;
		if (no_eras > 0) {

			lambda[1] = Alpha_to[eras_pos[0]];
			for (i = 1; i < no_eras; i++) {
				u = eras_pos[i];
				for (j = i + 1; j > 0; j--) {
					tmp = Index_of[lambda[j - 1]];
					if (tmp != A0)
						lambda[j] ^= Alpha_to[modnn(u + tmp)];
				}
			}
		}
		for (i = 0; i < NN - KK + 1; i++)
			b[i] = Index_of[lambda[i]];

		r = no_eras;
		el = no_eras;

		while (++r <= NN - KK) {
			discr_r = 0;
			for (i = 0; i < r; i++) {
				if ((lambda[i] != 0) && (s[r - i] != A0)) {
					discr_r ^= Alpha_to[modnn(Index_of[lambda[i]] + s[r - i])];
				}
			}
			discr_r = Index_of[discr_r];
			if (discr_r == A0) {
				down_array(b, NN - KK);
				b[0] = A0;
			} else {
				t[0] = lambda[0];
				for (i = 0; i < NN - KK; i++) {
					if (b[i] != A0)
						t[i + 1] = lambda[i + 1]
								^ Alpha_to[modnn(discr_r + b[i])];
					else
						t[i + 1] = lambda[i + 1];
				}
				if (2 * el <= r + no_eras - 1) {
					el = r + no_eras - el;
					for (i = 0; i <= NN - KK; i++)
						b[i] = (lambda[i] == 0) ? A0
								: modnn(Index_of[lambda[i]] - discr_r + NN);
				} else {
					down_array(b, NN - KK);
					b[0] = A0;
				}
				COPY(lambda, t, NN - KK + 1);
			}
		}// while
		deg_lambda = 0;
		for (i = 0; i < NN - KK + 1; i++) {
			lambda[i] = Index_of[lambda[i]];
			if (lambda[i] != A0)
				deg_lambda = i;
		}

		// COPY(reg[1],lambda[1],NN-KK);
		for (int ci = NN - KK - 1; ci >= 0; ci--)
			reg[ci + 1] = lambda[ci + 1];
		count = 0;
		for (i = 1; i <= NN; i++) {
			q = 1;
			for (j = deg_lambda; j > 0; j--)
				if (reg[j] != A0) {
					reg[j] = modnn(reg[j] + 11 * j);
					q ^= Alpha_to[reg[j]];
				}
			if (q == 0) {
				root[count] = i;
				loc[count] = NN - i;
				count++;
			}
		}

		if (deg_lambda != count) {
			return -1;
		}
		deg_omega = 0;
		for (i = 0; i < NN - KK; i++) {
			tmp = 0;
			j = (deg_lambda < i) ? deg_lambda : i;
			for (; j >= 0; j--) {
				if ((s[i + 1 - j] != A0) && (lambda[j] != A0))
					tmp ^= Alpha_to[modnn(s[i + 1 - j] + lambda[j])];
			}
			if (tmp != 0)
				deg_omega = i;
			omega[i] = Index_of[tmp];
		}
		omega[NN - KK] = A0;

		for (j = count - 1; j >= 0; j--) {
			num1 = 0;
			for (i = deg_omega; i >= 0; i--) {
				if (omega[i] != A0)
					num1 ^= Alpha_to[modnn(omega[i] + 11 * i * root[j])];
			}
			num2 = Alpha_to[modnn(root[j] * 11 * (B0 - 1) + NN)];
			den = 0;

			for (i = min(deg_lambda, NN - KK - 1) & ~1; i >= 0; i -= 2) {
				if (lambda[i + 1] != A0)
					den ^= Alpha_to[modnn(lambda[i + 1] + 11 * i * root[j])];
			}
			if (den == 0) {

				return -1;
			}
			if (num1 != 0) {
				bCorrect = true;
				data[loc[j]] ^= Alpha_to[modnn(Index_of[num1] + Index_of[num2]
						+ NN - Index_of[den])];
			}
		}
		if (bCorrect) {

		}
		return count;

	}

	public int decode(int[] data) {
		// if (!Init_var) {
		init_rs();
		// Init_var = true;
		// }
		int[] eras_pos = new int[NN - KK];
		int no_eras = 0;
		for (int i = 0; i < NN - KK; i++)
			eras_pos[i] = 0;
		int err_count = eras_dec_rs(data, eras_pos, no_eras); // RS decoding

		return err_count;
	}

	public void init(int n, int m, int max_length) {
		// init the number n(rscode length),m is the power
		NN = n;
		MM = m;

		if (MM == 2) // admittedly silly*/
			Pp = new int[] { 1, 1, 1 };
		else if (MM == 3)
			Pp = new int[] { 1, 1, 0, 1 };
		else if (MM == 4)
			Pp = new int[] { 1, 1, 0, 0, 1 };
		else if (MM == 5)
			Pp = new int[] { 1, 0, 1, 0, 0, 1 };

		else if (MM == 6)
			Pp = new int[] { 1, 1, 0, 0, 0, 0, 1 };

		else if (MM == 7)
			Pp = new int[] { 1, 0, 0, 1, 0, 0, 0, 1 };

		else if (MM == 8)
			/* 1+x^2+x^3+x^4+x^8 */
			Pp = new int[] { 1, 1, 1, 0, 0, 0, 0, 1, 1 };

		else if (MM == 9)
			Pp = new int[] { 1, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

		else if (MM == 10)
			Pp = new int[] { 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1 };

		else if (MM == 11)
			Pp = new int[] { 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 };

		else if (MM == 12)
			Pp = new int[] { 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1 };

		else if (MM == 13)
			Pp = new int[] { 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 };

		else if (MM == 14)
			Pp = new int[] { 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1 };

		else if (MM == 15)
			Pp = new int[] { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };

		else if (MM == 16)
			Pp = new int[] { 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1 };

		// use this can check 10% of application error in the mobile phone
		KK = NN - max_length / 9;
		if (KK < max_length) {
			init(NN * 2, MM + 1, max_length);
			return;
		}

		if (KK % 2 == 0) {
			KK = KK + 1;
		}
		B0 = KK / 2 + 1;

		Alpha_to = new int[NN + 1];
		Index_of = new int[NN + 1];
		A0 = NN;
		Gg = new int[NN - KK + 1];

	}
}