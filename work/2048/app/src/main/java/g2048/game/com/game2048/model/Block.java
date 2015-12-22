package g2048.game.com.game2048.model;

/**
 * 滑块实体类
 * @author walk
 *
 */
public class Block {
	
	public static final int MAX = 2048;
	
	private int number;
	
	private int color;
	
	public Block(int number) {
		super();
		this.number = number;
    setColor();
	}

	public int getNumber() {
		return number;
	}
	
	public int getColor() {
		return color;
	}

	private void setColor () {
		switch (number) {
		case 0:
			color = 0xFFCDC0B5;
			break;
		case 2:
			color = 0xFFEEE4DB;
			break;
		case 4:
			color = 0xFFEDE0C9;
			break;
		case 8:
			color = 0xFFF0B17D;
			break;
		case 16:
			color = 0xFFF39568;
			break;
		case 32:
			color = 0xFFF47C63;
			break;
		case 64:
			color = 0xFFF35F43;
			break;
		case 128:
			color = 0xFFECCB69;
			break;
		case 256:
			color = 0xFFECC75A;
			break;
		case 512:
			color = 0xFFFFAA00;
			break;
		case 1024:
			color = 0xFFFFEE00;
			break;
		case 2048:
			color = 0xFF1100FF;
			break;
		}
	}


	@Override
	public String toString() {
		return "Block [number=" + number + ", color=" + color + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color;
		result = prime * result + number;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (color != other.color)
			return false;
		if (number != other.number)
			return false;
		return true;
	}
	
}
