package rcooper.bookmanager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import jdk.nashorn.internal.scripts.JO;
import rcooper.bookmanager.model.Book;
import rcooper.bookmanager.model.FictionalBook;
import rcooper.bookmanager.model.HistoryBook;
import rcooper.bookmanager.model.Library;
import rcooper.bookmanager.model.TextBook;
import rcooper.bookmanager.util.DateConverter;
import rcooper.bookmanager.util.LibraryReaderWriter;
import rcooper.bookmanager.util.PriceConverter;

/**
 * Creates a Book Manager Application able to record, store and display details
 * of a <code>Library</code> model.
 * 
 * @version 1.2
 * @author Rick Cooper r.p.cooper1@edu.salford.ac.uk
 */
public class BookManagerApp extends JFrame
{

	private static final String VERSION = "0.7";

	private final int SAVE_DIALOG = 0, OPEN_DIALOG = 1;
	private final Object[] BOOK_TYPES = { "Fictional", "History", "Textbook" };

	private JMenuItem mnIApplyFilter, mnIRemoveFilter, mnISortAsc, mnISortDesc, mnIUnSort;
	private JPanel reportView, detailsPanel;
	private JSplitPane detailsView;
	private JScrollPane listPane;
	private JLabel lblInfo, valTotalBooks, valTotalVal, valTotalFict, valTotalHist, valTotalText, lblFilterApplied;
	private JTextField txtTitle, txtIsbn, txtAuthor, txtPublisher, txtPrice, txtInfo, txtType, txtPubDate;
	private JButton btnAdd, btnEdit, btnRemove, btnToggle;
	private JList<String> list, lstAuthors, lstPublishers, lstPubDates;
	private Library library;
	private List<Book> tempFilter, tempSort;
	private boolean listSorted;

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					BookManagerApp frame = new BookManagerApp();
					frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/* INITIALISATION */

	public BookManagerApp()
	{
		library = new Library();
		listSorted = false;
		
		library.addBook(new FictionalBook("978-1505297409", "Treasure Island", "Robert Louis Stevenson", "Cassel & Co.",
				new GregorianCalendar(1883, 11, 14), 599, "Historical Fiction"));
		library.addBook(new FictionalBook("978-1503292383", "Robinson Crusoe", "Daniel Defoe", "W. Taylor",
				new GregorianCalendar(1719, 4, 25), 599, "Historical Fiction"));
		library.addBook(new FictionalBook("978-0552124751", "The Colour of Magic", "Terry Pratchett", "Corgi",
				new GregorianCalendar(1984, 1, 18), 550, "Fantasy/Comedy"));
		library.addBook(new TextBook("978-0132492660", "Objects First with Java", "David Barnes & Michael Kolling", "Pearson",
				new GregorianCalendar(2011, 9, 30), 5999, "Java Programming"));
		library.addBook(new TextBook("978-1118500446", "Levison's Textbook for Dental Nurses", "Carole Hollins", "Wiley-Blackwell",
				new GregorianCalendar(2013, 7, 5), 2564, "Dentistry"));
		library.addBook(new TextBook("978-3642365485", "Equilibrium Thermodynamics", "Mario J. de Oliveira", "Springer",
				new GregorianCalendar(2013, 5, 17), 6799, "Physics"));
		library.addBook(new HistoryBook("978-1846683800", "SPQR: A history of Ancient Rome", "Professor Mary Beard", "Profile Books",
				new GregorianCalendar(2015, 10, 20), 1700, "Ancient History"));
		library.addBook(new HistoryBook("978-0007503742", "The Secret War: Spies, Codes and Guerillas 1939-1945", "Max Hastings",
				"William Collins", new GregorianCalendar(2015, 9, 10), 1199, "20th Century"));
		library.addBook(new HistoryBook("978-0099502371", "The Brother Gardeners: Botany, Empire and the Birth of an Obsession",
				"Andrea Wulf", "Windmill Books", new GregorianCalendar(2009, 2, 5), 998, "18th Century"));
		init(new JPanel(), new JMenuBar());

		initDataBindings();
		pack();
	}

	private void init(JPanel mainPanel, JMenuBar menuBar)
	{
		setTitle("Book Manager");
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setJMenuBar(menuBar);
		setContentPane(mainPanel);
		initTabs(mainPanel); // app.tabbedPane setup
		initMenu(menuBar); // app.menuBar setup
	}

