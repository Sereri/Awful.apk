/********************************************************************************
 * Copyright (c) 2011, Scott Ferguson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the software nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awfulapp.thread;

import android.content.ContentValues;

import com.ferg.awfulapp.util.AwfulError;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Thread {
    private static final String TAG = "Reply";

    private static final String FORMKEY    = "//input[@name='formkey']";
    private static final String FORMCOOKIE = "//input[@name='form_cookie']";

    private static final String PARAM_ACTION      = "action";
    private static final String PARAM_THREADID    = "threadid";
    private static final String PARAM_POSTID      = "postid";
    private static final String PARAM_FORMKEY     = "formkey";
    private static final String PARAM_FORM_COOKIE = "form_cookie";
    private static final String PARAM_BOOKMARK    = "bookmark";
    private static final String PARAM_ATTACHMENT  = "attachment";
    
    private static final String VALUE_ACTION      = "postthread";
    private static final String VALUE_POSTID      = "";
    private static final String VALUE_FORM_COOKIE = "formcookie";


    public static final ContentValues processThread(Document page, int forumId) throws AwfulError {
        ContentValues newThread = new ContentValues();
        newThread.put(AwfulMessage.ID, forumId);
        getFormData(page, newThread);
        newThread.put(AwfulPost.FORM_BOOKMARK, getBookmarkOption(page));
        newThread.put(AwfulPost.FORM_SIGNATURE, getSignatureOption(page));
        newThread.put(AwfulPost.FORM_DISABLE_SMILIES, getDisableEmotesOption(page));
        return newThread;
    }

    public static final String getBookmarkOption(Document data){
        Element formBookmark = data.getElementsByAttributeValue("name", "bookmark").first();
        if(formBookmark.hasAttr("checked")){
            return "checked";
        }else{
            return "";
        }
    }

    public static final String getDisableEmotesOption(Document data){
        Element formDisableEmotes = data.getElementsByAttributeValue("name", AwfulMessage.REPLY_DISABLE_SMILIES).first();
        if(formDisableEmotes.hasAttr("checked")){
            return "checked";
        }else{
            return "";
        }
    }

    public static final String getSignatureOption(Document data){
        Element formSignature = data.getElementsByAttributeValue("name", AwfulMessage.REPLY_SIGNATURE).first();
        if(formSignature != null && formSignature.hasAttr("checked")){
            return "checked";
        }else{
            return "";
        }
    }

    public static final ContentValues getFormData(Document data, ContentValues results) throws AwfulError {
        try{
            Element formKey = data.getElementsByAttributeValue("name", "formkey").first();
            Element formCookie = data.getElementsByAttributeValue("name", "form_cookie").first();
            results.put(AwfulPost.FORM_KEY, formKey.val());
            results.put(AwfulPost.FORM_COOKIE, formCookie.val());
        }catch (Exception e){
            throw new AwfulError("Failed to load reply");
        }
        return results;
    }

}
