package com.ferg.awfulapp.forums

import android.content.Context
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder
import com.ferg.awfulapp.AwfulActivity
import com.ferg.awfulapp.R
import com.ferg.awfulapp.databinding.ForumIndexItemBinding
import com.ferg.awfulapp.databinding.ForumIndexSubforumItemBinding
import com.ferg.awfulapp.forums.ForumListAdapter.SubforumHolder
import com.ferg.awfulapp.forums.ForumListAdapter.TopLevelForumHolder
import com.ferg.awfulapp.preferences.AwfulPreferences
import com.ferg.awfulapp.provider.ColorProvider

/**
 * Created by baka kaba on 13/04/2016.
 * 
 * 
 * A RecyclerView adapter for displaying expandable two-level lists of forums.
 */
class ForumListAdapter private constructor(
    context: Context,
    topLevelForums: MutableList<TopLevelForum?>,
    private val eventListener: EventListener,
    private val awfulPrefs: AwfulPreferences?
) : ExpandableRecyclerAdapter<TopLevelForumHolder, SubforumHolder>(topLevelForums) {
    private val parent: AwfulActivity = context as AwfulActivity
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * interpolator for any animations a view holder wants to do
     */
    private val interpolator: Interpolator = FastOutSlowInInterpolator()

    /**
     * Create TopLevelForums from a list of Forums, adding them to a supplied list.
     * 
     * @param forums         The forums to add
     * @param topLevelForums The list to add to
     */
    private fun addToTopLevelForums(
        forums: MutableList<Forum>,
        topLevelForums: MutableList<TopLevelForum?>
    ) {
        for (forum in forums) {
            topLevelForums.add(TopLevelForum(forum))
        }
    }

    /**
     * Update the contents of the data set with a new list of forums.
     * 
     * @param forums The new list to display
     * (see [.getInstance] for the list format)
     */
    fun updateForumList(forums: MutableList<Forum>) {
        val itemList: MutableList<TopLevelForum?> = parentItemList as MutableList<TopLevelForum?>

        // we can't just reassign the dataset variable, we have to mess with the contents instead
        val oldSize = itemList.size
        if (oldSize > 0) {
            notifyParentItemRangeRemoved(0, oldSize)
        }
        itemList.clear()
        addToTopLevelForums(forums, itemList)
        val newSize = forums.size
        if (newSize > 0) {
            notifyParentItemRangeInserted(0, newSize)
        }
    }

    private fun setText(
        forum: Forum,
        title: TextView,
        subtitle: TextView,
        sectionTitle: TextView?
    ) {
        title.text = forum.title
        subtitle.text = forum.subtitle
        if (sectionTitle != null) {
            sectionTitle.text = forum.title
        }
    }


    /**//////////////////////////////////////////////////////////////////////// */ // List items!
    /**//////////////////////////////////////////////////////////////////////// */
    private fun handleSubtitles(forum: Forum, subtitleView: TextView) {
        // we remove the subtitle if it's not there (or it's disabled) so that the title gets vertically centred
        var subtitlesEnabled = false
        if (awfulPrefs != null) {
            subtitlesEnabled = awfulPrefs.forumIndexShowSubtitles
        }
        subtitleView.visibility = if (!forum.subtitle!!.isEmpty() && subtitlesEnabled) View.VISIBLE else View.GONE
    }

    /**
     * Rotate the dropdown button to the up or down position.
     * 
     * @param dropdown  The view to rotate
     * @param down      True to rotate to the down state (default rotation)
     * @param immediate Set rotation immediately, false will animate
     */
    private fun rotateDropdown(dropdown: ImageView, down: Boolean, immediate: Boolean) {
        val DOWN_ROTATION = 0
        val UP_ROTATION = -540
        dropdown.animate()
            .setDuration((if (immediate) 0 else 400).toLong())
            .rotation((if (down) DOWN_ROTATION else UP_ROTATION).toFloat()).interpolator =
            interpolator
    }

    /**
     * Apply colour theming
     * 
     * @param mainView The main item layout, has its background set
     */
    private fun setThemeColours(mainView: View, title: TextView, subtitle: TextView) {
        mainView.setBackgroundColor(ColorProvider.BACKGROUND.color)
        title.setTextColor(ColorProvider.PRIMARY_TEXT.color)
        subtitle.setTextColor(ColorProvider.ALT_TEXT.color)
    }

    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup?): TopLevelForumHolder {
        val view = inflater.inflate(R.layout.forum_index_item, parentViewGroup, false)
        return TopLevelForumHolder(view)
    }

    override fun onCreateChildViewHolder(childViewGroup: ViewGroup?): SubforumHolder {
        val view = inflater.inflate(R.layout.forum_index_subforum_item, childViewGroup, false)
        return SubforumHolder(view)
    }

    override fun onBindParentViewHolder(
        parentViewHolder: TopLevelForumHolder,
        position: Int,
        parentListItem: ParentListItem?
    ) {
        parentViewHolder.bind((parentListItem as TopLevelForum?)!!)
    }


    /**//////////////////////////////////////////////////////////////////////// */ // Internal adapter wiring
    /**//////////////////////////////////////////////////////////////////////// */
    override fun onBindChildViewHolder(
        childViewHolder: SubforumHolder,
        position: Int,
        childListItem: Any?
    ) {
        childViewHolder.bind((childListItem as com.ferg.awfulapp.forums.Forum?)!!)
    }


    interface EventListener {
        fun onForumClicked(forum: Forum)

        fun onContextMenuCreated(forum: Forum, contextMenu: Menu)
    }

    class TopLevelForum(val forum: Forum) : ParentListItem {
        override fun getChildItemList(): MutableList<*> {
            return forum.subforums
        }


        override fun isInitiallyExpanded(): Boolean {
            return false
        }
    }

    inner class TopLevelForumHolder(// list item sections - overall view, left column (tags etc), right column (details)
        private val itemView: View
    ) : ParentViewHolder(itemView) {
        private val binding: ForumIndexItemBinding = ForumIndexItemBinding.bind(itemView)


        private var forum: Forum? = null
        private var hasSubforums = false


        init {
            binding.forumDetails.setOnCreateContextMenuListener(OnCreateContextMenuListener { contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenuInfo? ->
                eventListener.onContextMenuCreated(
                    forum!!,
                    contextMenu!!
                )
            })
        }


        fun bind(forumItem: TopLevelForum) {
            forum = forumItem.forum
            hasSubforums = !forumItem.childItemList.isEmpty()

            /* section items hide everything but the section title,
               other forum types hide the section title and show the other components.
               Think of of them as two alternative layouts in the same Layout file */
            binding.tagAndDropdownArrow.visibility = if (forum!!.isType(ForumType.SECTION)) View.GONE else View.VISIBLE
            binding.forumDetails.visibility = if (forum!!.isType(ForumType.SECTION)) View.GONE else View.VISIBLE
            binding.sectionTitle.visibility = if (forum!!.isType(ForumType.SECTION)) View.VISIBLE else View.GONE

            // hide the list divider for section titles and expanded parent forums
            val hideDivider = forum!!.isType(ForumType.SECTION) || forumItem.isInitiallyExpanded
            binding.listDivider.visibility = if (hideDivider) View.INVISIBLE else View.VISIBLE

            // sectionTitle is basically a differently formatted version of the title
            setText(forum!!, binding.forumTitle, binding.forumSubtitle, binding.sectionTitle)
            setThemeColours(itemView, binding.forumTitle, binding.forumSubtitle)
            handleSubtitles(forum!!, binding.forumSubtitle)

            parent.setPreferredFont(itemView)

            binding.forumFavouriteMarker.visibility = if (forum!!.isFavourite) View.VISIBLE else View.GONE

            /* the left section (potentially) has a tag and a dropdown button, anything missing
               is set to GONE so whatever's there gets vertically centred, and the space remains */

            // if there's a forum tag then display it, otherwise remove it
            val hasForumTag = forum!!.tagUrl != null
            if (hasForumTag) {
                TagProvider.setSquareForumTag(binding.forumTag, forum!!)
                binding.forumTag.visibility = View.VISIBLE
            } else {
                binding.forumTag.visibility = View.GONE
            }

            // if this item has subforums, show the dropdown and make it work, otherwise remove it
            if (hasSubforums) {
                rotateDropdown(binding.subforumsExpandArrow, !isExpanded, true)
                binding.subforumsExpandArrow.visibility = View.VISIBLE
            } else {
                binding.subforumsExpandArrow.visibility = View.GONE
            }
            binding.tagAndDropdownArrow.setOnClickListener {
                if (hasSubforums) {
                    if (isExpanded) {
                        collapseView()
                    } else {
                        expandView()
                    }
                }
            }

            binding.forumDetails.setOnClickListener { eventListener.onForumClicked(forum!!) }
        }


        override fun shouldItemViewClickToggleExpansion(): Boolean {
            return false
        }


        override fun onExpansionToggled(closing: Boolean) {
            super.onExpansionToggled(closing)
            rotateDropdown(binding.subforumsExpandArrow, closing, false)
            binding.listDivider.visibility = if (closing) View.VISIBLE else View.INVISIBLE
        }
    }

    inner class SubforumHolder(itemView: View) : ChildViewHolder(itemView) {
        var forum: Forum? = null
        var binding: ForumIndexSubforumItemBinding = ForumIndexSubforumItemBinding.bind(itemView)


        init {
            binding.forumDetails.setOnCreateContextMenuListener(OnCreateContextMenuListener { contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenuInfo? ->
                eventListener.onContextMenuCreated(
                    forum!!,
                    contextMenu!!
                )
            })
        }


        fun bind(forumItem: Forum) {
            forum = forumItem
            setText(forum!!, binding.forumTitle, binding.forumSubtitle, null)
            setThemeColours(binding.getRoot(), binding.forumTitle, binding.forumSubtitle)
            handleSubtitles(forum!!, binding.forumSubtitle)
            binding.forumFavouriteMarker.visibility = if (forum!!.isFavourite) View.VISIBLE else View.GONE
            binding.forumDetails.setOnClickListener { eventListener.onForumClicked(forum!!) }
        }
    }

    companion object {
        /**
         * Returns a configured adapter.
         * 
         * 
         * Takes a list of Forums which will form the main list.
         * Any of those which has items in [Forum.subforums] will be expandable,
         * and the subforums will be shown as an inner list. Any subforums of those items
         * will be ignored. Use [ForumStructure.ListBuilder] etc.
         * to flatten the forums hierarchy into two levels.
         * 
         * @param context          Used for layout inflation
         * @param forums           A list of Forums to display
         * @param listener         Gets callbacks for clicks etc
         * @param awfulPreferences used to check for user options
         * @return an adapter containing the provided forums
         */
        fun getInstance(
            context: Context,
            forums: MutableList<Forum>,
            listener: EventListener,
            awfulPreferences: AwfulPreferences?
        ): ForumListAdapter {
            val topLevelForums: MutableList<TopLevelForum?> = ArrayList<TopLevelForum?>()
            val adapter = ForumListAdapter(context, topLevelForums, listener, awfulPreferences)
            // this is a stupid hack so we can supply the constructor with a list of objects we
            // can't even create without an instance... it's better than pulling TopLevelForum out
            // into a separate file at least
            adapter.addToTopLevelForums(forums, topLevelForums)
            adapter.notifyParentItemRangeInserted(0, topLevelForums.size)
            return adapter
        }
    }
}
