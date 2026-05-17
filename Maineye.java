package eye.eye03;

import drjava.util.*;
import eye.eye01.CodeWindow;
import eye.eye01.EyeGuiUtil;
import eye.eye01.MakeImageDialog;
import eye.eye01.RecognizerInfoWindow;
import eye.eye02.EyeDialogs;
import eye.eye02.LearnAFontDialog;
import eye.eye04.ChallengesDialog;
import eye.eye04.KnownCharactersWindow;
import eye.eye05.DebugInfoWindow;
import eye.eye05.RecognizableImage;
import eyedev._01.*;
import eyedev._09.RecognizedLine;
import eyedev._09.Subrecognition;
import eyedev._13.StartIRDialog;
import eyedev._16.TextLocation;
import eyedev._18.TrainingExample;
import eyedev._18.TrainingExampleOnDisk;
import eyedev._18.TrainingExamplesDialog;
import eyedev._18.WithFratboySegmenter;
import eyedev._21.Correction;
import eyedev._21.ImageInfo;
import prophecy.common.gui.CenteredLine;
import prophecy.common.gui.RightAlignedLine;
import prophecy.common.gui.SingleComponentPanel;
import prophecy.common.image.BWImage;
import prophecy.common.image.ImageProcessing;
import prophecy.common.image.RGBImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static final String version = "Alpha 10";
  public static final String releaseNotesHtml =
    "<p>Improvements in Alpha 10: Character corrections are now " +
      "associated to the original image which makes them much more " +
      "persistent and easier to use.</p>";

  protected EyeData eyeData;
  protected Recognizers recognizers;
  protected TrainingExamplesDialog trainingExamplesDialog;

  protected BufferedImage image;
  protected File imagePath;
  protected List<DebugItem> debugInfo;
  protected ImageInfo imageInfo;

  protected JFrame frame;
  private JMenuBar menuBar;
  protected JPanel buttons;
  private RecognizableImage recognizableImage;
  private ActionListener openImage;
  private ActionListener makeImage;
  protected JMenu extrasMenu;
  private JButton btnDebug;
  private RecognizerSelector recognizerSelector;
  private JRecognizedText textArea;
  private ImageReader recognizer;
  private boolean ignoreRecognizerChange;
  private boolean recognizerModified;
  private SingleComponentPanel optionsPanel;
  //private boolean autoRecognize;
  private JButton btnRecognize;
  private List<Pair<Option,JTextField>> optionFields = new ArrayList<Pair<Option, JTextField>>();

  // move text area caret on mouse over in image?
  // if false, caret is only moved on click
  private boolean quickCaretMove = true;

  int blockImageAndTextUpdates = 0;
  public JCheckBox chkAutoRecognize;
  public ScalingSplitPane splitPane;

  public static void main(String[] args) {
    //Prophecy.systemLookAndFeel();
    new Main();
  }

  public Main() {
    miscInit();
    makeFrame();
    showFrame();
    showWelcomeDialogIfUnshown();
  }

  private void showWelcomeDialogIfUnshown() {
    String shown = eyeData.welcomeDialogShownForVersion().get();
    if (!version.equals(shown)) {
      new WelcomeDialog().setVisible(true);
      eyeData.welcomeDialogShownForVersion().set(version);
    }
  }

  public void miscInit() {
    //UIManager.getDefaults().setDefaultLocale(Locale.ENGLISH);
    EyeEnv.init();
    eyeData = new EyeData();
    recognizers = eyeData.recognizers;
    ToolTipManager.sharedInstance().setInitialDelay(100); // for RecognizableImage character tooltips
  }

  protected void makeFrame() {
    frame = new JFrame("Eye (" + version + ")");

    Rectangle bounds = eyeData.getMainWindowBounds();
    boolean maximized = eyeData.getMainWindowMaximized();
    if (bounds != null)
      frame.setBounds(bounds);
    else {
      frame.setSize(700, 500);
      GUIUtil.centerOnScreen(frame);
    }
    if (maximized) frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

    splitPane = new ScalingSplitPane(ScalingSplitPane.VERTICAL_SPLIT, eyeData.getMainWindowSplit());

    recognizableImage = new MyRecognizableImage();

    String imagePath = eyeData.getLastImagePath();
    if (imagePath != null) {
      JButton btnLoadLastImage = new JButton(/*"Use same image as last time"*/"Load " + new File(imagePath).getName());
      btnLoadLastImage.setToolTipText("Load " + imagePath);
      btnLoadLastImage.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          try {
            String path = eyeData.getLastImagePath();
            if (path != null)
              loadImage(new File(path));
          } catch (Throwable e) {
            Errors.report(e);
          }
        }
      });
      recognizableImage.setComponent(new MiddleComponent(new CenteredLine(btnLoadLastImage)));
    }

    recognizerSelector = new RecognizerSelector(recognizers);
    recognizerSelector.selectRecognizer(eyeData.getDefaultRecognizerName());
    recognizerSelector.addTriggerListener(frame);
    recognizerSelector.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        if (itemEvent.getStateChange() == ItemEvent.SELECTED && !ignoreRecognizerChange) {
          newRecognizer();
          if (!prepareRecognizer())
            recognize();
        }
      }
    });

    JPanel topPanel = new JPanel(new LetterLayout("S", "O", "I", "I").setSpacing(5).setBorder(5));
    topPanel.add("I", GUIUtil.withTitle("Input image", recognizableImage));
    /*JMultilineLabel note = new JMultilineLabel("Note: This window uses the Eye standard recognizer trained on the Arial font. " +
      "To recognize other fonts, please select \"Extras/Learn a font...\" or \"Recognizers\".") {
      public Dimension getMinimumSize() {
        return new Dimension(100, 50);
      }
    };
    topPanel.add("N", note);*/

    JButton btnInfo = new JButton("Info...");
    btnInfo.addActionListener(EyeGuiUtil.actionListener(this, "recognizerInfo"));

    JPanel selectorPanel = new JPanel(new LetterLayout("SSB"));
    selectorPanel.add("S", GUIUtil.withLabel("Recognizer to use:", recognizerSelector));
    selectorPanel.add("B", btnInfo);

    topPanel.add("S", selectorPanel);
    optionsPanel = new SingleComponentPanel();
    topPanel.add("O", optionsPanel);
    splitPane.setTopComponent(/*GUIUtil.withTitle(
      note,
      GUIUtil.withTitle("Input image", scrollableImage))*/ topPanel);

    textArea = new JRecognizedText();

    /*JPanel bottomPanel = new JPanel(new LetterLayout("R", "T", "T"));
    bottomPanel.add("R", GUIUtil.withLabel("Recognizer:", cbRecognizer));
    bottomPanel.add("T", GUIUtil.withTitle("Recognized text", new JScrollPane(textArea)));*/
    splitPane.setBottomComponent(GUIUtil.withTitle("Recognized text", new JScrollPane(textArea)));

    ActionListener openImage = EyeGuiUtil.actionListener(this, "loadImage");
    ActionListener makeImage = EyeGuiUtil.actionListener(this, "makeImage");

    buttons = new JPanel(LetterLayout.stalactite());
    JButton btnOpenImage = new JButton("Load image...");
    this.openImage = openImage;
    btnOpenImage.addActionListener(this.openImage);
    buttons.add(btnOpenImage);

    JButton btnMakeImage = new JButton("Make image...");
    this.makeImage = makeImage;
    btnMakeImage.addActionListener(this.makeImage);
    buttons.add(btnMakeImage);

    buttons.add(new JPanel());

    chkAutoRecognize = new JCheckBox("Autorecognize", eyeData.getAutoRecognize());
    chkAutoRecognize.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        eyeData.setAutoRecognize(chkAutoRecognize.isSelected());
      }
    });

    btnRecognize = new JButton("Recognize");
    btnRecognize.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          recognize();
        } catch (Throwable t) {
          Errors.report(t);
        }
      }
    });

    JPanel recognizeButtonPanel = new JPanel(new LetterLayout("C", "B").setSpacing(0));
    recognizeButtonPanel.add("C", chkAutoRecognize);
    recognizeButtonPanel.add("B", btnRecognize);
    buttons.add(recognizeButtonPanel);

    JPanel mainPanel = new JPanel(new LetterLayout("CCB").setBorder(10));
    mainPanel.add("C", splitPane);
    mainPanel.add("B", buttons);
    frame.getContentPane().add(mainPanel);

    menuBar = new JMenuBar();

    makeMenus();
    frame.setJMenuBar(menuBar);

    buttons.add(new JPanel());

    JButton button = new JButton("Manage recognizers");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        manageRecognizers();
      }
    });
    buttons.add(button);

    //buttons.add(new JPanel());

    /*button = new JButton("Challenges");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        challenges();
      }
    });
    buttons.add(button);*/

    btnDebug = new JButton("Debug");
    btnDebug.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        debug();
      }
    });
    btnDebug.setEnabled(false);
    buttons.add(btnDebug);

    connectImageAndText();

    prepareRecognizer();

    frame.addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        splitPane.setDividerLocation(eyeData.getMainWindowSplit());
      }
    });

    addFrameClosingHandler();
  }

  private void addFrameClosingHandler() {
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (recognizerModified) {
          int option = JOptionPane.showConfirmDialog(frame,
            "Recognizer was modified. Save?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
          if (option == JOptionPane.CANCEL_OPTION)
            return;
          if (option == JOptionPane.YES_OPTION) {
            saveRecognizer();
            if (recognizerModified)
              return;
          }
        }
        eyeData.setMainWindowBounds(frame.getBounds(), frame.getExtendedState() == JFrame.MAXIMIZED_BOTH);
        eyeData.setMainWindowSplit((float) splitPane.getRelativeDividerLocation());
        System.exit(0);
      }
    });
  }

  private void connectImageAndText() {
    textArea.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent e) {
        if (blockImageAndTextUpdates != 0) return;
        ++blockImageAndTextUpdates;
        TextLocation location = textArea.findLocationAtCaret();
        if (location != null)
          recognizableImage.setMarkedSubrecognition(location.subrecognition, true);
        --blockImageAndTextUpdates;
      }
    });

    if (quickCaretMove)
      recognizableImage.markChangeTrigger.addListener(new Runnable() {
        public void run() {
          if (blockImageAndTextUpdates != 0) return;
          ++blockImageAndTextUpdates;
          Subrecognition s = recognizableImage.getMarkedSubrecognition();
          if (s != null)
            textArea.jumpToLocation(s);
          --blockImageAndTextUpdates;
        }
      });
  }

  private void newRecognizer() {
    recognizer = null;
  }

  protected void showFrame() {
    frame.setVisible(true);
  }

  protected void makeMenus() {
    JMenu infoMenu = new JMenu("Info");
    JMenu imageMenu = new JMenu("Image");
    JMenu recognizerMenu = new JMenu("Recognizer");
    extrasMenu = new JMenu("Extras");
    JMenu experimentalMenu = new JMenu("Experimental");

    int spacing = 5;
    menuBar.add(infoMenu);
    menuBar.add(Box.createHorizontalStrut(spacing));
    menuBar.add(imageMenu);
    menuBar.add(Box.createHorizontalStrut(spacing));
    menuBar.add(recognizerMenu);
    menuBar.add(Box.createHorizontalStrut(spacing));
    menuBar.add(extrasMenu);
    menuBar.add(Box.createHorizontalStrut(spacing));
    menuBar.add(experimentalMenu);

    JMenuItem mi;

    mi = new JMenuItem("About Eye...");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "info"));
    infoMenu.add(mi);

    mi = new JMenuItem("Load image...");
    mi.addActionListener(openImage);
    imageMenu.add(mi);
    mi = new JMenuItem("Make image...");
    mi.addActionListener(makeImage);
    imageMenu.add(mi);
    mi = new JMenuItem("Resize image...");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "resizeImage"));
    imageMenu.add(mi);
    imageMenu.addSeparator();
    mi = new JMenuItem("Rerecognize");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "recognize"));
    imageMenu.add(mi);
    imageMenu.addSeparator();

    final JCheckBoxMenuItem miShowMarkLines = new JCheckBoxMenuItem("Show mark lines", true);
    miShowMarkLines.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        recognizableImage.setDrawMarkLines(miShowMarkLines.isSelected());
      }
    });
    recognizableImage.setDrawMarkLines(true);
    imageMenu.add(miShowMarkLines);

    final JCheckBoxMenuItem miConfidenceBoxes = new JCheckBoxMenuItem("Show confidence boxes", false);
    miConfidenceBoxes.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        recognizableImage.setDrawConfidenceBoxes(miConfidenceBoxes.isSelected());
      }
    });
    recognizableImage.setDrawConfidenceBoxes(false);
    imageMenu.add(miConfidenceBoxes);

    mi = new JMenuItem("Save recognizer...");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "saveRecognizer"));
    recognizerMenu.add(mi);

    mi = new JMenuItem("Recognizer info");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "recognizerInfo"));
    recognizerMenu.add(mi);

    mi = new JMenuItem("Known characters...");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "showKnownCharacters"));
    recognizerMenu.add(mi);

    mi = new JMenuItem("Known characters with images...");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "showKnownCharactersWithImages"));
    recognizerMenu.add(mi);

    mi = new JMenuItem("Learn a font");
    mi.addActionListener(EyeGuiUtil.actionListener(this, "makeRecognizer"));
    extrasMenu.add(mi);

    mi = new JMenuItem("Recognizers");
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        manageRecognizers();
      }
    });
    extrasMenu.add(mi);

    mi = new JMenuItem("Training examples");
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        showTrainingExamplesDialog();
      }
    });
    extrasMenu.add(mi);

    mi = new JMenuItem("Challenges");
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        challenges();
      }
    });
    experimentalMenu.add(mi);

    mi = new JMenuItem("Interactive recognition...");
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        interactiveRecognition();
      }
    });
    experimentalMenu.add(mi);
  }

  private void interactiveRecognition() {
    new StartIRDialog(recognizers).setVisible(true);
  }

  /*private void showError(Throwable t) {
    t.printStackTrace();
    JOptionPane.showMessageDialog(frame, t.toString());
  }*/

  @SuppressWarnings({"UnusedDeclaration"})
  public void loadImage() throws IOException {
    JFileChooser fileChooser = new JFileChooser();
    String path = eyeData.getLastImagePath();
    File dir = null;
    if (path != null) dir = new File(path).
      getParentFile();
    if (dir == null || !dir.exists())
      dir = new File("examples");
    fileChooser.setCurrentDirectory(dir);
    if (fileChooser.showDialog(frame, "Open image") == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      if (file != null)
        loadImage(file);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void makeImage() {
    MakeImageDialog dialog = new MakeImageDialog(frame);
    dialog.setModal(true);
    dialog.setVisible(true);
    if (dialog.image != null)
      setImage(dialog.image.getBufferedImage(), null);
  }

  public void loadImage(final File file) throws IOException {
    eyeData.setLastImagePath(file.getPath());
    new ProgressDialog<BufferedImage>(frame, "Loading image") {
      protected void done(BufferedImage image) {
        setImage(image, file);
      }

      protected BufferedImage doInBackground() throws Exception {
        return ImageIO.read(file);
      }
    }.start();
  }

  public void setImage(BufferedImage image, File imagePath) {
    this.imagePath = imagePath;
    this.image = image;
    try {
      imageInfo = eyeData.imageInfoDB.getImageInfo(imagePath);
    } catch (Throwable e) {
      Errors.add(e);
      imageInfo = null;
    }
    recognizableImage.setImage(image);
    if (imageInfo != null) {
      recognizableImage.setCorrections(imageInfo.getCorrections());
      recognizableImage.repaintImageSurface();
    }

    if (btnRecognize != null) btnRecognize.setEnabled(true);
    if (chkAutoRecognize.isSelected()) recognize();
  }

  public void recognize() {
    try {
      if (prepareRecognizer()) return;

      if (image == null) return;

      for (Pair<Option, JTextField> pair : optionFields) {
        pair.a.setValue(pair.b.getText());
      }

      recognizer.setCollectDebugInfo(true);
      System.out.println("Starting recognition");
      recognizeInBackground(recognizer);
    } catch (Throwable e) {
      Errors.report(e);
    }
  }

  private boolean prepareRecognizer() {
    if (recognizer == null) {
      RecognizerInfo recognizerInfo = recognizerSelector.getSelectedRecognizer();
      if (recognizerInfo == null) return true;
      eyeData.setDefaultRecognizerName(recognizerInfo.getName());

      Tree code = recognizerInfo.getCode();
      System.out.println("Recognizer: " + EyeGuiUtil.shortenCode(code.toString()));
      recognizer = OCRUtil.makeImageReader(code);

      List<Option> options = new ArrayList<Option>();
      optionFields.clear();
      recognizer.collectOptions(options);
      if (options.size() != 0) {
        RightAlignedLine line = new RightAlignedLine();

        for (Option option : options) {
          line.add(option.name + ":");
          JTextField textField = new JTextField(option.value);
          textField.setMinimumSize(new Dimension(40, 10));
          line.add(textField);
          optionFields.add(new Pair<Option, JTextField>(option, textField));
        }

        optionsPanel.setComponent(line);
        //autoRecognize = false;
        return true;
      } else {
        optionsPanel.setComponent(null);
        //autoRecognize = true;
      }

      if (recognizerInfo.getInputType() == RecognizerInputType.character) {
        // If it's a character recognizer => add default segmenter
        //recognizer = new WithTextFinder1(recognizer.getDescription());

        // This one also does mark lines
        recognizer = new WithFratboySegmenter(recognizer);
      }
    }
    return false;
  }

  /*private void recognize(ImageReader recognizer) {
    String text = recognizer.readImage(image.toBW());
    recognitionDone(recognizer, text);
  }*/

  void recognizeInBackground(ImageReader recognizer) {
    InputImage inputImage = new InputImage(new BWImage(image));
    if (imageInfo != null)
      inputImage.corrections = imageInfo.getCorrections();
    new RecognitionProgressDialog(this, recognizer, inputImage).setVisible(true);
  }

  void recognitionDone(ImageReader recognizer, RecognizedText recognizedText) {
    try {
      debugInfo = recognizer.getDebugInfo();
      btnDebug.setEnabled(debugInfo != null);
      recognizableImage.setDebugInfo(debugInfo);

      ++blockImageAndTextUpdates;
      String text = recognizedText != null ? recognizedText.text : "";
      textArea.setText(text, debugInfo);
      --blockImageAndTextUpdates;
    } catch (Throwable t) {
      Errors.report(t);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void makeRecognizer() {
    new LearnAFontDialog(recognizers).setVisible(true);
  }

  public void manageRecognizers() {
    try {
      new RecognizersDialog(recognizers).setVisible(true);
    } catch (Throwable e) {
      Errors.report(e);
    }
  }

  private void challenges() {
    try {
      new ChallengesDialog(ChallengesDialog.standardChallenges(), recognizers).setVisible(true);
    } catch (Throwable e) {
      Errors.report(e);
    }
  }

  private void debug() {
    try {
      new DebugInfoWindow(debugInfo).setVisible(true);
    } catch (Throwable e) {
      Errors.report(e);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void info() {
    /*JOptionPane.showMessageDialog(frame, "Eye " + version + "\n" +
      "by Stefan Reich\n\n" +
      "http//eyeocr.sf.net", "About Eye", JOptionPane.INFORMATION_MESSAGE);*/
    new WelcomeDialog().setVisible(true);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void saveRecognizer() {
    if (recognizer == null) return;
    RecognizerInfo recognizerInfo = recognizerSelector.getSelectedRecognizer();
    String suffix = " + corrections";
    String name = recognizerInfo.getName();
    if (name.indexOf("+") < 0) name += suffix;
    if (EyeDialogs.saveRecognizer(frame, recognizers, recognizer.getDescription(),
      recognizerInfo.getPreferredFontName(), RecognizerInputType.lines, name, false) != null) {
      recognizerModified = false;

      // Now select the new recognizer in the drop-down box. (This is a little tricky
      // because when the user selects a recognizer, it triggers some stuff, and we
      // don't want that here, so we disable it. I forgot what the invokeLater is for,
      // I'm sure there's a reason why it's there :))

      eyeData.setDefaultRecognizerName(name);
      final String finalName = name;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          ignoreRecognizerChange = true;
          recognizerSelector.selectRecognizer(finalName);
          ignoreRecognizerChange = false;
        }
      });
    }
  }

  private class MyRecognizableImage extends RecognizableImage {
    @Override
    public void fillPopupMenu(JPopupMenu menu, Point point) {
      super.fillPopupMenu(menu, point);

      prependMenuItem_saveLineAsTrainingExample(menu, point);
      prependMenuItem_correctCharacter(menu);
      prependMenuItem_removeCorrection(menu, point);
    }

    private void prependMenuItem_removeCorrection(JPopupMenu menu, Point point) {
      if (imageInfo != null && imageInfo.getCorrections() != null) {
        for (final Correction correction : imageInfo.getCorrections()) {
          if (correction.getRectangle().contains(point)) {
            JMenuItem mi = new JMenuItem("Remove correction");
            mi.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                imageInfo.getCorrections().remove(correction);
                saveImageInfo();
              }
            });
            menu.insert(mi, 0);
            return;
          }
        }
      }
    }

    private void prependMenuItem_correctCharacter(JPopupMenu menu) {
      final Subrecognition subrecognition = lastMarkedSubrecognition;
      if (subrecognition != null && recognizer != null) {
        final CharacterLearner characterLearner = recognizer.getCharacterLearner();
        //System.out.println("characterLearner of " + recognizer + ": " + characterLearner);
        if (characterLearner != null) {
          String text;
          if (subrecognition.text != null)
            text = "Correct character (" + subrecognition.text + ")...";
          else
            text = "Enter character...";
          JMenuItem mi = new JMenuItem(text);
          //JMenuItem mi = new JMenuItem("Correct character...");
          mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              try {
                correctCharacter(subrecognition, characterLearner);
              } catch (Throwable e) {
                Errors.add(e);
              }
            }
          });
          if (menu.getComponentCount() != 0)
            menu.insert(new JPopupMenu.Separator(), 0);
          menu.insert(mi, 0);
        }
      }
    }

    private void prependMenuItem_saveLineAsTrainingExample(JPopupMenu menu, Point point) {
      final RecognizedLine line = findLine(point);
      if (line != null) {
        JMenuItem mi = new JMenuItem("Save line as training example");
        mi.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            saveTrainingExample(line);
          }
        });
        if (menu.getComponentCount() != 0)
          menu.insert(new JPopupMenu.Separator(), 0);
        menu.insert(mi, 0);
      }
    }

    private void correctCharacter(Subrecognition subrecognition, CharacterLearner characterLearner) {
      String newText = JOptionPane.showInputDialog("Correct character",
        subrecognition.text == null ? "" : subrecognition.text);
      if (newText != null)
        correctCharacter(subrecognition, characterLearner, newText);
    }

    private void correctCharacter(Subrecognition subrecognition, CharacterLearner characterLearner, String newText) {
      subrecognition.text = newText;
      ImageWithMarkLines imageWithMarkLines =
        new ImageWithMarkLines(subrecognition.image,
          subrecognition.topLine, subrecognition.baseLine);
      characterLearner.learnCharacter(imageWithMarkLines, newText);
      recognizerModified = true;
      if (imageInfo != null) {
        imageInfo.getCorrections().add(new Correction(subrecognition.clip, newText));
        saveImageInfo();
      }
    }

    public void subrecognitionClicked(Subrecognition s) {
      if (!quickCaretMove) {
        ++blockImageAndTextUpdates;
        textArea.jumpToLocation(s);
        --blockImageAndTextUpdates;
      }
    }
  }

  private void saveImageInfo() {
    try {
      eyeData.imageInfoDB.saveImageInfo(imageInfo);
    } catch (Throwable e) {
      Errors.add(e);
    }
    recognizableImage.setCorrections(imageInfo.getCorrections());
    recognizableImage.repaintImageSurface();
  }

  private void saveTrainingExample(RecognizedLine line) {
    try {
      // save example first, without reference to image
      TrainingExample trainingExample = new TrainingExample();
      trainingExample.setOriginalImage(imagePath == null ? null : imagePath.getName());
      trainingExample.setOriginalClip(line.boundingBox);
      trainingExample.setText(line.text);
      TrainingExampleOnDisk onDisk = eyeData.trainingExamples.save(trainingExample);

      // now we have a proper file name for the image. save it
      String filePath = onDisk.getFile().getPath();
      File imageFile = new File(filePath.substring(0, filePath.lastIndexOf(".")) + ".png");
      BufferedImage lineImage = new RGBImage(image).clip(line.boundingBox).getBufferedImage();
      ImageIO.write(lineImage, "PNG", imageFile);

      // add image reference to example, save it again
      trainingExample.setImage(imageFile.getName(), line.boundingBox.width, line.boundingBox.height);
      eyeData.trainingExamples.save(onDisk);

      showTrainingExamplesDialog();
      trainingExamplesDialog.scan();
    } catch (Throwable e) {
      Errors.report(e);
    }
  }

  private void showTrainingExamplesDialog() {
    if (trainingExamplesDialog == null)
      trainingExamplesDialog = new TrainingExamplesDialog(eyeData.trainingExamples, this);
    trainingExamplesDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    trainingExamplesDialog.setVisible(true);
  }

  private RecognizedLine findLine(Point point) {
    if (debugInfo == null) return null;
    for (DebugItem item : debugInfo) {
      if (item.data instanceof RecognizedLine) {
        RecognizedLine line = (RecognizedLine) item.data;
        if (line.boundingBox.contains(point))
          return line;
      }
    }
    return null;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void resizeImage() {
    try {
      if (image == null) return;
      String s = JOptionPane.showInputDialog(frame, "Enter scaling factor in percent:", "100");
      if (s == null) return;
      float f = Float.parseFloat(s)/100;
      RGBImage image = new RGBImage(this.image);
      image = ImageProcessing.resize(image, (int) (image.getWidth()*f), (int) (image.getHeight()*f));
      setImage(image.getBufferedImage(), imagePath);
    } catch (Throwable e) {
      Errors.report(e);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void showKnownCharacters() {
    if (recognizer == null) return;
    CharacterLearner characterLearner = recognizer.getCharacterLearner();
    if (characterLearner != null) {
      String text = StringUtil.join(" ", characterLearner.getKnownCharacters());
      String name = recognizerSelector.getSelectedRecognizer().getName();
      if (recognizerModified) name = "Modified " + name;
      CodeWindow.show(text, name + ": Known characters/ligatures", null);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void showKnownCharactersWithImages() {
    if (recognizer == null) return;
    CharacterLearner characterLearner = recognizer.getCharacterLearner();
    if (characterLearner != null)
      new KnownCharactersWindow(characterLearner).setVisible(true);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void recognizerInfo() {
    if (recognizer == null) return;
    RecognizerInfo recognizerInfo = recognizerSelector.getSelectedRecognizer();
    if (recognizerInfo == null) return;
    if (recognizerModified) {
      recognizerInfo = recognizerInfo.clone();
      recognizerInfo.setCode(recognizer.toTree());
      recognizerInfo.setName("Modified " + recognizerInfo.getName());
    }
    RecognizerInfoWindow.show(recognizerInfo);
  }
}
