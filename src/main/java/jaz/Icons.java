package jaz;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

final class Icons {
	static final ImageIcon STAR_ON = createIcon("star_on.png");
	static final ImageIcon STAR_OFF = createIcon("star_off.png");

	static final ImageIcon COLOR = createIcon("color.png");

	private static ImageIcon createIcon(String fileName) {
		try {
			return new ImageIcon(ImageIO.read(Icons.class.getResourceAsStream(fileName)));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Icons() {
	}
}
