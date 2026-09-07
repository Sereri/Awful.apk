package com.ferg.awfulapp.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.view.Menu
import com.ferg.awfulapp.R
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * Created by baka kaba on 17/11/2016.
 * 
 * 
 * A component that displays a menu in a themed bottom sheet dialog.
 * 
 * 
 * Initialise it with a menu, then set any listeners you need. Calling [.toggleVisible]
 * will display the dialog using the bottom sheet styles in the activity's current theme.
 * The sheet can also be explicitly dismissed with a call to [.dismiss].
 */
class ThemedBottomSheetDialog
/**
 * Create a new BottomSheetDialog to display the contents of a menu.
 */(private val sheetMenu: Menu) {
    private var bottomSheetMenuDialog: BottomSheetMenuDialog? = null
    private var itemClickListener: BottomSheetItemClickListener? = null
    private var onCancelListener: Runnable? = null
    private var onDismissListener: Runnable? = null


    /**
     * Set the listeners for this bottom sheet.
     */
    fun setClickListeners(
        itemClickListener: BottomSheetItemClickListener?,
        onCancelListener: Runnable?,
        onDismissListener: Runnable?
    ) {
        this.itemClickListener = itemClickListener
        this.onCancelListener = onCancelListener
        this.onDismissListener = onDismissListener
    }


    /**
     * Display or hide the bottom sheet, as appropriate.
     */
    @SuppressLint("ResourceType")
    fun toggleVisible(activity: Activity) {
        // if there's a current sheet dialog, just dismiss it
        if (bottomSheetMenuDialog != null) {
            bottomSheetMenuDialog?.dismissWithAnimation()
            bottomSheetMenuDialog = null
            return
        }

        // need to apply themed background and text colors programmatically it seems
        val a = activity.theme.obtainStyledAttributes(
            intArrayOf(
                R.attr.bottomSheetBackgroundColor,
                R.attr.bottomSheetItemTextColor
            )
        )
        val backgroundColour = a.getResourceId(0, 0)
        val itemTextColour = a.getResourceId(1, 0)
        a.recycle()

        // build and display the sheet dialog
        bottomSheetMenuDialog = BottomSheetBuilder(activity)
            .setBackgroundColor(backgroundColour)
            .setItemTextColor(itemTextColour)
            .setMode(BottomSheetBuilder.MODE_GRID)
            .setTitleTextColor(itemTextColour)
            .setMenu(sheetMenu)
            .setItemClickListener(itemClickListener)
            .createDialog()?.let {
                it.setOnCancelListener(DialogInterface.OnCancelListener { dialog: DialogInterface? ->
                    handleCancelDismiss(
                        onCancelListener
                    )
                })
                it.setOnDismissListener(DialogInterface.OnDismissListener { dialog: DialogInterface? ->
                    handleCancelDismiss(
                        onDismissListener
                    )
                })
                it.show()

                // force the dialog to expand since peek/collapsed has some measurement issue in landscape
                it.behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                return
            }

    }


    /**
     * Dismiss the current dialog, if visible
     */
    fun dismiss() {
        bottomSheetMenuDialog?.dismiss()
        bottomSheetMenuDialog = null
    }


    /**
     * Convenience method to clear the dialog and run a listener if appropriate.
     */
    private fun handleCancelDismiss(listener: Runnable?) {
        bottomSheetMenuDialog = null
        listener?.run()
    }
}
