package prototypeFinal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import net.miginfocom.swing.MigLayout;
import prototypeFinal.ThreadTestGui.guiState;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.JLabel;

public class App extends JFrame {

	private JPanel contentPane;
	private JTable table;
	private MyDialog dialog;
	private LabelDialog lblDialog ;
	private WekaLauncher weka;
	private AudioRecorder recorder;
	private EmotionDialog emoDialog;
	private MatlabLauncher matlab;
	private CsvFileWriter cfw;
	private CsvToArff ctoa;
	private ArffWriter arff;
	JLabel lblLastRecordedEmotion = new JLabel("Last Recorded Emotion: ");
	public static volatile JLabel lblTimer = new JLabel("Timer:");
	String[] column = {"Time Length", "Agent Assessment", "Detected Emotion"};
	
	private List<String> txtList = new ArrayList<>();
	
	JLabel lblCorrectlyClassified = new JLabel("Correctly Classified: ");
	JLabel lblIncorrectlyClassified = new JLabel("Incorrectly Classified: ");
	
	public static boolean isLabelSet = false;
	public static boolean isModelSet = false;
	//public static volatile boolean isMatlabRunning = false;
	
	Path currentRelativePath = Paths.get("");
	/**
	 * Launch the application.
	 */
	
	public static enum guiState{
		Record, Stop
	}
	
	public static enum matlabState{
		Running, Stopped
	}
	public static volatile matlabState matlabStat = matlabState.Stopped;
	public static volatile guiState guiStat = guiState.Stop;
	public static volatile int time = 0;
	
