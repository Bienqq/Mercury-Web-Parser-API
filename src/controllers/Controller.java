package controllers;

import application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Controller
{
    @FXML private TextField addressTextField;
    @FXML private TextField keyTextField;
    @FXML private TextArea lastRequestTextArea;
    @FXML private TextArea pageInfoTextArea;
    @FXML private TextArea contentTextArea;
    @FXML private TextArea historyTextArea;
    @FXML private CategoryAxis categoryAxis = new CategoryAxis();
    @FXML private NumberAxis numberAxis = new NumberAxis();
    @FXML private BarChart<String, Number> tagStatisticsChart = new BarChart<>(categoryAxis,numberAxis);

    private final HttpClient client = new HttpClient();
    private final String DEFAULT_KEY = "YAUQ4pKYuJYjg3i9S7xT27B4NUwFoU72H7ynN4kX";
    private final int HTML_TAGS_AMOUNT = 10;
    private final int URLS_HISTORY = 5;

    private List<String> urlList = new ArrayList<>();
    private boolean keyWasSet = false;

    public void initialize()
    {
        tagStatisticsChart.setVisible(false);
        // Auto-scroll history
        historyTextArea.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                historyTextArea.setScrollTop(Double.MIN_VALUE);
            }
        });
    }

    @FXML private void onCheckBoxSelected()
    {
        if (!keyWasSet)
        {
            keyTextField.setDisable(true);
            keyTextField.setText(DEFAULT_KEY);
        }
        else
        {
            keyTextField.setDisable(false);
            keyTextField.clear();
        }
        keyWasSet = !keyWasSet;
    }

    @FXML private void onParseButtonClicked() throws Exception
    {
        boolean userInputOK = verifyUserInput();
        if(!userInputOK)
        {
            return;
        }

        // sending request in new thread
        Thread clientThread = new Thread(client);
        clientThread.start();
        //waiting request thread to finish
        clientThread.join();

        //getting and checking request status
        HttpClient.RequestStatus status = client.getStatus();
        checkRequestStatus(status);
    }

    private void checkRequestStatus(HttpClient.RequestStatus status)
    {
        if(status == HttpClient.RequestStatus.SUCCESSFUL)
        {
            displayPageInfoAndContent();
            displayPreviousRequests();
            displayHTMLStatistics();
        }
        else if(status == HttpClient.RequestStatus.TIMEOUT)
        {
            showError("Failed connection to server.");
        }
        else
        {
            showError("URL or key is incorrect.");
        }
    }

    // displaying error with appropriate message
    private void  showError(String errorMessage)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(errorMessage);
        alert.getDialogPane().setCursor(Cursor.HAND);
        alert.showAndWait();
    }

    private boolean verifyUserInput()
    {
        String url = addressTextField.getText();
        String key = keyTextField.getText();

        if(url.isEmpty() || key.isEmpty())
        {
            showError("Url or key is empty.");
            return false;
        }

        client.setUrl(url);
        client.setUserKey(key);
        return true;
    }

    private void displayPageInfoAndContent()
    {
        pageInfoTextArea.setText(client.getPageInfo());
        contentTextArea.setText(client.getContent());
    }

    private void displayPreviousRequests()
    {
        String lastRequest = client.getFormattedLastRequest();
        lastRequestTextArea.setText(lastRequest);

        if(urlList.size() == URLS_HISTORY)
        {
            urlList.remove(0);
        }

        String url = client.getURL() + "\n" ;
        urlList.add(url);

        historyTextArea.clear();
        for(int i = urlList.size() - 1 ; i >=0 ; i--)
        {
            historyTextArea.appendText(urlList.get(i));
        }
    }

    private void displayHTMLStatistics()
    {
        tagStatisticsChart.getData().clear();
        HTMLCounter counter = new HTMLCounter(client.getContent());
        counter.countAndSortTags();
        Map<String, Long> tagList = counter.getSortedTagList();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        int it = 0;
        for (Map.Entry<String, Long> entry : tagList.entrySet())
        {
            if(it == HTML_TAGS_AMOUNT)
                break;
            Number frequency = entry.getValue();
            String tag = entry.getKey() + " " + frequency;
            XYChart.Data<String, Number> data = new XYChart.Data<>(tag, frequency);
            series.getData().add(data);
            it++;
        }

        selectOptimalBarWidth(it);
        tagStatisticsChart.setVisible(true);
        tagStatisticsChart.getData().addAll(series);
        tagStatisticsChart.setLegendVisible(false);
    }

    private void selectOptimalBarWidth(int barNumber)
    {
        double barWidth;
        switch (barNumber)
        {
            case 1:
                barWidth = 700;
                break;
            case 2:
                barWidth = 300;
                break;
            case 3:
                barWidth = 150;
                break;
            case 4:
                barWidth = 100;
                break;
            case 5:
            default:
                barWidth = 0;
        }
        tagStatisticsChart.setCategoryGap(barWidth);
    }
}
