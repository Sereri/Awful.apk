package com.ferg.awfulapp.thread;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by Christoph on 24.09.2016.
 */

public class AwfulPoll {

    private String title;
    private ArrayList<AwfulPollItem> items;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<AwfulPollItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<AwfulPollItem> items) {
        this.items = items;
    }

    public static class AwfulPollItem {
        private int id;
        private String optionTitle;
        private int votes;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getOptionTitle() {
            return optionTitle;
        }

        public void setOptionTitle(String optionTitle) {
            this.optionTitle = optionTitle;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }
    }

    public static AwfulPoll parsePoll(Document doc){

        AwfulPoll result = null;

        Element table = doc.getElementsByTag("table").first();

        if(table != null){
            result = new AwfulPoll();
            ArrayList<AwfulPollItem> pollOptions = new ArrayList<>();

            Elements tr = table.getElementsByTag("tr");
            result.setTitle(tr.first().text().trim());
            tr.remove(tr.first());
            tr.remove(tr.last());
            int index = 0;
            for (Element row : tr) {
                AwfulPollItem option = new AwfulPollItem();
                option.setOptionTitle(row.child(0).text());
                option.setId(index++);
                option.setVotes(Integer.parseInt(row.child(2).text()));
                pollOptions.add(option);
            }
            result.setItems(pollOptions);
        }

        return result;
    }
}
