package com.congxiaoyao.cmd;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.StringReader;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import com.congxiaoyao.cmd.CodeAssistant.WeightedString;

/**
 * 用JTextArea写成的仿windows的CMD窗口，作为CMD框架的一部分，主要负责UI方面的内容
 * 调用无参的构造函数实例化后，使用{@code CommandWindow#setVisible()}方法显示 关闭窗口情调用{@code closeWindow}方法
 * 用户输入的每一行合法内容都会以回调接口的形式通知外界
 * {@code OnSubmitListener#onSubmit(String)}
 * 支持代码提示功能，默认不开启，如需启用请构造{@code CodeAssistant}实例并传入
 * {@code #setAssistant(CodeAssistant)}
 * 按住ctrl+上下箭头可查看之前输入过的内容
 * 其他小功能请看类内共有方法的方法注释
 *
 * @see OnSubmitListener#onSubmit(String)
 * @see #setAssistant(CodeAssistant)
 *
 * @version 1.1
 * @author congxiaoyao
 * @date 2016.1.24
 */

public class CommandWindow extends JFrame{

	public static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
	public static final KeyStroke BACK = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0);
	public static final KeyStroke PASTE = KeyStroke.getKeyStroke(KeyEvent.VK_V,KeyEvent.CTRL_MASK);
	public static final KeyStroke CUT = KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.CTRL_MASK);
	public static final KeyStroke ARROW_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP,KeyEvent.CTRL_MASK);
	public static final KeyStroke ARROW_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,KeyEvent.CTRL_MASK);
	
	public String HINT = "请输入>";
	public String LFHINT = "\n请输入>";
	public int    LEN_HINT = HINT.length();

	private JTextArea textArea;
	private JScrollPane scrollPane;
	private Font font = new Font("黑体", Font.BOLD, 15);

	private PrintStream printStream;
	
	private RoundList<String> inputs = new RoundList<>(10);
	private int inputsPointer = 0;
	
	private CodeAssistant assistant;
	
	private OnSubmitListener onSubmitListener;

	public CommandWindow(int width , int height) {
		super("请输入命令");

		initPrintStream();

		textArea = new CMDTextArea(HINT);
		textArea.setFont(font);
		textArea.enableInputMethods(false);
		textArea.setLineWrap(true);
		moveCaretToBottom();
		scrollPane = new JScrollPane(textArea);
		add(scrollPane);

		setBounds(width , height);
		setVisible(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public CommandWindow(boolean fullScreen) {
		this();
		if(fullScreen) {
			Toolkit kit = Toolkit.getDefaultToolkit();
	        Dimension dimension = kit.getScreenSize();
	        setBounds(dimension.width, dimension.height);
		}
	}

	public CommandWindow() {
		this(480,400);
	}

	/**
	 * 将输出流重定向的CommandWindow里，只有四个print方法
	 */
	private void initPrintStream() {
		printStream = new PrintStream(System.out){
			@Override
			public void println(String x) {
				CommandWindow.this.println(x);
			}
			@Override
			public void println(Object x) {
				if(x == null) 
					println("null");
				else println(x.toString());
			}
			@Override
			public void print(String s) {
				textArea.append(s);
				moveCaretToBottom();
			}
			@Override
			public void print(Object object) {
				print(object.toString());
			}
		};
		System.setOut(printStream);
	}
	
	/**
	 * @return 光标位置
	 */
	private int getCaretPosition() {
		return textArea.getCaretPosition();
	}
	
	private int getHintPosition() {
		int position = 0;
		try {
			position = textArea.getLineStartOffset(textArea.getLineCount()-1);
			position += HINT.length();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return position;
	}
	
	/**
	 * 将光标移至最低端
	 */
	private void moveCaretToBottom() {
		textArea.setCaretPosition(getTextLength());
	}
	
	/**
	 * @return 当前内容的长度
	 */
	private int getTextLength() {
		return textArea.getDocument().getLength();
	}
	
	/**
	 * @return 光标在最后一个字符后面返回true 否则false
	 */
	private boolean isCaretAtBottom() {
		return getTextLength() == getCaretPosition();
	}
	
	/**
	 * @return 如果选择的部分位于可编辑区返回true
	 */
	private boolean isSelectLegal() {
		Caret caret = textArea.getCaret();
        int hintPos = getHintPosition();
        return (caret.getDot() - hintPos >= 0 && caret.getMark() - hintPos >= 0);
	}
	
	/**
	 * @return 如果处于选择状态返回true
	 */
	private boolean isSelecting() {
		Caret caret = textArea.getCaret();
		return caret.getDot() != caret.getMark();
	}
	
	/**
	 * @return 光标到提示符的最后一个字符的距离，如：
	 * 请输入>abc|de
	 * |代表光标，此时返回3
	 */
	private int distanceBetweenCaretAndHint() {
		return getCaretPosition() - getHintPosition();
	}
	
	/**
	 * @return textArea中最后一行的内容
	 */
	private String getLastLine() {
		try {
			int offset = textArea.getLineStartOffset(textArea.getLineCount()-1);
			return textArea.getDocument().getText(offset, getTextLength() - offset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return 用户在最后一行的输入
	 */
	private String getLastInput(String lastLine) {
		if(lastLine == null) {
			lastLine = getLastLine();
		}
		if(lastLine.length() == 0) return "";
		return lastLine.substring(LEN_HINT, lastLine.length());
	}
	
	/**
	 * 替换掉用户输入的部分为content，也就是最后一行的内容（不包含hint）
	 * @param content
	 */
	private void replaceInputing(String content) {
		try {
			int offset = textArea.getLineStartOffset(textArea.getLineCount()-1);
			textArea.getDocument().remove(offset, getTextLength() - offset);
			textArea.append(HINT);
			textArea.append(content);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置控制台高度
	 * @param height 高度的像素值
	 */
	public void setCommandHeight(int height) {
		setBounds(getWidth(), height);
	}
	
	/**
	 * 关闭窗口
	 */
	public void closeWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dispose();
	}
	
	/**
	 * 清除窗口中的内容
	 */
	public void clearCommandWindow() {
		textArea.setText("");
	}
	
	/**
	 * 设置字体大小
	 * @param size
	 */
	public void setFontSize(int size) {
		this.font = new Font("宋体", Font.TYPE1_FONT, size);
		textArea.setFont(font);
	}
	
	/**
	 * 设置提示符显示的内容
	 * @param hint
	 */
	public void setHint(String hint) {
		HINT = hint;
		LFHINT = "\n"+hint;
		LEN_HINT = hint.length();
	}
	
	/**
	 * 显示窗口
	 * @return
	 */
	public CommandWindow setVisible() {
		setVisible(true);
		return this;
	}
	
	/**
	 * 设置一个窗口的大小并让其居中显示
	 * @param width frame宽度
	 * @param height frame高度
	 */
	public void setBounds(int width ,int height)
	{
		Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();   
		setBounds((int) (scrSize.getWidth()-width)/2, (int) (scrSize.getHeight()-height)/2  
				,width, height);
	}
	
	/**
	 * 不重定向输出流，让sysout重回控制台
	 */
	public void resetPrintStream() {
		printStream = new PrintStream(System.out);
		System.setOut(printStream);
	}
	
	/**
	 * 在窗口中输出string的值,如果在string的结尾遇到\4，则认为需要在下一行输出HINT
	 * @param string
	 */
	public void println(String string){
		int len = string.length();
		if (len != 0 && string.charAt(0) == '\5') {
			printlnSmoothly(string.substring(1, len));
		}
		else if(len != 0 && string.charAt(len-1) == '\4') {
			textArea.append(string.substring(0, len-1));
			textArea.append(LFHINT);
		}else {
			textArea.append(string);
			textArea.append("\n");
		}
		moveCaretToBottom();
	}
	
	/**
	 * 在窗口中输出string的值,但不是一下子蹦出来，是缓慢的出来的，牵扯到多线程问题，请慎用
	 * 请尽量在处理函数中使用此函数，否则可能造成数据显示不完整
	 * @param string
	 */
	private boolean can = true;
	public void printlnSmoothly(String string) {
		if(string == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					textArea.getDocument().remove(getTextLength()-HINT.length(), HINT.length());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				BufferedReader reader = new BufferedReader(new StringReader(string));
				String line = null;
				try {
					can =false;
					while((line = reader.readLine()) != null) {
						System.out.println(line);
						Thread.sleep(5);
					}
					can = true;
					reader.close();
					textArea.append(HINT);
					moveCaretToBottom();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public JTextArea getTextArea() {
		return textArea;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	public int getFontSize() {
		return font.getSize();
	}
	
	public void setAssistant(CodeAssistant assistant) {
		this.assistant = assistant;
	}

	public void setOnSubmitListener(OnSubmitListener onSubmitListener) {
		this.onSubmitListener = onSubmitListener;
	}
	
	private class CMDTextArea extends JTextArea {

		private static final long serialVersionUID = 1L;

		public CMDTextArea(String HINT) {
			super(HINT);
		}

		/**
		 * 重写了processKeyBinding方法，拦截了回车、退格、粘贴、可见字母符号并重新加以处理
		 * 限制了对textArea的一部分操作，使得他表现的像是真的CMD一样
		 * return那里也是随便return的，true或false无所谓了
		 */
		@Override
		protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			if(condition != 0) return false;
			//先检查最后一行的提示符是否存在
			String lastLine = getLastLine();
			if(lastLine.length() < HINT.length() && can) {
				textArea.append(HINT);
				moveCaretToBottom();
				if(ks.equals(ENTER)) return false;
				return processKeyBinding(ks, e, condition, pressed);
			}
			//拦截ctrl+上箭头、下箭头
			if(ks.equals(ARROW_UP)) {
				if(inputs.size() > 0) {
					replaceInputing(inputs.getAllowsNegativeIndex(--inputsPointer));
					moveCaretToBottom();
				}
			}else if(ks.equals(ARROW_DOWN)) {
				if(inputs.size() > 0) {
					replaceInputing(inputs.getAllowsNegativeIndex(++inputsPointer));
					moveCaretToBottom();
				}
			}
			//拦截回车
			if(ks.equals(ENTER)) {
				//如果处于代码提示状态
				if(isSelecting() && assistant !=null) {
					int len = getTextLength();
					select(len, len);
					moveCaretToBottom();
					return true;
				}
				if(!isCaretAtBottom()) {
					moveCaretToBottom();
					return processKeyBinding(ks, e, condition, pressed);
				}
				//提取用户输入
				String content = getLastInput(lastLine);
				if(!content.equals("") && onSubmitListener != null){
					textArea.append("\n");
					inputs.add(content);
					inputsPointer = inputs.size();
					onSubmitListener.onSubmit(content);
					lastLine = getLastLine();
					if (lastLine.isEmpty()) {
						textArea.append(HINT);
					}else if(!lastLine.equals(HINT)){
						textArea.append(LFHINT);
					}
				}else {
					textArea.append(LFHINT);
				}
				moveCaretToBottom();
				return false;
			}
			//拦截退格
			else if (ks.equals(BACK)) {
				if(isSelecting()) {
					if(isSelectLegal()) 
						return super.processKeyBinding(ks, e, condition, pressed);
				}else {
					int dis = distanceBetweenCaretAndHint();
					if(dis > 0) 
						return super.processKeyBinding(ks, e, condition, pressed);
				}
				moveCaretToBottom();
				return false;
			}
			//拦截粘贴
			else if(ks.equals(PASTE)){
				if(!isSelectLegal()) {
					moveCaretToBottom();
					return false;
				}
				return super.processKeyBinding(ks, e, condition, pressed);
			}
			//拦截剪切
			else if(ks.equals(CUT)) {
				if(isSelectLegal())
					return super.processKeyBinding(ks, e, condition, pressed);
		        return false;
			}
			//防止内容在不正确的地方输入
			else if(ks.getKeyChar() > 31 && ks.getKeyChar() < 127){
				if(!isSelectLegal()) {
					moveCaretToBottom();
					return super.processKeyBinding(KeyStroke.getKeyStroke(ks.getKeyChar()), e, condition, pressed);
				}
			}
			return handleCodeCompletion(ks, e) ? true : super.processKeyBinding(ks, e, condition, pressed);
		}

		/**
		 * 处理代码提示，通过类内的CodeAssistant来查找相应代码并显示在commandWindow内
		 * @param ks
		 * @param e
         * @return 如果拦截了空格或提示了代码的话返回true，否则返回false
         */
		public boolean handleCodeCompletion(KeyStroke ks , KeyEvent e) {
			if (assistant == null) return false;
			if(ks.getKeyEventType() == 400 &&ks.getKeyChar() == ' ') {
				if(isSelecting()) {
					int pos = getTextLength();
					textArea.select(pos, pos);
					
					return true;
				}else {
					return false;
				}
			}
			if(ks.getKeyEventType() == 402 &&e.getKeyChar() > 31 && e.getKeyChar() < 127) {
				int start = getCaretPosition();
				String lastInput = getLastInput(null);
				SelectableArray<WeightedString> find = assistant.find(lastInput);
				int select = -1, min = Integer.MAX_VALUE;
				for (int i = 0; i < find.size(); i++) {
					WeightedString weightedString = find.get(i);
					if (weightedString.weight == 0) {
						int dis = weightedString.string.length() - lastInput.length();
						if (dis < min) {
							min = dis;
							select = i;
						}
					}
				}
				if (select != -1) {
					replaceInputing(find.get(select).string);
					select(getTextLength(), start);
					moveCaretPosition(start);
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * 用户输入的内容会通过此接口回调，将每一次用户的一行输入作为函数的参数通知外界
	 * @author congxiaoyao
	 *
	 */
	public interface OnSubmitListener{
		void onSubmit(String content);
	}
}