	JButton btnStop = new JButton("Stop");
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App frame = new App();
					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	public App() throws Exception {
		dialog = new MyDialog();
		emoDialog = new EmotionDialog();
		recorder = new AudioRecorder();
		lblDialog = new LabelDialog();
		matlab = new MatlabLauncher(false);
		cfw = new CsvFileWriter();
		ctoa = new CsvToArff();
		arff = new ArffWriter();
		arff.setAttList("hfd");
		arff.setAttList("katz");
		arff.setAttList("hurst");
		arff.setAttList("box");
		arff.setAttList("dfa");
		weka = new WekaLauncher();
		String s = currentRelativePath.toAbsolutePath().toString() +"/Files";
		String s1 = currentRelativePath.toAbsolutePath().toString() +"/PermanentFiles";
		File theDir = new File(s.replaceAll("\\\\", "/"));
		File Dir = new File(s1.replaceAll("\\\\", "/"));
		recorder.setFilePath(s.replaceAll("\\\\", "/") +"/Record/");
        if(!Dir.exists()){
        	Dir.mkdir();
        	Dir = new File(s1.replaceAll("\\\\", "/") +"/MatlabCodes");
        	Dir.mkdir();
	        theDir = new File(s.replaceAll("\\\\", "/") +"/Model");
	        theDir.mkdir();
        	System.out.println("DIR created");
        }
		
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + s.replaceAll("\\\\", "/"));
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        theDir = new File(s.replaceAll("\\\\", "/") +"/Record");
		        theDir.mkdir();
		        theDir = new File(s.replaceAll("\\\\", "/") +"/Csv");
		        theDir.mkdir();
		        theDir = new File(s.replaceAll("\\\\", "/") +"/Arff");
		        theDir.mkdir();
		        theDir = new File(s.replaceAll("\\\\", "/") +"/FinalResult");
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}else
			System.out.println("DIR exists");
		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 370);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new MigLayout("", "[grow]", "[][grow][][][]"));
		setContentPane(contentPane);
		
		JButton btnLoadModel = new JButton("Load Model");
		btnLoadModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.openDialogBox();
				weka.setModelpath(dialog.getNamePath().toString());
				try {
					weka.LoadModel();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println(dialog.getNamePath());
				btnLoadModel.setText("Model Loaded");
				isModelSet = true;
			}
		});
		contentPane.add(btnLoadModel, "flowx,cell 0 0");
		
		JButton btnSetLabel = new JButton("Set Label");
		btnSetLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblDialog.start();
				isLabelSet = true;
			}
		});
		contentPane.add(btnSetLabel, "cell 0 0");
		
		JButton btnRecord = new JButton("Record");
		btnRecord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isModelSet == true && isLabelSet == true){
					
					Timer timer = new Timer(1000,new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
							//lblTimer.setText("Timer: " + sdf.format(new java.util.Date()));
							time++;
							lblTimer.setText("Timer: " + timeTransfer(time));
						}
					});
					timer.start();
					
					btnRecord.setText("Recording");
					guiStat = guiState.Record;
					emoDialog.setEmotionLabel(LabelDialog.labelList);
					
					btnLoadModel.setEnabled(false);
					btnSetLabel.setEnabled(false);
					emoDialog.setBtnStop(btnStop);
					emoDialog.setLblRecord(lblLastRecordedEmotion);
					//guiStat.notifyAll();

					swingController();
				}else
					JOptionPane.showMessageDialog(null, "Set model and label first");
			}
		});
		
		Object[][] data = {};
		table = new JTable(data,column);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		JScrollPane scrollPane = new JScrollPane(table);
		contentPane.add(scrollPane, "cell 0 1,grow");
		
		
		contentPane.add(lblLastRecordedEmotion, "flowx,cell 0 2");
		
		
		contentPane.add(lblTimer, "flowx,cell 0 3");
		contentPane.add(btnRecord, "flowx,cell 0 4");
			
		
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnRecord.setText("Record");
				guiStat = guiState.Stop;
				btnLoadModel.setEnabled(true);
				btnSetLabel.setEnabled(true);
				while(matlabStat == matlabState.Running){
					btnStop.setText("Processing");
				}
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				cfw.setValuesList(matlab.getResultList());
				cfw.setEmotionList(emoDialog.getEmotionList());
				cfw.writeCsvFile();
				try {
					ctoa.csvToArff(cfw.getFilename(), cfw.getPath());
					arff.setNominalLabelList(LabelDialog.labelList);
					arff.addAttributeArff(ctoa.getPathwName());
					cfw.reader(arff.getFilepath());
					weka.setFilepath(currentRelativePath.toAbsolutePath().toString() + "/Files/Arff/newemotest0.arff");
					weka.start();
					populateTable();
					cfw.writeTextFile(txtList);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				btnStop.setText("Stop");
				//guiStat.notifyAll();
			}
		});
		contentPane.add(btnStop, "cell 0 4");

		contentPane.add(lblCorrectlyClassified, "cell 0 2");
		
		
		contentPane.add(lblIncorrectlyClassified, "cell 0 3");
	}
	
	void populateTable(){
		DefaultTableModel dtm = new DefaultTableModel(new Object[] {"Time Length", " Agent Assessment", "Detected Emotion"},0);//asd.toArray(new Object[][] {}), column);
		for(int i = 0 ; i < emoDialog.getEmotionList().size(); i++ ){//emoDialog.getEmotionList().size();i++){
			String[] data  = new String[3];
			data[0] = recorder.getTimeline().get(i);
			data[1] = emoDialog.getEmotionList().get(i);
			data[2] = weka.getEmotionList().get(i);			
			dtm.addRow(data);
			txtList.add("Time length: " + recorder.getTimeline().get(i)+" Agent Assessment: " + emoDialog.getEmotionList().get(i)+" Predicted "+weka.getEmotionList().get(i+2) +" ");
			//System.out.println(asd.get(i));
		}

		table.setModel(dtm);
		
		lblCorrectlyClassified.setText(weka.getAcc().get(1));
		lblIncorrectlyClassified.setText(weka.getAcc().get(2));
		for(String i: weka.getAcc()){
			System.out.println(i);
		}
	}

	void swingController(){
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				// TODO Auto-generated method stub
				
				while(guiStat == guiState.Record){
					recorder.record();
					
					emoDialog.start();
					
					if(guiStat == guiState.Stop){
						recorder.getFilename().remove(recorder.getFilename().size());
					}
					
					Thread t = new Thread(new Runnable(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(matlabStat == matlabState.Stopped){

								while(recorder.getFilename().size() != 0){
									matlabStat = matlabState.Running;
									System.out.println("start matlab " + recorder.getFilename().get(0));
									matlab.setPath(currentRelativePath.toAbsolutePath().toString() +"/Files/Record/" + recorder.getFilename().get(0));

									try {
										matlab.runMatlab();
									} catch (MatlabConnectionException | MatlabInvocationException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									System.out.println("done matlab " + recorder.getFilename().get(0));
									recorder.getFilename().remove(0);
									matlabStat = matlabState.Stopped;
								}
							}
						}

					});
					t.start();
				}
				return (Void) null;
			}
			
		};
		worker.execute();
	}

	public static String timeTransfer(int counter){
		
		final int MINUTES_IN_AN_HOUR = 60;
		final int SECONDS_IN_A_MINUTE = 60;
		
		int seconds = counter % SECONDS_IN_A_MINUTE;
		int totalMinutes = counter/ SECONDS_IN_A_MINUTE;
		int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
		int hours = totalMinutes/ MINUTES_IN_AN_HOUR;
		
		return hours + ":" + minutes + ":" + seconds;
	}
	
}
