package com.ferg.awfulapp.task;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.ferg.awfulapp.constants.Constants;
import com.ferg.awfulapp.thread.AwfulPoll;
import com.ferg.awfulapp.util.AwfulError;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * Created by matt on 8/7/13.
 */
public class PollViewRequest extends AwfulRequest<AwfulPoll> {

    public static final Object REQUEST_TAG = new Object();

    private String pollId;
    public PollViewRequest(Context context, String pollId) {
        super(context, Constants.FUNCTION_POLL);
        this.pollId = pollId;
    }


    @Override
    public Object getRequestTag() {
        return REQUEST_TAG;
    }


    @Override
    protected String generateUrl(Uri.Builder urlBuilder) {
        urlBuilder.appendQueryParameter(Constants.PARAM_ACTION, Constants.ACTION_SHOWRESULTS);
        urlBuilder.appendQueryParameter(Constants.PARAM_POLL_ID, this.pollId);
        return urlBuilder.build().toString();
    }

    @Override
    protected AwfulPoll handleResponse(Document doc) throws AwfulError {
        AwfulPoll poll = null;
        try {
            poll = AwfulPoll.parsePoll(doc);
        }catch (Exception e){
            Log.e(TAG, "Erroooor",e);
        }
        return poll;
    }

    @Override
    protected boolean handleError(AwfulError error, Document doc) {
        return error.isCritical();
    }
}
