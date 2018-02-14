package application;

import java.beans.EventHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;



public class SampleController implements Initializable {


	@FXML private TableView<TableDataModel> myTableView;
	@FXML private TableColumn<TableDataModel, String> Market;
	@FXML private TableColumn<TableDataModel, String> Coin;
	@FXML private TableColumn<TableDataModel, String> Price;
	@FXML private TextField marketField;
	@FXML private TextField coinField;
	@FXML private Button addButton;
	@FXML private Button deleteButton;
	@FXML private Button toRight;
	@FXML private Button toLeft;
	@FXML private TextField upperValueField;
	@FXML private TextField lowerValueField;
	@FXML private TableView<TableDataModelWithStatus> mySecondTable;
	@FXML private TableColumn<TableDataModelWithStatus, String> coin2;
	@FXML private TableColumn<TableDataModelWithStatus, String> price2;
	@FXML private TableColumn<TableDataModelWithStatus, String> lowStatus;
	@FXML private TableColumn<TableDataModelWithStatus, String> highStatus;


	private boolean stop;
	private double upperPercentage;
	private double lowerPercentage;


	ObservableList<TableDataModel> myList = FXCollections.observableArrayList();
	ObservableList<TableDataModel> mySecondList = FXCollections.observableArrayList();


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	
		stop = false;
		initFXML();
		readFileAndSetTable();
		
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				while(!stop) {
					Platform.runLater(()-> {
						myTableView.refresh();
						mySecondTable.refresh();
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}


		};

		thread.setDaemon(true);
		thread.start();



	}
	
	private void readFileAndSetTable() {
		BufferedReader br = null;
		try {
		
			
			br = new BufferedReader(new FileReader("UpbitPriceCheckerProperties.txt"));
			System.out.println("start reading file");
			String line = br.readLine();
			while (line != null) {
				System.out.println(line);
				String[] splitedLine = line.split(" ");
				if(splitedLine[0].equals("upperPercentage")) {
					upperPercentage = Double.valueOf(splitedLine[1]);
				}else if(splitedLine[0].equals("lowerPercentage")) {
					lowerPercentage = Double.valueOf(splitedLine[1]);
				}				
				else if (splitedLine.length == 2) {
					loadTable(splitedLine[0], splitedLine[1]);
				}else if (splitedLine.length == 4) {
					loadTable(splitedLine[0], splitedLine[1],splitedLine[2],splitedLine[3]);
				}				
				line = br.readLine();
	
				
			}
		
		
		
		} catch (IOException e) {
			System.out.println("There is no file name UpbitPriceCheckerProperties.txt");
		}finally {
			try {
				if (br != null) {
					br.close();
				}
			}catch (IOException e) {
				System.out.println("error closeing bufferedreader");
			}
		}
		
	}
	
	private void loadTable(String market, String coin) {
		myTableView.getItems().add(new TableDataModel(new SimpleStringProperty(market),new SimpleStringProperty(coin)));
	}
	
