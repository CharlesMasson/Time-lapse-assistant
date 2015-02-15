package interfaceGraphique;

import interfaceGraphique.arborescenceDossiers.FileTree;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import coeur.ExifTool;
import coeur.ExifToolFerméException;
import coeur.Photo;
import coeur.Tag;
import coeur.Timelapse;
import coeur.TypeInterpolation;
import interfaceGraphique.arborescenceDossiers.FileTreeSelectionEvent;
import interfaceGraphique.arborescenceDossiers.FileTreeSelectionListener;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class TLAssistant extends JFrame {

	private static final long serialVersionUID = 1L;

	private final static long ATTENTE_TERMINAISON_THREAD = 50;

	private static File dossierCourant, exécutableExifTool;

	private double ratioPhotos;
	private int intervalleLecture;
	private ButtonGroup groupeBoutonsDéflickage;
	private ExifTool fluxExifTool;
	private File dossier, dossierJPEG;
	private FileTree arborescence;
	private JButton lire, traiter;
	private JCheckBox activerDéflickage, activerInterpolation;
	private JPanel barreStatut, conteneurDroit, conteneurGauche,
			conteneurInfoPhotos, conteneurMoyenneDéflickage,
			conteneurPrévisualisation, contrôles, déflickage, interpolation,
			paramètresDéflickage, traitement;
	private JPanelImage conteneurImage;
	private JProgressBar barreProgression;
	private JRadioButton constante, moyenne;
	private JScrollBar barreDeDéfilement;
	private JScrollPane conteneurTableau, conteneurTableauTags;
	private JSpinner nombrePhotosMoyenne;
	private JSplitPane conteneurPrincipal;
	private JTable représentationTableau, représentationTableauTags;
	private JTextField statut;
	private AbstractTableModel tableau;
	private AbstractTableModel tableauTags;
	private JTextField texteMoyenneCalculée, textePhotos;
	private Thread threadChargementTimelapse, threadLecture,
			threadTraitementTimelapse;
	private Timelapse timelapse;

	private FilenameFilter filtreExtensions = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".dng")
					|| name.toLowerCase().endsWith(".tif");
		}
	};

	private final Runnable CHARGEMENT_TIMELAPSE = new Runnable() {
		private void initialiserChargement() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Chargement de la liste des fichiers");
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void initialiserTableau(final int maximum) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					tableau.fireTableStructureChanged();
					représentationTableau.getSelectionModel().addSelectionInterval(0, 0);
					barreDeDéfilement.setValues(0, 0, 0, maximum - 1);
					barreDeDéfilement.setEnabled(true);
					lire.setEnabled(true);
					barreProgression.setMinimum(0);
					barreProgression.setMaximum(maximum);
					barreProgression.setVisible(true);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void mettreÀJourStatut(final int valeur, final int nombrePhotos) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Chargement de la photo " + (valeur + 1) + " sur "
							+ nombrePhotos + " (" + (100 * valeur / nombrePhotos) + " %).");
					barreProgression.setValue(valeur);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void mettreÀJourTableau(final int valeur) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					tableau.fireTableCellUpdated(valeur, 1);
					tableau.fireTableCellUpdated(valeur, 3);
					if (représentationTableau.getSelectedRow() == valeur)
						conteneurImage.afficherImage(timelapse.photos[représentationTableau
								.getSelectedRow()].prévisualisation);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void terminerChargement() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Chargement terminé");
					barreProgression.setVisible(false);
					tableauTags.fireTableStructureChanged();
					activerDéflickage.setSelected(true);
					activerDéflickage.setEnabled(true);
					activerInterpolation.setEnabled(true);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void actualiserAffichageSiTimelapseVide() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Le dossier sélectionné ne contient pas de photos (" + dossier + ").");
					barreDeDéfilement.setEnabled(false);
					lire.setEnabled(false);
					tableau.fireTableStructureChanged();
					tableauTags.fireTableStructureChanged();
					activerDéflickage.setSelected(false);
					activerDéflickage.setEnabled(false);
					activerInterpolation.setSelected(false);
					activerInterpolation.setEnabled(false);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		@Override
		public void run() {
			initialiserChargement();
			File[] listeFichiers = dossier.listFiles(filtreExtensions);
			Arrays.sort(listeFichiers);
			if (listeFichiers.length > 0) {
				timelapse = new Timelapse(listeFichiers.length);
				initialiserTableau(listeFichiers.length);
				for (int i = 0; i < listeFichiers.length; i++) {
					if (Thread.currentThread().isInterrupted())
						return;
					mettreÀJourStatut(i, listeFichiers.length + 1);
					try {
						timelapse.photos[i] = new Photo(fluxExifTool, dossierJPEG,
								listeFichiers[i]);
					} catch (ExifToolFerméException e) {
						e.printStackTrace();
					}
					if (Thread.currentThread().isInterrupted())
						return;
					mettreÀJourTableau(i);
				}
				if (Thread.currentThread().isInterrupted())
					return;
				timelapse.initialiserTags();
				terminerChargement();
			} else {
				timelapse = null;
				actualiserAffichageSiTimelapseVide();
			}
			// Code permettant le calcul de l'écart-type de la luminosité des photos
      /*
			 * float moyenne = 0f; for (Photo p : timelapse.photos) moyenne +=
			 * p.luminosité; moyenne /= timelapse.photos.length;
			 * System.out.println(moyenne); float variance = 0f; for (Photo p :
			 * timelapse.photos) variance += (p.luminosité - moyenne) * (p.luminosité
			 * - moyenne); System.out.println(Math.sqrt(variance));
			 */

		}
	};

	private final Runnable TRAITEMENT = new Runnable() {
		private void initialiserTraitement() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					activerDéflickage.setEnabled(false);
					activerInterpolation.setEnabled(false);
					traiter.setText("Annuler le traitement");
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void initialiserInterpolation() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Interpolation en cours");

				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void initialiserEnregistrement() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					barreProgression.setMinimum(0);
					barreProgression.setMaximum(timelapse.photos.length);
					barreProgression.setVisible(true);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void mettreÀJourStatut(final int valeur, final int nombrePhotos) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Enregistrement des tags de la photo " + (valeur + 1)
							+ " sur " + nombrePhotos + " (" + (100 * valeur / nombrePhotos)
							+ " %).");
					barreProgression.setValue(valeur);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void terminer() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Traitement terminé.");
					barreProgression.setVisible(false);
					traiter.setText("Lancer le traitement");
					tableauTags.fireTableStructureChanged();
					activerDéflickage.setSelected(false);
					activerDéflickage.setEnabled(true);
					activerInterpolation.setEnabled(true);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		private void annuler() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					statut.setText("Traitement annulé.");
					barreProgression.setVisible(false);
					traiter.setText("Lancer le traitement");
					activerDéflickage.setEnabled(true);
					activerInterpolation.setEnabled(true);
				}
			};
			SwingUtilities.invokeLater(r);
		}

		@Override
		public void run() {
			initialiserTraitement();
			Set<Tag> tagsInterpolés = timelapse.tagsInterpolés();
			if (activerInterpolation.isSelected()) {
				initialiserInterpolation();
				for (Tag tag : tagsInterpolés)
					timelapse.interpoler(tag, TypeInterpolation.LINÉAIRE);
			}
			if (activerDéflickage.isSelected()) {
				timelapse.déflicker(constante.isSelected(),
						(int) nombrePhotosMoyenne.getValue());
				tagsInterpolés.add(Tag.EXPOSITION);
			}
			initialiserEnregistrement();
			for (int i = 0; i < timelapse.photos.length; i++) {
				if (Thread.currentThread().isInterrupted()) {
					annuler();
					return;
				}
				mettreÀJourStatut(i, timelapse.photos.length);
				try {
					timelapse.photos[i].enregistrerMédonnéesDansFichier(fluxExifTool,
							tagsInterpolés);
				} catch (ExifToolFerméException e) {
					e.printStackTrace();
				}
			}
			terminer();
		}
	};

	private final Runnable LECTURE = new Runnable() {
		private void terminer() {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					lire.setText("Lire");
				}
			};
			SwingUtilities.invokeLater(r);
		}

		@Override
		public void run() {
			int i;
			while (true) {
				i = représentationTableau.getSelectedRow() + 1;
				if (Thread.currentThread().isInterrupted()
						|| i >= timelapse.photos.length || timelapse.photos[i] == null) {
					terminer();
					return;
				}
				représentationTableau.getSelectionModel().addSelectionInterval(i, i);
				try {
					Thread.sleep(intervalleLecture);
				} catch (InterruptedException e) {
					terminer();
					return;
				}
			}
		}
	};

	public TLAssistant() {
		super("Assistant de création de time-lapses");
		setPreferredSize(new Dimension(1200, 800));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (threadTraitementTimelapse != null
						&& threadTraitementTimelapse.isAlive())
					if (JOptionPane
							.showConfirmDialog(
									null,
									"Le programme est actuellement en train de traiter vos photos et d'enregistrer les modifications.\nSi vous le fermez maintenant, les données non enregistrées seront perdues.\nSouhaitez-vous le fermer malgré tout ?",
									"Opération en cours", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
						threadTraitementTimelapse.interrupt();
					else
						return;
				if (threadChargementTimelapse != null)
					threadChargementTimelapse.interrupt();
				if (threadLecture != null)
					threadLecture.interrupt();
				while ((threadTraitementTimelapse != null && threadTraitementTimelapse
						.isAlive())
						|| (threadChargementTimelapse != null && threadChargementTimelapse
						.isAlive())
						|| (threadLecture != null && threadLecture.isAlive()))
					try {
						Thread.sleep(ATTENTE_TERMINAISON_THREAD);
					} catch (InterruptedException e1) {
					}
				fluxExifTool.fermer();
				dispose();
			}
		});

		conteneurImage = new JPanelImage();
		conteneurImage.setBackground(Color.BLACK);

		lire = new JButton("Lire");
		lire.setEnabled(false);
		lire.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (threadLecture != null && threadLecture.isAlive())
					threadLecture.interrupt();
				else {
					if (représentationTableau.getSelectedRow() == timelapse.photos.length - 1)
						représentationTableau.getSelectionModel()
								.setSelectionInterval(0, 0);
					threadLecture = new Thread(LECTURE);
					threadLecture.start();
					lire.setText("Pause");
				}
			}
		});

		barreDeDéfilement = new JScrollBar(Adjustable.HORIZONTAL);
		barreDeDéfilement.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				représentationTableau.getSelectionModel().setSelectionInterval(
						e.getValue(), e.getValue());
			}
		});
		barreDeDéfilement.setEnabled(false);

		contrôles = new JPanel();
		contrôles.setLayout(new BoxLayout(contrôles, BoxLayout.X_AXIS));
		contrôles.add(lire);
		contrôles.add(barreDeDéfilement);

		conteneurPrévisualisation = new JPanel(new BorderLayout());
		conteneurPrévisualisation.add(conteneurImage, BorderLayout.CENTER);
		conteneurPrévisualisation.add(contrôles, BorderLayout.SOUTH);

		arborescence = new FileTree((FileTreeSelectionEvent e) -> {
			chargerTimelapse(e.getFile());
		});
		

		conteneurGauche = new JPanel();
		conteneurGauche.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				ajusterRatioConteneurImage();
			}
		});
		conteneurGauche.setLayout(new BoxLayout(conteneurGauche, BoxLayout.Y_AXIS));
		conteneurGauche.add(conteneurPrévisualisation);
		conteneurGauche.add(arborescence);

		tableau = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
					case 0:
						return "Num";
					case 1:
						return "Nom du fichier";
					case 2:
						return "Références";
					case 3:
						return "Luminosité";
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
					case 0:
						return Integer.class;
					case 1:
						return String.class;
					case 2:
						return Boolean.class;
					case 3:
						return Float.class;
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				switch (columnIndex) {
					case 0:
						return false;
					case 1:
						return false;
					case 2:
						return true;
					case 3:
						return false;
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public int getColumnCount() {
				return 4;
			}

			@Override
			public int getRowCount() {
				return timelapse == null ? 0 : timelapse.photos.length;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch (columnIndex) {
					case 0:
						return rowIndex + 1;
					case 1:
						if (timelapse.photos[rowIndex] != null)
							return timelapse.photos[rowIndex].fichier.getName();
						else
							return "";
					case 2:
						return timelapse.références[rowIndex];
					case 3:
						if (timelapse.photos[rowIndex] != null)
							return timelapse.photos[rowIndex].luminosité;
						else
							return 0;
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public void setValueAt(Object val, int rowIndex, int columnIndex) {
				switch (columnIndex) {
					case 2:
						timelapse.références[rowIndex] = (boolean) val;
						fireTableCellUpdated(rowIndex, columnIndex);
						traiter.setEnabled(activerDéflickage.isSelected()
								|| (activerInterpolation.isSelected() && timelapse
								.existeAuMoinsUneRéférence()));
						break;
					default:
						throw new IndexOutOfBoundsException();
				}
			}
		};

		représentationTableau = new JTable(tableau);
		représentationTableau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		représentationTableau.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						barreDeDéfilement.setValue(représentationTableau.getSelectedRow());
						if (représentationTableau.getSelectedRow() != -1
						&& timelapse.photos[représentationTableau.getSelectedRow()] != null)
							conteneurImage
							.afficherImage(timelapse.photos[représentationTableau
									.getSelectedRow()].prévisualisation);
						else
							conteneurImage.afficherImage(null);
					}
				});

		conteneurTableau = new JScrollPane(représentationTableau);

		conteneurInfoPhotos = new JPanel(new BorderLayout());
		conteneurInfoPhotos.add(conteneurTableau, BorderLayout.CENTER);

		activerDéflickage = new JCheckBox("Déflickage");
		activerDéflickage.setEnabled(false);
		activerDéflickage.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				constante.setEnabled(activerDéflickage.isSelected());
				moyenne.setEnabled(activerDéflickage.isSelected());
				nombrePhotosMoyenne.setEnabled(activerDéflickage.isSelected()
						&& moyenne.isSelected());
				texteMoyenneCalculée.setEnabled(activerDéflickage.isSelected()
						&& moyenne.isSelected());
				textePhotos.setEnabled(activerDéflickage.isSelected()
						&& moyenne.isSelected());
				traiter.setEnabled(activerDéflickage.isSelected()
						|| (activerInterpolation.isSelected() && timelapse
						.existeAuMoinsUneRéférence()));
			}
		});

		constante = new JRadioButton("Luminosité constante", true);
		constante.setEnabled(false);

		moyenne = new JRadioButton("Luminosité moyennée sur plusieurs photos",
				false);
		moyenne.setEnabled(false);
		moyenne.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				nombrePhotosMoyenne.setEnabled(moyenne.isSelected());
				texteMoyenneCalculée.setEnabled(moyenne.isSelected());
				textePhotos.setEnabled(moyenne.isSelected());
			}
		});

		texteMoyenneCalculée = new JTextField("Moyenne calculée sur ");
		texteMoyenneCalculée.setEditable(false);
		texteMoyenneCalculée.setBorder(null);
		texteMoyenneCalculée.setEnabled(false);

		textePhotos = new JTextField(" photos");
		textePhotos.setEditable(false);
		textePhotos.setBorder(null);
		textePhotos.setEnabled(false);

		nombrePhotosMoyenne = new JSpinner(new SpinnerNumberModel(30, 2, 10000, 1));
		nombrePhotosMoyenne.setPreferredSize(new Dimension(45,
				(int) nombrePhotosMoyenne.getPreferredSize().getHeight()));
		nombrePhotosMoyenne.setEnabled(false);

		conteneurMoyenneDéflickage = new JPanel();
		conteneurMoyenneDéflickage.setLayout(new FlowLayout(FlowLayout.LEFT));
		conteneurMoyenneDéflickage.add(texteMoyenneCalculée);
		conteneurMoyenneDéflickage.add(nombrePhotosMoyenne);
		conteneurMoyenneDéflickage.add(textePhotos);

		groupeBoutonsDéflickage = new ButtonGroup();
		groupeBoutonsDéflickage.add(constante);
		groupeBoutonsDéflickage.add(moyenne);

		paramètresDéflickage = new JPanel();
		paramètresDéflickage.setLayout(new BoxLayout(paramètresDéflickage,
				BoxLayout.Y_AXIS));
		paramètresDéflickage.setAlignmentX(Component.LEFT_ALIGNMENT);
		paramètresDéflickage.add(constante);
		paramètresDéflickage.add(moyenne);
		paramètresDéflickage.add(conteneurMoyenneDéflickage);
		paramètresDéflickage.setAlignmentX(Component.LEFT_ALIGNMENT);
		paramètresDéflickage.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		déflickage = new JPanel(new BorderLayout());
		déflickage.add(activerDéflickage, BorderLayout.NORTH);
		déflickage.add(paramètresDéflickage);
		déflickage.setMaximumSize(new Dimension((int) déflickage.getMaximumSize()
				.getWidth(), (int) déflickage.getPreferredSize().getHeight()));

		activerInterpolation = new JCheckBox("Interpolation");
		activerInterpolation.setEnabled(false);
		activerInterpolation.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				traiter.setEnabled(activerDéflickage.isSelected()
						|| (activerInterpolation.isSelected() && timelapse != null && timelapse
						.existeAuMoinsUneRéférence()));
			}
		});

		tableauTags = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValueAt(int arg0, int arg1) {
				switch (arg1) {
					case 0:
						return timelapse.tagsInterpolables[arg0];
					case 1:
						return timelapse.tagsInterpolés[arg0];
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public int getRowCount() {
				return timelapse == null ? 0 : timelapse.nombreTagsInterpolables;
			}

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
					case 0:
						return "Tag";
					case 1:
						return "Interpolation";
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
					case 0:
						return String.class;
					case 1:
						return Boolean.class;
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public void setValueAt(Object val, int rowIndex, int columnIndex) {
				switch (columnIndex) {
					case 1:
						timelapse.tagsInterpolés[rowIndex] = (boolean) val;
						fireTableCellUpdated(rowIndex, columnIndex);
						break;
					default:
						throw new IndexOutOfBoundsException();
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				switch (columnIndex) {
					case 0:
						return false;
					case 1:
						return true;
					default:
						throw new IndexOutOfBoundsException();
				}
			}
		};

		représentationTableauTags = new JTable(tableauTags);

		conteneurTableauTags = new JScrollPane(représentationTableauTags);

		interpolation = new JPanel(new BorderLayout());
		interpolation.add(activerInterpolation, BorderLayout.NORTH);
		interpolation.add(conteneurTableauTags, BorderLayout.CENTER);

		traiter = new JButton("Lancer le traitement");
		traiter.setEnabled(false);
		traiter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (threadTraitementTimelapse != null
						&& threadTraitementTimelapse.isAlive()) {
					if (JOptionPane.showConfirmDialog(null,
							"Souhaitez-vous vraiment annuler l'opération en cours ?",
							"Traitement en cours", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
						threadTraitementTimelapse.interrupt();
				} else {
					threadTraitementTimelapse = new Thread(TRAITEMENT);
					threadTraitementTimelapse.start();
				}
			}
		});

		traitement = new JPanel();
		traitement.setLayout(new BoxLayout(traitement, BoxLayout.Y_AXIS));
		traitement.add(déflickage);
		traitement.add(interpolation);
		traitement.add(traiter);

		conteneurDroit = new JPanel();
		conteneurDroit.setLayout(new BoxLayout(conteneurDroit, BoxLayout.X_AXIS));
		conteneurDroit.add(conteneurInfoPhotos);
		conteneurDroit.add(traitement);

		conteneurPrincipal = new JSplitPaneWithInitialDividerLocation(
				JSplitPane.HORIZONTAL_SPLIT, true, conteneurGauche, conteneurDroit, 0.3);
		conteneurPrincipal.setResizeWeight(0.5);

		statut = new JTextField(
				"Sélectionnez le dossier dans lequel se trouvent les photos du timelapse.");
		statut.setEditable(false);

		barreProgression = new JProgressBar();
		barreProgression.setVisible(false);

		barreStatut = new JPanel();
		barreStatut.setLayout(new BoxLayout(barreStatut, BoxLayout.X_AXIS));
		barreStatut.add(statut);
		barreStatut.add(barreProgression);

		setLayout(new BorderLayout());
		add(conteneurPrincipal, BorderLayout.CENTER);
		add(barreStatut, BorderLayout.SOUTH);

		fluxExifTool = new ExifTool(exécutableExifTool);
		dossierJPEG = new File(dossierCourant.getAbsolutePath() + File.separator
				+ "JPEG");
		intervalleLecture = 41;
		ratioPhotos = 3. / 2.;

		pack();
		ajusterRatioConteneurImage();
		setVisible(true);

	}

	public final void chargerTimelapse(File dossier) {
		if (threadChargementTimelapse != null
				&& threadChargementTimelapse.isAlive()) {
			threadChargementTimelapse.interrupt();
			while (threadChargementTimelapse.isAlive())
				try {
					Thread.sleep(ATTENTE_TERMINAISON_THREAD);
				} catch (InterruptedException e) {
				}
		}
		this.dossier = dossier;
		threadChargementTimelapse = new Thread(CHARGEMENT_TIMELAPSE);
		threadChargementTimelapse.start();
	}

	public void ajusterRatioConteneurImage() {
		conteneurImage.setPreferredSize(new Dimension(conteneurImage.getWidth(),
				(int) (conteneurImage.getWidth() / ratioPhotos)));
		arborescence.setPreferredSize(new Dimension(arborescence.getWidth(),
				conteneurGauche.getHeight()
				- (int) (conteneurImage.getWidth() / ratioPhotos)
				- contrôles.getHeight()));
	}

	public static void main(String[] args) {
		try {
//			dossierCourant = new File(URLDecoder.decode(ClassLoader
//					.getSystemClassLoader().getResource(".").getPath(), "UTF-8"));
//			
			//TODO
			dossierCourant = new File("/home/charles/Dropbox/Netbeans/Time-lapse assistant/Image-ExifTool-9.83");
			exécutableExifTool = new File(dossierCourant.getAbsolutePath()
					+ File.separator + "exiftool");
			if (!exécutableExifTool.exists())
				throw new Exception();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null,
					"L'exécutable \"exiftool.exe\" est introuvable.\n" + exécutableExifTool.getAbsolutePath(),
					"ExifTool introuvable", JOptionPane.ERROR_MESSAGE);
			return;
		}
		new TLAssistant();
	}
}
