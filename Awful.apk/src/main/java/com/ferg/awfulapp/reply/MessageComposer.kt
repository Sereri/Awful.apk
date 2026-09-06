package com.ferg.awfulapp.reply

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.ferg.awfulapp.AwfulActivity
import com.ferg.awfulapp.EmotePicker
import com.ferg.awfulapp.EmotePickerListener
import com.ferg.awfulapp.FontManager
import com.ferg.awfulapp.NavigationEvent
import com.ferg.awfulapp.R
import com.ferg.awfulapp.preferences.AwfulPreferences
import com.ferg.awfulapp.reply.BasicTextInserter.BbCodeTag
import com.ferg.awfulapp.reply.Inserter.Untagged
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.view.size
import androidx.core.view.get

/**
 * Created by baka kaba on 07/11/2016.
 * 
 * 
 * A fragment holding an EditText, with functionality specific to composing messages and posts.
 * 
 * 
 * This is basically meant as a drop-in composer window, handling BBcode and smiley insertion
 * and exposing some methods to get and set the current contents. It has its own menu options,
 * so you need to make sure [.onCreateOptionsMenu] and
 * [.onOptionsItemSelected] are called when appropriate.
 */
class MessageComposer : Fragment(), EmotePickerListener {
    private lateinit var messageBox: EditText
    private var bottomSheetMenuDialog: BottomSheetMenuDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val result = inflater.inflate(R.layout.message_composer, container, true)
        messageBox = result.findViewById(R.id.message_edit_text)
        addBbCodeToSelectionMenu(messageBox)
        (activity as AwfulActivity).setPreferredFont(result)
        return result
    }


    override fun onDestroy() {
        super.onDestroy()
        if (bottomSheetMenuDialog != null) {
            bottomSheetMenuDialog!!.dismiss()
        }
    }


    /**//////////////////////////////////////////////////////////////////////// */ // Menu handling
    /**//////////////////////////////////////////////////////////////////////// */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.message_composer, menu)
        val fm = FontManager.getInstance()
        for (i in 0..<menu.size) {
            fm.setMenuItemFont(menu[i])
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_bbcode_menu -> toggleBottomSheet()
            R.id.bbcode_bold -> insertWith(BbCodeTag.BOLD)
            R.id.bbcode_italics -> insertWith(BbCodeTag.ITALICS)
            R.id.bbcode_underline -> insertWith(BbCodeTag.UNDERLINE)
            R.id.bbcode_strikeout -> insertWith(BbCodeTag.STRIKEOUT)
            R.id.bbcode_spoiler -> insertWith(BbCodeTag.SPOILER)
            R.id.bbcode_superscript -> insertWith(BbCodeTag.SUPERSCRIPT)
            R.id.bbcode_subscript -> insertWith(BbCodeTag.SUBSCRIPT)
            R.id.bbcode_fixed_width -> insertWith(BbCodeTag.FIXED)
            R.id.emotes -> EmotePicker().show(getChildFragmentManager(), "emotes")
            R.id.bbcode_image -> insertWith(ImageInserter::smartInsert as Untagged)

            R.id.bbcode_imgur -> if (AwfulPreferences.getInstance().imgurAccount != null) {
                val imgurInserter = ImgurInserter()
                imgurInserter.setTargetFragment(this, -1)
                // TODO: 29/12/2017 switch this to childFragmentManager and test
                imgurInserter.show(requireFragmentManager(), "imgur uploader")
            } else {
                (activity as AwfulActivity).navigate(NavigationEvent.Settings("account"))
            }

            R.id.bbcode_video -> insertWith(VideoInserter::smartInsert as Untagged)

            R.id.bbcode_url -> insertWith(UrlInserter::smartInsert as Untagged)

            R.id.bbcode_quote -> insertWith(QuoteInserter::smartInsert as Untagged)

            R.id.bbcode_list -> insertWith(ListInserter::smartInsert as Untagged)

            R.id.bbcode_code -> insertWith(CodeInserter::smartInsert as Untagged)

            R.id.bbcode_pre -> insertWith(BbCodeTag.PRE)
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun insertWith(inserter: Untagged) {
        inserter.smartInsert(messageBox, requireActivity())
    }

    private fun insertWith(bbCodeTag: BbCodeTag) {
        BasicTextInserter.smartInsert(messageBox, bbCodeTag, requireActivity())
    }

    /**//////////////////////////////////////////////////////////////////////// */ // Callbacks
    /**//////////////////////////////////////////////////////////////////////// */
    fun onImageUploaded(url: String, useThumbnail: Boolean) {
        ImageInserter.insertWithoutDialog(messageBox, url, useThumbnail)
    }


    fun onHtml5VideoUploaded(url: String) {
        // embedding doesn't use [video] tags for some reason, needs to be a url with no link text
        UrlInserter.insertWithoutDialog(messageBox, url, null)
    }


    /////////////////////////////////////////////////////////////////////////
    // Useful public functions
    /////////////////////////////////////////////////////////////////////////
    /**
    * Set the contents of the composer's EditText.
    *
    * @param messageText The text to set (empty if null)
    * @param selectAll Select the contents, e.g. for easy deletion by the user
    */
    fun setText(messageText: String?, selectAll: Boolean) {
        messageBox.setText(messageText ?: "")
        if (selectAll) {
            messageBox.setSelection(messageBox.length())
        }
    }


    val text: String
        /**
         * Get the text contents of the composer's EditText.
         */
        get() = messageBox.text.toString()


    /**
     * Set the background colour of the EditText.
     */
    fun setBackgroundColor(@ColorInt color: Int) {
        messageBox.setBackgroundColor(color)
    }

    /**
     * Set the text colour of the EditText.
     */
    fun setTextColor(@ColorInt color: Int) {
        messageBox.setTextColor(color)
    }

    /**
     * Hide the keyboard, if currently active in the composer.
     */
    fun hideKeyboard() {
        if (activity != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(messageBox.applicationWindowToken, 0)
        }
    }


    /**
     * Insert a smiley
     * 
     * @param emoteCode the smiley code to insert at the current selection point
     */
    override fun onEmoteChosen(emoteCode: String) {
        val selectionStart = messageBox.selectionStart
        messageBox.editableText.insert(selectionStart, emoteCode)
        messageBox.setSelection(selectionStart + emoteCode.length)
    }


    /////////////////////////////////////////////////////////////////////////
    // Internal handling
    /////////////////////////////////////////////////////////////////////////
    /**
    * Adds the BBcode option to the text selection action menu.
    *
    *
    * This is mainly to fix the issue with the action menu replacing the action bar on earlier
    * versions of Android, meaning the BBcode option can't be pressed (and work on selected text).
    *
    * @param editText the textview to add the selection option to
    */
    private fun addBbCodeToSelectionMenu(editText: EditText) {
        val callback: ActionMode.Callback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
                menu.add(Menu.NONE, R.id.show_bbcode_menu, Menu.NONE, getString(R.string.bbcode))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setIcon(R.drawable.ic_bb)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                if (item.itemId == R.id.show_bbcode_menu) {
                    toggleBottomSheet()
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }
        }

        editText.customSelectionActionModeCallback = callback
        // add it to the insert menu too for consistency, why not
        // noinspection AndroidLintNewApi
        editText.customInsertionActionModeCallback = callback
    }


    /**
     * Display or hide the bottom sheet as appropriate.
     */
    @SuppressLint("ResourceType")
    private fun toggleBottomSheet() {
        // if we already have a sheet, get rid of it
        if (bottomSheetMenuDialog != null) {
            bottomSheetMenuDialog?.dismissWithAnimation()
            bottomSheetMenuDialog = null
            return
        }

        // Stupid hack to ensure the text selected when the options are shown is still selected
        // when an option is chosen. This is all because older versions use a Contextual Action Bar
        // for text selection, which a) deselects the text when you pick an option from it,
        // and b) covers the action bar so you can't use the menu item there that works fine
        val selectionRange = if (!messageBox.hasSelection()) null else intArrayOf(
            messageBox.selectionStart,
            messageBox.selectionEnd
        )

        // build a full menu to populate the sheet with
        val activity = getActivity()
        val sheetMenu = PopupMenu(activity, view).menu
        val inflater = MenuInflater(activity)
        inflater.inflate(R.menu.insert_into_message, sheetMenu)
        inflater.inflate(R.menu.format_message, sheetMenu)

        // need to apply themed background and text colors programmatically it seems
        val a = requireActivity().theme.obtainStyledAttributes(
            intArrayOf(
                R.attr.bottomSheetBackgroundColor,
                R.attr.bottomSheetItemTextColor
            )
        )
        val backgroundColour = a.getResourceId(0, 0)
        val itemTextColour = a.getResourceId(1, 0)
        a.recycle()

        bottomSheetMenuDialog = with(BottomSheetBuilder(activity)) {
            setBackgroundColorResource(backgroundColour)
            setItemTextColorResource(itemTextColour)
            setMode(BottomSheetBuilder.MODE_GRID)
            setMenu(sheetMenu)
            setItemClickListener { item: MenuItem? ->
                // restore any selection in the EditText before invoking the format/insert options
                if (selectionRange != null) {
                    messageBox.setSelection(selectionRange[0], selectionRange[1])
                }
                onOptionsItemSelected(item!!)
            }
            createDialog()
        }

        val bsmDialog = bottomSheetMenuDialog ?: return

        // drop the reference to an existing sheet when it goes away
        bsmDialog.setOnCancelListener { _: DialogInterface? ->
            bottomSheetMenuDialog = null
        }
        bsmDialog.setOnDismissListener { _: DialogInterface? ->
            bottomSheetMenuDialog = null
        }

        bsmDialog.show()
        // force the dialog to expand since peek/collapsed has some measurement issue in landscape
        bsmDialog.behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
    }
}