	private void initMenu(JMenuBar menuBar)
	{
		// // Menu bar menus ////
		JMenu mnFile = new JMenu("File");
		JMenu mnData = new JMenu("Data");
		JMenu mnHelp = new JMenu("Help");

		// // File menu items ////
		JMenuItem mnIOpen = new JMenuItem("Open...");
		JMenuItem mnISave = new JMenuItem("Save");
		JMenuItem mnIClose = new JMenuItem("Close");

		// // Data menu items ////
		mnIApplyFilter = new JMenuItem("Apply Filter");
		mnIRemoveFilter = new JMenuItem("Remove Filter");
		JMenu mnSort = new JMenu("Sort");

		// // Data menu Sort sub-items ////
		mnISortAsc = new JMenuItem("Ascending");
		mnISortDesc = new JMenuItem("Descending");
		mnIUnSort = new JMenuItem("Remove Sorting");

		// // Help menu items ////
		JMenuItem mnIAbout = new JMenuItem("About Book Manager");

		// // app.menuBar setup ////
		menuBar.add(mnFile);
		menuBar.add(mnData);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(mnHelp);

		// // app.menuBar.file setup ////
		mnFile.add(mnIOpen);
		mnFile.add(mnISave);
		mnFile.add(mnIClose);

		// // app.menuBar.data setup ////
		mnData.add(mnIApplyFilter);
		mnData.add(mnIRemoveFilter);
		mnData.add(mnSort);

		// // app.menuBar.data.sort setup ////
		mnSort.add(mnISortAsc);
		mnSort.add(mnISortDesc);
		mnSort.add(mnIUnSort);

		// // app.menuBar.help setup ////
		mnHelp.add(mnIAbout);

		mnIRemoveFilter.setEnabled(false);
		mnIUnSort.setEnabled(false);

		initActionListeners(mnIOpen, mnISave, mnIClose, mnIApplyFilter, mnIRemoveFilter, mnIAbout);
	}

	private void initTabs(JPanel mainPanel)
	{
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		detailsView = new JSplitPane();
		reportView = new JPanel();
		btnToggle = new JButton("Report View");
		mainPanel.add(btnToggle);
		mainPanel.add(detailsView);
		mainPanel.add(reportView);

		btnToggle.setAlignmentX(SwingConstants.RIGHT);
		detailsView.setAlignmentX(SwingConstants.LEFT);
		reportView.setAlignmentX(SwingConstants.LEFT);
		reportView.setVisible(false);

		initDetailsView(detailsView);
		initReportView(reportView);
	}

	private void initDetailsView(JSplitPane detailsView)
	{
		detailsPanel = new JPanel();
		listPane = new JScrollPane();
		detailsView.setLeftComponent(listPane);
		detailsView.setRightComponent(detailsPanel);

		// // app.tabbedPane.detailsView.listPane setup ////
		initListPane(listPane);

		lblFilterApplied = new JLabel();

		lblFilterApplied.setVisible(false);

		// // app.tabbedPane.detailsView.detailsPanel setup ////
		initDetailsPanel();
	}

