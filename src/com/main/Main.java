package com.main;

import java.awt.Button;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class Main extends Frame {

	/**
	 * Auhtor: YHSPY
	 * Date: 20150610
	 */
	
	private static final long serialVersionUID = 1L;
	static boolean imageLoaded = false;
	private String filePath = "";
	private Label cs = null;
	private Label ds = null;
	private JLabel jlImg = null;
	private JLabel csImg = null;
	private MenuBar menuBar;
	private Menu menuFile;
	private MenuItem openFromFile;
	private Button startFunc;
	private Button getHiddenData;
	private TextArea hiddenData;
	private TextArea getHiddenTxt;
	private TextArea getContent;

	public static void main(String[] args) {
		new Main().launchMethod();
	}
	
	public void launchMethod() {

		startFunc = new Button("Add Watermark");
		startFunc.setSize(250, 50);
		startFunc.setLocation(20, 360);
		startFunc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				ImageObserver myImageObserver = new ImageObserver() {
					public boolean imageUpdate(Image image, int flags, int x, int y, int width, int height) {
						if ((flags & ALLBITS) != 0) {
							imageLoaded = true;
							System.out.println("---- Image Loading Finished! ----");
							return false;
						}
						return true;
					}
				};
				
				String DecPath = filePath.replaceAll("\\\\", "\\\\\\\\");
				String imageURL = DecPath;
				
				Image sourceImage = Toolkit.getDefaultToolkit().getImage(imageURL);
				sourceImage.getWidth(myImageObserver);
				
				while (!imageLoaded) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {}
				}
				GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
				GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

				BufferedImage image = graphicsConfiguration.createCompatibleImage(sourceImage.getWidth(null),sourceImage.getHeight(null),Transparency.BITMASK);

				Graphics2D graphics = image.createGraphics();

				graphics.drawImage(sourceImage, 0, 0, null);
				graphics.dispose();
				
				String hiddenStr = hiddenData.getText();
				byte[] strByte = hiddenStr.getBytes();
				int byteLength = strByte.length;
				String binaryStr = "";
				for(int i = 0;i < byteLength;i ++){
					String temp = Integer.toBinaryString(strByte[i]);
					while(temp.length() < 8){
						temp = "0" + temp;
				    }
					binaryStr = binaryStr + temp;
				}
				String [] delArr = new String[binaryStr.length() / 2];
				for(int i = 0;i < delArr.length;i ++){
					delArr[i] = binaryStr.substring(2 * i, 2 * i + 2);
				}
				int x = 0;
				int y = 0;

				int w = image.getWidth(null);
				int h = image.getHeight(null);
				int[] rgbs = new int[w * h];
				image.getRGB(0, 0, w, h, rgbs, 0, w);

				int plusFlag = 0;
				int rgbApp = 0;
				int dataVol = 0;
				for (y = 0; y < w - 100; y++) {
					for (x = 0; x < h - 100; x++) {
						if (y > 1 && x > 1) {
							if ((image.getRGB(x, y - 1) == 0xFF000000 && image.getRGB(x, y + 1) == 0xFFFFFFFF)) {
								dataVol ++;
								if(plusFlag >= delArr.length) continue; 
		
								switch (delArr[plusFlag]){
								case "00":
									rgbApp = 0xFFFFFFFF;
									break;
								case "01":
									rgbApp = 0xFFFFFFFE;
									break;
								case "10":
									rgbApp = 0xFFFEFFFF;
									break;
								case "11":
									rgbApp = 0xFFFFFEFF;
									break;
								}
								plusFlag ++;
								image.setRGB(x, y, rgbApp);
							}
						}
					}
				}
	
				startFunc.setLabel("Hidden Volume:" + dataVol/4 + "chars");
		
				File file = new File(imageURL.substring(0, imageURL.lastIndexOf("\\")) + "temp.png");
				ImageIcon icon = new ImageIcon(imageURL.substring(0, imageURL.lastIndexOf("\\")) + "temp.png"); // 在此直接创建对象
				icon.setImage(icon.getImage().getScaledInstance(240, 240, Image.SCALE_DEFAULT));
				csImg.setIcon(icon);
				csImg.setVisible(true);
				try {
					ImageIO.write(image, "png", file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		getHiddenData = new Button("Get Watermark");
		getHiddenData.setSize(250, 50);
		getHiddenData.setLocation(290, 360);
	
		getHiddenData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			
				ImageObserver myImageObserver = new ImageObserver() {
					public boolean imageUpdate(Image image, int flags, int x, int y, int width, int height) {
						if ((flags & ALLBITS) != 0) {
							imageLoaded = true;
							System.out.println("---- Image Loading Finished! ----");
							return false;
						}
						return true;
					}
				};
				
				String DecPath = filePath.replaceAll("\\\\", "\\\\\\\\");;
				String imageURL = DecPath;
				
		
				String originInfo = Main.decodePR(imageURL);
				getContent.setText(originInfo);
				
				Image sourceImage = Toolkit.getDefaultToolkit().getImage(imageURL);
				sourceImage.getWidth(myImageObserver);
				
				while (!imageLoaded) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {}
				}
				
			
				GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
				GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

				BufferedImage image = graphicsConfiguration.createCompatibleImage(sourceImage.getWidth(null),sourceImage.getHeight(null),Transparency.BITMASK);

				Graphics2D graphics = image.createGraphics();

				graphics.drawImage(sourceImage, 0, 0, null);
				graphics.dispose();
				
				int x = 0;
				int y = 0;

			
				int w = image.getWidth(null);
				int h = image.getHeight(null);
				int[] rgbs = new int[w * h];
				image.getRGB(0, 0, w, h, rgbs, 0, w);
		
				String hiddenStr = "";
				for (y = 0; y < w - 1; y++) {
					for (x = 0; x < h - 1; x++) {
						if (y > 1 && x > 1) {
						
							if ((image.getRGB(x, y - 1) == 0xFF000000 && image.getRGB(x, y + 1) == 0xFFFFFFFF)) {
								switch (image.getRGB(x, y)){
								case 0xFFFFFFFF:
									hiddenStr = hiddenStr + "00";
									break;
								case 0xFFFFFFFE:
									hiddenStr = hiddenStr + "01";
									break;
								case 0xFFFEFFFF:
									hiddenStr = hiddenStr + "10";
									break;
								case 0xFFFFFEFF:
									hiddenStr = hiddenStr + "11";
									break;
								}
							}
						}
					}
				}
				
				byte [] revByteArr = new byte[hiddenStr.length() / 8];
				for(int i = 0;i < hiddenStr.length() / 8;i ++){
					revByteArr[i] = (byte) Integer.parseInt(hiddenStr.substring(8* i, 8 * i + 8), 2);
				}
				String revStr = null;
				revStr = new String(revByteArr);
				getHiddenTxt.setText(revStr);
			}
		});
		hiddenData = new TextArea("", 10, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
		hiddenData.setLocation(20, 420);
		hiddenData.setSize(520, 80);
		
		getHiddenTxt = new TextArea("", 10, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
		getHiddenTxt.setLocation(555, 100);
		getHiddenTxt.setSize(190, 270);
		
		getContent = new TextArea("", 10, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
		getContent.setLocation(555, 380);
		getContent.setSize(190, 120);
		
		ds = new Label();
		ds.setLocation(600, 350);
		ds.setSize(120, 20);
		ds.setText("Original Data:");

		cs = new Label();
		cs.setLocation(600, 60);
		cs.setSize(120, 20);
		cs.setText("Hidden Data:");

		jlImg = new JLabel();
		jlImg.setOpaque(true);
		jlImg.setBackground(new Color(240, 240, 240));
		jlImg.setLocation(20, 100);
		jlImg.setSize(250, 250);

		csImg = new JLabel();
		csImg.setOpaque(true);
		csImg.setBackground(new Color(240, 240, 240));
		csImg.setLocation(290, 100);
		csImg.setSize(250, 250);
	
		menuBar = new MenuBar();
		menuFile = new Menu("Open");
		openFromFile = new MenuItem("From File");
		openFromFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(Main.this, "Here, please choose one QR barcode", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getDirectory() == null || fd.getFile() == null) {
					return;
				} else {
					filePath = fd.getDirectory() + fd.getFile();
				}
				
				ImageIcon icon = new ImageIcon(filePath);
				icon.setImage(icon.getImage().getScaledInstance(250, 250, Image.SCALE_DEFAULT));
				jlImg.setIcon(icon);
				jlImg.setVisible(true);
			}
		});
		menuFile.add(openFromFile);
		menuBar.add(menuFile);
		this.setMenuBar(menuBar);
		this.add(hiddenData);
		this.add(getHiddenTxt);
		this.add(startFunc);
		this.add(ds);
		this.add(cs);
		this.add(getHiddenData);
		this.add(jlImg);
		this.add(csImg);
		this.add(getContent);
		this.setTitle("PixelWatermark-QRCode-YHSPY");
		this.setResizable(false);
		this.setLocation(50, 50);
		this.setSize(760, 520);
		this.setLayout(null);
		this.setVisible(true);
	
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

	}
	
	public static String decodePR(String imgPath) {
        BufferedImage image = null;
        Result result = null;
        try {
            image = ImageIO.read(new File(imgPath));
            if (image == null) {
                System.out.println("Something Wrong!");
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
 
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");

            result = new MultiFormatReader().decode(bitmap, hints);
           
            return result.getText();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
