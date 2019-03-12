package sub.kyung.history;

import java.util.Date;

public class History {
    private int historyIdx;
    private String historyKeyword;
    private String historyContent;
    private Date historyDate;

    public History(String historyKeyword, String historyContent, Date historyDate){
        this.historyKeyword = historyKeyword;
        this.historyContent = historyContent;
        this.historyDate = historyDate;
    }

    public History(int historyIdx, String historyKeyword, String historyContent, Date historyDate){
        this.historyIdx = historyIdx;
        this.historyKeyword = historyKeyword;
        this.historyContent = historyContent;
        this.historyDate = historyDate;
    }

    public int getHistoryIdx() {
        return historyIdx;
    }

    public void setHistoryIdx(int historyIdx) {
        this.historyIdx = historyIdx;
    }

    public String getHistoryKeyword() {
        return historyKeyword;
    }

    public void setHistoryKeyword(String historyKeyword) {
        this.historyKeyword = historyKeyword;
    }

    public String getHistoryContent() {
        return historyContent;
    }

    public void setHistoryContent(String historyContent) {
        this.historyContent = historyContent;
    }

    public Date getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Date historyDate) {
        this.historyDate = historyDate;
    }

    public String getStringDate(){
        return this.historyDate.toString();
    }

    public int getYear(){
        return this.historyDate.getYear();
    }

    public int getMonth(){
        return this.historyDate.getMonth();
    }

    public int getDay(){
        return this.historyDate.getDate();
    }
}