	private void loadTable(String market, String coin, String upperValue, String lowerValue) {
		mySecondTable.getItems().add(new TableDataModelWithStatus(
				new SimpleStringProperty(market),
				new SimpleStringProperty(coin),
				new SimpleStringProperty(upperValue),
				new SimpleStringProperty(lowerValue)
				));
	}
	
	
	private void saveFile() {
		BufferedWriter writer = null;
		 
        try {
            writer = new BufferedWriter(new FileWriter(new File("UpbitPriceCheckerProperties.txt")));
            writer.write("upperPercentage " +upperPercentage);
            writer.newLine();
            writer.write("lowerPercentage " +lowerPercentage);
            writer.newLine();
            
            writer.write("Table for Simple Price check\n");
            for (int i = 0 ; i <myTableView.getItems().size() ; i++ ) {
            	writer.write(myTableView.getItems().get(i).marketProperty().getValue() + " " 
            + myTableView.getItems().get(i).coinProperty().getValue());
            writer.newLine();
            }
            writer.write("Table for Price check with upper and lower chekcer\n");
            for (int i = 0 ; i <mySecondTable.getItems().size() ; i++ ) {
            	writer.write(mySecondTable.getItems().get(i).coinProperty().getValue() + " " +
            			mySecondTable.getItems().get(i).upperValueProperty().getValue() + " " +
            			mySecondTable.getItems().get(i).lowerValueProperty().getValue());
            writer.newLine();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


	}
	
	private void initFXML() {
		//setting first Table
		Market.setCellValueFactory(cellData -> cellData.getValue().marketProperty());
		Coin.setCellValueFactory(cellData -> cellData.getValue().coinProperty());
		Price.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
		myTableView.setItems(myList);

		//Setting second table
		coin2.setCellValueFactory(cellData -> cellData.getValue().coinProperty());
		price2.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
		lowStatus.setCellValueFactory(cellData -> cellData.getValue().lowerValueProperty());
		highStatus.setCellValueFactory(cellData -> cellData.getValue().upperValueProperty());

		lowStatus.setCellFactory(column -> {
			return new TableCell <TableDataModelWithStatus, String > () {
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {

					} else {
						setText(item);
						int rowIndex = getTableRow().getIndex();
						String PriceCell = getTableView().getItems().get(rowIndex).priceProperty().get();

						if(!PriceCell.equals("ERROR")) {
							PriceCell = PriceCell.replace(",", "");



							if (Double.valueOf(PriceCell) <= (Double.valueOf(item.toString()) / 100.0 * (100.0 + lowerPercentage))) {
								Platform.runLater(()-> {
									setText(item);
									setStyle("-fx-background-color: red");
								});
								//Set the style in the first cell based on the value of the second cell
							} else {
								setText(item);
								setStyle("");
							}
						}


					}
				}
			};
		});

		
		highStatus.setCellFactory(column -> {
					return new TableCell <TableDataModelWithStatus, String > () {
						protected void updateItem(String item, boolean empty) {
							super.updateItem(item, empty);
		
							if (item == null || empty) {

							} else {
								setText(item);
								int rowIndex = getTableRow().getIndex();
								String PriceCell = getTableView().getItems().get(rowIndex).priceProperty().get();

								if(!PriceCell.equals("ERROR")) {
									PriceCell = PriceCell.replace(",", "");



									if (Double.valueOf(PriceCell) >= (Double.valueOf(item.toString()) / 100.0 * (100.0 - upperPercentage))) {
									System.out.println((Double.valueOf(item.toString()) / 100.0 * (100.0 - upperPercentage)));

										Platform.runLater(()-> {
											setText(item);
											setStyle("-fx-background-color: red");
										});
										//Set the style in the first cell based on the value of the second cell
									} else {
										setText(item);
										setStyle("");
									}
								}


							}
						}
					};
				});


		addButton.setOnAction(event->addItemToTable(event));
		deleteButton.setOnAction(event ->deleteSelectedItem(event));
		toRight.setOnAction(event->moveItemToRight(event));
		toLeft.setOnAction(event -> deleteSelectedItemFromSecondTable(event));

	}



	private void deleteSelectedItemFromSecondTable(ActionEvent event) {
		// TODO Auto-generated method stub
		if (mySecondTable.getSelectionModel().isEmpty()) {
			System.out.println("Choose Item to Delete");
		}else {
			int index = mySecondTable.getSelectionModel().getSelectedIndex();
			mySecondTable.getItems().remove(index);
			mySecondTable.getSelectionModel().select(null);
			saveFile();
		}

	}


	private void moveItemToRight(ActionEvent event) {
		
		// TODO Auto-generated method stub
        //todo
		if (lowerValueField.getText().isEmpty() || upperValueField.getText().isEmpty()) {
			System.out.println("NEED TO PUT VALUES");
		}else if (myTableView.getSelectionModel().isEmpty()){
			System.out.println("choose first");
		}else {
			mySecondTable.getItems().add(new TableDataModelWithStatus(
					myTableView.getSelectionModel().getSelectedItem().marketProperty(),
					myTableView.getSelectionModel().getSelectedItem().coinProperty(),
					new SimpleStringProperty(lowerValueField.getText()),
					new SimpleStringProperty(upperValueField.getText())
					));
			//mySecondTable.refresh();
			lowerValueField.clear();
			upperValueField.clear();
			myTableView.getSelectionModel().select(null);
			saveFile();
		}

	}


	private void deleteSelectedItem(ActionEvent event) {
		// TODO Auto-generated method stub
		if (myTableView.getSelectionModel().isEmpty()) {
			System.out.println("Choose Item to Delete");
		}else {
			int index = myTableView.getSelectionModel().getSelectedIndex();
			myTableView.getItems().remove(index);
			myTableView.getSelectionModel().select(null);
			saveFile();
		}
	}


	private void addItemToTable(ActionEvent event) {
		// TODO Auto-generated method stub
		if(marketField.getText().isEmpty() || coinField.getText().isEmpty()) {
			System.out.println("Error need to fill in");
		}else {
			myTableView.getItems().add(new TableDataModel(new SimpleStringProperty(marketField.getText()),new SimpleStringProperty(coinField.getText())));
			marketField.clear();
			coinField.clear();
			saveFile();
		}

	}



}