	private void initReportView(JPanel reportView)
	{
		lstAuthors = new JList<String>();
		lstPublishers = new JList<String>();
		lstPubDates = new JList<String>();
		valTotalBooks = new JLabel("totalBooks");
		valTotalFict = new JLabel("totalFict");
		valTotalHist = new JLabel("totalHist");
		valTotalText = new JLabel("totalText");
		valTotalVal = new JLabel("totalVal");

		Font lblFont = new Font("Arial", Font.BOLD, 12);
		Font valFont = new Font("Arial", Font.PLAIN, 12);
		GridBagLayout gbl_reportView = new GridBagLayout();
		JScrollPane scrAuthors = new JScrollPane();
		JScrollPane scrPublishers = new JScrollPane();
		JScrollPane scrDates = new JScrollPane();
		JLabel lblAuthors = new JLabel("Authors");
		JLabel lblPublishers = new JLabel("Publishers");
		JLabel lblDates = new JLabel("Publish Dates");
		JLabel lblTotalBooks = new JLabel("Total No. of Books:");
		JLabel lblTotalFict = new JLabel("Total No. of Fictional Books:");
		JLabel lblTotalHistory = new JLabel("Total No. of History Books:");
		JLabel lblTotalText = new JLabel("Total No. of Text Books:");
		JLabel lblTotalValue = new JLabel("Total Value of all Books:");

		reportView.setLayout(gbl_reportView);

		GridBagConstraints gbc_scrAuthors = new GridBagConstraints();
		gbc_scrAuthors.weighty = 1.0;
		gbc_scrAuthors.insets = new Insets(0, 0, 0, 5);
		gbc_scrAuthors.gridheight = 5;
		gbc_scrAuthors.fill = GridBagConstraints.BOTH;
		gbc_scrAuthors.weightx = 0.2;
		gbc_scrAuthors.gridx = 0;
		gbc_scrAuthors.gridy = 0;
		reportView.add(scrAuthors, gbc_scrAuthors);

		GridBagConstraints gbc_scrPublishers = new GridBagConstraints();
		gbc_scrPublishers.weighty = 1.0;
		gbc_scrPublishers.insets = new Insets(0, 0, 0, 5);
		gbc_scrPublishers.weightx = 0.1;
		gbc_scrPublishers.gridheight = 5;
		gbc_scrPublishers.fill = GridBagConstraints.BOTH;
		gbc_scrPublishers.gridx = 1;
		gbc_scrPublishers.gridy = 0;
		reportView.add(scrPublishers, gbc_scrPublishers);

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 0.2;
		gbc_scrollPane.weightx = 0.2;
		gbc_scrollPane.gridheight = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 0;
		reportView.add(scrDates, gbc_scrollPane);

		GridBagConstraints gbc_lblTotalBooks = new GridBagConstraints();
		gbc_lblTotalBooks.anchor = GridBagConstraints.LINE_END;
		gbc_lblTotalBooks.weighty = 0.2;
		gbc_lblTotalBooks.weightx = 0.2;
		gbc_lblTotalBooks.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalBooks.gridx = 3;
		gbc_lblTotalBooks.gridy = 0;
		reportView.add(lblTotalBooks, gbc_lblTotalBooks);

		GridBagConstraints gbc_valTotalbooks = new GridBagConstraints();
		gbc_valTotalbooks.fill = GridBagConstraints.HORIZONTAL;
		gbc_valTotalbooks.anchor = GridBagConstraints.LINE_START;
		gbc_valTotalbooks.weighty = 0.2;
		gbc_valTotalbooks.weightx = 0.2;
		gbc_valTotalbooks.insets = new Insets(0, 0, 5, 35);
		gbc_valTotalbooks.gridx = 4;
		gbc_valTotalbooks.gridy = 0;
		reportView.add(valTotalBooks, gbc_valTotalbooks);

		GridBagConstraints gbc_lblTotalFict = new GridBagConstraints();
		gbc_lblTotalFict.anchor = GridBagConstraints.LINE_END;
		gbc_lblTotalFict.weighty = 0.2;
		gbc_lblTotalFict.weightx = 0.2;
		gbc_lblTotalFict.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalFict.gridx = 3;
		gbc_lblTotalFict.gridy = 1;
		reportView.add(lblTotalFict, gbc_lblTotalFict);

		GridBagConstraints gbc_valTotalFict = new GridBagConstraints();
		gbc_valTotalFict.insets = new Insets(0, 0, 5, 35);
		gbc_valTotalFict.fill = GridBagConstraints.HORIZONTAL;
		gbc_valTotalFict.anchor = GridBagConstraints.LINE_START;
		gbc_valTotalFict.weightx = 0.2;
		gbc_valTotalFict.weighty = 0.2;
		gbc_valTotalFict.gridx = 4;
		gbc_valTotalFict.gridy = 1;
		reportView.add(valTotalFict, gbc_valTotalFict);

		GridBagConstraints gbc_lblTotalHistory = new GridBagConstraints();
		gbc_lblTotalHistory.anchor = GridBagConstraints.LINE_END;
		gbc_lblTotalHistory.weighty = 0.2;
		gbc_lblTotalHistory.weightx = 0.2;
		gbc_lblTotalHistory.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalHistory.gridx = 3;
		gbc_lblTotalHistory.gridy = 2;
		reportView.add(lblTotalHistory, gbc_lblTotalHistory);

		GridBagConstraints gbc_valTotalHist = new GridBagConstraints();
		gbc_valTotalHist.insets = new Insets(0, 0, 5, 35);
		gbc_valTotalHist.fill = GridBagConstraints.HORIZONTAL;
		gbc_valTotalHist.anchor = GridBagConstraints.LINE_START;
		gbc_valTotalHist.weighty = 0.2;
		gbc_valTotalHist.weightx = 0.2;
		gbc_valTotalHist.gridx = 4;
		gbc_valTotalHist.gridy = 2;
		reportView.add(valTotalHist, gbc_valTotalHist);

		GridBagConstraints gbc_lblTotalText = new GridBagConstraints();
		gbc_lblTotalText.anchor = GridBagConstraints.LINE_END;
		gbc_lblTotalText.weighty = 0.2;
		gbc_lblTotalText.weightx = 0.2;
		gbc_lblTotalText.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalText.gridx = 3;
		gbc_lblTotalText.gridy = 3;
		reportView.add(lblTotalText, gbc_lblTotalText);

		GridBagConstraints gbc_valTotalText = new GridBagConstraints();
		gbc_valTotalText.insets = new Insets(0, 0, 5, 35);
		gbc_valTotalText.anchor = GridBagConstraints.LINE_START;
		gbc_valTotalText.fill = GridBagConstraints.HORIZONTAL;
		gbc_valTotalText.weighty = 0.2;
		gbc_valTotalText.weightx = 0.2;
		gbc_valTotalText.gridx = 4;
		gbc_valTotalText.gridy = 3;
		reportView.add(valTotalText, gbc_valTotalText);

		GridBagConstraints gbc_lblTotalValue = new GridBagConstraints();
		gbc_lblTotalValue.anchor = GridBagConstraints.LINE_END;
		gbc_lblTotalValue.weighty = 0.2;
		gbc_lblTotalValue.weightx = 0.2;
		gbc_lblTotalValue.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalValue.gridx = 3;
		gbc_lblTotalValue.gridy = 4;
		reportView.add(lblTotalValue, gbc_lblTotalValue);

		GridBagConstraints gbc_valTotalVal = new GridBagConstraints();
		gbc_valTotalVal.insets = new Insets(0, 0, 0, 35);
		gbc_valTotalVal.anchor = GridBagConstraints.LINE_START;
		gbc_valTotalVal.fill = GridBagConstraints.HORIZONTAL;
		gbc_valTotalVal.weighty = 0.2;
		gbc_valTotalVal.weightx = 0.2;
		gbc_valTotalVal.gridx = 4;
		gbc_valTotalVal.gridy = 4;
		reportView.add(valTotalVal, gbc_valTotalVal);

		valTotalBooks.setFont(valFont);
		valTotalBooks.setHorizontalAlignment(SwingConstants.CENTER);
		valTotalBooks.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		valTotalFict.setFont(valFont);
		valTotalFict.setHorizontalAlignment(SwingConstants.CENTER);
		valTotalFict.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		valTotalHist.setFont(valFont);
		valTotalHist.setHorizontalAlignment(SwingConstants.CENTER);
		valTotalHist.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		valTotalText.setFont(valFont);
		valTotalText.setHorizontalAlignment(SwingConstants.CENTER);
		valTotalText.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		valTotalVal.setFont(valFont);
		valTotalVal.setHorizontalAlignment(SwingConstants.CENTER);
		valTotalVal.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		lblAuthors.setFont(new Font("Arial", Font.PLAIN, 14));
		lblAuthors.setHorizontalAlignment(SwingConstants.CENTER);
		lblPublishers.setFont(new Font("Arial", Font.PLAIN, 14));
		lblPublishers.setHorizontalAlignment(SwingConstants.CENTER);
		lblDates.setFont(new Font("Arial", Font.PLAIN, 14));
		lblDates.setHorizontalAlignment(SwingConstants.CENTER);
		lblTotalBooks.setFont(lblFont);
		lblTotalFict.setFont(lblFont);
		lblTotalHistory.setFont(lblFont);
		lblTotalText.setFont(lblFont);
		lblTotalValue.setFont(lblFont);

		scrAuthors.setColumnHeaderView(lblAuthors);
		scrAuthors.setViewportView(lstAuthors);
		scrPublishers.setColumnHeaderView(lblPublishers);
		scrPublishers.setViewportView(lstPublishers);
		scrDates.setColumnHeaderView(lblDates);
		scrDates.setViewportView(lstPubDates);
	}

	private void initDetailsPanel()
	{
		JLabel lblType = new JLabel("Type:");
		JLabel lblIsbn = new JLabel("ISBN:");
		JLabel lblTitle = new JLabel("Title:");
		JLabel lblAuthor = new JLabel("Author:");
		JLabel lblPublisher = new JLabel("Publisher:");
		JLabel lblPubDate = new JLabel("Publication Date:");
		JLabel lblPrice = new JLabel("Retail Price:");
		JPanel pnlControls = new JPanel(new GridBagLayout());
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		lblInfo = new JLabel();
		txtType = new JTextField();
		txtIsbn = new JTextField();
		txtTitle = new JTextField();
		txtAuthor = new JTextField();
		txtPublisher = new JTextField();
		txtPubDate = new JTextField();
		txtPrice = new JTextField();
		txtInfo = new JTextField();

		layout.columnWeights = new double[] { 0.15, 0.4, 0.3, 0.15 };
		layout.rowWeights = new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 };
		detailsPanel.setLayout(layout);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		detailsPanel.add(lblTitle, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(lblAuthor, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(lblPubDate, gbc);
		detailsPanel.add(lblInfo, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		gbc.gridx = 2;
		detailsPanel.add(lblType, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(lblPrice, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(lblPublisher, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(lblIsbn, gbc);

		gbc = (GridBagConstraints) gbc.clone();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		detailsPanel.add(txtTitle, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(txtAuthor, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(txtPubDate, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(txtInfo, gbc);

		gbc = (GridBagConstraints) gbc.clone();
		gbc.insets = new Insets(0, 0, 0, 35);
		gbc.gridx = 3;
		detailsPanel.add(txtType, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(txtPrice, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(txtPublisher, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		detailsPanel.add(txtIsbn, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.BOTH;
		detailsPanel.add(pnlControls, gbc);

		txtType.setEditable(false);
		setDetailsVisible(false);
		setFieldsEditable(false);
		initDetailControls(pnlControls);
	}

	private void initDetailControls(JPanel pnlControls)
	{
		btnAdd = new JButton("Add");
		btnEdit = new JButton("Edit");
		btnRemove = new JButton("Remove");

		Insets buttonInsets = new Insets(10, 20, 10, 20);

		btnAdd.setMargin(buttonInsets);
		btnEdit.setEnabled(false);
		btnEdit.setMargin(buttonInsets);
		btnRemove.setMargin(buttonInsets);
		btnRemove.setEnabled(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.3;
		pnlControls.add(btnAdd, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		pnlControls.add(btnEdit, gbc);
		gbc = (GridBagConstraints) gbc.clone();
		pnlControls.add(btnRemove, gbc);
	}

	private void initListPane(JScrollPane listPane)
	{
		list = new JList<String>();

		listPane.setViewportView(list);
		listPane.setPreferredSize(new Dimension(250, 600));
		listPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFont(new Font("Arial", Font.PLAIN, 10));
	}

	private void initActionListeners(JMenuItem mnIOpen, JMenuItem mnISave, JMenuItem mnIClose, JMenuItem mnIApplyFilter,
			JMenuItem mnIRemoveFilter, JMenuItem mnIAbout)
	{
		mnISave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectFile(SAVE_DIALOG);
			}
		});

		mnIOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectFile(OPEN_DIALOG);
			}
		});

		mnIClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				closeLibrary();
			}
		});

		mnIApplyFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				applyFilter();
			}
		});

		mnIRemoveFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				removeFilter();
			}
		});

		mnISortAsc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				adjustMenuForSorting();
				library.sortAscending();
			}
		});

		mnISortDesc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				adjustMenuForSorting();
				library.sortDescending();
			}
		});

		mnIUnSort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				listSorted = false;
				mnIUnSort.setEnabled(listSorted);
				if(tempFilter == null) {
					library.replaceBooks(tempSort);
				} else {
					removeFilter();
				}
			}
		});

		mnIAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				aboutDialog();
			}
		});

		btnToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String lblReportView = "Report View";
				if(btnToggle.getText().equals(lblReportView)) {
					btnToggle.setText("Details View");
				} else {
					btnToggle.setText(lblReportView);
				}
				detailsView.setVisible(!detailsView.isVisible());
				reportView.setVisible(!reportView.isVisible());
				updateTotals();
			}
		});

		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int index = list.getSelectedIndex();
				if(list.isSelectionEmpty()) {
					index++;
				}
				addBook(index, BOOK_TYPES[0]); // Fictional by default
			}
		});

		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFieldsEditable(true);
			}
		});

		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				removeBook();
			}
		});

		list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				setFieldsEditable(false);
				if(!list.isSelectionEmpty()) {
					setDetailsVisible(true);
					btnEdit.setEnabled(true);
					btnRemove.setEnabled(true);
				} else {
					setDetailsVisible(false);
					btnEdit.setEnabled(false);
					btnRemove.setEnabled(false);
				}
			}
		});
	}

	/* ADD/REMOVE BOOK */

	/*
	 * Adds a book at the specified index (either selected index or 0, moving
	 * books up one index that follow). Takes BOOK_TYPES element as booktype.
	 */
	private Book addBook(int index, Object booktype)
	{
		Book book = null;
		String choice = (String) JOptionPane.showInputDialog(this, "Please select a book type:", "Type Selection",
				JOptionPane.PLAIN_MESSAGE, null, BOOK_TYPES, booktype);
		if(choice != null) { // Cancel not pressed
			switch(choice) {
			case "Fictional":
				book = new FictionalBook();
				break;
			case "History":
				book = new HistoryBook();
				break;
			case "Textbook":
				book = new TextBook();
				break;
			}

			library.addBook(index, book);
			list.setSelectedIndex(index);
			setFieldsEditable(true);
		}
		return book;
	}

	/*
	 * Removes the currently selected book after checking with the user.
	 */
	private void removeBook()
	{
		int currentIndex = list.getSelectedIndex();
		Object[] options = { "Confirm", "Cancel" };
		int option = JOptionPane.showOptionDialog(this, "Are you sure you want to remove this book?", "Remove Book",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if(option == JOptionPane.YES_OPTION) {
			library.removeBook(library.getBook(currentIndex));
			list.setSelectedIndex(currentIndex);
		}
	}

	/* FILTER FUNCTIONALITY */

	/*
	 * Applies a filter by allowing the user to select a start and optional end
	 * date.
	 */
	private void applyFilter()
	{
		JTextField txtStartDate = new JTextField();
		JTextField txtEndDate = new JTextField();

		if(!library.isEmpty()) {
			int option = getDateRange(txtStartDate, txtEndDate);
			if(option == JOptionPane.YES_OPTION) {
				GregorianCalendar startCal = getDateFromString(txtStartDate.getText(), false);
				GregorianCalendar endCal = getDateFromString(txtEndDate.getText(), true);
				GregorianCalendar now = new GregorianCalendar();

				if(startCal != null) {
					if(startCal.compareTo(now) < 1) {
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
						String start = formatter.format(startCal.getTime());
						String end = formatter.format(endCal.getTime());
						List<Book> books = library.getBooksFilteredByDate(startCal, endCal);

						if(books != null) {
							tempFilter = library.getBooks();
							library.replaceBooks(books);
							lblFilterApplied.setText("Filter applied: " + start + " - " + end);
							indicateFilter(true);
							updateTotals();
						} else {
							dateErrorEndBeforeStartDialog();
						}
					} else {
						dateErrorFormatDialog();
					}
				}
			}
		} else {
			noBooksDialog();
		}
	}

	/*
	 * Removes a filter if it has been applied.
	 */
	private void removeFilter()
	{
		library.replaceBooks(tempFilter);
		indicateFilter(false);
		updateTotals();
	}

	/*
	 * Attempts to parse a given string into date format, showing a warning
	 * dialog if input is incorrect.
	 */
	private GregorianCalendar getDateFromString(String strDate, boolean isEnd)
	{
		SimpleDateFormat all = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat monthYear = new SimpleDateFormat("MM/yyyy");
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		GregorianCalendar calendar = null;
		Date date = null;
		try {
			date = all.parse(strDate);
		} catch(ParseException e) {
			try {
				date = monthYear.parse(strDate);
			} catch(ParseException f) {
				try {
					date = year.parse(strDate);
				} catch(ParseException g) {
					if(!isEnd) {
						dateErrorFormatDialog();
					}
				}
			}
		}
		if(date != null || isEnd) {
			calendar = new GregorianCalendar();
		}
		if(date != null) {
			calendar.setTime(date);
		}
		return calendar;
	}

	/* SAVE OPEN CLOSE */

	/*
	 * Attempts to save the list of books within a library to a file, showing a
	 * warning dialog if there is an I/O error.
	 */
	private void saveLibrary(LibraryReaderWriter lrw, String path, String fileName)
	{
		try {
			lrw.writeObjects(library.getBooks());
		} catch(IOException ioe) {
			fatalException(ioe);
		}
	}

	/*
	 * Attempts to open a .blf file. Shows a warning dialog if the incorrect
	 * file extension is specified, the file has been deleted, or for an I/O
	 * error.
	 */
	private void openLibrary(LibraryReaderWriter lrw, String path, String fileName)
	{
		List<Book> books = library.getBooks();
		try {
			library.replaceBooks(lrw.readObjects());
		} catch(IOException ioe) { // Closing stream fail
			fatalException(ioe);
		} catch(NullPointerException npe) {
			String message = "Error opening file: ";
			String extension = fileName.split("\\.")[1];
			if(extension.equals("blf")) {
				message += "Library corrupt or deleted.";
			} else {
				message += "Not a Book Library File.";
			}
			JOptionPane.showMessageDialog(this, message, "File Error", JOptionPane.WARNING_MESSAGE);
			library.replaceBooks(books); // Reopen original library
		}
	}

	/*
	 * Closes the current library by clearing the it's list.
	 */
	private void closeLibrary()
	{
		if(library.isEmpty()) {
			noBooksDialog();
		} else {
			String message = "If you close the library you will lose any " + "unsaved changes! Do you wish to save?";
			int choice = JOptionPane.showConfirmDialog(this, message, "Close Library",
					JOptionPane.YES_NO_CANCEL_OPTION);

			switch(choice) {
			case JOptionPane.YES_OPTION:
				selectFile(SAVE_DIALOG);
				/* falls through */
			case JOptionPane.NO_OPTION:
				library.replaceBooks(new ArrayList<Book>());
				break;
			}
		}
	}

	/*
	 * Open dialog for file opening. Adds extension if not typed. Checks 
	 * overwrites. 
	 */
	private void selectFile(int dialogChoice)
	{
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Book Library File", "blf");
		JFileChooser fc = new JFileChooser(new File(".")); // Set dir to same as
															// jar
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		int action = -1; // Non-value
		if(dialogChoice == SAVE_DIALOG) {
			action = fc.showSaveDialog(this);
		} else if(dialogChoice == OPEN_DIALOG) {
			action = fc.showOpenDialog(this);
		}

		if(action == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(!file.getName().endsWith(".blf")) {
				file = new File(file + ".blf");
			}
			String path = file.getAbsolutePath();
			LibraryReaderWriter lrw = new LibraryReaderWriter(path);
			if(dialogChoice == SAVE_DIALOG) {
				int choice = JOptionPane.NO_OPTION;
				if(file.exists()) {
					choice = checkOverwriteDialog();
				}
				if(choice == JOptionPane.YES_OPTION) {
					saveLibrary(lrw, path, file.getName());
				}
			} else if(dialogChoice == OPEN_DIALOG) {
				openLibrary(lrw, path, file.getName());
			}
		}
	}

	/* GUI ADJUSTMENTS */

	/*
	 * Turns various elements on or off when a filter is applied.
	 */
	private void indicateFilter(boolean filterApplied)
	{
		mnIApplyFilter.setEnabled(!filterApplied);
		lblFilterApplied.setVisible(filterApplied);
		mnIRemoveFilter.setEnabled(filterApplied);
		if(filterApplied) {
			listPane.setColumnHeaderView(lblFilterApplied);
		} else {
			listPane.setColumnHeaderView(null);
		}
	}

	/*
	 * Switches the visibility of all text fields and labels in the details
	 * panel.
	 */
	private void setDetailsVisible(boolean isVisible)
	{
		for(Component component : detailsPanel.getComponents()) {
			if(!(component instanceof JPanel)) {
				component.setVisible(isVisible);
			}
		}
	}

	/*
	 * Switches the editability of the text fields we want the user to have
	 * access to.
	 */
	private void setFieldsEditable(boolean isEditable)
	{
		for(Component component : detailsPanel.getComponents()) {
			if(component instanceof JTextField && !component.equals(txtType)) {
				JTextField textField = (JTextField) component;
				textField.setEditable(isEditable);
			}
		}
	}

	/*
	 * Called by toggle reports button action listener
	 */
	private void updateTotals()
	{
		valTotalBooks.setText(library.getBookCount() + "");
		valTotalFict.setText(library.getFictionalCount() + "");
		valTotalHist.setText(library.getHistoryCount() + "");
		valTotalText.setText(library.getTextCount() + "");
		valTotalVal.setText(new PriceConverter().convertForward(library.getTotalPrices()));
	}
	
	/*
	 * Enables unsort option and stores the library in its original condition 
	 * if not already done.
	 */
	private void adjustMenuForSorting()
	{	
		if(!listSorted) {
			tempSort = library.getBooks();
		}
		listSorted = true;
		mnIUnSort.setEnabled(listSorted);
	}

	/* DIALOGS */

	/*
	 * Panel for selecting start and end dates from user input.
	 */
	private int getDateRange(JTextField txtStartDate, JTextField txtEndDate)
	{
		String message = "Please enter a start date and an optional end date.";
		Object[] options = new Object[] { "OK", "Cancel" };
		JPanel mainPanel = new JPanel(new GridLayout(2, 0));
		JPanel optionPanel = new JPanel(new GridLayout(0, 3));
		JLabel dash = new JLabel("-");

		dash.setHorizontalAlignment(SwingConstants.CENTER);
		optionPanel.add(new JLabel("Start Date (e.g. dd/mm/yyyy):"));
		optionPanel.add(Box.createHorizontalStrut(5));
		optionPanel.add(new JLabel("End Date (e.g. dd/mm/yyyy):"));
		optionPanel.add(txtStartDate);
		optionPanel.add(dash);
		optionPanel.add(txtEndDate);
		mainPanel.add(new JLabel(message));
		mainPanel.add(optionPanel);

		return JOptionPane.showOptionDialog(this, mainPanel, "Pick Date Range", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}

	private int checkOverwriteDialog()
	{
		Object[] options = { "Yes", "No" };
		return JOptionPane.showOptionDialog(null, "The file already exists. Do you wish to overwrite?", "Save", JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
	}
	
	private void aboutDialog()
	{
		JOptionPane.showMessageDialog(this, "Book Manager App Version " + VERSION);
	}

	private void fatalException(Exception e)
	{
		String message = "A fatal error has occurred! The program will now close.";
		JOptionPane.showMessageDialog(this, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	private void noBooksDialog()
	{
		JOptionPane.showMessageDialog(this, "There are no books in this library.", "Empty library",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void dateErrorFormatDialog()
	{
		dateErrorDialog("Dates must be entered in the valid format: dd/mm/yyyy, mm/yyyy or yyyy.");

	}

	private void dateErrorEndBeforeStartDialog()
	{
		dateErrorDialog("If specified, the end-date must not be earlier than the start-date.");
	}

	private void dateErrorDialog(String message)
	{
		JOptionPane.showMessageDialog(this, message + "\nPlease try again", "Date Error", JOptionPane.ERROR_MESSAGE);
	}
	protected void initDataBindings() {
		BeanProperty<Library, List<Book>> libraryBeanProperty = BeanProperty.create("books");
		JListBinding<Book, Library, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, library, libraryBeanProperty, list);
		//
		ELProperty<Book, Object> bookEvalutionProperty = ELProperty.create("${title} - ${author}");
		jListBinding.setDetailBinding(bookEvalutionProperty);
		//
		jListBinding.bind();
		//
		JListBinding<Book, Library, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, library, libraryBeanProperty, lstAuthors);
		//
		ELProperty<Book, Object> bookEvalutionProperty_1 = ELProperty.create("${author}");
		jListBinding_1.setDetailBinding(bookEvalutionProperty_1);
		//
		jListBinding_1.bind();
		//
		JListBinding<Book, Library, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ, library, libraryBeanProperty, lstPublishers);
		//
		ELProperty<Book, Object> bookEvalutionProperty_2 = ELProperty.create("${publisher}");
		jListBinding_2.setDetailBinding(bookEvalutionProperty_2);
		//
		jListBinding_2.bind();
		//
		JListBinding<Book, Library, JList> jListBinding_3 = SwingBindings.createJListBinding(UpdateStrategy.READ, library, libraryBeanProperty, lstPubDates);
		//
		ELProperty<Book, Object> bookEvalutionProperty_3 = ELProperty.create("${strDate}");
		jListBinding_3.setDetailBinding(bookEvalutionProperty_3);
		//
		jListBinding_3.bind();
		//
		BeanProperty<JList, String> jListBeanProperty = BeanProperty.create("selectedElement.title");
		BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
		AutoBinding<JList, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty, txtTitle, jTextFieldBeanProperty);
		autoBinding.bind();
		//
		BeanProperty<JList, String> jListBeanProperty_1 = BeanProperty.create("selectedElement.author");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
		AutoBinding<JList, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_1, txtAuthor, jTextFieldBeanProperty_1);
		autoBinding_1.bind();
		//
		BeanProperty<JList, String> jListBeanProperty_2 = BeanProperty.create("selectedElement.publisher");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
		AutoBinding<JList, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_2, txtPublisher, jTextFieldBeanProperty_2);
		autoBinding_2.bind();
		//
		BeanProperty<JList, String> jListBeanProperty_3 = BeanProperty.create("selectedElement.type");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
		AutoBinding<JList, String, JTextField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_3, txtType, jTextFieldBeanProperty_3);
		autoBinding_3.bind();
		//
		BeanProperty<JList, Integer> jListBeanProperty_4 = BeanProperty.create("selectedElement.priceInPence");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
		AutoBinding<JList, Integer, JTextField, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_4, txtPrice, jTextFieldBeanProperty_4);
		autoBinding_4.setConverter(new PriceConverter());
		autoBinding_4.bind();
		//
		BeanProperty<JList, String> jListBeanProperty_5 = BeanProperty.create("selectedElement.infoLabel");
		BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
		AutoBinding<JList, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_5, lblInfo, jLabelBeanProperty);
		autoBinding_5.bind();
		//
		BeanProperty<JList, String> jListBeanProperty_6 = BeanProperty.create("selectedElement.infoValue");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_5 = BeanProperty.create("text");
		AutoBinding<JList, String, JTextField, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_6, txtInfo, jTextFieldBeanProperty_5);
		autoBinding_6.bind();
		//
		BeanProperty<JList, GregorianCalendar> jListBeanProperty_7 = BeanProperty.create("selectedElement.pubDate");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_6 = BeanProperty.create("text");
		AutoBinding<JList, GregorianCalendar, JTextField, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, list, jListBeanProperty_7, txtPubDate, jTextFieldBeanProperty_6);
		autoBinding_12.setConverter(new DateConverter());
		autoBinding_12.bind();
		//
		BeanProperty<JList, String> jListBeanProperty_8 = BeanProperty.create("selectedElement.isbn");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_7 = BeanProperty.create("text");
		AutoBinding<JList, String, JTextField, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_8, txtIsbn, jTextFieldBeanProperty_7);
		autoBinding_7.bind();
	}
}